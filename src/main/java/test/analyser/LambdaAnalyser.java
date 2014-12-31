package test.analyser;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import test.transactions.Transaction;

public class LambdaAnalyser extends FraudAnalyser {

    private final Predicate<Transaction> skipAnalysis;
    private final Predicate<Transaction> suspectIndividually;

    public LambdaAnalyser(
            final Predicate<Transaction> skipAnalysis,
            final Predicate<Transaction> suspectIndividually)
    {
        this.skipAnalysis = checkNotNull(skipAnalysis);
        this.suspectIndividually = checkNotNull(suspectIndividually);
    }

    @Override
    public Iterator<Transaction> analyse(final Iterator<Transaction> transactions, final Date date)
    {
        final List<Transaction> all = newArrayList(transactions);
        final List<Transaction> toAnalyse = all.stream().filter(skipAnalysis.negate()).collect(Collectors.toList());
        final List<Transaction> suspectedIndividually = toAnalyse.stream().filter(suspectIndividually).collect(Collectors.toList());

        return suspectedIndividually.iterator();
    }
}
