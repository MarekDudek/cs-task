package solution.utils;

import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SECOND;

import java.util.Calendar;
import java.util.Date;

public class DateUtilities {

    public static Date startOfDayNDaysEarlier(final Date date, final int days)
    {
	final Calendar calendar = Calendar.getInstance();

	calendar.setTime(date);

	calendar.add(DAY_OF_YEAR, -days);

	calendar.set(HOUR_OF_DAY, 0);
	calendar.set(MINUTE, 0);
	calendar.set(SECOND, 0);
	calendar.set(MILLISECOND, 0);

	return calendar.getTime();
    }

    public static Date endOfDayNDaysLater(final Date date, final int days)
    {
	final Calendar calendar = Calendar.getInstance();

	calendar.setTime(date);

	calendar.add(DAY_OF_YEAR, days);
	calendar.set(HOUR_OF_DAY, 23);
	calendar.set(MINUTE, 59);
	calendar.set(SECOND, 59);
	calendar.set(MILLISECOND, 999);

	return calendar.getTime();
    }

}
