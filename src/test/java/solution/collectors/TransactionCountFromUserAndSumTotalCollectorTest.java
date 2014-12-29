package solution.collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static solution.TransactionBuilder.transaction;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.javatuples.Pair;
import org.junit.Test;

import test.transactions.Transaction;

public class TransactionCountFromUserAndSumTotalCollectorTest {

    private static final long USER_1 = 1L;
    private static final long USER_2 = 2L;
    private static final long USER_3 = 3L;
    private static final long USER_4 = 4L;
    private static final long USER_5 = 5L;
    private static final long USER_6 = 6L;
    private static final long USER_7 = 7L;
    private static final long USER_8 = 8L;
    private static final long USER_9 = 9L;

    // won't exceed: count below any, sum below any
    private static final Transaction TRANSACTION_1 = transaction().user(USER_1).amount(new BigDecimal(2000)).build();
    private static final Transaction TRANSACTION_2 = transaction().user(USER_1).amount(new BigDecimal(2000)).build();

    // won't exceed: count below any, sum between
    private static final Transaction TRANSACTION_3 = transaction().user(USER_2).amount(new BigDecimal(4000)).build();
    private static final Transaction TRANSACTION_4 = transaction().user(USER_2).amount(new BigDecimal(4000)).build();

    // won't exceed: count below any, sum above any
    private static final Transaction TRANSACTION_5 = transaction().user(USER_3).amount(new BigDecimal(6000)).build();
    private static final Transaction TRANSACTION_6 = transaction().user(USER_3).amount(new BigDecimal(6000)).build();

    // won't exceed: count between, sum below any
    private static final Transaction TRANSACTION_7 = transaction().user(USER_4).amount(new BigDecimal(1000)).build();
    private static final Transaction TRANSACTION_8 = transaction().user(USER_4).amount(new BigDecimal(1000)).build();
    private static final Transaction TRANSACTION_9 = transaction().user(USER_4).amount(new BigDecimal(1000)).build();
    private static final Transaction TRANSACTION_10 = transaction().user(USER_4).amount(new BigDecimal(1000)).build();

    // won't exceed: count between, sum between
    private static final Transaction TRANSACTION_11 = transaction().user(USER_5).amount(new BigDecimal(2000)).build();
    private static final Transaction TRANSACTION_12 = transaction().user(USER_5).amount(new BigDecimal(2000)).build();
    private static final Transaction TRANSACTION_13 = transaction().user(USER_5).amount(new BigDecimal(2000)).build();
    private static final Transaction TRANSACTION_14 = transaction().user(USER_5).amount(new BigDecimal(2000)).build();

    // will exceed I: count between, sum above
    private static final Transaction TRANSACTION_15 = transaction().user(USER_6).amount(new BigDecimal(3000)).build();
    private static final Transaction TRANSACTION_16 = transaction().user(USER_6).amount(new BigDecimal(3000)).build();
    private static final Transaction TRANSACTION_17 = transaction().user(USER_6).amount(new BigDecimal(3000)).build();
    private static final Transaction TRANSACTION_18 = transaction().user(USER_6).amount(new BigDecimal(3000)).build();

    // won't exceed: count above, sum below
    private static final Transaction TRANSACTION_19 = transaction().user(USER_7).amount(new BigDecimal(500)).build();
    private static final Transaction TRANSACTION_20 = transaction().user(USER_7).amount(new BigDecimal(500)).build();
    private static final Transaction TRANSACTION_21 = transaction().user(USER_7).amount(new BigDecimal(500)).build();
    private static final Transaction TRANSACTION_22 = transaction().user(USER_7).amount(new BigDecimal(500)).build();
    private static final Transaction TRANSACTION_23 = transaction().user(USER_7).amount(new BigDecimal(500)).build();
    private static final Transaction TRANSACTION_24 = transaction().user(USER_7).amount(new BigDecimal(500)).build();

    // will exceed II: count above, sum between
    private static final Transaction TRANSACTION_25 = transaction().user(USER_8).amount(new BigDecimal(1000)).build();
    private static final Transaction TRANSACTION_26 = transaction().user(USER_8).amount(new BigDecimal(1000)).build();
    private static final Transaction TRANSACTION_27 = transaction().user(USER_8).amount(new BigDecimal(1000)).build();
    private static final Transaction TRANSACTION_28 = transaction().user(USER_8).amount(new BigDecimal(1000)).build();
    private static final Transaction TRANSACTION_29 = transaction().user(USER_8).amount(new BigDecimal(1000)).build();
    private static final Transaction TRANSACTION_30 = transaction().user(USER_8).amount(new BigDecimal(1000)).build();

    // will exceed both: count above, sum above
    private static final Transaction TRANSACTION_31 = transaction().user(USER_9).amount(new BigDecimal(2000)).build();
    private static final Transaction TRANSACTION_32 = transaction().user(USER_9).amount(new BigDecimal(2000)).build();
    private static final Transaction TRANSACTION_33 = transaction().user(USER_9).amount(new BigDecimal(2000)).build();
    private static final Transaction TRANSACTION_34 = transaction().user(USER_9).amount(new BigDecimal(2000)).build();
    private static final Transaction TRANSACTION_35 = transaction().user(USER_9).amount(new BigDecimal(2000)).build();
    private static final Transaction TRANSACTION_36 = transaction().user(USER_9).amount(new BigDecimal(2000)).build();

    @Test
    public void test()
    {
        // given
        @SuppressWarnings("unchecked")
        final List<Pair<Integer, BigDecimal>> thresholds = newArrayList
                (
                        Pair.with(3, new BigDecimal(10000)),
                        Pair.with(5, new BigDecimal(5000))
                );
        final StatsCollector collector = new TransactionCountFromUserAndSumTotalCollector(thresholds);

        // when
        collector.collect(TRANSACTION_1);
        collector.collect(TRANSACTION_2);
        collector.collect(TRANSACTION_3);
        collector.collect(TRANSACTION_4);
        collector.collect(TRANSACTION_5);
        collector.collect(TRANSACTION_6);
        collector.collect(TRANSACTION_7);
        collector.collect(TRANSACTION_8);
        collector.collect(TRANSACTION_9);
        collector.collect(TRANSACTION_10);
        collector.collect(TRANSACTION_11);
        collector.collect(TRANSACTION_12);
        collector.collect(TRANSACTION_13);
        collector.collect(TRANSACTION_14);
        collector.collect(TRANSACTION_15);
        collector.collect(TRANSACTION_16);
        collector.collect(TRANSACTION_17);
        collector.collect(TRANSACTION_18);
        collector.collect(TRANSACTION_19);
        collector.collect(TRANSACTION_20);
        collector.collect(TRANSACTION_21);
        collector.collect(TRANSACTION_22);
        collector.collect(TRANSACTION_23);
        collector.collect(TRANSACTION_24);
        collector.collect(TRANSACTION_25);
        collector.collect(TRANSACTION_26);
        collector.collect(TRANSACTION_27);
        collector.collect(TRANSACTION_28);
        collector.collect(TRANSACTION_29);
        collector.collect(TRANSACTION_30);
        collector.collect(TRANSACTION_31);
        collector.collect(TRANSACTION_32);
        collector.collect(TRANSACTION_33);
        collector.collect(TRANSACTION_34);
        collector.collect(TRANSACTION_35);
        collector.collect(TRANSACTION_36);

        final Collection<Transaction> suspicious = collector.suspicious();

        // then
        assertThat(suspicious, hasSize(16));
    }
}
