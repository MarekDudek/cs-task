package solution;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;

import solution.iterator.FilteringIterator;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class FilteringIteratorTest {

    private Iterator<Integer> iterator;

    @Before
    public void setup()
    {
    }

    @Test
    public void test()
    {
	// given
	final Range<Integer> range = Range.closed(1, 10);
	final Set<Integer> set = ContiguousSet.create(range, DiscreteDomain.integers());
	final Iterator<Integer> numbers = set.iterator();

	final Predicate<Integer> even = number -> (number % 2) == 0;

	// when
	iterator = new FilteringIterator<Integer>(numbers, even);

	// then
	assertThat(iterator.hasNext(), is(true));
	assertThat(iterator.next(), is(equalTo(2)));

	assertThat(iterator.hasNext(), is(true));
	assertThat(iterator.next(), is(equalTo(4)));

	assertThat(iterator.hasNext(), is(true));
	assertThat(iterator.next(), is(equalTo(6)));

	assertThat(iterator.hasNext(), is(true));
	assertThat(iterator.next(), is(equalTo(8)));

	assertThat(iterator.hasNext(), is(true));
	assertThat(iterator.next(), is(equalTo(10)));

	assertThat(iterator.hasNext(), is(false));
    }
}
