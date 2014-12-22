package solution.collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;

import test.transactions.Transaction;

public class MultiStatCollector implements StatsCollector {

    private Collection<StatsCollector> collectors;

    public MultiStatCollector(final StatsCollector... collectors)
    {
	this.collectors = newArrayList(collectors);
    }

    @Override
    public void collect(final Transaction transaction)
    {
	for (final StatsCollector collector : collectors)
	{
	    collector.collect(transaction);
	}
    }

    @Override
    public Collection<Transaction> suspicious()
    {
	final Collection<Transaction> union = newHashSet();

	for (final StatsCollector collector : collectors)
	{
	    union.addAll(collector.suspicious());
	}

	return union;
    }
}
