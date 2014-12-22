package test.analyser;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static solution.PredicateFactory.belongsTo;
import static solution.PredicateFactory.sameDate;
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

    // given
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

    private static final Predicate<Transaction> SKIP_ANALYSIS = belongsTo(WHITELISTED_USERS).or(sameDate(DUE_DAY).negate());
    private static final Predicate<Transaction> SUSPECT_INDIVIDUALLY = belongsTo(BLACKLISTED_USERS);

    private static final Transaction WHITELISTED_USER_ON_DUE_DAY = transaction().date(DUE_DAY).user(WHITELISTED_USER_1).build();
    private static final Transaction BLACKLISTED_USER_ON_DUE_DAY = transaction().date(DUE_DAY).user(BLACKLISTED_USER_1).build();
    private static final Transaction BLACKLISTED_USER_ON_OTHER_DAY = transaction().date(OTHER_DAY).user(BLACKLISTED_USER_1).build();
    private static final Transaction REGULAR_USER_ON_DUE_DAY = transaction().date(DUE_DAY).user(REGULAR_USER_1).build();

    @Test
    public void skipping_and_individual_analysis_work_fine()
    {
	// given
	final StatsCollector nullCollector = new StatsCollector() {
	};

	analyser = new FraudAnalyser(SKIP_ANALYSIS, SUSPECT_INDIVIDUALLY, nullCollector);

	final List<Transaction> transactions = newArrayList
		(
			WHITELISTED_USER_ON_DUE_DAY,
			BLACKLISTED_USER_ON_OTHER_DAY,
			BLACKLISTED_USER_ON_DUE_DAY,
			REGULAR_USER_ON_DUE_DAY
		);

	// when
	final Iterator<Transaction> iterator = analyser.analyse(transactions.iterator(), DUE_DAY);
	final List<Transaction> output = newArrayList(iterator);

	// then
	assertThat(output, hasSize(1));
	assertThat(output, hasItem(BLACKLISTED_USER_ON_DUE_DAY));
    }

    @Test
    public void merging_suspicious_works_fine()
    {
	// given
	final StatsCollector collector = mock(StatsCollector.class);
	given(collector.suspicious()).willReturn(newArrayList(REGULAR_USER_ON_DUE_DAY));

	analyser = new FraudAnalyser(SKIP_ANALYSIS, SUSPECT_INDIVIDUALLY, collector);

	final List<Transaction> transactions = newArrayList
		(
			WHITELISTED_USER_ON_DUE_DAY,
			BLACKLISTED_USER_ON_OTHER_DAY,
			BLACKLISTED_USER_ON_DUE_DAY,
			REGULAR_USER_ON_DUE_DAY
		);

	// when
	final Iterator<Transaction> iterator = analyser.analyse(transactions.iterator(), DUE_DAY);
	final List<Transaction> output = newArrayList(iterator);

	// then
	assertThat(output, hasSize(2));
	assertThat(output, hasItem(BLACKLISTED_USER_ON_DUE_DAY));
	assertThat(output, hasItem(REGULAR_USER_ON_DUE_DAY));
    }
}
