package test.integration;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.intersection;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.MILLISECOND;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static solution.PredicateFactory.belongsTo;
import static solution.PredicateFactory.sameDate;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import test.analyser.IteratingFraudAnalyser;
import test.analyser.SimpleFraudAnalyser;
import test.transactions.Transaction;

public class PerformanceTest {

    private static final int SEED = 0;

    private static final int MIN_ID = 1000;
    private static final int MAX_ID = 9999;

    private static final int USER_COUNT = 100;
    private static final int ACCOUNT_COUNT = 100;

    private static final int WHITELISTED_COUNT = 10;
    private static final int BLACKLISTED_COUNT = 10;

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

    // Statistics collectors

    private static final int MAX_ALLOWED_FROM_ACCOUNT = 6000;
    private static final StatsCollector FROM_ACCOUNT = new TransactionCountFromAccoutCollector(MAX_ALLOWED_FROM_ACCOUNT);

    private static final int MAX_ALLOWED_TO_ACCOUNT_BY_USER = 140;
    private static final StatsCollector TO_ACCOUNT_BY_USER = new TransactionCountToAccountByUserCollector(MAX_ALLOWED_TO_ACCOUNT_BY_USER);

    @SuppressWarnings("unchecked")
    private static final List<Pair<Integer, BigDecimal>> THRESHOLDS = newArrayList
	    (
		    Pair.with(1_000_000, new BigDecimal(10_000_000)),
		    Pair.with(6699, new BigDecimal(3_685_272))
	    );
    private static final StatsCollector FROM_USER_AND_SUM_TOTAL = new TransactionCountFromUserAndSumTotalCollector(THRESHOLDS);

    /** Collectors configuration. */
    private static final StatsCollector COLLECTOR = new MultiStatCollector
	    (
		    FROM_ACCOUNT,
		    TO_ACCOUNT_BY_USER,
		    FROM_USER_AND_SUM_TOTAL
	    );

    /** Number of transactions to generate. */
    private static final int NUMBER_OF_TRANSACTIONS = 1_000_000;

    @Test
    public void simple_fraud_analyser()
    {
	// given
	final TransactionGenerator generator = new TransactionGenerator(CONFIG);

	final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
	final Predicate<Transaction> skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

	final List<Long> blacklisted = generator.chooseBlacklisted(BLACKLISTED_COUNT);
	final Predicate<Transaction> suspectIndividually = belongsTo(blacklisted);

	final FraudAnalyser analyser = new SimpleFraudAnalyser(skipAnalysis, suspectIndividually, COLLECTOR);

	// when
	final Iterator<Transaction> transactions = generator.generateIterator(NUMBER_OF_TRANSACTIONS);
	final Iterator<Transaction> suspicious = analyser.analyse(transactions, DUE_DAY);

	// then
	assertThat(whitelisted, hasSize(WHITELISTED_COUNT));
	assertThat(blacklisted, hasSize(BLACKLISTED_COUNT));

	final Set<Long> common = intersection(newHashSet(whitelisted), newHashSet(blacklisted));
	assertThat(common, is(empty()));

	assertThat(newArrayList(suspicious), hasSize(45287));
    }

    @Test
    public void iterating_fraud_analyser()
    {
	// given
	final TransactionGenerator generator = new TransactionGenerator(CONFIG);

	final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
	final Predicate<Transaction> skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

	final List<Long> blacklisted = generator.chooseBlacklisted(BLACKLISTED_COUNT);
	final Predicate<Transaction> suspectIndividually = belongsTo(blacklisted);

	final FraudAnalyser analyser = new IteratingFraudAnalyser(skipAnalysis, suspectIndividually);

	// when
	final Iterator<Transaction> transactions = generator.generateIterator(NUMBER_OF_TRANSACTIONS);
	final Iterator<Transaction> suspicious = analyser.analyse(transactions, DUE_DAY);

	// then
	assertThat(newArrayList(suspicious), hasSize(33339));
    }
}
