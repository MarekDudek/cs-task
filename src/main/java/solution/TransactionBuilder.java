package solution;

import java.math.BigDecimal;
import java.util.Date;

import test.transactions.Transaction;

public class TransactionBuilder {

    public static TransactionBuilder transaction() {
	return new TransactionBuilder();
    }

    private TransactionBuilder() {
	super();
    }

    public Transaction build()
    {
	final Transaction transaction = new Transaction();

	transaction.setTransactionId(transactionId);
	transaction.setUserId(userId);
	transaction.setDate(date);
	transaction.setAccountToId(accountToId);
	transaction.setAccountFromId(accountFromId);
	transaction.setAmount(amount);

	return transaction;
    }

    private Long transactionId;
    private Long userId;
    private Date date;
    private Long accountToId;
    private Long accountFromId;
    private BigDecimal amount;

    public TransactionBuilder id(final long id) {
	transactionId = Long.valueOf(id);
	return this;
    }

    public TransactionBuilder user(final long user) {
	userId = Long.valueOf(user);
	return this;
    }

    public TransactionBuilder date(final Date date) {
	this.date = date;
	return this;
    }

    public TransactionBuilder toAccount(final long toAccount) {
	accountToId = Long.valueOf(toAccount);
	return this;
    }

    public TransactionBuilder fromAccount(final long fromAccount) {
	accountFromId = fromAccount;
	return this;
    }

    public TransactionBuilder amount(final BigDecimal amount) {
	this.amount = amount;
	return this;
    }
}
