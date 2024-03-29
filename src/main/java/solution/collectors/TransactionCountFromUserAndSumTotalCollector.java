package solution.collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.javatuples.Pair;

import test.transactions.Transaction;

public class TransactionCountFromUserAndSumTotalCollector implements StatsCollector {

    private final Map<Long, Collection<Transaction>> transactionsPerUser = newHashMap();
    private final Map<Long, BigDecimal> sumPerUser = newHashMap();
    private final List<Pair<Integer, BigDecimal>> thresholds;

    public TransactionCountFromUserAndSumTotalCollector(final List<Pair<Integer, BigDecimal>> thresholds)
    {
        this.thresholds = thresholds;
    }

    @Override
    public void clear()
    {
        transactionsPerUser.clear();
        sumPerUser.clear();
    }

    @Override
    public void collect(final Transaction transaction)
    {
        final Long user = transaction.getUserId();

        Collection<Transaction> transactions = transactionsPerUser.get(user);
        if (transactions == null) {
            transactions = newArrayList();
        }

        transactions.add(transaction);
        transactionsPerUser.put(user, transactions);

        final BigDecimal amount = transaction.getAmount();

        BigDecimal sum = sumPerUser.get(user);
        if (sum == null) {
            sum = BigDecimal.ZERO;
        }

        sum = sum.add(amount);
        sumPerUser.put(user, sum);
    }

    @Override
    public List<Transaction> suspicious()
    {
        final List<Transaction> union = newArrayList();

        for (final Long user : transactionsPerUser.keySet())
        {
            final Collection<Transaction> transactions = transactionsPerUser.get(user);
            final BigDecimal sum = sumPerUser.get(user);

            final Predicate<Pair<Integer, BigDecimal>> countAbove = pair -> transactions.size() > pair.getValue0();
            final Predicate<Pair<Integer, BigDecimal>> sumAbove = pair -> sum.compareTo(pair.getValue1()) > 0;

            final boolean anyThresholdExceeded = thresholds.stream().anyMatch(countAbove.and(sumAbove));
            if (anyThresholdExceeded)
            {
                union.addAll(transactions);
            }
        }

        return union;
    }
}
