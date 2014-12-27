package solution;

import static com.google.common.collect.ContiguousSet.create;
import static com.google.common.collect.DiscreteDomain.integers;
import static com.google.common.collect.Range.closed;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import solution.iterator.FilteringIterator;

import com.google.common.collect.Range;

public class FilteringIteratorTest {

    /** System under test. */
    private Iterator<Integer> iterator;

    private Iterator<Integer> numbers;
    private Predicate<Integer> even;

    @Before
    public void setup()
    {
	// given
	final Range<Integer> range = closed(1, 10);
	final Set<Integer> set = create(range, integers());
	numbers = set.iterator();

	even = number -> (number % 2) == 0;
    }

    @Test
    public void iteration_with_checking_if_next_exists()
    {
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

    @Ignore
    @Test
    public void iteration_without_checking_if_next_exists()
    {
	// when
	iterator = new FilteringIterator<Integer>(numbers, even);

	// then
	assertThat(iterator.next(), is(equalTo(2)));
	assertThat(iterator.next(), is(equalTo(4)));
	assertThat(iterator.next(), is(equalTo(6)));
	assertThat(iterator.next(), is(equalTo(8)));
	assertThat(iterator.next(), is(equalTo(10)));
    }
}
