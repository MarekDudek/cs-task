package test.analyser;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;

import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.BooleanUtils;

import solution.collectors.StatsCollector;
import solution.iterator.FilteringIterator;
import test.transactions.Transaction;

public class IteratingFraudAnalyser extends FraudAnalyser {

    private final Predicate<Transaction> skipAnalysis;
    private final Predicate<Transaction> suspectIndividually;
    private final StatsCollector collector;

    private final Deque<Transaction> suspicious = newLinkedList();

    public IteratingFraudAnalyser(
	    final Predicate<Transaction> skipAnalysis,
	    final Predicate<Transaction> suspectIndividually,
	    final StatsCollector collector)
    {
	this.skipAnalysis = checkNotNull(skipAnalysis);
	this.suspectIndividually = checkNotNull(suspectIndividually);
	this.collector = checkNotNull(collector);
    }

    @Override
    public Iterator<Transaction> analyse(final Iterator<Transaction> transactions, final Date date)
    {
	return new FilteringIterator<Transaction>(transactions, skipAnalysis.negate()) {

	    @Override
	    public boolean hasNext()
	    {
		if (BooleanUtils.isFalse(suspicious.isEmpty())) {
		    return true;
		}

		final Optional<Transaction> optional = findNextSuspiciousIndividually();
		if (optional.isPresent()) {
		    final Transaction transaction = optional.get();
		    suspicious.add(transaction);
		}

		return BooleanUtils.isFalse(suspicious.isEmpty());
	    }

	    @Override
	    public Transaction next()
	    {
		if (suspicious.isEmpty()) {
		    hasNext();
		}

		return suspicious.pop();
	    }

	    private Optional<Transaction> findNextSuspiciousIndividually()
	    {
		do {
		    final Transaction candidate = super.next();
		    if (candidate == null) {
			return Optional.empty();
		    }
		    if (suspectIndividually.test(candidate)) {
			return Optional.of(candidate);
		    }
		} while (true);
	    }
	};
    }
}
