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
import test.analyser.IteratingFraudAnalyser;
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

    private static TransactionGenerator GENERATOR;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private Predicate<Transaction> skipAnalysis;
    private Predicate<Transaction> suspectIndividually;
    private StatsCollector collector;

    @BeforeClass
    public static void beforeSuite()
    {
        // given
        GENERATOR = new TransactionGenerator(CONFIG);
    }

    @Before
    public void setUp()
    {
        // given
        final List<Long> whitelisted = GENERATOR.chooseWhitelisted(WHITELISTED_COUNT);
        skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final List<Long> blacklisted = GENERATOR.chooseBlacklisted(BLACKLISTED_COUNT);
        suspectIndividually = belongsTo(blacklisted);

        collector = new MultiStatCollector
                (
                        new TransactionCountFromAccoutCollector(MAX_ALLOWED_FROM_ACCOUNT),
                        new TransactionCountToAccountByUserCollector(MAX_ALLOWED_BY_USER_TO_ACCOUNT),
                        new TransactionCountFromUserAndSumTotalCollector(THRESHOLDS)
                );
    }

    @Test
    public void simple_analyser()
    {
        // given
        final FraudAnalyser analyser =
                new SimpleFraudAnalyser(skipAnalysis, suspectIndividually, collector);
        // when
        final Iterator<Transaction> transactions = GENERATOR.generateIterator(NUMBER_OF_TRANSACTIONS);
        final Iterator<Transaction> suspicious = analyser.analyse(transactions, DUE_DAY);

        // then
        assertThat(newArrayList(suspicious).size(), greaterThan(0));
    }

    @Test
    public void iterating_analyser()
    {
        // given
        final FraudAnalyser analyser =
                new IteratingFraudAnalyser(skipAnalysis, suspectIndividually, collector);

        // when
        final Iterator<Transaction> transactions = GENERATOR.generateIterator(NUMBER_OF_TRANSACTIONS);
        final Iterator<Transaction> suspicious = analyser.analyse(transactions, DUE_DAY);

        // then
        assertThat(newArrayList(suspicious).size(), greaterThan(0));
    }

    @Test
    public void lambda_analyser()
    {
        // given
        final FraudAnalyser analyser =
                new LambdaAnalyser(skipAnalysis, suspectIndividually, MAX_ALLOWED_FROM_ACCOUNT, MAX_ALLOWED_BY_USER_TO_ACCOUNT, THRESHOLDS);

        // when
        final Iterator<Transaction> transactions = GENERATOR.generateIterator(NUMBER_OF_TRANSACTIONS);
        final Iterator<Transaction> suspicious = analyser.analyse(transactions, DUE_DAY);

        // then
        assertThat(newArrayList(suspicious).size(), greaterThan(0));
    }
}
