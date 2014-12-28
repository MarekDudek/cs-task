package test.analyser;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static solution.PredicateFactory.belongsTo;
import static solution.PredicateFactory.sameDate;
import static test.analyser.TestGeneratorSettings.BLACKLISTED_COUNT;
import static test.analyser.TestGeneratorSettings.CONFIG;
import static test.analyser.TestGeneratorSettings.DUE_DAY;
import static test.analyser.TestGeneratorSettings.NUMBER_OF_TRANSACTIONS;
import static test.analyser.TestGeneratorSettings.USER_COUNT;
import static test.analyser.TestGeneratorSettings.WHITELISTED_COUNT;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
	final long count = newArrayList(transactions)
		.stream()
		.filter(skipAnalysis.negate())
		.filter(suspectIndividually)
		.count();

	// then
	assertThat(count, equalTo(33339L));
    }

    @Test
    public void transaction_count_from_account()
    {
	final TransactionGenerator generator = new TransactionGenerator(CONFIG);

	final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
	final Predicate<Transaction> skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

	final Iterator<Transaction> result = generator.generateIterator(NUMBER_OF_TRANSACTIONS);
	final List<Transaction> transactions = newArrayList(result);

	// when
	final Map<Long, List<Transaction>> transactionsPerUser = transactions
		.stream()
		.filter(skipAnalysis.negate())
		.collect(Collectors.groupingBy(Transaction::getUserId));

	// then
	assertThat(transactionsPerUser.keySet(), hasSize(USER_COUNT - WHITELISTED_COUNT - 1));
    }
}
