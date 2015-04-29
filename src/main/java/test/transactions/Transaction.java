package test.transactions;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.google.common.base.MoreObjects;

/**
 * This is single transaction. Imagine that This class is part of another
 * system.
 */
@SuppressWarnings("serial")
public class Transaction implements Serializable {

    private BigDecimal amount;
    private Long userId;
    private Long transactionId;
    private Long accountFromId;
    private Long accountToId;
    private Date transactionDate;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ID", transactionId)
                .add("user", userId)
                .add("date", transactionDate)
                .add("from account", accountFromId)
                .add("to account", accountToId)
                .add("amount", amount)
                .toString();
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
