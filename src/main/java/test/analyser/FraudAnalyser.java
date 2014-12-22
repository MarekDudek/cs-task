package test.analyser;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import test.transactions.Transaction;

/**
 * Main class. Analyse all transactions from given date and return suspicious
 * transactions. Do not return transactions that are not suspicious.
 */
public class FraudAnalyser {

    private Predicate<Transaction> skipAnalysis;
    private Predicate<Transaction> suspiciousIndividually;

    public FraudAnalyser(
	    final Predicate<Transaction> skipAnalysis,
	    final Predicate<Transaction> suspiciousIndividually)
    {
	this.skipAnalysis = skipAnalysis;
	this.suspiciousIndividually = suspiciousIndividually;
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
	final List<Transaction> output = newArrayList();

	for (final Transaction transaction : input)
	{
	    if (skipAnalysis.test(transaction)) {
		continue;
	    }

	    if (suspiciousIndividually.test(transaction)) {
		output.add(transaction);
	    }
	}

	return output.iterator();
    }
}