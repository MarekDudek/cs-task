package test.integration;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static solution.PredicateFactory.belongsTo;
import static solution.PredicateFactory.sameDate;
import static test.analyser.TestGeneratorSettings.BLACKLISTED_COUNT;
import static test.analyser.TestGeneratorSettings.CONFIG;
import static test.analyser.TestGeneratorSettings.DUE_DAY;
import static test.analyser.TestGeneratorSettings.EXPECTED_NUMBER_OF_ALL_SUSPICIOUS;
import static test.analyser.TestGeneratorSettings.MAX_ALLOWED_BY_USER_TO_ACCOUNT;
import static test.analyser.TestGeneratorSettings.MAX_ALLOWED_FROM_ACCOUNT;
import static test.analyser.TestGeneratorSettings.NUMBER_OF_TRANSACTIONS;
import static test.analyser.TestGeneratorSettings.THRESHOLDS;
import static test.analyser.TestGeneratorSettings.WHITELISTED_COUNT;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.junit.AfterClass;
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
import test.analyser.ConcurrentAnalyser;
import test.analyser.FraudAnalyser;
import test.analyser.LambdaAnalyser;
import test.analyser.SimpleFraudAnalyser;
import test.transactions.Transaction;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

@BenchmarkOptions(benchmarkRounds = BenchmarkTest.BENCHMARK_ROUNDS, warmupRounds = BenchmarkTest.WARMUP_ROUNDS)
@BenchmarkMethodChart(filePrefix = "src/test/resources/benchmarks/graphs/analysers-method")
@BenchmarkHistoryChart(filePrefix = "src/test/resources/benchmarks/graphs/analysers-history")
public class BenchmarkTest {

    // Benchmark settings

    public static final int WARMUP_ROUNDS = 5;
    public static final int BENCHMARK_ROUNDS = 10;

    private static TransactionGenerator GENERATOR;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private static Predicate<Transaction> SKIP_ANALYSIS;
    private static Predicate<Transaction> SUSPECT_INDIVIDUALLY;
    private static StatsCollector COLLECTOR_SIMPLE_ANALYSER;

    private static List<Transaction> TRANSACTIONS;
    private static Iterator<Transaction> SUSPICIOUS;

    private static FraudAnalyser SIMPLE_ANALYSER;
    private static FraudAnalyser LAMBDA_ANALYSER;
    private static FraudAnalyser CONCURRENT_ANALYSER;

    @BeforeClass
    public static void beforeSuite()
    {
        // given
        GENERATOR = new TransactionGenerator(CONFIG);

        final List<Long> whitelisted = GENERATOR.chooseWhitelisted(WHITELISTED_COUNT);
        SKIP_ANALYSIS = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final List<Long> blacklisted = GENERATOR.chooseBlacklisted(BLACKLISTED_COUNT);
        SUSPECT_INDIVIDUALLY = belongsTo(blacklisted);

        COLLECTOR_SIMPLE_ANALYSER = new MultiStatCollector
                (
                        new TransactionCountFromAccoutCollector(MAX_ALLOWED_FROM_ACCOUNT),
                        new TransactionCountToAccountByUserCollector(MAX_ALLOWED_BY_USER_TO_ACCOUNT),
                        new TransactionCountFromUserAndSumTotalCollector(THRESHOLDS)
                );

        SIMPLE_ANALYSER =
                new SimpleFraudAnalyser(SKIP_ANALYSIS, SUSPECT_INDIVIDUALLY, COLLECTOR_SIMPLE_ANALYSER);
        LAMBDA_ANALYSER =
                new LambdaAnalyser(SKIP_ANALYSIS, SUSPECT_INDIVIDUALLY, MAX_ALLOWED_FROM_ACCOUNT, MAX_ALLOWED_BY_USER_TO_ACCOUNT, THRESHOLDS);
        CONCURRENT_ANALYSER =
                new ConcurrentAnalyser(SKIP_ANALYSIS, SUSPECT_INDIVIDUALLY, MAX_ALLOWED_FROM_ACCOUNT, MAX_ALLOWED_BY_USER_TO_ACCOUNT, THRESHOLDS);

        final Iterator<Transaction> lazyIterator = GENERATOR.generateIterator(NUMBER_OF_TRANSACTIONS);
        TRANSACTIONS = newArrayList(lazyIterator);
    }

    @AfterClass
    public static void tearDown()
    {
        // then
        final long count = newArrayList(SUSPICIOUS).parallelStream().count();
        assertThat((int) count, is(equalTo(EXPECTED_NUMBER_OF_ALL_SUSPICIOUS)));
    }

    @Test
    public void simple_analyser()
    {
        // when
        SUSPICIOUS = SIMPLE_ANALYSER.analyse(newArrayList(TRANSACTIONS).iterator(), DUE_DAY);
    }

    @Test
    public void lambda_analyser()
    {
        // when
        SUSPICIOUS = LAMBDA_ANALYSER.analyse(newArrayList(TRANSACTIONS).iterator(), DUE_DAY);
    }

    @Test
    public void concurrent_analyser()
    {
        // when
        SUSPICIOUS = CONCURRENT_ANALYSER.analyse(newArrayList(TRANSACTIONS).iterator(), DUE_DAY);
    }
}
