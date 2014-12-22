package test.analyser;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import solution.collectors.StatsCollector;
import test.transactions.Transaction;

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
     * @return list of suspicious transactions
     */
    public Iterator<Transaction> analyse(final Iterator<Transaction> transactions, final Date date)
    {
	final Collection<Transaction> input = newArrayList(transactions);
	final Collection<Transaction> suspicious = newArrayList();

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

	final Collection<Transaction> suspiciousBasedOnStats = collector.suspicious();

	final Set<Transaction> union = newHashSet(suspicious);
	union.addAll(suspiciousBasedOnStats);

	return union.iterator();
    }
}