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
import static test.analyser.TestGeneratorSettings.EXPECTED_NUMBER_OF_ALL_SUSPICIOUS;
import static test.analyser.TestGeneratorSettings.MAX_ALLOWED_BY_USER_TO_ACCOUNT;
import static test.analyser.TestGeneratorSettings.MAX_ALLOWED_FROM_ACCOUNT;
import static test.analyser.TestGeneratorSettings.NUMBER_OF_TRANSACTIONS;
import static test.analyser.TestGeneratorSettings.THRESHOLDS;
import static test.analyser.TestGeneratorSettings.WHITELISTED_COUNT;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.junit.AfterClass;
import org.junit.BeforeClass;
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
@BenchmarkMethodChart(filePrefix = "src/test/resources/benchmarks/graphs/collecting-analysis-results-method")
@BenchmarkHistoryChart(filePrefix = "src/test/resources/benchmarks/graphs/collecting-analysis-results-history")
public class CollectingAnalysisResultsBenchmarkTest {
    // Benchmark settings

    public static final int WARMUP_ROUNDS = 5;
    public static final int BENCHMARK_ROUNDS = 10;

    private static TransactionGenerator GENERATOR;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private static Predicate<Transaction> SKIP_ANALYSIS;
    private static Predicate<Transaction> SUSPECT_INDIVIDUALLY;

    private static ConcurrentAnalyser ANALYSER;

    private static List<Transaction> ALL_TRANSACTIONS;
    private static List<Transaction> TO_ANALYSE;

    private static CompletableFuture<List<Transaction>> INDIVIDUAL_PROMISE;
    private static CompletableFuture<List<Transaction>> COUNT_FROM_ACCOUNT_PROMISE;
    private static CompletableFuture<List<Transaction>> COUNT_BY_USER_TO_ACCOUNT_PROMISE;
    private static CompletableFuture<List<Transaction>> COUNT_AND_TOTAL_AMOUNT_BY_USER_PROMISE;

    private static Collection<Transaction> RESULT;

    @BeforeClass
    public static void setup()
    {
        // given
        GENERATOR = new TransactionGenerator(CONFIG);

        final List<Long> whitelisted = GENERATOR.chooseWhitelisted(WHITELISTED_COUNT);
        SKIP_ANALYSIS = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final List<Long> blacklisted = GENERATOR.chooseBlacklisted(BLACKLISTED_COUNT);
        SUSPECT_INDIVIDUALLY = belongsTo(blacklisted);

        ANALYSER =
                new ConcurrentAnalyser(SKIP_ANALYSIS, SUSPECT_INDIVIDUALLY, MAX_ALLOWED_FROM_ACCOUNT, MAX_ALLOWED_BY_USER_TO_ACCOUNT, THRESHOLDS);

        final Iterator<Transaction> iterator = GENERATOR.generateIterator(NUMBER_OF_TRANSACTIONS);

        ALL_TRANSACTIONS = newArrayList(iterator);
        TO_ANALYSE = ANALYSER.skipWhitelistedConcurrently(ALL_TRANSACTIONS);

        INDIVIDUAL_PROMISE = ANALYSER.individuallyPromise(TO_ANALYSE);
        COUNT_FROM_ACCOUNT_PROMISE = ANALYSER.countFromAccountPromise(TO_ANALYSE);
        COUNT_BY_USER_TO_ACCOUNT_PROMISE = ANALYSER.countByUserToAccountPromise(TO_ANALYSE);
        COUNT_AND_TOTAL_AMOUNT_BY_USER_PROMISE = ANALYSER.countAndTotalAmountByUserPromise(TO_ANALYSE);
    }

    @AfterClass
    public static void tearDown()
    {
        // then
        final int count = (int) RESULT.parallelStream().count();
        assertThat(count, is(equalTo(EXPECTED_NUMBER_OF_ALL_SUSPICIOUS)));
    }

    @Test
    public void combining_results()
    {
        // when
        RESULT = ANALYSER.combineDistinctElements
                (
                        INDIVIDUAL_PROMISE,
                        COUNT_FROM_ACCOUNT_PROMISE,
                        COUNT_BY_USER_TO_ACCOUNT_PROMISE,
                        COUNT_AND_TOTAL_AMOUNT_BY_USER_PROMISE
                );
    }
}
