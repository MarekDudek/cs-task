package solution;

import static solution.TransactionBuilder.transaction;
import test.transactions.Transaction;

public class TestRequirements {

    public static long WHITELISTED_USER_1 = 101L;
    public static long WHITELISTED_USER_2 = 606L;

    public static Transaction WHITELISTED_USER_1_TRANSACTION = transaction().user(WHITELISTED_USER_1).build();
    public static Transaction WHITELISTED_USER_2_TRANSACTION = transaction().user(WHITELISTED_USER_2).build();

    public static long BLACKLISTED_USER_1 = 542L;
    public static long BLACKLISTED_USER_2 = 1052L;
    public static long BLACKLISTED_USER_3 = 2103L;

    public static Transaction BLACKLISTED_USER_1_TRANSACTION = transaction().user(BLACKLISTED_USER_1).build();
    public static Transaction BLACKLISTED_USER_2_TRANSACTION = transaction().user(BLACKLISTED_USER_2).build();
    public static Transaction BLACKLISTED_USER_3_TRANSACTION = transaction().user(BLACKLISTED_USER_3).build();

    private TestRequirements() {
	super();
    }
}
