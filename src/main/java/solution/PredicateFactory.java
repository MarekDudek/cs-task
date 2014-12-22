package solution;

import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.time.DateUtils;

import test.transactions.Transaction;

public class PredicateFactory {

    public static Predicate<Transaction> whitelisted(final List<Long> whitelistedUsers)
    {
	return transaction -> whitelistedUsers.contains(transaction.getUserId());
    }

    public static Predicate<Transaction> sameDate(final Date date)
    {
	return transaction -> DateUtils.isSameDay(transaction.getDate(), date);
    }

    public static Predicate<Transaction> blacklisted(final List<Long> blacklistedUsers)
    {
	return transaction -> blacklistedUsers.contains(transaction.getUserId());
    }
}
