package solution;

import static com.google.common.collect.Lists.newArrayList;
import static solution.TransactionBuilder.transaction;

import java.util.List;

import test.transactions.Transaction;

public class TestRequirements {

    public static final long WHITELISTED_USER_1 = 101L;
    public static final long WHITELISTED_USER_2 = 606L;

    public static final List<Long> WHITELISTED_USERS = newArrayList(WHITELISTED_USER_1, WHITELISTED_USER_2);

    public static final Transaction WHITELISTED_USER_1_TRANSACTION = transaction().user(WHITELISTED_USER_1).build();
    public static final Transaction WHITELISTED_USER_2_TRANSACTION = transaction().user(WHITELISTED_USER_2).build();

    public static final long BLACKLISTED_USER_1 = 542L;
    public static final long BLACKLISTED_USER_2 = 1052L;
    public static final long BLACKLISTED_USER_3 = 2103L;

    public static final List<Long> BLACKLISTED_USERS = newArrayList(BLACKLISTED_USER_1, BLACKLISTED_USER_2, BLACKLISTED_USER_3);

    public static final Transaction BLACKLISTED_USER_1_TRANSACTION = transaction().user(BLACKLISTED_USER_1).build();
    public static final Transaction BLACKLISTED_USER_2_TRANSACTION = transaction().user(BLACKLISTED_USER_2).build();
    public static final Transaction BLACKLISTED_USER_3_TRANSACTION = transaction().user(BLACKLISTED_USER_3).build();

    private TestRequirements() {
	super();
    }
}