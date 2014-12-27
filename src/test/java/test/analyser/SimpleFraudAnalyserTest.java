package test.analyser;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static test.analyser.TestData.BLACKLISTED_USER_ON_DUE_DAY;
import static test.analyser.TestData.DUE_DAY;
import static test.analyser.TestData.REGULAR_USER_ON_DUE_DAY;
import static test.analyser.TestData.SKIP_ANALYSIS;
import static test.analyser.TestData.SUSPECT_INDIVIDUALLY;
import static test.analyser.TestData.VARIOUS_TRANSACTIONS;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import solution.collectors.StatsCollector;
import test.transactions.Transaction;

public class SimpleFraudAnalyserTest {

    /** System under test. */
    private FraudAnalyser analyser;

    @Test
    public void skipping_and_individual_analysis_work_fine()
    {
	// given
	final StatsCollector nullCollector = new StatsCollector() {
	};

	analyser = new SimpleFraudAnalyser(SKIP_ANALYSIS, SUSPECT_INDIVIDUALLY, nullCollector);

	// when
	final Iterator<Transaction> iterator = analyser.analyse(VARIOUS_TRANSACTIONS.iterator(), DUE_DAY);
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

	analyser = new SimpleFraudAnalyser(SKIP_ANALYSIS, SUSPECT_INDIVIDUALLY, collector);

	// when
	final Iterator<Transaction> iterator = analyser.analyse(VARIOUS_TRANSACTIONS.iterator(), DUE_DAY);
	final List<Transaction> output = newArrayList(iterator);

	// then
	assertThat(output, hasSize(2));
	assertThat(output, hasItem(BLACKLISTED_USER_ON_DUE_DAY));
	assertThat(output, hasItem(REGULAR_USER_ON_DUE_DAY));
    }
}
