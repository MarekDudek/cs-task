package test.integration;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.MILLISECOND;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static requirements.TestRequirements.BLACKLISTED_USERS;
import static requirements.TestRequirements.WHITELISTED_USERS;
import static solution.PredicateFactory.belongsTo;
import static solution.PredicateFactory.sameDate;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.javatuples.Pair;
import org.junit.Test;

import solution.collectors.MultiStatCollector;
import solution.collectors.StatsCollector;
import solution.collectors.TransactionCountFromAccoutCollector;
import solution.collectors.TransactionCountFromUserAndSumTotalCollector;
import solution.collectors.TransactionCountToAccountByUserCollector;
import solution.transactions.TransactionGenerator;
import solution.transactions.TransactionGeneratorConfig;
import test.analyser.FraudAnalyser;
import test.transactions.Transaction;

public class LongRunTest {

    private static final int SEED = 0;

    private static final int MIN_ID = 1000;
    private static final int MAX_ID = 9999;

    private static final int USER_COUNT = 4;
    private static final int ACCOUNT_COUNT = 5;

    private static final Date DUE_DAY = new Calendar.Builder()
	    .setDate(2014, DECEMBER, 22)
	    .setTimeOfDay(12, 33, 58)
	    .set(MILLISECOND, 523)
	    .build().getTime();
    private static final int DAYS_MARGIN = 1;

    private static final BigDecimal MIN_AMOUNT = new BigDecimal(100);
    private static final BigDecimal MAX_AMOUNT = new BigDecimal(999);

    /** Generator configuration. */
    private static final TransactionGeneratorConfig CONFIG =
	    new TransactionGeneratorConfig(SEED, MIN_ID, MAX_ID, USER_COUNT, ACCOUNT_COUNT, DUE_DAY, DAYS_MARGIN, MIN_AMOUNT, MAX_AMOUNT);

    // Fraud analyzer components

    private static final Predicate<Transaction> SKIP_ANALYSIS = belongsTo(WHITELISTED_USERS).or(sameDate(DUE_DAY).negate());
    private static final Predicate<Transaction> SUSPECT_INDIVIDUALLY = belongsTo(BLACKLISTED_USERS);

    // Statistics collectors

    private static final int MAX_ALLOWED_FROM_ACCOUNT = 5;
    private static final StatsCollector FROM_ACCOUNT = new TransactionCountFromAccoutCollector(MAX_ALLOWED_FROM_ACCOUNT);

    private static final int MAX_ALLOWED_TO_ACCOUNT_BY_USER = 4;
    private static final StatsCollector TO_ACCOUNT_BY_USER = new TransactionCountToAccountByUserCollector(MAX_ALLOWED_TO_ACCOUNT_BY_USER);

    @SuppressWarnings("unchecked")
    private static final List<Pair<Integer, BigDecimal>> THRESHOLDS = newArrayList
	    (
		    Pair.with(2, new BigDecimal(6000)),
		    Pair.with(3, new BigDecimal(3000))
	    );
    private static final StatsCollector FROM_USER_AND_SUM_TOTAL = new TransactionCountFromUserAndSumTotalCollector(THRESHOLDS);

    /** Collectors configuration. */
    private static final StatsCollector COLLECTOR = new MultiStatCollector(FROM_ACCOUNT, TO_ACCOUNT_BY_USER, FROM_USER_AND_SUM_TOTAL);

    /** Number of transactions to generate. */
    private static final int NUMBER_OF_TRANSACTIONS = 100;

    @Test
    public void test()
    {
	// given
	final TransactionGenerator generator = new TransactionGenerator(CONFIG);
	final FraudAnalyser analyser = new FraudAnalyser(SKIP_ANALYSIS, SUSPECT_INDIVIDUALLY, COLLECTOR);

	// when
	final Iterator<Transaction> transactions = generator.generateIterator(NUMBER_OF_TRANSACTIONS);
	final Iterator<Transaction> suspicious = analyser.analyse(transactions, DUE_DAY);

	// then
	assertThat(newArrayList(suspicious), hasSize(26));
    }
}
