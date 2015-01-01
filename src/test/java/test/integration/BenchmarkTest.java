package test.integration;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static solution.PredicateFactory.belongsTo;
import static solution.PredicateFactory.sameDate;
import static test.analyser.TestGeneratorSettings.BLACKLISTED_COUNT;
import static test.analyser.TestGeneratorSettings.CONFIG;
import static test.analyser.TestGeneratorSettings.DUE_DAY;
import static test.analyser.TestGeneratorSettings.MAX_ALLOWED_BY_USER_TO_ACCOUNT;
import static test.analyser.TestGeneratorSettings.MAX_ALLOWED_FROM_ACCOUNT;
import static test.analyser.TestGeneratorSettings.NUMBER_OF_TRANSACTIONS;
import static test.analyser.TestGeneratorSettings.THRESHOLDS;
import static test.analyser.TestGeneratorSettings.WHITELISTED_COUNT;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import solution.collectors.MultiStatCollector;
import solution.collectors.StatsCollector;
import solution.collectors.TransactionCountFromAccoutCollector;
import solution.collectors.TransactionCountFromUserAndSumTotalCollector;
import solution.collectors.TransactionCountToAccountByUserCollector;
import solution.transactions.TransactionGenerator;
import test.analyser.FraudAnalyser;
import test.analyser.LambdaAnalyser;
import test.analyser.SimpleFraudAnalyser;
import test.transactions.Transaction;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

@BenchmarkOptions(benchmarkRounds = BenchmarkTest.BENCHMARK_ROUNDS, warmupRounds = BenchmarkTest.WARMUP_ROUNDS)
@BenchmarkMethodChart(filePrefix = "src/test/resources/benchmarks/graphs/benchmark-analysers-method")
@BenchmarkHistoryChart(filePrefix = "src/test/resources/benchmarks/graphs/benchmark-analysers-history")
public class BenchmarkTest {

    // Benchmark settings

    public static final int WARMUP_ROUNDS = 5;
    public static final int BENCHMARK_ROUNDS = 10;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    // Systems under test

    private static TransactionGenerator GENERATOR;

    private static List<Long> WHITELISTED;
    private static Predicate<Transaction> SKIP_ANALYSIS;

    private static List<Long> BLACKLISTED;
    private static Predicate<Transaction> SUSPECT_INDIVIDUALLY;

    private static StatsCollector COLLECTOR;

    /** System under benchmark. */
    private static FraudAnalyser SIMPLE_ANALYSER;
    /** System under benchmark. */
    private static FraudAnalyser LAMBDA_ANALYSER;

    private static AtomicInteger COUNTER = new AtomicInteger(0);

    @BeforeClass
    public static void prepare()
    {
        // given

        GENERATOR = new TransactionGenerator(CONFIG);

        WHITELISTED = GENERATOR.chooseWhitelisted(WHITELISTED_COUNT);
        SKIP_ANALYSIS = belongsTo(WHITELISTED).or(sameDate(DUE_DAY).negate());

        BLACKLISTED = GENERATOR.chooseBlacklisted(BLACKLISTED_COUNT);
        SUSPECT_INDIVIDUALLY = belongsTo(BLACKLISTED);

        COLLECTOR = new MultiStatCollector
                (
                        new TransactionCountFromAccoutCollector(MAX_ALLOWED_FROM_ACCOUNT),
                        new TransactionCountToAccountByUserCollector(MAX_ALLOWED_BY_USER_TO_ACCOUNT),
                        new TransactionCountFromUserAndSumTotalCollector(THRESHOLDS)
                );

        SIMPLE_ANALYSER =
                new SimpleFraudAnalyser(SKIP_ANALYSIS, SUSPECT_INDIVIDUALLY, COLLECTOR);

        LAMBDA_ANALYSER =
                new LambdaAnalyser(SKIP_ANALYSIS, SUSPECT_INDIVIDUALLY, MAX_ALLOWED_FROM_ACCOUNT, MAX_ALLOWED_BY_USER_TO_ACCOUNT, THRESHOLDS);

    }

    @Before
    public void setup()
    {
        COUNTER.addAndGet(1);
        System.out.println("Call #" + COUNTER.get());
    }

    @Test
    public void simple_analyser()
    {
        // when
        final Iterator<Transaction> transactions = GENERATOR.generateIterator(NUMBER_OF_TRANSACTIONS);
        final Iterator<Transaction> suspicious = SIMPLE_ANALYSER.analyse(transactions, DUE_DAY);

        // then
        assertThat(newArrayList(suspicious).size(), greaterThan(0));
    }

    @Test
    public void lambda_analyser()
    {
        // when
        final Iterator<Transaction> transactions = GENERATOR.generateIterator(NUMBER_OF_TRANSACTIONS);
        final Iterator<Transaction> suspicious = LAMBDA_ANALYSER.analyse(transactions, DUE_DAY);

        // then
        assertThat(newArrayList(suspicious).size(), greaterThan(0));
    }
}
