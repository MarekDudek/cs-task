package solution.transactions;

import java.math.BigDecimal;
import java.util.Date;

public final class TransactionGeneratorConfig {

    public final int seed;

    public final int minId;
    public final int maxId;

    public final int userCount;
    public final int accountCount;

    public final Date medianDate;
    public final int daysMargin;

    public final BigDecimal minAmount;
    public final BigDecimal maxAmount;

    public TransactionGeneratorConfig(
	    final int seed,
	    final int minId,
	    final int maxId,
	    final int userCount,
	    final int accountCount,
	    final Date medianDate,
	    final int daysMargin,
	    final BigDecimal minAmount,
	    final BigDecimal maxAmount)
    {
	this.seed = seed;
	this.minId = minId;
	this.maxId = maxId;
	this.userCount = userCount;
	this.accountCount = accountCount;
	this.medianDate = medianDate;
	this.daysMargin = daysMargin;
	this.minAmount = minAmount;
	this.maxAmount = maxAmount;
    }
}
