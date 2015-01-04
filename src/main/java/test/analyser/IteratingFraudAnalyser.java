package test.analyser;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;

import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
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
        cache.clear();
        suspicious.clear();
        collector.clear();
        inputExhausted = false;

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

                final Transaction transaction = findNextSuspiciousIndividually();
                if (transaction == null) {
                    inputExhausted = true;
                } else {
                    cache.add(transaction);
                    suspicious.add(transaction);
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

            private Transaction findNextSuspiciousIndividually()
            {
                do {
                    final Transaction candidate = super.next();
                    if (candidate == null) {
                        return null;
                    }
                    collector.collect(candidate);
                    if (suspectIndividually.test(candidate)) {
                        return candidate;
                    }
                } while (true);
            }
        };
    }
}
