package test.analyser;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import solution.transactions.TransactionGenerator;
import test.transactions.Transaction;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static test.analyser.TestGeneratorSettings.*;

@Ignore
public class SparkAnalyserTest implements Serializable {

    private static JavaSparkContext CONTEXT;

    @BeforeClass
    public static void setUpClass() throws IOException {

        final SparkConf config = new SparkConf();
        config.setMaster("local");
        config.setAppName("Spark Analyser job");

        CONTEXT = new JavaSparkContext(config);
    }

    @AfterClass
    public static void tearDownClass() {

        CONTEXT.stop();
    }

    @Test
    public void test() {

        final TransactionGenerator generator = new TransactionGenerator(CONFIG);

        final List<Long> blacklisted = generator.chooseBlacklisted(BLACKLISTED_COUNT);
        final Function<Transaction, Boolean> suspectIndividually = new SparkAnalyser.SuspectIndividually(blacklisted);

        final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
        final Function<Transaction, Boolean> allowAnalysis = new SparkAnalyser.AllowAnalysisPredicate(whitelisted, DUE_DAY);

        final FraudAnalyser analyser = new SparkAnalyser(CONTEXT, allowAnalysis, suspectIndividually, MAX_ALLOWED_FROM_ACCOUNT, MAX_ALLOWED_BY_USER_TO_ACCOUNT, THRESHOLDS);

        // when
        final Iterator<Transaction> transactions = generator.generateIterator(NUMBER_OF_TRANSACTIONS);
        final Iterator<Transaction> suspicious = analyser.analyse(transactions, DUE_DAY);

        // then
        assertThat(copyOf(suspicious), hasSize(EXPECTED_NUMBER_OF_ALL_SUSPICIOUS));
    }
}
