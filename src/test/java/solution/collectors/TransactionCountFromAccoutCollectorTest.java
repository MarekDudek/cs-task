package solution.collectors;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static solution.TransactionBuilder.transaction;

import java.util.Collection;

import org.junit.Test;

import test.transactions.Transaction;

public class TransactionCountFromAccoutCollectorTest {

    private static final long ACCOUNT_1 = 1L;
    private static final long ACCOUNT_2 = 2L;
    private static final long ACCOUNT_3 = 3L;

    private static final Transaction TRANSACTION_1 = transaction().fromAccount(ACCOUNT_1).build();

    private static final Transaction TRANSACTION_2 = transaction().fromAccount(ACCOUNT_2).build();
    private static final Transaction TRANSACTION_3 = transaction().fromAccount(ACCOUNT_2).build();

    private static final Transaction TRANSACTION_4 = transaction().fromAccount(ACCOUNT_3).build();
    private static final Transaction TRANSACTION_5 = transaction().fromAccount(ACCOUNT_3).build();
    private static final Transaction TRANSACTION_6 = transaction().fromAccount(ACCOUNT_3).build();

    @Test
    public void test()
    {
	// given
	final TransactionCountFromAccoutCollector collector = new TransactionCountFromAccoutCollector(2);

	// when
	collector.collect(TRANSACTION_1);
	collector.collect(TRANSACTION_2);
	collector.collect(TRANSACTION_3);
	collector.collect(TRANSACTION_4);
	collector.collect(TRANSACTION_5);
	collector.collect(TRANSACTION_6);

	final Collection<Transaction> suspicious = collector.suspicious();

	// then
	assertThat(suspicious, hasSize(3));
	assertThat(suspicious, hasItem(TRANSACTION_4));
	assertThat(suspicious, hasItem(TRANSACTION_5));
	assertThat(suspicious, hasItem(TRANSACTION_6));
    }
}
