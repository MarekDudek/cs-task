package solution.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtilities {

    public static Date startOfDayNDaysEarlier(final Date date, final int days)
    {
        final Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);

        calendar.add(Calendar.DAY_OF_YEAR, -days);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static Date endOfDayNDaysLater(final Date date, final int days)
    {
        final Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);

        calendar.add(Calendar.DAY_OF_YEAR, days);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }
}
