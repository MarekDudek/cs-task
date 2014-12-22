package solution.transactions;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import test.transactions.Transaction;

public class TransactionGenerator {

    final Random generator;

    public TransactionGenerator(final long seed)
    {
	generator = new Random(seed);
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

	transaction.setTransactionId(generator.nextLong());
	transaction.setUserId(generator.nextLong());
	transaction.setDate(randomDate(generator));
	transaction.setAccountFromId(generator.nextLong());
	transaction.setAccountToId(generator.nextLong());
	transaction.setAmount(bigDecimal(generator));

	return transaction;
    }

    private Date randomDate(final Random generator)
    {
	return new Date(generator.nextLong());
    }

    private BigDecimal bigDecimal(final Random generator)
    {
	return new BigDecimal(generator.nextLong());
    }
}
