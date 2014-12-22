package solution.transactions;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import test.transactions.Transaction;

public class TransactionGeneratorTest {

    private static final int SEED = 0;
    private TransactionGenerator generator;

    @Before
    public void setup()
    {
	// given
	generator = new TransactionGenerator(0);
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
	final TransactionGenerator secondGenerator = new TransactionGenerator(SEED);

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
}
