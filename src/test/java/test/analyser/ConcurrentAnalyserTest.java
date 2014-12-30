package test.analyser;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static solution.PredicateFactory.belongsTo;
import static solution.PredicateFactory.sameDate;
import static test.analyser.TestGeneratorSettings.ACCOUNT_COUNT;
import static test.analyser.TestGeneratorSettings.BLACKLISTED_COUNT;
import static test.analyser.TestGeneratorSettings.CONFIG;
import static test.analyser.TestGeneratorSettings.DUE_DAY;
import static test.analyser.TestGeneratorSettings.MAX_ALLOWED_FROM_ACCOUNT;
import static test.analyser.TestGeneratorSettings.MAX_ALLOWED_TO_ACCOUNT_BY_USER;
import static test.analyser.TestGeneratorSettings.NUMBER_OF_TRANSACTIONS;
import static test.analyser.TestGeneratorSettings.THRESHOLDS;
import static test.analyser.TestGeneratorSettings.WHITELISTED_COUNT;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.javatuples.Pair;
import org.junit.Test;

import solution.transactions.TransactionGenerator;
import test.transactions.Transaction;

public class ConcurrentAnalyserTest {

    @Test
    public void individual_suspicious()
    {
        // given
        final TransactionGenerator generator = new TransactionGenerator(CONFIG);

        final List<Long> blacklisted = generator.chooseBlacklisted(BLACKLISTED_COUNT);
        final Predicate<Transaction> suspectIndividually = belongsTo(blacklisted);

        final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
        final Predicate<Transaction> skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final Iterator<Transaction> transactions = generator.generateIterator(NUMBER_OF_TRANSACTIONS);

        // when
        final List<Transaction> suspicious = newArrayList(transactions)
                .stream()
                .filter(skipAnalysis.negate())
                .filter(suspectIndividually)
                .collect(Collectors.toList());

        // then
        assertThat(suspicious, hasSize(33339));
    }

    @Test
    public void transactions_from_account()
    {
        // given
        final TransactionGenerator generator = new TransactionGenerator(CONFIG);

        final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
        final Predicate<Transaction> skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final Iterator<Transaction> result = generator.generateIterator(NUMBER_OF_TRANSACTIONS);
        final List<Transaction> transactions = newArrayList(result);

        // when
        final Map<Long, List<Transaction>> transactionsPerFromAccount = transactions
                .stream()
                .filter(skipAnalysis.negate())
                .collect(Collectors.groupingBy(Transaction::getAccountFromId));

        // then
        assertThat(transactionsPerFromAccount.keySet(), hasSize(ACCOUNT_COUNT - 1));
    }

    @Test
    public void transactions_from_accounts_that_have_more_that_allowed_transactions()
    {
        // given
        final TransactionGenerator generator = new TransactionGenerator(CONFIG);

        final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
        final Predicate<Transaction> skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final Iterator<Transaction> result = generator.generateIterator(NUMBER_OF_TRANSACTIONS);
        final List<Transaction> transactions = newArrayList(result);

        // when
        final Map<Long, List<Transaction>> transactionsPerFromAccount = transactions
                .stream()
                .filter(skipAnalysis.negate())
                .collect(
                        Collectors.groupingBy(
                                Transaction::getAccountFromId
                                )
                );

        final List<Transaction> suspicious = transactionsPerFromAccount.values()
                .stream()
                .filter(list -> list.size() > MAX_ALLOWED_FROM_ACCOUNT)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // then
        assertThat(suspicious, hasSize(6029));
    }

    @Test
    public void transactions_to_account_by_user()
    {
        // given
        final TransactionGenerator generator = new TransactionGenerator(CONFIG);

        final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
        final Predicate<Transaction> skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final Iterator<Transaction> result = generator.generateIterator(NUMBER_OF_TRANSACTIONS);
        final List<Transaction> transactions = newArrayList(result);

        // when
        final Map<Long, Map<Long, List<Transaction>>> transactionsPerToAccountPerUser = transactions.stream()
                .filter(skipAnalysis.negate())
                .collect(
                        Collectors.groupingBy(
                                Transaction::getAccountToId,
                                Collectors.groupingBy(Transaction::getUserId)
                                )
                );

        // then
        assertThat(transactionsPerToAccountPerUser.keySet(), hasSize(ACCOUNT_COUNT - 1));

        // when
        final List<Transaction> suspicious = transactionsPerToAccountPerUser.values()
                .stream()
                .flatMap(userMap -> userMap.values().stream())
                .filter(list -> list.size() > MAX_ALLOWED_TO_ACCOUNT_BY_USER)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // then
        assertThat(suspicious, hasSize(145));
    }

    @Test
    public void transactions_from_user_that_count_and_total_above_thresholds()
    {

        // given
        final TransactionGenerator generator = new TransactionGenerator(CONFIG);

        final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
        final Predicate<Transaction> skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final Iterator<Transaction> result = generator.generateIterator(NUMBER_OF_TRANSACTIONS);
        final List<Transaction> transactions = newArrayList(result);

        // when
        final Map<Long, List<Transaction>> transactionsPerUser = transactions.stream()
                .filter(skipAnalysis.negate())
                .collect(Collectors.groupingBy(Transaction::getUserId));

        final Predicate<List<Transaction>> sumTotalAndCountBothExceedAnyOfThresholds = new Predicate<List<Transaction>>() {
            @Override
            public boolean test(final List<Transaction> transactions)
            {
                final long count = transactions.stream()
                        .count();
                final BigDecimal sumTotal = transactions.stream()
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal::add)
                        .get();

                final Predicate<Pair<Integer, BigDecimal>> countAbove =
                        pair -> count > pair.getValue0();
                final Predicate<Pair<Integer, BigDecimal>> sumAbove =
                        pair -> sumTotal.compareTo(pair.getValue1()) > 0;

                return THRESHOLDS.stream().anyMatch(countAbove.and(sumAbove));
            }
        };

        final List<Transaction> suspicious = transactionsPerUser.values()
                .stream()
                .filter(sumTotalAndCountBothExceedAnyOfThresholds)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // then
        assertThat(suspicious, hasSize(6700));
    }
}
