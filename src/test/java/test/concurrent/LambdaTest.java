package test.concurrent;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class LambdaTest {

    private static final int MILLION = 1_000_000;

    @Test
    public void simple_reduction()
    {
        // given
        final Range<Long> range = Range.closed(1L, (long) MILLION);
        final Set<Long> numbers = ContiguousSet.create(range, DiscreteDomain.longs());

        // when
        final long sum = numbers.parallelStream()
                .map(x -> x * x)
                .reduce(0L, (a, b) -> a + b);

        // then
        assertThat(sum, equalTo(333333833333500000L));
    }

    @Test
    public void reduction_with_side_effect()
    {
        // given
        final Set<Long> set = ContiguousSet.create(Range.closed(1L, (long) MILLION), DiscreteDomain.longs());
        final BlockingQueue<Long> numbers = new LinkedBlockingQueue<>();

        // when
        final long sum = set.parallelStream()
                .map(x -> {
                    numbers.add(x);
                    return x * x;
                })
                .reduce(0L, (a, b) -> a + b);

        // then
        assertThat(sum, equalTo(333333833333500000L));
        assertThat(numbers, hasSize(MILLION));
    }
}
