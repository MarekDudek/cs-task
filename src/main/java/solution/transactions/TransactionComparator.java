package solution.transactions;

import java.util.Comparator;

import test.transactions.Transaction;

import com.google.common.collect.ComparisonChain;

public class TransactionComparator implements Comparator<Transaction> {

    public static final TransactionComparator INSTANCE = new TransactionComparator();

    @Override
    public int compare(final Transaction t1, final Transaction t2)
    {
        return ComparisonChain.start()
                .compare(t1.getTransactionId(), t2.getTransactionId())
                .compare(t1.getUserId(), t2.getUserId())
                .compare(t1.getDate(), t2.getDate())
                .compare(t1.getAccountFromId(), t2.getAccountFromId())
                .compare(t1.getAccountToId(), t2.getAccountToId())
                .compare(t1.getAmount(), t2.getAmount())
                .result();
    }
}
