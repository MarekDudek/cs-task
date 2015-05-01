package solution.transactions;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import solution.utils.RichGenerator;
import test.transactions.Transaction;

public class TransactionGenerator {

    private final Random generator;
    private final RichGenerator richGenerator;
    private final int minId;
    private final int maxId;
    private final List<Long> users;
    private final List<Long> accounts;
    private final Date medianDate;
    private final int daysMargin;
    private final BigDecimal minAmount;
    private final BigDecimal maxAmount;

    public TransactionGenerator(final TransactionGeneratorConfig config)
    {
        generator = new Random(config.seed);
        richGenerator = new RichGenerator();

        minId = config.minId;
        maxId = config.maxId;

        users = newArrayListWithCapacity(config.userCount);
        for (int i = 0; i < config.userCount; i++) {
            users.add(richGenerator.longBetweenInclusive(generator, config.minId, config.maxId));
        }

        accounts = newArrayListWithCapacity(config.accountCount);
        for (int i = 0; i < config.accountCount; i++) {
            accounts.add(richGenerator.longBetweenInclusive(generator, config.minId, config.maxId));
        }

        medianDate = new Date(config.medianDate.getTime());
        daysMargin = config.daysMargin;

        minAmount = config.minAmount;
        maxAmount = config.maxAmount;
    }

    public List<Long> chooseWhitelisted(final int count)
    {
        final List<Long> whitelisted = newArrayList(users.subList(0, count));
        return Collections.unmodifiableList(whitelisted);
    }

    public List<Long> chooseBlacklisted(final int count)
    {
        final int last = users.size();
        final int first = last - count;

        final List<Long> blacklisted = newArrayList(users.subList(first, last));
        return Collections.unmodifiableList(blacklisted);
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

        transaction.setTransactionId(richGenerator.longBetweenInclusive(generator, minId, maxId));
        transaction.setUserId(randomUser(generator));
        transaction.setDate(richGenerator.dateAroundMedianWithMargins(generator, medianDate, daysMargin));
        transaction.setAccountFromId(randomAccount(generator));
        transaction.setAccountToId(randomAccount(generator));
        transaction.setAmount(richGenerator.decimalBetweenInclusive(generator, minAmount, maxAmount));

        return transaction;
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
