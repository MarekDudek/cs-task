package test.analyser;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import test.transactions.Transaction;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.System.out;

public class SparkAnalyser extends FraudAnalyser implements Serializable {

    private final JavaSparkContext context;

    private final Function<Transaction, Boolean> allowAnalysis;
    private final Function<Transaction, Boolean> suspectIndividually;

    public SparkAnalyser
            (
                    final JavaSparkContext context,
                    final Function<Transaction, Boolean> allowAnalysis,
                    final Function<Transaction, Boolean> suspectIndividually
            ) {
        super();
        this.context = context;
        this.allowAnalysis = allowAnalysis;
        this.suspectIndividually = suspectIndividually;
    }

    @Override
    public Iterator<Transaction> analyse(final Iterator<Transaction> transactions, final Date date) {

        final JavaRDD<Transaction> all = context.parallelize(newArrayList(transactions));
        out.format("%d transactions in input%n", all.count());

        final JavaRDD<Transaction> toAnalyse = all.filter(allowAnalysis);
        out.format("%d transaction allowed to analysis%n", toAnalyse.count());

        final JavaRDD<Transaction> individually = toAnalyse.filter(suspectIndividually);
        out.format("%d transactions suspected individually%n", individually.count());

        return null;
    }
}
