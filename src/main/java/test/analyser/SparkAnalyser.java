package test.analyser;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.javatuples.Pair;
import scala.Tuple2;
import test.transactions.Transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang.BooleanUtils.isFalse;
import static org.apache.commons.lang.time.DateUtils.isSameDay;

@SuppressWarnings("serial")
public final class SparkAnalyser extends FraudAnalyser implements Serializable {

    private final transient JavaSparkContext context;

    private final Function<Transaction, Boolean> allowAnalysis;
    private final Function<Transaction, Boolean> suspectIndividually;

    private final int maxAllowedFromAccount;
    private final int maxAllowedByUserToAccount;
    private final List<Pair<Integer, BigDecimal>> thresholds;

    public SparkAnalyser
            (
                    final JavaSparkContext context,
                    final Function<Transaction, Boolean> allowAnalysis,
                    final Function<Transaction, Boolean> suspectIndividually,
                    final int maxAllowedFromAccount,
                    final int maxAllowedByUserToAccount,
                    final List<Pair<Integer, BigDecimal>> thresholds
            ) {

        this.context = context;
        this.allowAnalysis = allowAnalysis;
        this.suspectIndividually = suspectIndividually;
        this.maxAllowedFromAccount = maxAllowedFromAccount;
        this.maxAllowedByUserToAccount = maxAllowedByUserToAccount;
        this.thresholds = thresholds;
    }

    @Override
    public Iterator<Transaction> analyse(final Iterator<Transaction> transactions, final Date date) {

        final JavaRDD<Transaction> all = context.parallelize(newArrayList(transactions));
        final JavaRDD<Transaction> toAnalyse = all.filter(allowAnalysis);

        final JavaRDD<Transaction> individually = toAnalyse.filter(suspectIndividually);
        final JavaRDD<Transaction> countFromAccount = suspiciousBasedOnCountFromAccount(toAnalyse);
        final JavaRDD<Transaction> countByUserToAccount = suspiciousCountByUserToAccount(toAnalyse);
        final JavaRDD<Transaction> countAndTotalAmountByUser = suspiciousBasedOnCountAndTotalAmountByUser(toAnalyse);

        final JavaRDD<Transaction> union = individually.union(countFromAccount).union(countByUserToAccount).union(countAndTotalAmountByUser);
        return newHashSet(union.collect()).iterator();
    }

    private JavaRDD<Transaction> suspiciousBasedOnCountFromAccount(final JavaRDD<Transaction> transactions) {

        final JavaPairRDD<Long, Iterable<Transaction>> groupedByFromAccount = transactions.groupBy(GROUPED_BY_FROM_ACCOUNT);

        final Function<Tuple2<Long, Iterable<Transaction>>, Boolean> exceedsAllowedCount =
                transactionsFromAccount -> copyOf(transactionsFromAccount._2()).size() > maxAllowedFromAccount;

        final JavaPairRDD<Long, Iterable<Transaction>> suspiciousAccounts = groupedByFromAccount.filter(exceedsAllowedCount);

        return suspiciousAccounts.flatMap(TRANSACTIONS_FROM_GROUP);
    }

    private JavaRDD<Transaction> suspiciousCountByUserToAccount(final JavaRDD<Transaction> transactions) {

        final JavaPairRDD<Pair<Long, Long>, Iterable<Transaction>> byUserToAccount = transactions.groupBy(GROUPED_BY_USER_TO_ACCOUNT);

        final Function<Tuple2<Pair<Long, Long>, Iterable<Transaction>>, Boolean> exceedsAllowedCount =
                transactionsByUserToAccount -> copyOf(transactionsByUserToAccount._2()).size() > maxAllowedByUserToAccount;

        final JavaPairRDD<Pair<Long, Long>, Iterable<Transaction>> suspiciousUserToAccount = byUserToAccount.filter(exceedsAllowedCount);

        return suspiciousUserToAccount.flatMap(TRANSACTIONS_BY_USER_TO_ACCOUNT);
    }

    private JavaRDD<Transaction> suspiciousBasedOnCountAndTotalAmountByUser(final JavaRDD<Transaction> transactions) {

        final JavaPairRDD<Long, Iterable<Transaction>> groupedByUser = transactions.groupBy(GROUPED_BY_USER);

        final Function<Tuple2<Long, Iterable<Transaction>>, Boolean> exceedsCountAndTotal = new Function<Tuple2<Long, Iterable<Transaction>>, Boolean>() {
            @Override
            public Boolean call(final Tuple2<Long, Iterable<Transaction>> transactionsByUser) throws Exception {
                final List<Transaction> transactions = copyOf(transactionsByUser._2());
                return thresholds.stream().anyMatch(
                        ((Predicate<Pair<Integer, BigDecimal>>)
                                countAndTotalAmount -> transactions.stream()
                                        .count() > countAndTotalAmount.getValue0()
                        ).and((Predicate<Pair<Integer, BigDecimal>>)
                                        countAndTotalAmount -> transactions.stream()
                                                .map(Transaction::getAmount)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(countAndTotalAmount.getValue1()) > 0
                        ));
            }
        };
        final JavaPairRDD<Long, Iterable<Transaction>> suspiciousCountsAndTotal = groupedByUser.filter(exceedsCountAndTotal);

        return suspiciousCountsAndTotal.flatMap(TRANSACTIONS_FROM_GROUP);
    }

    private static final Function<Transaction, Long> GROUPED_BY_FROM_ACCOUNT =
            transaction -> transaction.getAccountFromId();

    private static final Function<Transaction, Pair<Long, Long>> GROUPED_BY_USER_TO_ACCOUNT =
            transaction -> Pair.with(transaction.getUserId(), transaction.getAccountToId());

    private static final FlatMapFunction<Tuple2<Long, Iterable<Transaction>>, Transaction> TRANSACTIONS_FROM_GROUP =
            transactionsFromAccount -> transactionsFromAccount._2();

    private static final FlatMapFunction<Tuple2<Pair<Long, Long>, Iterable<Transaction>>, Transaction> TRANSACTIONS_BY_USER_TO_ACCOUNT =
            suspiciousUserToAccount -> suspiciousUserToAccount._2();

    private static final Function<Transaction, Long> GROUPED_BY_USER =
            transaction -> transaction.getUserId();

    public static class AllowAnalysisPredicate implements Function<Transaction, Boolean> {

        private final List<Long> whitelisted;
        private final Date dueDay;

        public AllowAnalysisPredicate(final List<Long> whitelisted, final Date dueDay) {
            this.whitelisted = whitelisted;
            this.dueDay = dueDay;
        }

        @Override
        public Boolean call(final Transaction transaction) {
            return isFalse(whitelisted.contains(transaction.getUserId())) && isSameDay(transaction.getDate(), dueDay);
        }
    }

    public static class SuspectIndividually implements Function<Transaction, Boolean> {

        private final List<Long> blacklisted;

        public SuspectIndividually(final List<Long> blacklisted) {
            this.blacklisted = blacklisted;
        }

        @Override
        public Boolean call(final Transaction transaction) {
            return blacklisted.contains(transaction.getUserId());
        }
    }
}
