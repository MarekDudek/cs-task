package solution.utils;

import java.util.Random;

public class RichGenerator {

    public long positiveLong(final Random generator)
    {
        while (true) {
            final long integer = generator.nextLong();
            if (integer != 0 && integer != Long.MIN_VALUE) {
                return Math.abs(integer);
            }
        }
    }
}
