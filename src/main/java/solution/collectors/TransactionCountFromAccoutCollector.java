package solution.collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import test.transactions.Transaction;

public class TransactionCountFromAccoutCollector implements StatsCollector {

    private final int maximumAllowed;
    private final Map<Long, Collection<Transaction>> transactionsPerFromAccount = newHashMap();

    public TransactionCountFromAccoutCollector(final int maximumAllowed) {
        this.maximumAllowed = maximumAllowed;
    }

    @Override
    public void clear()
    {
        transactionsPerFromAccount.clear();
    }

    @Override
    public void collect(final Transaction transaction)
    {
        final Long accountFrom = transaction.getAccountFromId();

        Collection<Transaction> transactions = transactionsPerFromAccount.get(accountFrom);

        if (transactions == null) {
            transactions = newArrayList();
        }

        transactions.add(transaction);
        transactionsPerFromAccount.put(accountFrom, transactions);
    }

    @Override
    public List<Transaction> suspicious()
    {
        final List<Transaction> union = newArrayList();

        for (final Long account : transactionsPerFromAccount.keySet())
        {
            final Collection<Transaction> transactions = transactionsPerFromAccount.get(account);
            if (transactions.size() > maximumAllowed)
            {
                union.addAll(transactions);
            }
        }

        return union;
    }
}
