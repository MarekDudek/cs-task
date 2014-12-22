package solution;

import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.time.DateUtils;

import test.transactions.Transaction;

public class PredicateFactory {

    public static Predicate<Transaction> belongsTo(final List<Long> memebers)
    {
	return transaction -> memebers.contains(transaction.getUserId());
    }

    public static Predicate<Transaction> sameDate(final Date date)
    {
	return transaction -> DateUtils.isSameDay(transaction.getDate(), date);
    }
}
