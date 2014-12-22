package solution.collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Map;

import test.transactions.Transaction;

public class TransactionCountFromAccoutCollector implements StatsCollector {

    private final Map<Long, Collection<Transaction>> transactionsPerFromAccount = newHashMap();
    private final int maximumAllowed;

    public TransactionCountFromAccoutCollector(final int maximumAllowed) {
	this.maximumAllowed = maximumAllowed;
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
    public Collection<Transaction> suspicious()
    {
	final Collection<Transaction> union = newArrayList();

	for (final Long account : transactionsPerFromAccount.keySet())
	{
	    final Collection<Transaction> transactions = transactionsPerFromAccount.get(account);
	    if (transactions.size() > maximumAllowed) {
		union.addAll(transactions);
	    }
	}

	return union;
    }
}
