package solution.utils;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class RichGeneratorTest {

    private static final long ZERO = 0L;

    private static final long ONE = 1L;
    private static final long MINUS_ONE = -1L;

    private static final long TEN = 10L;
    private static final long MINUS_TEN = -10L;

    /** System under test. */
    private RichGenerator generator;

    // Collaborators
    private Random random;

    @Before
    public void setup()
    {
        // given
        generator = new RichGenerator();
        random = mock(Random.class);
    }

    @Test
    public void zero_can_be_returned_when_requested_non_negative()
    {
        // given
        given(random.nextLong())
                .willReturn(ZERO);

        // when
        final long nonNegative = generator.nonNegativeLong(random);

        // then
        assertThat(nonNegative, is(equalTo(ZERO)));
    }

    @Test
    public void zero_will_not_be_returned_when_requested_positive()
    {
        // given
        given(random.nextLong())
                .willReturn(ZERO)
                .willReturn(ONE);

        // when
        final long positive = generator.positiveLong(random);

        // then
        assertThat(positive, is(equalTo(ONE)));
    }

    @Test
    public void negative_will_not_be_returned_when_requested_positive__absolute_value_will()
    {
        // given
        given(random.nextLong())
                .willReturn(MINUS_ONE);

        // when
        final long positive = generator.positiveLong(random);

        // then
        assertThat(positive, is(equalTo(ONE)));
    }

    @Test
    public void min_long_value_is_absolute_of_itself__however_strange_it_may_seem()
    {
        assertThat(Long.MIN_VALUE, is(equalTo(Math.abs(Long.MIN_VALUE))));
    }

    @Test
    public void min_long_will_not_be_returned_when_requested_positive()
    {
        // given
        given(random.nextLong())
                .willReturn(Long.MIN_VALUE)
                .willReturn(ONE);

        // when
        final long positive = generator.positiveLong(random);

        // then
        assertThat(positive, is(equalTo(ONE)));
    }

    @Test
    public void positive_big_decimal_is_generated_from_positive_long()
    {
        // given
        given(random.nextLong())
                .willReturn(ONE);

        // when
        final BigDecimal decimal = generator.positiveDecimal(random);

        // then
        assertThat(decimal, is(equalTo(BigDecimal.ONE)));
    }

    @Test
    public void lower_bound_can_be_returned()
    {
        // given
        given(random.nextLong())
                .willReturn(ZERO);

        // when
        final long number = generator.longBetweenInclusive(random, MINUS_TEN, TEN);

        // then
        assertThat(number, is(equalTo(MINUS_TEN)));
    }

    @Test
    public void upper_bound_can_be_returned()
    {
        // given
        given(random.nextLong())
                .willReturn(20L);

        // when
        final long number = generator.longBetweenInclusive(random, MINUS_TEN, TEN);

        // then
        assertThat(number, is(equalTo(TEN)));
    }

    @Test
    public void upper_bound_can_be_returned__when_negative_value_returned_from_random()
    {
        // given
        given(random.nextLong())
                .willReturn(-20L);

        // when
        final long number = generator.longBetweenInclusive(random, MINUS_TEN, TEN);

        // then
        assertThat(number, is(equalTo(TEN)));
    }

    @Test
    public void lower_bound_can_be_returned__for_decimal()
    {
        // given
        given(random.nextLong())
                .willReturn(ZERO);

        // when
        final BigDecimal decimal = generator.decimalBetweenInclusive(random, new BigDecimal(100), new BigDecimal(300));

        // then
        assertThat(decimal, is(equalTo(new BigDecimal(100))));
    }

    @Test
    public void upper_bound_can_be_returned__for_decimal()
    {
        // given
        given(random.nextLong())
                .willReturn(200L);

        // when
        final BigDecimal decimal = generator.decimalBetweenInclusive(random, new BigDecimal(100), new BigDecimal(300));

        // then
        assertThat(decimal, is(equalTo(new BigDecimal(300))));
    }
}
