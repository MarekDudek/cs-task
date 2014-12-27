package test.analyser;

import static com.google.common.collect.Lists.newArrayList;
import static requirements.TestRequirements.BLACKLISTED_USERS;
import static requirements.TestRequirements.BLACKLISTED_USER_1;
import static requirements.TestRequirements.REGULAR_USER_1;
import static requirements.TestRequirements.WHITELISTED_USERS;
import static requirements.TestRequirements.WHITELISTED_USER_1;
import static solution.PredicateFactory.belongsTo;
import static solution.PredicateFactory.sameDate;
import static solution.TransactionBuilder.transaction;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import test.transactions.Transaction;

public final class TestData {

    public static final Date DUE_DAY =
	    new Calendar.Builder()
		    .setDate(2014, Calendar.DECEMBER, 21)
		    .setTimeOfDay(23, 59, 59)
		    .build().getTime();

    public static final Date OTHER_DAY =
	    new Calendar.Builder()
		    .setDate(1997, Calendar.JULY, 1)
		    .setTimeOfDay(1, 1, 1)
		    .build().getTime();

    public static final Transaction WHITELISTED_USER_ON_DUE_DAY = transaction().date(DUE_DAY).user(WHITELISTED_USER_1).build();
    public static final Transaction BLACKLISTED_USER_ON_DUE_DAY = transaction().date(DUE_DAY).user(BLACKLISTED_USER_1).build();
    public static final Transaction BLACKLISTED_USER_ON_OTHER_DAY = transaction().date(OTHER_DAY).user(BLACKLISTED_USER_1).build();
    public static final Transaction REGULAR_USER_ON_DUE_DAY = transaction().date(DUE_DAY).user(REGULAR_USER_1).build();

    public static final List<Transaction> VARIOUS_TRANSACTIONS = newArrayList
	    (
		    WHITELISTED_USER_ON_DUE_DAY,
		    BLACKLISTED_USER_ON_OTHER_DAY,
		    BLACKLISTED_USER_ON_DUE_DAY,
		    REGULAR_USER_ON_DUE_DAY
	    );

    public static final Predicate<Transaction> SKIP_ANALYSIS = belongsTo(WHITELISTED_USERS).or(sameDate(DUE_DAY).negate());
    public static final Predicate<Transaction> SUSPECT_INDIVIDUALLY = belongsTo(BLACKLISTED_USERS);

    private TestData() {
	super();
    }
}
