package test.analyser;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.MILLISECOND;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import org.javatuples.Pair;

import solution.transactions.TransactionGeneratorConfig;
import test.transactions.Transaction;

public final class TestGeneratorSettings {

    public static final int SEED = 0;

    public static final int MIN_ID = 1000;
    public static final int MAX_ID = 9999;

    public static final int USER_COUNT = 100;
    public static final int WHITELISTED_COUNT = 10;
    public static final int BLACKLISTED_COUNT = 10;

    public static final int ACCOUNT_COUNT = 100;

    public static final Date DUE_DAY = new Calendar.Builder()
            .setDate(2014, DECEMBER, 22)
            .setTimeOfDay(12, 33, 58)
            .set(MILLISECOND, 523)
            .build().getTime();
    public static final int DAYS_MARGIN = 1;

    public static final BigDecimal MIN_AMOUNT = new BigDecimal(100);
    public static final BigDecimal MAX_AMOUNT = new BigDecimal(999);

    /** Generator configuration. */
    public static final TransactionGeneratorConfig CONFIG =
            new TransactionGeneratorConfig(SEED, MIN_ID, MAX_ID, USER_COUNT, ACCOUNT_COUNT, DUE_DAY, DAYS_MARGIN, MIN_AMOUNT, MAX_AMOUNT);

    public static final int MAX_ALLOWED_FROM_ACCOUNT = 6000;
    public static final int MAX_ALLOWED_TO_ACCOUNT_BY_USER = 140;

    @SuppressWarnings("unchecked")
    public static final List<Pair<Integer, BigDecimal>> THRESHOLDS = newArrayList
            (
                    Pair.with(1_000_000, new BigDecimal(10_000_000)),
                    Pair.with(6699, new BigDecimal(3_685_272))
            );

    public static final int VERY_BIG_NUMBER = 1_000_000_000;

    /** Number of transactions to generate. */
    public static final int NUMBER_OF_TRANSACTIONS = 1_000_000;

    // Result counts
    public static final int EXPECTED_NUMBER_OF_ALL_SUSPICIOUS = 33_426;
    public static final int SUSPICIOUS_INDIVIDUALLY_COUNT = 33_426;
    public static final int EXCEEDING_COUNT_FROM_ACCOUNT = 5_986;
    public static final int EXCEEDING_COUNT_BY_USER_TO_ACCOUNT = 338;
    public static final int EXCEEDING_ANY_THRESHOLD_OF_COUNT_AND_TOTAL_AMOUNT = 10_387;

    public static final Predicate<Transaction> ALWAYS_FAIL = transaction -> false;

    private TestGeneratorSettings() {
        super();
    }
}
