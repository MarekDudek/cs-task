package solution.collectors;

import java.util.Collection;
import java.util.Collections;

import test.transactions.Transaction;

public interface StatsCollector {

    default void collect(Transaction transaction) {
    }

    default Collection<Transaction> suspicious() {
        return Collections.emptyList();
    }

    default void clear() {
    }
}
