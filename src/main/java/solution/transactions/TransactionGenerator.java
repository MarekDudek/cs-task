package solution.transactions;

import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import solution.utils.DateUtilities;
import test.transactions.Transaction;

import com.google.common.annotations.VisibleForTesting;

public class TransactionGenerator {

    private final Random generator;
    private final List<Long> users;
    private final List<Long> accounts;
    private final Date medianDate;
    private final int daysMargin;
    private final BigDecimal maximumAmount;

    public TransactionGenerator(
	    final long seed,
	    final int numberOfUsers,
	    final int numberOfAccounts,
	    final Date medianDate,
	    final int daysMargin,
	    final BigDecimal maximumAmount)
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

	this.medianDate = new Date(medianDate.getTime());
	this.daysMargin = daysMargin;

	this.maximumAmount = maximumAmount;
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
		return randomTransaction();
	    }
	};
    }

    public Transaction randomTransaction()
    {
	final Transaction transaction = new Transaction();

	transaction.setTransactionId(positiveLong());
	transaction.setUserId(randomUser(generator));
	transaction.setDate(randomDate(medianDate, daysMargin));
	transaction.setAccountFromId(randomAccount(generator));
	transaction.setAccountToId(randomAccount(generator));
	transaction.setAmount(randomAmount(maximumAmount));

	return transaction;
    }

    @VisibleForTesting
    Date randomDate(final Date median, final int margin)
    {
	final Date lowerBound = DateUtilities.startOfDayNDaysEarlier(median, margin);
	final long lowerMillis = lowerBound.getTime();

	final Date upperBound = DateUtilities.endOfDayNDaysLater(median, margin);
	final long upperMillis = upperBound.getTime();

	final long difference = upperMillis - lowerMillis;

	final long randomMillis = lowerMillis + positiveLong() % difference;
	final Date randomDate = new Date(randomMillis);

	return randomDate;
    }

    @VisibleForTesting
    BigDecimal randomAmount(final BigDecimal maximum)
    {
	final BigDecimal positive = positiveBigDecimal();
	final BigDecimal nonNegativeBelowMaximum = positive.remainder(maximum);

	return nonNegativeBelowMaximum.add(BigDecimal.ONE);
    }

    private long positiveLong()
    {
	long integer;
	do {
	    integer = generator.nextLong();
	} while (integer == 0 || integer == Integer.MIN_VALUE);
	final long positive = Math.abs(integer);
	return positive;
    }

    private BigDecimal positiveBigDecimal()
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
