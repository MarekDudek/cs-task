package test.analyser;

import java.util.Date;
import java.util.Iterator;

import test.transactions.Transaction;

public abstract class FraudAnalyser {

    public abstract Iterator<Transaction> analyse(Iterator<Transaction> transactions, Date date);
}
