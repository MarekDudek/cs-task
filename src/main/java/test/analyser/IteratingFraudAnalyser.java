package test.analyser;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;

import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
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

    private final Deque<Transaction> cache = newLinkedList();
    private final List<Transaction> suspicious = newLinkedList();

    private boolean inputExhausted = false;

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
		if (inputExhausted) {
		    return cacheHasItems();
		}

		if (cacheHasItems()) {
		    return true;
		}

		final Optional<Transaction> optional = findNextSuspiciousIndividually();
		if (optional.isPresent()) {
		    final Transaction transaction = optional.get();
		    cache.add(transaction);
		    suspicious.add(transaction);
		} else {
		    inputExhausted = true;
		}

		if (inputExhausted) {
		    final Collection<Transaction> suspiciousBasedOnStats = collector.suspicious();
		    suspiciousBasedOnStats.removeAll(suspicious);
		    cache.addAll(suspiciousBasedOnStats);
		}

		return cacheHasItems();
	    }

	    @Override
	    public Transaction next()
	    {
		if (cache.isEmpty()) {
		    hasNext();
		}

		return cache.pop();
	    }

	    private boolean cacheHasItems()
	    {
		return BooleanUtils.isFalse(cache.isEmpty());
	    }

	    private Optional<Transaction> findNextSuspiciousIndividually()
	    {
		do {
		    final Transaction candidate = super.next();
		    if (candidate == null) {
			return Optional.empty();
		    }
		    collector.collect(candidate);
		    if (suspectIndividually.test(candidate)) {
			return Optional.of(candidate);
		    }
		} while (true);
	    }
	};
    }
}
