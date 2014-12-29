package solution;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static solution.TransactionBuilder.transaction;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

import test.transactions.Transaction;

public class TransactionBuilderTest {

    // given
    private static final long TRANSACTION_ID = 11111L;
    private static final long USER_ID = 22222L;
    private static final Date DATE = new Date();
    private static final long TO_ACCOUNT = 33333L;
    private static final long FROM_ACCOUNT = 44444L;
    private static final BigDecimal AMOUNT = new BigDecimal(55555);

    @Test
    public void transaction_is_properly_built()
    {
        // when
        final Transaction transaction = transaction()
                .id(TRANSACTION_ID)
                .user(USER_ID)
                .date(DATE)
                .toAccount(TO_ACCOUNT)
                .fromAccount(FROM_ACCOUNT)
                .amount(AMOUNT)
                .build();

        // then
        assertThat(transaction.getTransactionId(), is(equalTo(TRANSACTION_ID)));
        assertThat(transaction.getUserId(), is(equalTo(USER_ID)));
        assertThat(transaction.getDate(), is(equalTo(DATE)));
        assertThat(transaction.getAccountToId(), is(equalTo(TO_ACCOUNT)));
        assertThat(transaction.getAccountFromId(), is(equalTo(FROM_ACCOUNT)));
        assertThat(transaction.getAmount(), is(equalTo(AMOUNT)));
    }
}
