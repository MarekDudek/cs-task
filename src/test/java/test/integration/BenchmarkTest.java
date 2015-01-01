package test.integration;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;

public class BenchmarkTest {

    private static final int WARMUP_ROUNDS = 5;
    private static final int BENCHMARK_ROUNDS = 10;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private static List<BigDecimal> NUMBERS;
    private static BigDecimal RESULT;

    @BeforeClass
    public static void prepare()
    {
        // given
        NUMBERS = newArrayList();

        final BigDecimal max = new BigDecimal("10000000");
        for (BigDecimal i = BigDecimal.ZERO; i.compareTo(max) < 0; i = i.add(BigDecimal.ONE)) {
            NUMBERS.add(i);
        }

        RESULT = new BigDecimal("49999995000000");
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = BENCHMARK_ROUNDS, warmupRounds = WARMUP_ROUNDS)
    public void sequentialExecution() throws Exception
    {
        // when
        final BigDecimal sum = NUMBERS.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        // then
        assertThat(sum, is(equalTo(RESULT)));
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = BENCHMARK_ROUNDS, warmupRounds = WARMUP_ROUNDS)
    public void parallelExecution() throws Exception
    {
        // when
        final BigDecimal sum = NUMBERS.parallelStream().reduce(BigDecimal.ZERO, BigDecimal::add);

        // then
        assertThat(sum, is(equalTo(RESULT)));
    }
}
