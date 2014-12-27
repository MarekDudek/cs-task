package solution.transactions;

import java.util.Iterator;
import java.util.List;

import test.transactions.Transaction;

public class TransactionPrinter {

    public static void print(Iterator<Transaction> suspicious)
    {
	while (suspicious.hasNext()) {
	    System.out.println(suspicious.next());
	}
    }

    public static void print(List<Transaction> list) {
	print(list.iterator());
    }
}
