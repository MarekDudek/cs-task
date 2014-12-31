package solution.utils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

public final class RichGenerator {

    public long positiveLong(final Random generator)
    {
        while (true)
        {
            final long integer = nonNegativeLong(generator);
            if (integer != 0)
            {
                return integer;
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

    public BigDecimal positiveDecimal(final Random generator)
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

    public BigDecimal decimalBetweenInclusive(final Random generator, final BigDecimal lowerBound, final BigDecimal upperBound)
    {
        final long longBetweenInclusive = longBetweenInclusive(generator, lowerBound.longValue(), upperBound.longValue());
        return new BigDecimal(longBetweenInclusive);
    }

    public Date randomDate(final Random generator, final Date median, final int margin)
    {
        final long lowerMillis = DateUtilities.startOfDayNDaysEarlier(median, margin).getTime();
        final long upperMillis = DateUtilities.endOfDayNDaysLater(median, margin).getTime();

        final long difference = upperMillis - lowerMillis + 1;

        final long randomMillis = lowerMillis + nonNegativeLong(generator) % difference;
        final Date randomDate = new Date(randomMillis);

        return randomDate;
    }
}
