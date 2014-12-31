package test.analyser;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import test.transactions.Transaction;

public class LambdaAnalyser extends FraudAnalyser {

    private final Predicate<Transaction> skipAnalysis;
    private final Predicate<Transaction> suspectIndividually;
    private final int maxAllowedFromAccount;

    public LambdaAnalyser(
            final Predicate<Transaction> skipAnalysis,
            final Predicate<Transaction> suspectIndividually,
            final int maxAllowedFromAccount)
    {
        this.skipAnalysis = checkNotNull(skipAnalysis);
        this.suspectIndividually = checkNotNull(suspectIndividually);
        this.maxAllowedFromAccount = maxAllowedFromAccount;
    }

    @Override
    public Iterator<Transaction> analyse(final Iterator<Transaction> transactions, final Date date)
    {
        final List<Transaction> all = newArrayList(transactions);
        final List<Transaction> toAnalyse = toAnalyse(all);

        final List<Transaction> suspiciousIndividually = suspiciousIndividually(toAnalyse);
        final List<Transaction> suspiciousBasedOnCountFromAccount = suspiciousBasedOnCountFromAccount(toAnalyse);

        final List<Transaction> union = distinctElements(suspiciousIndividually, suspiciousBasedOnCountFromAccount);
        return union.iterator();
    }

    @SafeVarargs
    private static final List<Transaction> distinctElements(final List<Transaction>... lists)
    {
        final Set<Transaction> union = newHashSet();
        for (final List<Transaction> list : lists) {
            union.addAll(list);
        }
        return newArrayList(union);
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
        final Map<Long, List<Transaction>> grouppedByFromAccount = transactions
                .stream()
                .collect(Collectors.groupingBy(Transaction::getAccountFromId));

        final List<Transaction> suspicious = grouppedByFromAccount.values()
                .stream()
                .filter(list -> list.size() > maxAllowedFromAccount)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return suspicious;
    }
}
