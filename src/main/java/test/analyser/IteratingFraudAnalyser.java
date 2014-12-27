package test.analyser;

import java.util.Date;
import java.util.Iterator;
import java.util.function.Predicate;

import solution.iterator.FilteringIterator;
import test.transactions.Transaction;

public class IteratingFraudAnalyser extends FraudAnalyser {

    private Predicate<Transaction> skipAnalysis;

    public IteratingFraudAnalyser(final Predicate<Transaction> skipAnalysis)
    {
	this.skipAnalysis = skipAnalysis;
    }

    @Override
    public Iterator<Transaction> analyse(final Iterator<Transaction> transactions, final Date date)
    {
	return new FilteringIterator<Transaction>(transactions, skipAnalysis.negate()) {

	    @Override
	    public boolean hasNext()
	    {
		return super.hasNext();
	    }

	    @Override
	    public Transaction next()
	    {
		return super.next();
	    }
	};
    }
}
