package test.analyser;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import test.transactions.Transaction;

import com.google.common.annotations.VisibleForTesting;

public class ConcurrentAnalyser extends FraudAnalyser {

    private final Predicate<Transaction> skipAnalysis;
    private final Predicate<Transaction> suspectIndividually;
    private final int maxAllowedFromAccount;
    private final int maxAllowedByUserToAccount;
    private final List<Pair<Integer, BigDecimal>> thresholds;

    private final Executor executor;

    public ConcurrentAnalyser(
            final Predicate<Transaction> skipAnalysis,
            final Predicate<Transaction> suspectIndividually,
            final int maxAllowedFromAccount,
            final int maxAllowedByUserToAccount,
            final List<Pair<Integer, BigDecimal>> thresholds)
    {
        this.skipAnalysis = checkNotNull(skipAnalysis);
        this.suspectIndividually = checkNotNull(suspectIndividually);
        this.maxAllowedFromAccount = maxAllowedFromAccount;
        this.maxAllowedByUserToAccount = maxAllowedByUserToAccount;
        this.thresholds = checkNotNull(thresholds);

        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(availableProcessors,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(final Runnable runnable) {
                        final Thread thread = new Thread(runnable);
                        thread.setDaemon(true);
                        return thread;
                    }
                });
    }

    @Override
    public Iterator<Transaction> analyse(final Iterator<Transaction> transactions, final Date date)
    {
        final List<Transaction> all = newArrayList(transactions);
        final List<Transaction> toAnalyse = skipWhitelistedConcurrently(all);

        final CompletableFuture<List<Transaction>> individually = individuallyPromise(toAnalyse);

        final CompletableFuture<List<Transaction>> countFromAccount = countFromAccountPromise(toAnalyse);
        final CompletableFuture<List<Transaction>> countByUserToAccount = countByUserToAccountPromise(toAnalyse);
        final CompletableFuture<List<Transaction>> countAndTotalAmountByUser = countAndTotalAmountByUserPromise(toAnalyse);

        CompletableFuture.allOf(
                individually,
                countFromAccount,
                countByUserToAccount,
                countAndTotalAmountByUser
                ).join();

        final List<Transaction> suspicious =
                distinctElements(
                        individually.join(),
                        countFromAccount.join(),
                        countByUserToAccount.join(),
                        countAndTotalAmountByUser.join()
                );
        return suspicious.iterator();
    }

    @SafeVarargs
    private static final List<Transaction> distinctElements(final List<Transaction>... lists)
    {
        final List<List<Transaction>> listOfLists = newArrayList(lists);
        final Set<Transaction> distinct = listOfLists.parallelStream()
                .flatMap(List::parallelStream)
                .collect(Collectors.toSet());

        return newArrayList(distinct);
    }

    @VisibleForTesting
    List<Transaction> skipWhitelistedConcurrently(final List<Transaction> transactions)
    {
        final CompletableFuture<List<Transaction>> promise =
                CompletableFuture.supplyAsync(() -> skipWhitelisted(transactions), executor);

        return promise.join();
    }

    @VisibleForTesting
    List<Transaction> skipWhitelisted(final List<Transaction> transactions)
    {
        final List<Transaction> toAnalyse = transactions.parallelStream()
                .filter(skipAnalysis.negate())
                .collect(Collectors.toList());

        return toAnalyse;
    }

    @VisibleForTesting
    CompletableFuture<List<Transaction>> individuallyPromise(final List<Transaction> transactions) {

        return CompletableFuture.supplyAsync(() -> individually(transactions), executor);
    }

    @VisibleForTesting
    List<Transaction> individually(final List<Transaction> transactions)
    {
        final List<Transaction> suspicious = transactions.parallelStream()
                .filter(suspectIndividually)
                .collect(Collectors.toList());

        return suspicious;
    }

    @VisibleForTesting
    CompletableFuture<List<Transaction>> countFromAccountPromise(final List<Transaction> transactions) {

        return CompletableFuture.supplyAsync(() -> countFromAccount(transactions), executor);
    }

    @VisibleForTesting
    List<Transaction> countFromAccount(final List<Transaction> transactions)
    {
        final Map<Long, List<Transaction>> grouppedByFromAccount = transactions.parallelStream()
                .collect(Collectors.groupingByConcurrent(Transaction::getAccountFromId));

        final List<Transaction> suspicious = grouppedByFromAccount.values().parallelStream()
                .filter(list -> list.size() > maxAllowedFromAccount)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());

        return suspicious;
    }

    @VisibleForTesting
    CompletableFuture<List<Transaction>> countByUserToAccountPromise(final List<Transaction> transactions) {

        return CompletableFuture.supplyAsync(() -> countByUserToAccount(transactions), executor);
    }

    @VisibleForTesting
    List<Transaction> countByUserToAccount(final List<Transaction> transactions)
    {
        final Map<Long, ConcurrentMap<Long, List<Transaction>>> grouppedByUserAndToAccount = transactions.parallelStream()
                .collect(Collectors.groupingByConcurrent(Transaction::getUserId, Collectors.groupingByConcurrent(Transaction::getAccountToId)));

        final List<Transaction> suspicious = grouppedByUserAndToAccount.values().parallelStream()
                .flatMap(byToAccountMap -> byToAccountMap.values().parallelStream())
                .filter(list -> list.size() > maxAllowedByUserToAccount)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());

        return suspicious;
    }

    private CompletableFuture<List<Transaction>> countAndTotalAmountByUserPromise(final List<Transaction> transactions) {

        return CompletableFuture.supplyAsync(() -> countAndTotalAmountByUser(transactions), executor);
    }

    private List<Transaction> countAndTotalAmountByUser(final List<Transaction> transactions)
    {
        final Map<Long, List<Transaction>> grouppedByUser = transactions.parallelStream()
                .collect(Collectors.groupingByConcurrent(Transaction::getUserId));

        // @formatter:off
        final List<Transaction> suspicious = grouppedByUser.values()
                .parallelStream()
                .filter(list -> thresholds.stream()
                        .anyMatch(
                                ((Predicate<Pair<Integer, BigDecimal>>) 
                                        countAndTotalAmount -> list.parallelStream()
                                                .count() > countAndTotalAmount.getValue0()
                                ).and((Predicate<Pair<Integer, BigDecimal>>)
                                        countAndTotalAmount -> list.parallelStream()
                                                .map(Transaction::getAmount)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(countAndTotalAmount.getValue1()) > 0
                                )
                         )
                )
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());
        // @formatter:on

        return suspicious;
    }
}