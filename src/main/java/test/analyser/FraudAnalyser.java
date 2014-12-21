package test.analyser;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import test.transactions.Transaction;

/**
 * Main class. Analyse all transactions from given date and return suspicious
 * transactions. Do not return transactions that are not suspicious.
 */
public class FraudAnalyser {

    /**
     * @param list
     *            off transactions to be checked
     * @param date
     *            - analyse only transactions from the given day
     * @return list off suspicious transactions
     */
    public Iterator<Transaction> analyse(Iterator<Transaction> transactions, Date date)
    {
	return Collections.<Transaction> emptyList().iterator();
    }
}