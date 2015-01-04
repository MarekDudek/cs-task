package solution.collectors;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import test.transactions.Transaction;

public class MultiStatCollector implements StatsCollector {

    private List<StatsCollector> collectors;

    public MultiStatCollector(final StatsCollector... collectors)
    {
        this.collectors = newArrayList(collectors);
    }

    @Override
    public void clear()
    {
        collectors.stream()
                .forEach(collector -> collector.clear());
    }

    @Override
    public void collect(final Transaction transaction)
    {
        collectors.stream()
                .forEach(collector -> collector.collect(transaction));
    }

    @Override
    public List<Transaction> suspicious()
    {
        final Set<Transaction> union = collectors.stream()
                .map(collector -> collector.suspicious())
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        return newArrayList(union);
    }
}
