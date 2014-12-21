package solution;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static solution.TestRequirements.WHITELISTED_USER_1;
import static solution.TestRequirements.WHITELISTED_USER_1_TRANSACTION;
import static solution.TestRequirements.WHITELISTED_USER_2;

import java.util.List;
import java.util.function.Predicate;

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
}
