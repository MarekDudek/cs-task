package test.analyser;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;

import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Predicate;

import solution.iterator.FilteringIterator;
import test.transactions.Transaction;

public class IteratingFraudAnalyser extends FraudAnalyser {

    private final Predicate<Transaction> skipAnalysis;
    private final Deque<Transaction> suspicious = newLinkedList();

    public IteratingFraudAnalyser(final Predicate<Transaction> skipAnalysis)
    {
	this.skipAnalysis = checkNotNull(skipAnalysis);
    }

    @Override
    public Iterator<Transaction> analyse(final Iterator<Transaction> transactions, final Date date)
    {
	return new FilteringIterator<Transaction>(transactions, skipAnalysis.negate()) {

	    @Override
	    public boolean hasNext()
	    {
		final boolean hasNext = super.hasNext();
		if (hasNext) {
		    final Transaction next = super.next();
		    suspicious.add(next);
		}
		return hasNext;
	    }

	    @Override
	    public Transaction next()
	    {
		return suspicious.pop();
	    }
	};
    }
}
