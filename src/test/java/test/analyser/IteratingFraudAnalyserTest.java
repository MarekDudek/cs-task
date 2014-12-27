package test.analyser;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static test.analyser.TestData.BLACKLISTED_USER_ON_DUE_DAY;
import static test.analyser.TestData.REGULAR_USER_ON_DUE_DAY;
import static test.analyser.TestData.SKIP_ANALYSIS;
import static test.analyser.TestData.SUSPECT_INDIVIDUALLY;
import static test.analyser.TestData.VARIOUS_TRANSACTIONS;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.junit.Test;

import test.transactions.Transaction;

public class IteratingFraudAnalyserTest {

    private static final Predicate<Transaction> SUSPECT_ALL = transaction -> true;

    private FraudAnalyser analyser;

    @Test(expected = NoSuchElementException.class)
    public void iteration_with_checking_if_next_exists()
    {
	// given

	analyser = new IteratingFraudAnalyser(SKIP_ANALYSIS, SUSPECT_ALL);

	// when
	final Iterator<Transaction> suspicious = analyser.analyse(VARIOUS_TRANSACTIONS.iterator(), null);

	// then
	assertThat(suspicious.hasNext(), is(true));
	assertThat(suspicious.hasNext(), is(true));
	assertThat(suspicious.next(), is(BLACKLISTED_USER_ON_DUE_DAY));

	assertThat(suspicious.hasNext(), is(true));
	assertThat(suspicious.hasNext(), is(true));
	assertThat(suspicious.next(), is(REGULAR_USER_ON_DUE_DAY));

	assertThat(suspicious.hasNext(), is(false));
	assertThat(suspicious.hasNext(), is(false));

	assertThat(suspicious.next(), any(Transaction.class));
    }

    @Test(expected = NoSuchElementException.class)
    public void iteration_without_checking_if_next_exists()
    {
	// given
	analyser = new IteratingFraudAnalyser(SKIP_ANALYSIS, SUSPECT_ALL);

	// when
	final Iterator<Transaction> suspicious = analyser.analyse(VARIOUS_TRANSACTIONS.iterator(), null);

	// then
	assertThat(suspicious.next(), is(BLACKLISTED_USER_ON_DUE_DAY));
	assertThat(suspicious.next(), is(REGULAR_USER_ON_DUE_DAY));

	assertThat(suspicious.next(), any(Transaction.class));
    }

    @Test(expected = NoSuchElementException.class)
    public void iteration_with_checking_if_next_exists__when_not_all_are_suspected()
    {
	// given

	analyser = new IteratingFraudAnalyser(SKIP_ANALYSIS, SUSPECT_INDIVIDUALLY);

	// when
	final Iterator<Transaction> suspicious = analyser.analyse(VARIOUS_TRANSACTIONS.iterator(), null);

	// then
	assertThat(suspicious.hasNext(), is(true));
	assertThat(suspicious.hasNext(), is(true));
	assertThat(suspicious.next(), is(BLACKLISTED_USER_ON_DUE_DAY));

	assertThat(suspicious.hasNext(), is(false));
	assertThat(suspicious.hasNext(), is(false));

	assertThat(suspicious.next(), any(Transaction.class));
    }

    @Test(expected = NoSuchElementException.class)
    public void iteration_without_checking_if_next_exists__when_not_all_are_suspected()
    {
	// given
	analyser = new IteratingFraudAnalyser(SKIP_ANALYSIS, SUSPECT_INDIVIDUALLY);

	// when
	final Iterator<Transaction> suspicious = analyser.analyse(VARIOUS_TRANSACTIONS.iterator(), null);

	// then
	assertThat(suspicious.next(), is(BLACKLISTED_USER_ON_DUE_DAY));

	assertThat(suspicious.next(), any(Transaction.class));
    }
}
