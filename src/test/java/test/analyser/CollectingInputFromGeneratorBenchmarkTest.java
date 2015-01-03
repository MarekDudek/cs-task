package test.analyser;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static test.analyser.TestGeneratorSettings.CONFIG;
import static test.analyser.TestGeneratorSettings.NUMBER_OF_TRANSACTIONS;

import java.util.Iterator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
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
@BenchmarkMethodChart(filePrefix = "src/test/resources/benchmarks/graphs/collecting-from-generator-method")
@BenchmarkHistoryChart(filePrefix = "src/test/resources/benchmarks/graphs/collecting-from-generator-history")
public class CollectingInputFromGeneratorBenchmarkTest {

    private static TransactionGenerator GENERATOR;
    private static Iterator<Transaction> ITERATOR;
    private static List<Transaction> TRANSACTIONS;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    @BeforeClass
    public static void setup()
    {
        // given
        GENERATOR = new TransactionGenerator(CONFIG);
    }

    @AfterClass
    public static void tearDown()
    {
        final long count = TRANSACTIONS.parallelStream().count();
        assertThat((int) count, is(equalTo(NUMBER_OF_TRANSACTIONS)));
    }

    @Before
    public void setUp()
    {
        // given
        ITERATOR = GENERATOR.generateIterator(NUMBER_OF_TRANSACTIONS);
    }

    @Test
    public void collecting_from_generator()
    {
        // when
        TRANSACTIONS = newArrayList(ITERATOR);
    }
}
