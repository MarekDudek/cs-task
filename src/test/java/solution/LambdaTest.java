package solution;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static solution.PredicateFactory.sameDate;
import static solution.PredicateFactory.whitelisted;
import static solution.TestRequirements.BLACKLISTED_USER_1;
import static solution.TestRequirements.WHITELISTED_USERS;
import static solution.TestRequirements.WHITELISTED_USER_1_TRANSACTION;
import static solution.TransactionBuilder.transaction;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Predicate;

import org.junit.Test;

import test.transactions.Transaction;

public class LambdaTest {

    // given
    private static final Calendar CALENDAR =
	    new Calendar.Builder()
		    .setDate(2014, Calendar.DECEMBER, 21)
		    .build();

    private static final Date DUE_DAY = CALENDAR.getTime();

    private static final Date TIMESTAMP_IN_DUE_DAY =
	    new Calendar.Builder()
		    .setDate(CALENDAR.get(YEAR), CALENDAR.get(MONTH), CALENDAR.get(DAY_OF_MONTH))
		    .setTimeOfDay(23, 59, 59)
		    .build().getTime();

    @Test
    public void whitelisted_predicate()
    {
	// when
	final boolean test = whitelisted(WHITELISTED_USERS).test(WHITELISTED_USER_1_TRANSACTION);

	// then
	assertThat(test, is(equalTo(true)));
    }

    @Test
    public void same_date_predicate()
    {
	// given
	final Transaction transaction = transaction().date(TIMESTAMP_IN_DUE_DAY).build();

	// when
	final boolean test = sameDate(DUE_DAY).test(transaction);

	// then
	assertThat(test, is(equalTo(true)));
    }

    /** System under test. */
    private static final Predicate<Transaction> ANALYSED = sameDate(DUE_DAY).and(whitelisted(WHITELISTED_USERS).negate());

    @Test
    public void compound_predicate()
    {
	// given
	final Transaction analysed = transaction().user(BLACKLISTED_USER_1).date(TIMESTAMP_IN_DUE_DAY).build();

	// when
	final boolean test = ANALYSED.test(analysed);

	// then
	assertThat(test, is(equalTo(true)));
    }
}
