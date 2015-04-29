package test.analyser;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;
import test.transactions.Transaction;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.System.out;

public class SparkAnalyser extends FraudAnalyser implements Serializable {

    private final transient JavaSparkContext context;

    private final Function<Transaction, Boolean> allowAnalysis;
    private final Function<Transaction, Boolean> suspectIndividually;
    private final int maxAllowedFromAccount;

    public SparkAnalyser
            (
                    final JavaSparkContext context,
                    final Function<Transaction, Boolean> allowAnalysis,
                    final Function<Transaction, Boolean> suspectIndividually,
                    final int maxAllowedFromAccount
            ) {

        this.context = context;
        this.allowAnalysis = allowAnalysis;
        this.suspectIndividually = suspectIndividually;
        this.maxAllowedFromAccount = maxAllowedFromAccount;
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

        return null;

    }

    private JavaRDD<Transaction> suspiciousBasedOnCountFromAccount(final JavaRDD<Transaction> transactions) {

        final JavaPairRDD<Long, Iterable<Transaction>> groupedByFromAccount = transactions.groupBy(GROUPED_BY_FROM_ACCOUNT);

        final Function<Tuple2<Long, Iterable<Transaction>>, Boolean> exceedsAllowedCount =
                new Function<Tuple2<Long, Iterable<Transaction>>, Boolean>() {
                    @Override
                    public Boolean call(final Tuple2<Long, Iterable<Transaction>> transactionsFromAccount) {
                        final Iterable<Transaction> transactions = transactionsFromAccount._2();
                        return newArrayList(transactions).size() > maxAllowedFromAccount;
                    }
                };

        final JavaPairRDD<Long, Iterable<Transaction>> suspiciousAccounts = groupedByFromAccount.filter(exceedsAllowedCount);
        final JavaRDD<Transaction> suspicious = suspiciousAccounts.flatMap(TRANSACTIONS_FROM_ACCOUNT);

        return suspicious;
    }

    private static final Function<Transaction, Long> GROUPED_BY_FROM_ACCOUNT =
            new Function<Transaction, Long>() {
                @Override
                public Long call(final Transaction transaction) {
                    return transaction.getAccountFromId();
                }
            };

    private final FlatMapFunction<Tuple2<Long, Iterable<Transaction>>, Transaction> TRANSACTIONS_FROM_ACCOUNT =
            new FlatMapFunction<Tuple2<Long, Iterable<Transaction>>, Transaction>() {
                @Override
                public Iterable<Transaction> call(final Tuple2<Long, Iterable<Transaction>> transactionsFromAccount) {
                    return transactionsFromAccount._2();
                }
            };
}
