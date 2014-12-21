package solution;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static solution.TestRequirements.WHITELISTED_USER_1;
import static solution.TestRequirements.WHITELISTED_USER_1_TRANSACTION;
import static solution.TestRequirements.WHITELISTED_USER_2;
import static solution.TransactionBuilder.transaction;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import test.transactions.Transaction;

import com.google.common.collect.Lists;

public class LambdaTest {

    @Test
    public void whitelisted_predicate()
    {
	// given
	final List<Long> whitelistedUsers = Lists.newArrayList(WHITELISTED_USER_1, WHITELISTED_USER_2);
	final Predicate<Transaction> whitelisted = tr -> whitelistedUsers.contains(tr.getUserId());

	// when
	final boolean test = whitelisted.test(WHITELISTED_USER_1_TRANSACTION);

	// then
	assertThat(test, is(true));
    }

    @Test
    public void date_comparison_predicate()
    {
	// given
	final Date startOfDay = new Calendar.Builder()
		.setDate(2014, Calendar.DECEMBER, 21)
		.setTimeOfDay(0, 1, 1)
		.build().getTime();

	final Date endOfDay = new Calendar.Builder()
		.setDate(2014, Calendar.DECEMBER, 21)
		.setTimeOfDay(23, 59, 59)
		.build().getTime();

	final Predicate<Transaction> sameDay = tr -> DateUtils.isSameDay(tr.getDate(), startOfDay);

	final Transaction transaction = transaction().date(endOfDay).build();

	// when
	final boolean test = sameDay.test(transaction);

	// then
	assertThat(test, is(true));
    }
}
