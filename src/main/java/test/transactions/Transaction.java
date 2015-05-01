package test.transactions;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (transactionId != null ? !transactionId.equals(that.transactionId) : that.transactionId != null)
            return false;
        if (accountFromId != null ? !accountFromId.equals(that.accountFromId) : that.accountFromId != null)
            return false;
        if (accountToId != null ? !accountToId.equals(that.accountToId) : that.accountToId != null) return false;
        return !(transactionDate != null ? !transactionDate.equals(that.transactionDate) : that.transactionDate != null);

    }

    @Override
    public int hashCode() {
        int result = amount != null ? amount.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (transactionId != null ? transactionId.hashCode() : 0);
        result = 31 * result + (accountFromId != null ? accountFromId.hashCode() : 0);
        result = 31 * result + (accountToId != null ? accountToId.hashCode() : 0);
        result = 31 * result + (transactionDate != null ? transactionDate.hashCode() : 0);
        return result;
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
