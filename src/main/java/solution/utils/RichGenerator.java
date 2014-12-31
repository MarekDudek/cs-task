package solution.utils;

import java.util.Random;

public class RichGenerator {

    public long positiveLong(final Random generator)
    {
        long integer;
        do {
            integer = generator.nextLong();
        } while (integer == 0 || integer == Long.MIN_VALUE);
        return Math.abs(integer);
    }
}
