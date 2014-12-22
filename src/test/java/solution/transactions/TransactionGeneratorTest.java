package solution.transactions;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.MILLISECOND;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import test.transactions.Transaction;

public class TransactionGeneratorTest {

    private static final int SEED = 0;

    private static final Date MEDIAN_DATE = new Calendar.Builder()
	    .setDate(2014, DECEMBER, 22)
	    .setTimeOfDay(12, 33, 58)
	    .set(MILLISECOND, 523)
	    .build().getTime();
    private static final int DAYS_MARGIN = 1;

    private static final BigDecimal MAXIMUM_AMOUNT = new BigDecimal(100);

    private TransactionGenerator generator;

    @Before
    public void setup()
    {
	// given

	generator = new TransactionGenerator(0, 10, 20, MEDIAN_DATE, DAYS_MARGIN, MAXIMUM_AMOUNT);
    }

    @Test
    public void iterator_generates_proper_number_of_elements()
    {
	// when
	final Iterator<Transaction> transactions = generator.generateIterator(100);

	// then
	assertThat(newArrayList(transactions), hasSize(100));
    }

    @Test
    public void random_transaction_has_all_fields()
    {
	// when
	final Transaction transaction = generator.randomTransaction();

	// then
	assertThat(transaction.getTransactionId(), notNullValue());
	assertThat(transaction.getUserId(), notNullValue());
	assertThat(transaction.getDate(), notNullValue());
	assertThat(transaction.getAccountFromId(), notNullValue());
	assertThat(transaction.getAccountToId(), notNullValue());
	assertThat(transaction.getAmount(), notNullValue());
    }

    @Test
    public void values_are_random_but_alway_the_same_sequence_is_generated()
    {
	// given
	final TransactionGenerator secondGenerator = new TransactionGenerator(SEED, 10, 20, MEDIAN_DATE, DAYS_MARGIN, MAXIMUM_AMOUNT);

	// when
	final Transaction transaction1 = generator.randomTransaction();
	final Transaction transaction2 = secondGenerator.randomTransaction();

	// then
	assertThat(transaction1, not(sameInstance(transaction2)));

	assertThat(transaction1.getTransactionId(), equalTo(transaction2.getTransactionId()));
	assertThat(transaction1.getUserId(), equalTo(transaction2.getUserId()));
	assertThat(transaction1.getDate(), equalTo(transaction2.getDate()));
	assertThat(transaction1.getAccountFromId(), equalTo(transaction2.getAccountFromId()));
	assertThat(transaction1.getAccountToId(), equalTo(transaction2.getAccountToId()));
	assertThat(transaction1.getAmount(), equalTo(transaction2.getAmount()));
    }

    @Test
    public void date_is_generated_from_median_and_margins()
    {
	// given
	final Date median = new Calendar.Builder()
		.setDate(2014, DECEMBER, 22)
		.setTimeOfDay(12, 33, 58)
		.set(MILLISECOND, 523)
		.build().getTime();

	final int margin = 2;

	final Date lowerBound = new Calendar.Builder()
		.setDate(2014, DECEMBER, 20)
		.setTimeOfDay(0, 0, 0)
		.set(MILLISECOND, 0)
		.build().getTime();

	final Date upperBound = new Calendar.Builder()
		.setDate(2014, DECEMBER, 24)
		.setTimeOfDay(23, 59, 59)
		.set(MILLISECOND, 999)
		.build().getTime();

	for (int i = 0; i < 1000; i++)
	{
	    // when
	    final Date date = generator.randomDate(median, margin);

	    // then
	    assertThat(date, greaterThan(lowerBound));
	    assertThat(date, lessThan(upperBound));
	}
    }

    @Test
    public void amount_never_exceeds_specified_maximum()
    {
	final BigDecimal maximum = new BigDecimal(10);

	for (int i = 0; i < 1000; i++)
	{
	    // when
	    final BigDecimal amount = generator.randomAmount(maximum);

	    // then
	    assertThat(amount, lessThanOrEqualTo(maximum));
	    assertThat(amount, greaterThan(BigDecimal.ZERO));
	}
    }
}
