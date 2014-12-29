package solution.collectors;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

import solution.TransactionBuilder;
import test.transactions.Transaction;

public class TransactionCountToAccountByUserCollectorTest {

    private static final long USER_1 = 1L;
    private static final long USER_2 = 2L;

    private static final long ACCOUNT_1 = 11;
    private static final long ACCOUNT_2 = 12;

    private static final Transaction TRANSACTION_1 = TransactionBuilder.transaction().user(USER_1).toAccount(ACCOUNT_1).build();
    private static final Transaction TRANSACTION_2 = TransactionBuilder.transaction().user(USER_1).toAccount(ACCOUNT_1).build();
    private static final Transaction TRANSACTION_3 = TransactionBuilder.transaction().user(USER_1).toAccount(ACCOUNT_1).build();

    private static final Transaction TRANSACTION_4 = TransactionBuilder.transaction().user(USER_2).toAccount(ACCOUNT_2).build();
    private static final Transaction TRANSACTION_5 = TransactionBuilder.transaction().user(USER_2).toAccount(ACCOUNT_2).build();

    private static final Transaction TRANSACTION_6 = TransactionBuilder.transaction().user(USER_1).toAccount(ACCOUNT_2).build();
    private static final Transaction TRANSACTION_7 = TransactionBuilder.transaction().user(USER_2).toAccount(ACCOUNT_1).build();

    @Test
    public void test()
    {
        // given
        final StatsCollector collector = new TransactionCountToAccountByUserCollector(2);

        // when
        collector.collect(TRANSACTION_1);
        collector.collect(TRANSACTION_2);
        collector.collect(TRANSACTION_3);
        collector.collect(TRANSACTION_4);
        collector.collect(TRANSACTION_5);
        collector.collect(TRANSACTION_6);
        collector.collect(TRANSACTION_7);

        final Collection<Transaction> suspicious = collector.suspicious();

        // then
        assertThat(suspicious, hasSize(3));
        assertThat(suspicious, hasItem(TRANSACTION_1));
        assertThat(suspicious, hasItem(TRANSACTION_2));
        assertThat(suspicious, hasItem(TRANSACTION_3));
    }
}
