package solution.transactions;

import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import test.transactions.Transaction;

public class TransactionGenerator {

    private final Random generator;
    private final List<Long> users;
    private final List<Long> accounts;

    public TransactionGenerator(
	    final long seed,
	    final int numberOfUsers,
	    final int numberOfAccounts)
    {
	generator = new Random(seed);

	users = newArrayListWithCapacity(numberOfUsers);
	for (int i = 0; i < numberOfUsers; i++) {
	    users.add(positiveLong());
	}

	accounts = newArrayListWithCapacity(numberOfAccounts);
	for (int i = 0; i < numberOfAccounts; i++) {
	    accounts.add(positiveLong());
	}
    }

    public Iterator<Transaction> generateIterator(final int numberOfTransactions)
    {
	return new Iterator<Transaction>() {

	    private int currentIndex = 0;

	    @Override
	    public boolean hasNext()
	    {
		return currentIndex < numberOfTransactions;
	    }

	    @Override
	    public Transaction next()
	    {
		currentIndex++;
		return null;
	    }
	};
    }

    public Transaction randomTransaction()
    {
	final Transaction transaction = new Transaction();

	transaction.setTransactionId(positiveLong());
	transaction.setUserId(randomUser(generator));
	transaction.setDate(randomDate(generator));
	transaction.setAccountFromId(randomAccount(generator));
	transaction.setAccountToId(randomAccount(generator));
	transaction.setAmount(bigDecimal(generator));

	return transaction;
    }

    private long positiveLong()
    {
	long integer;
	do {
	    integer = generator.nextLong();
	} while (integer == 0);
	final long positive = Math.abs(integer);
	return positive;
    }

    private Date randomDate(final Random generator)
    {
	return new Date(generator.nextLong());
    }

    private BigDecimal bigDecimal(final Random generator)
    {
	return new BigDecimal(positiveLong());
    }

    private long randomUser(final Random generator)
    {
	final int randomIndex = generator.nextInt(users.size());
	return users.get(randomIndex);
    }

    private long randomAccount(final Random generator)
    {
	final int randomIndex = generator.nextInt(accounts.size());
	return accounts.get(randomIndex);
    }

}
