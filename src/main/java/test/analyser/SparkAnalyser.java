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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.System.out;
import static org.apache.commons.lang.BooleanUtils.isFalse;
import static org.apache.commons.lang.time.DateUtils.isSameDay;

@SuppressWarnings("serial")
public final class SparkAnalyser extends FraudAnalyser implements Serializable {

    private final transient JavaSparkContext context;

    private final Function<Transaction, Boolean> allowAnalysis;
    private final Function<Transaction, Boolean> suspectIndividually;

    private final int maxAllowedFromAccount;
    private final int maxAllowedByUserToAccount;

    public SparkAnalyser
            (
                    final JavaSparkContext context,
                    final Function<Transaction, Boolean> allowAnalysis,
                    final Function<Transaction, Boolean> suspectIndividually,
                    final int maxAllowedFromAccount,
                    final int maxAllowedByUserToAccount
            ) {

        this.context = context;
        this.allowAnalysis = allowAnalysis;
        this.suspectIndividually = suspectIndividually;
        this.maxAllowedFromAccount = maxAllowedFromAccount;
        this.maxAllowedByUserToAccount = maxAllowedByUserToAccount;
    }

    @Override
    public Iterator<Transaction> analyse(final Iterator<Transaction> transactions, final Date date) {

        final JavaRDD<Transaction> all = context.parallelize(newArrayList(transactions));
        out.format("%d in input%n", all.count());

        final JavaRDD<Transaction> toAnalyse = all.filter(allowAnalysis);
        out.format("%d allowed to analysis%n", toAnalyse.count());

        final JavaRDD<Transaction> individually = toAnalyse.filter(suspectIndividually);
        out.format("%d suspected individually%n", individually.count());

        final JavaRDD<Transaction> countFromAccount = suspiciousBasedOnCountFromAccount(toAnalyse);
        out.format("%d based on count from account%n", countFromAccount.count());

        final JavaRDD<Transaction> countByUserToAccount = suspiciousCountByUserToAccount(toAnalyse);
        out.format("%d based on count by user to account%n", countByUserToAccount.count());

        return null;

    }

    private JavaRDD<Transaction> suspiciousBasedOnCountFromAccount(final JavaRDD<Transaction> transactions) {

        final JavaPairRDD<Long, Iterable<Transaction>> groupedByFromAccount = transactions.groupBy(GROUPED_BY_FROM_ACCOUNT);

        final Function<Tuple2<Long, Iterable<Transaction>>, Boolean> exceedsAllowedCount =
                transactionsFromAccount -> copyOf(transactionsFromAccount._2()).size() > maxAllowedFromAccount;

        final JavaPairRDD<Long, Iterable<Transaction>> suspiciousAccounts = groupedByFromAccount.filter(exceedsAllowedCount);

        return suspiciousAccounts.flatMap(TRANSACTIONS_FROM_ACCOUNT);
    }

    private JavaRDD<Transaction> suspiciousCountByUserToAccount(final JavaRDD<Transaction> transactions) {

        final JavaPairRDD<Pair<Long, Long>, Iterable<Transaction>> byUserToAccount = transactions.groupBy(GROUPED_BY_USER_TO_ACCOUNT);

        final Function<Tuple2<Pair<Long, Long>, Iterable<Transaction>>, Boolean> exceedsAllowedCount =
                transactionsByUserToAccount -> copyOf(transactionsByUserToAccount._2()).size() > maxAllowedByUserToAccount;

        final JavaPairRDD<Pair<Long, Long>, Iterable<Transaction>> suspiciousUserToAccount = byUserToAccount.filter(exceedsAllowedCount);

        return suspiciousUserToAccount.flatMap(TRANSACTIONS_BY_USER_TO_ACCOUNT);
    }

    private static final Function<Transaction, Long> GROUPED_BY_FROM_ACCOUNT =
            transaction -> transaction.getAccountFromId();

    private static final Function<Transaction, Pair<Long, Long>> GROUPED_BY_USER_TO_ACCOUNT =
            transaction -> Pair.with(transaction.getUserId(), transaction.getAccountToId());

    private static final FlatMapFunction<Tuple2<Long, Iterable<Transaction>>, Transaction> TRANSACTIONS_FROM_ACCOUNT =
            transactionsFromAccount -> transactionsFromAccount._2();

    private static final FlatMapFunction<Tuple2<Pair<Long, Long>, Iterable<Transaction>>, Transaction> TRANSACTIONS_BY_USER_TO_ACCOUNT =
            suspiciousUserToAccount -> suspiciousUserToAccount._2();

    static class AllowAnalysisPredicate implements Function<Transaction, Boolean> {

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
}
