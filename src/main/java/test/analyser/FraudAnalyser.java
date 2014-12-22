package test.analyser;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import solution.collectors.StatsCollector;
import test.transactions.Transaction;

import com.google.common.collect.Sets;

/**
 * Main class. Analyze all transactions from given date and return suspicious
 * transactions. Do not return transactions that are not suspicious.
 */
public class FraudAnalyser {

    private Predicate<Transaction> skipAnalysis;
    private Predicate<Transaction> suspectIndividually;
    private StatsCollector collector;

    public FraudAnalyser(
	    final Predicate<Transaction> skipAnalysis,
	    final Predicate<Transaction> suspiciousIndividually,
	    final StatsCollector collector)
    {
	this.skipAnalysis = skipAnalysis;
	this.suspectIndividually = suspiciousIndividually;
	this.collector = collector;
    }

    /**
     * @param list
     *            of transactions to be checked
     * @param date
     *            analyze only transactions from the given day
     * 
     * @return list off suspicious transactions
     */
    public Iterator<Transaction> analyse(final Iterator<Transaction> transactions, final Date date)
    {
	final List<Transaction> input = newArrayList(transactions);
	final List<Transaction> suspicious = newArrayList();

	for (final Transaction transaction : input)
	{
	    if (skipAnalysis.test(transaction)) {
		continue;
	    }

	    if (suspectIndividually.test(transaction)) {
		suspicious.add(transaction);
	    }

	    collector.collect(transaction);
	}

	final List<Transaction> suspiciousBasedOnStats = collector.suspicious();

	final Set<Transaction> intersection = Sets.newHashSet(suspicious);
	intersection.addAll(suspiciousBasedOnStats);

	return intersection.iterator();
    }
}