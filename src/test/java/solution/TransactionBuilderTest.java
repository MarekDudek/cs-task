package solution;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static solution.TransactionBuilder.transaction;

import org.junit.Before;
import org.junit.Test;

import test.transactions.Transaction;

public class TransactionBuilderTest {

    private static final long TRANSACTION_ID = 11111L;
    private static final long USER_ID = 22222L;
    private static final long TO_ACCOUNT = 33333L;
    private static final long FROM_ACCOUNT = 44444L;

    @Before
    public void setup()
    {
    }

    @Test
    public void test()
    {
	// when
	final Transaction transaction = transaction()
		.id(TRANSACTION_ID)
		.user(USER_ID)
		.toAccount(TO_ACCOUNT)
		.fromAccount(FROM_ACCOUNT)
		.build();

	// then
	assertThat(transaction.getTransactionId(), equalTo(TRANSACTION_ID));
	assertThat(transaction.getUserId(), equalTo(USER_ID));
	assertThat(transaction.getAccountToId(), equalTo(TO_ACCOUNT));
	assertThat(transaction.getAccountFromId(), equalTo(FROM_ACCOUNT));
    }
}
