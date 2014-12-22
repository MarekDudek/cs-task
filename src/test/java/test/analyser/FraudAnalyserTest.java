package test.analyser;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static solution.PredicateFactory.blacklisted;
import static solution.PredicateFactory.sameDate;
import static solution.PredicateFactory.whitelisted;
import static solution.TestRequirements.BLACKLISTED_USERS;
import static solution.TestRequirements.BLACKLISTED_USER_1;
import static solution.TestRequirements.REGULAR_USER_1;
import static solution.TestRequirements.WHITELISTED_USERS;
import static solution.TestRequirements.WHITELISTED_USER_1;
import static solution.TransactionBuilder.transaction;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;

import solution.collectors.StatsCollector;
import test.transactions.Transaction;

public class FraudAnalyserTest {

    /** System under test. */
    private FraudAnalyser analyser;

    private static final Date DUE_DAY =
	    new Calendar.Builder()
		    .setDate(2014, Calendar.DECEMBER, 21)
		    .setTimeOfDay(23, 59, 59)
		    .build().getTime();

    private static final Date OTHER_DAY =
	    new Calendar.Builder()
		    .setDate(1997, Calendar.JULY, 1)
		    .setTimeOfDay(1, 1, 1)
		    .build().getTime();

    private Predicate<Transaction> skipAnalysis = whitelisted(WHITELISTED_USERS).or(sameDate(DUE_DAY).negate());
    private Predicate<Transaction> suspiciousIndividually = blacklisted(BLACKLISTED_USERS);

    @Test
    public void skipping_and_individual_tests_work_fine()
    {
	// given
	StatsCollector collector = new StatsCollector() {
	};
	analyser = new FraudAnalyser(skipAnalysis, suspiciousIndividually, collector);

	final List<Transaction> transactions = newArrayList
		(
			transaction().date(DUE_DAY).user(WHITELISTED_USER_1).build(),
			transaction().date(OTHER_DAY).user(BLACKLISTED_USER_1).build(),
			transaction().date(DUE_DAY).user(BLACKLISTED_USER_1).build(),
			transaction().date(DUE_DAY).user(REGULAR_USER_1).build()
		);

	// when
	final Iterator<Transaction> iterator = analyser.analyse(transactions.iterator(), DUE_DAY);
	final List<Transaction> output = newArrayList(iterator);

	// then
	assertThat(output, hasSize(1));
    }

    @Test
    public void merging_suspicious_works_fine()
    {
	// given
	final Transaction regularTransactionOnDueDay = transaction().date(DUE_DAY).user(REGULAR_USER_1).build();

	final StatsCollector collector = mock(StatsCollector.class);
	given(collector.suspicious()).willReturn(newArrayList(regularTransactionOnDueDay));

	analyser = new FraudAnalyser(skipAnalysis, suspiciousIndividually, collector);

	final List<Transaction> transactions = newArrayList
		(
			transaction().date(DUE_DAY).user(WHITELISTED_USER_1).build(),
			transaction().date(OTHER_DAY).user(BLACKLISTED_USER_1).build(),
			transaction().date(DUE_DAY).user(BLACKLISTED_USER_1).build(),
			regularTransactionOnDueDay
		);

	// when
	final Iterator<Transaction> iterator = analyser.analyse(transactions.iterator(), DUE_DAY);
	final List<Transaction> output = newArrayList(iterator);

	// then
	assertThat(output, hasSize(2));
    }
}
