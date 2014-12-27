package test.analyser;

import java.util.Date;
import java.util.Iterator;

import test.transactions.Transaction;

public class IteratingFraudAnalyser extends FraudAnalyser {

    @Override
    public Iterator<Transaction> analyse(final Iterator<Transaction> transactions, final Date date)
    {
	return null;
    }
}
