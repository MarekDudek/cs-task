package test.analyser;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static solution.TestRequirements.BLACKLISTED_USER_1_TRANSACTION;
import static solution.TestRequirements.BLACKLISTED_USER_2_TRANSACTION;
import static solution.TestRequirements.BLACKLISTED_USER_3_TRANSACTION;
import static solution.TestRequirements.WHITELISTED_USER_1_TRANSACTION;
import static solution.TestRequirements.WHITELISTED_USER_2_TRANSACTION;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import test.transactions.Transaction;

import com.google.common.collect.Lists;

public class FraudAnalyserTest {

    /** System under test. */
    private FraudAnalyser analyser;

    @Before
    public void setup()
    {
	analyser = new FraudAnalyser();
    }

    @Test
    public void test()
    {
	// when
	final Iterator<Transaction> suspictious = analyser.analyse(null, null);

	// then
	assertThat(suspictious, notNullValue());
    }

    @Test
    public void test_with_long_streak()
    {
	// given
	final List<Transaction> transactions = Lists.newArrayList
		(
			BLACKLISTED_USER_1_TRANSACTION,
			BLACKLISTED_USER_2_TRANSACTION,
			BLACKLISTED_USER_3_TRANSACTION,
			WHITELISTED_USER_1_TRANSACTION,
			WHITELISTED_USER_2_TRANSACTION
		);

	// when
	analyser.analyse(transactions.iterator(), new Date());
    }
}
