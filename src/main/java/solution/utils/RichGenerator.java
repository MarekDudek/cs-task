package solution.utils;

import java.math.BigDecimal;
import java.util.Random;

public class RichGenerator {

    public long positiveLong(final Random generator)
    {
        while (true)
        {
            final long integer = generator.nextLong();
            if (integer != 0 && integer != Long.MIN_VALUE)
            {
                return Math.abs(integer);
            }
        }
    }

    public long nonNegativeLong(final Random generator)
    {
        while (true)
        {
            final long integer = generator.nextLong();
            if (integer != Long.MIN_VALUE)
            {
                return Math.abs(integer);
            }
        }
    }

    public BigDecimal positiveBigDecimal(final Random generator)
    {
        final long positive = positiveLong(generator);
        return new BigDecimal(positive);
    }

    public long longBetweenInclusive(final Random generator, final long lowerBound, final long upperBound)
    {
        final long nonNegative = nonNegativeLong(generator);
        final long span = upperBound - lowerBound + 1;

        return lowerBound + nonNegative % span;
    }
}
