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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import test.transactions.Transaction;

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
        final List<Transaction> toAnalyse = toAnalyse(all);

        final CompletableFuture<List<Transaction>> futureIndivudually = futureSuspiciousIndividually(toAnalyse);
        final CompletableFuture<List<Transaction>> futureCountFromAccount = futureSuspiciousBasedOnCountFromAccount(toAnalyse);
        final CompletableFuture<List<Transaction>> futureCountByUserToAccount = futureSuspiciousBasedOnCountByUserToAccount(toAnalyse);
        final CompletableFuture<List<Transaction>> futureCountAndTotalAmountByUser = futureSuspiciousBasedOnCountAndTotalAmountByUser(toAnalyse);

        final CompletableFuture<Void> allLists =
                CompletableFuture.allOf(futureIndivudually, futureCountFromAccount, futureCountByUserToAccount, futureCountAndTotalAmountByUser);

        try {
            allLists.get();
            final List<Transaction> suspicious =
                    distinctElements(
                            futureIndivudually.get(),
                            futureCountFromAccount.get(),
                            futureCountByUserToAccount.get(),
                            futureCountAndTotalAmountByUser.get()
                    );
            return suspicious.iterator();
        } catch (final InterruptedException | ExecutionException ex) {
            throw new IllegalArgumentException(ex);
        }
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

    private List<Transaction> toAnalyse(final List<Transaction> transactions)
    {
        final List<Transaction> toAnalyse = transactions.parallelStream()
                .filter(skipAnalysis.negate())
                .collect(Collectors.toList());

        return toAnalyse;
    }

    private CompletableFuture<List<Transaction>> futureSuspiciousIndividually(final List<Transaction> transactions) {

        return CompletableFuture.supplyAsync(() -> suspiciousIndividually(transactions), executor);
    }

    private List<Transaction> suspiciousIndividually(final List<Transaction> transactions)
    {
        final List<Transaction> suspicious = transactions.parallelStream()
                .filter(suspectIndividually)
                .collect(Collectors.toList());

        return suspicious;
    }

    private CompletableFuture<List<Transaction>> futureSuspiciousBasedOnCountFromAccount(final List<Transaction> transactions) {

        return CompletableFuture.supplyAsync(() -> suspiciousBasedOnCountFromAccount(transactions), executor);
    }

    private List<Transaction> suspiciousBasedOnCountFromAccount(final List<Transaction> transactions)
    {
        final Map<Long, List<Transaction>> grouppedByFromAccount = transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::getAccountFromId));

        final List<Transaction> suspicious = grouppedByFromAccount.values().parallelStream()
                .filter(list -> list.size() > maxAllowedFromAccount)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());

        return suspicious;
    }

    private CompletableFuture<List<Transaction>> futureSuspiciousBasedOnCountByUserToAccount(final List<Transaction> transactions) {

        return CompletableFuture.supplyAsync(() -> suspiciousBasedOnCountByUserToAccount(transactions), executor);
    }

    private List<Transaction> suspiciousBasedOnCountByUserToAccount(final List<Transaction> transactions)
    {
        final Map<Long, Map<Long, List<Transaction>>> grouppedByUserAndToAccount = transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::getUserId, Collectors.groupingBy(Transaction::getAccountToId)));

        final List<Transaction> suspicious = grouppedByUserAndToAccount.values().parallelStream()
                .flatMap(byToAccountMap -> byToAccountMap.values().parallelStream())
                .filter(list -> list.size() > maxAllowedByUserToAccount)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());

        return suspicious;
    }

    private CompletableFuture<List<Transaction>> futureSuspiciousBasedOnCountAndTotalAmountByUser(final List<Transaction> transactions) {

        return CompletableFuture.supplyAsync(() -> suspiciousBasedOnCountAndTotalAmountByUser(transactions), executor);
    }

    private List<Transaction> suspiciousBasedOnCountAndTotalAmountByUser(final List<Transaction> transactions)
    {
        final Map<Long, List<Transaction>> grouppedByUser = transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::getUserId));

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