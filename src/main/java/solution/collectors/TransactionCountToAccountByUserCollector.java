package solution.collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Map;

import org.javatuples.Pair;

import test.transactions.Transaction;

public class TransactionCountToAccountByUserCollector implements StatsCollector {

    private final int maximumAllowed;
    private final Map<Pair<Long, Long>, Collection<Transaction>> transactionsPerAccountToAndUser = newHashMap();

    public TransactionCountToAccountByUserCollector(final int maximumAllowed)
    {
        this.maximumAllowed = maximumAllowed;
    }

    @Override
    public void clear()
    {
        transactionsPerAccountToAndUser.clear();
    }

    @Override
    public void collect(final Transaction transaction)
    {
        final Long accountTo = transaction.getAccountToId();
        final Long user = transaction.getUserId();

        final Pair<Long, Long> key = Pair.with(accountTo, user);

        Collection<Transaction> transactions = transactionsPerAccountToAndUser.get(key);
        if (transactions == null) {
            transactions = newArrayList();
        }

        transactions.add(transaction);
        transactionsPerAccountToAndUser.put(key, transactions);
    }

    @Override
    public Collection<Transaction> suspicious()
    {
        final Collection<Transaction> union = newArrayList();

        for (final Pair<Long, Long> key : transactionsPerAccountToAndUser.keySet())
        {
            final Collection<Transaction> transactions = transactionsPerAccountToAndUser.get(key);
            if (transactions.size() > maximumAllowed)
            {
                union.addAll(transactions);
            }
        }

        return union;
    }
}
