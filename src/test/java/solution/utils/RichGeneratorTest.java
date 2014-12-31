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
        final BigDecimal decimal = generator.positiveBigDecimal(random);

        // then
        assertThat(decimal, is(equalTo(BigDecimal.ONE)));
    }
}
