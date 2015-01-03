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
@BenchmarkMethodChart(filePrefix = "src/test/resources/benchmarks/graphs/filtering-individual-method")
@BenchmarkHistoryChart(filePrefix = "src/test/resources/benchmarks/graphs/filtering-individual-history")
public class AnalysisPhasesBenchmarkTest {

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
    private static List<Transaction> INDIVIDUALLY;
    private static List<Transaction> COUNT_FROM_ACCOUNT;
    private static List<Transaction> COUNT_BY_USER_TO_ACCOUNT;
    private static List<Transaction> COUNT_AND_TOTAL_AMOUNT_BY_USER;

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
    }

    @AfterClass
    public static void tearDown()
    {
        // then
        if (INDIVIDUALLY != null) {
            final long count = INDIVIDUALLY.parallelStream().count();
            assertThat(count, is(equalTo(33_426L)));
            INDIVIDUALLY = null;
        }

        if (COUNT_FROM_ACCOUNT != null) {
            final long count = COUNT_FROM_ACCOUNT.parallelStream().count();
            assertThat(count, is(equalTo(5_986L)));
            COUNT_FROM_ACCOUNT = null;
        }

        if (COUNT_BY_USER_TO_ACCOUNT != null) {
            final long count = COUNT_BY_USER_TO_ACCOUNT.parallelStream().count();
            assertThat(count, is(equalTo(338L)));
            COUNT_BY_USER_TO_ACCOUNT = null;
        }

        if (COUNT_AND_TOTAL_AMOUNT_BY_USER != null) {
            final long count = COUNT_AND_TOTAL_AMOUNT_BY_USER.parallelStream().count();
            assertThat(count, is(equalTo(10_387L)));
            COUNT_AND_TOTAL_AMOUNT_BY_USER = null;
        }
    }

    @Test
    public void filtering_individual()
    {
        // when
        INDIVIDUALLY = ANALYSER.individually(TO_ANALYSE);
    }

    @Test
    public void filtering_individual__concurrent()
    {
        // when
        final CompletableFuture<List<Transaction>> promise = ANALYSER.individuallyPromise(TO_ANALYSE);
        INDIVIDUALLY = promise.join();
    }

    @Test
    public void count_from_account()
    {
        // when
        COUNT_FROM_ACCOUNT = ANALYSER.countFromAccount(TO_ANALYSE);
    }

    @Test
    public void count_from_account__concurrent()
    {
        // when
        final CompletableFuture<List<Transaction>> promise = ANALYSER.countFromAccountPromise(TO_ANALYSE);
        COUNT_FROM_ACCOUNT = promise.join();
    }

    @Test
    public void count_by_user_to_account()
    {
        // when
        COUNT_BY_USER_TO_ACCOUNT = ANALYSER.countByUserToAccount(TO_ANALYSE);
    }

    @Test
    public void count_by_user_to_account__concurrent()
    {
        // when
        final CompletableFuture<List<Transaction>> promise = ANALYSER.countByUserToAccountPromise(TO_ANALYSE);
        COUNT_BY_USER_TO_ACCOUNT = promise.join();
    }

    @Test
    public void count_and_total_amount()
    {
        // when
        COUNT_AND_TOTAL_AMOUNT_BY_USER = ANALYSER.countAndTotalAmountByUser(TO_ANALYSE);
    }

    @Test
    public void count_and_total_amount__concurrent()
    {
        // when
        final CompletableFuture<List<Transaction>> promise = ANALYSER.countAndTotalAmountByUserPromise(TO_ANALYSE);
        COUNT_AND_TOTAL_AMOUNT_BY_USER = promise.join();
    }
}
