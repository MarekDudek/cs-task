package test.transactions;

import java.math.BigDecimal;
import java.util.Date;

/**
 * This is single transaction. Imagine that This class is part of another
 * system.
 */
public class Transaction {

    private BigDecimal amount;
    private Long userId;
    private Long transactionId;
    private Long accountFromId;
    private Long accountToId;
    private Date transactionDate;

    @Override
    public String toString() {
	return "Transaction [amount=" + amount + ", userId=" + userId + ", transactionId=" + transactionId + ", accountFromId=" + accountFromId
		+ ", accountToId=" + accountToId + ", transactionDate=" + transactionDate + "]";
    }

    public BigDecimal getAmount() {
	return amount;
    }

    public Long getUserId() {
	return userId;
    }

    public Long getTransactionId() {
	return transactionId;
    }

    public Long getAccountFromId() {
	return accountFromId;
    }

    public Long getAccountToId() {
	return accountToId;
    }

    public Date getDate() {
	return new Date(transactionDate.getTime());
    }

    public void setAmount(BigDecimal amount) {
	this.amount = amount;
    }

    public void setUserId(Long userId) {
	this.userId = userId;
    }

    public void setTransactionId(Long transactionId) {
	this.transactionId = transactionId;
    }

    public void setAccountFromId(Long accountFromId) {
	this.accountFromId = accountFromId;
    }

    public void setAccountToId(Long accountToId) {
	this.accountToId = accountToId;
    }

    public void setDate(Date date) {
	this.transactionDate = new Date(date.getTime());
    }
}
