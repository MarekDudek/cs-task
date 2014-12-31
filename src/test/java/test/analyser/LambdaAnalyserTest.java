package test.analyser;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static solution.PredicateFactory.belongsTo;
import static solution.PredicateFactory.sameDate;
import static test.analyser.TestGeneratorSettings.ALWAYS_FAIL;
import static test.analyser.TestGeneratorSettings.BLACKLISTED_COUNT;
import static test.analyser.TestGeneratorSettings.CONFIG;
import static test.analyser.TestGeneratorSettings.DUE_DAY;
import static test.analyser.TestGeneratorSettings.EXCEEDING_ANY_THRESHOLD_OF_COUNT_AND_TOTAL_AMOUNT;
import static test.analyser.TestGeneratorSettings.EXCEEDING_COUNT_BY_USER_TO_ACCOUNT;
import static test.analyser.TestGeneratorSettings.EXCEEDING_COUNT_FROM_ACCOUNT;
import static test.analyser.TestGeneratorSettings.NUMBER_OF_TRANSACTIONS;
import static test.analyser.TestGeneratorSettings.SUSPICIOUS_INDIVIDUALLY_COUNT;
import static test.analyser.TestGeneratorSettings.VERY_BIG_NUMBER;
import static test.analyser.TestGeneratorSettings.WHITELISTED_COUNT;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.javatuples.Pair;
import org.junit.Test;

import solution.transactions.TransactionGenerator;
import test.transactions.Transaction;

public class LambdaAnalyserTest {

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

        final FraudAnalyser analyser =
                new LambdaAnalyser(skipAnalysis, suspectIndividually, VERY_BIG_NUMBER, VERY_BIG_NUMBER, Collections.emptyList());

        // when
        final Iterator<Transaction> suspicious = analyser.analyse(transactions, DUE_DAY);

        // then
        assertThat(newArrayList(suspicious), hasSize(SUSPICIOUS_INDIVIDUALLY_COUNT));
    }

    @Test
    public void transactions_from_accounts_that_have_more_that_allowed_transactions()
    {
        // given
        final TransactionGenerator generator = new TransactionGenerator(CONFIG);

        final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
        final Predicate<Transaction> skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final Iterator<Transaction> transactions = generator.generateIterator(NUMBER_OF_TRANSACTIONS);

        // when
        // TODO: put this as global
        final int maxAllowedFromAccount = 5000;

        final FraudAnalyser analyser = new LambdaAnalyser(skipAnalysis, ALWAYS_FAIL, maxAllowedFromAccount, VERY_BIG_NUMBER,
                Collections.emptyList());
        final Iterator<Transaction> suspicious = analyser.analyse(transactions, DUE_DAY);

        // then
        assertThat(newArrayList(suspicious), hasSize(EXCEEDING_COUNT_FROM_ACCOUNT));
    }

    @Test
    public void transactions_to_account_by_user()
    {
        // given
        final TransactionGenerator generator = new TransactionGenerator(CONFIG);

        final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
        final Predicate<Transaction> skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final Iterator<Transaction> result = generator.generateIterator(NUMBER_OF_TRANSACTIONS);

        // when
        // TODO: put this as global
        final int maxAllowedByUserToAccount = 80;

        final FraudAnalyser analyser = new LambdaAnalyser(skipAnalysis, ALWAYS_FAIL, VERY_BIG_NUMBER, maxAllowedByUserToAccount,
                Collections.emptyList());
        final Iterator<Transaction> suspicious = analyser.analyse(result, DUE_DAY);

        // then
        assertThat(newArrayList(suspicious), hasSize(EXCEEDING_COUNT_BY_USER_TO_ACCOUNT));
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

        // TODO: put this as global
        @SuppressWarnings("unchecked")
        final List<Pair<Integer, BigDecimal>> thresholds = newArrayList
                (
                        Pair.with(1_000_000, new BigDecimal(10_000_000)),
                        Pair.with(3450, new BigDecimal(1_000_000))
                );

        // @formatter:off
        final List<Transaction> suspicious = transactionsPerUser.values()
                .stream()
                .filter(list -> thresholds.stream()
                        .anyMatch(
                                ((Predicate<Pair<Integer, BigDecimal>>) 
                                        countAndSum -> list.stream()
                                            .count() > countAndSum.getValue0()
                                ).and((Predicate<Pair<Integer, BigDecimal>>)
                                        countAndSum -> list.stream()
                                            .map(Transaction::getAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(countAndSum.getValue1()) > 0
                                )
                         )
                )
                .flatMap(List::stream)
                .collect(Collectors.toList());
        // @formatter:on

        // then
        assertThat(suspicious, hasSize(EXCEEDING_ANY_THRESHOLD_OF_COUNT_AND_TOTAL_AMOUNT));
    }
}
