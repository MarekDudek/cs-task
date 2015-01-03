package test.analyser;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import solution.transactions.TransactionGenerator;
import test.integration.BenchmarkTest;
import test.transactions.Transaction;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

@BenchmarkOptions(benchmarkRounds = BenchmarkTest.BENCHMARK_ROUNDS, warmupRounds = BenchmarkTest.WARMUP_ROUNDS)
@BenchmarkMethodChart(filePrefix = "src/test/resources/benchmarks/graphs/filtering-out-skipped-method")
@BenchmarkHistoryChart(filePrefix = "src/test/resources/benchmarks/graphs/filtering-out-skipped-history")
public class FilteringOutSkippedBenchmarkTest {

    // Benchmark settings

    public static final int WARMUP_ROUNDS = 5;
    public static final int BENCHMARK_ROUNDS = 10;

    private TransactionGenerator generator;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private Predicate<Transaction> skipAnalysis;
    private Predicate<Transaction> suspectIndividually;

    private ConcurrentAnalyser analyser;

    private List<Transaction> allTransactions;
    private List<Transaction> toAnalyse;

    @Before
    public void setup()
    {
        // given
        generator = new TransactionGenerator(CONFIG);

        final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
        skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final List<Long> blacklisted = generator.chooseBlacklisted(BLACKLISTED_COUNT);
        suspectIndividually = belongsTo(blacklisted);

        analyser =
                new ConcurrentAnalyser(skipAnalysis, suspectIndividually, MAX_ALLOWED_FROM_ACCOUNT, MAX_ALLOWED_BY_USER_TO_ACCOUNT, THRESHOLDS);

        final Iterator<Transaction> iterator = generator.generateIterator(NUMBER_OF_TRANSACTIONS);

        allTransactions = newArrayList(iterator);
    }

    @After
    public void tearDown()
    {
        // then
        final long count = toAnalyse.parallelStream().count();
        assertThat(count, is(equalTo(299_256L)));
    }

    @Test
    public void filtering_out_skipped()
    {
        // when
        toAnalyse = analyser.skipWhitelisted(allTransactions);
    }

    @Test
    public void filtering_out_skipped__concurrent()
    {
        // when
        toAnalyse = analyser.skipWhitelistedConcurrently(allTransactions);
    }
}
