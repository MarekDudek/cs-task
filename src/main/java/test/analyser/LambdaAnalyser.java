package test.analyser;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import test.transactions.Transaction;

public class LambdaAnalyser extends FraudAnalyser {

    private final Predicate<Transaction> skipAnalysis;
    private final Predicate<Transaction> suspectIndividually;
    private final int maxAllowedFromAccount;
    private final int maxAllowedByUserToAccount;
    private final List<Pair<Integer, BigDecimal>> thresholds;

    public LambdaAnalyser(
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
    }

    @Override
    public Iterator<Transaction> analyse(final Iterator<Transaction> transactions, final Date date)
    {
        final List<Transaction> all = newArrayList(transactions);
        final List<Transaction> toAnalyse = toAnalyse(all);

        final List<Transaction> individually = suspiciousIndividually(toAnalyse);
        final List<Transaction> countFromAccount = suspiciousBasedOnCountFromAccount(toAnalyse);
        final List<Transaction> countByUserToAccount = suspiciousBasedOnCountByUserToAccount(toAnalyse);
        final List<Transaction> countAndTotalAmountByUser = suspiciousBasedOnCountAndTotalAmountByUser(toAnalyse);

        final List<Transaction> suspicious = distinctElements(individually, countFromAccount, countByUserToAccount, countAndTotalAmountByUser);
        return suspicious.iterator();
    }

    @SafeVarargs
    private static final List<Transaction> distinctElements(final List<Transaction>... lists)
    {
        final List<List<Transaction>> listOfLists = newArrayList(lists);
        final Set<Transaction> distinct = listOfLists.stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        return newArrayList(distinct);
    }

    private List<Transaction> toAnalyse(final List<Transaction> transactions)
    {
        final List<Transaction> toAnalyse = transactions.stream()
                .filter(skipAnalysis.negate())
                .collect(Collectors.toList());

        return toAnalyse;
    }

    private List<Transaction> suspiciousIndividually(final List<Transaction> transactions)
    {
        final List<Transaction> suspicious = transactions.stream()
                .filter(suspectIndividually)
                .collect(Collectors.toList());

        return suspicious;
    }

    private List<Transaction> suspiciousBasedOnCountFromAccount(final List<Transaction> transactions)
    {
        final Map<Long, List<Transaction>> groupedByFromAccount = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getAccountFromId));

        final List<Transaction> suspicious = groupedByFromAccount.values().stream()
                .filter(list -> list.size() > maxAllowedFromAccount)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return suspicious;
    }

    private List<Transaction> suspiciousBasedOnCountByUserToAccount(final List<Transaction> transactions)
    {
        final Map<Long, Map<Long, List<Transaction>>> grouppedByUserAndToAccount = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getUserId, Collectors.groupingBy(Transaction::getAccountToId)));

        final List<Transaction> suspicious = grouppedByUserAndToAccount.values().stream()
                .flatMap(byToAccountMap -> byToAccountMap.values().stream())
                .filter(list -> list.size() > maxAllowedByUserToAccount)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return suspicious;
    }

    private List<Transaction> suspiciousBasedOnCountAndTotalAmountByUser(final List<Transaction> transactions)
    {
        final Map<Long, List<Transaction>> groupedByUser = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getUserId));

        // @formatter:off
        final List<Transaction> suspicious = groupedByUser.values()
                .stream()
                .filter(list -> thresholds.stream()
                        .anyMatch(
                                ((Predicate<Pair<Integer, BigDecimal>>) 
                                        countAndTotalAmount -> list.stream()
                                                .count() > countAndTotalAmount.getValue0()
                                ).and((Predicate<Pair<Integer, BigDecimal>>)
                                        countAndTotalAmount -> list.stream()
                                                .map(Transaction::getAmount)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(countAndTotalAmount.getValue1()) > 0
                                )
                         )
                )
                .flatMap(List::stream)
                .collect(Collectors.toList());
        // @formatter:on

        return suspicious;
    }
}
