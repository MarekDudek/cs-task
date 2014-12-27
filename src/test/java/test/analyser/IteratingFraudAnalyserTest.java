package test.analyser;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static test.analyser.TestData.BLACKLISTED_USER_ON_DUE_DAY;
import static test.analyser.TestData.REGULAR_USER_ON_DUE_DAY;
import static test.analyser.TestData.SKIP_ANALYSIS;
import static test.analyser.TestData.VARIOUS_TRANSACTIONS;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import test.transactions.Transaction;

public class IteratingFraudAnalyserTest {

    private FraudAnalyser analyser;

    @Before
    public void setup()
    {
    }

    @Test
    public void test()
    {
	// given
	analyser = new IteratingFraudAnalyser(SKIP_ANALYSIS);

	// when
	final Iterator<Transaction> suspicious = analyser.analyse(VARIOUS_TRANSACTIONS.iterator(), null);

	// then
	final List<Transaction> list = newArrayList(suspicious);
	assertThat(list, hasSize(2));
	assertThat(list, hasItem(BLACKLISTED_USER_ON_DUE_DAY));
	assertThat(list, hasItem(REGULAR_USER_ON_DUE_DAY));
    }
}
