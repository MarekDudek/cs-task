package solution.collectors;

import java.util.Collections;
import java.util.List;

import test.transactions.Transaction;

public interface StatsCollector {

    default void collect(Transaction transaction) {
    }

    default List<Transaction> suspicious() {
	return Collections.emptyList();
    };
}
