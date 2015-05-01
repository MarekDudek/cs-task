package test.integration;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import solution.collectors.*;
import solution.transactions.TransactionGenerator;
import test.analyser.*;
import test.transactions.Transaction;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.intersection;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static solution.PredicateFactory.belongsTo;
import static solution.PredicateFactory.sameDate;
import static test.analyser.TestGeneratorSettings.*;

public class ComparisonTest {

    @Test
    public void simple_fraud_analyser() {
        // given
        final TransactionGenerator generator = new TransactionGenerator(CONFIG);

        final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
        final Predicate<Transaction> skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final List<Long> blacklisted = generator.chooseBlacklisted(BLACKLISTED_COUNT);
        final Predicate<Transaction> suspectIndividually = belongsTo(blacklisted);

        final StatsCollector collector = new MultiStatCollector
                (
                        new TransactionCountFromAccoutCollector(MAX_ALLOWED_FROM_ACCOUNT),
                        new TransactionCountToAccountByUserCollector(MAX_ALLOWED_BY_USER_TO_ACCOUNT),
                        new TransactionCountFromUserAndSumTotalCollector(THRESHOLDS)
                );

        final FraudAnalyser analyser = new SimpleFraudAnalyser(skipAnalysis, suspectIndividually, collector);

        // when
        final Iterator<Transaction> transactions = generator.generateIterator(NUMBER_OF_TRANSACTIONS);
        final Iterator<Transaction> suspicious = analyser.analyse(transactions, DUE_DAY);

        // then
        assertThat(whitelisted, hasSize(WHITELISTED_COUNT));
        assertThat(blacklisted, hasSize(BLACKLISTED_COUNT));

        final Set<Long> common = intersection(newHashSet(whitelisted), newHashSet(blacklisted));
        assertThat(common, is(empty()));

        assertThat(newArrayList(suspicious), hasSize(EXPECTED_NUMBER_OF_ALL_SUSPICIOUS));
    }

    @Test
    public void lambda_fraud_analyser() {
        // given
        final TransactionGenerator generator = new TransactionGenerator(CONFIG);

        final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
        final Predicate<Transaction> skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final List<Long> blacklisted = generator.chooseBlacklisted(BLACKLISTED_COUNT);
        final Predicate<Transaction> suspectIndividually = belongsTo(blacklisted);

        final FraudAnalyser analyser =
                new LambdaAnalyser(skipAnalysis, suspectIndividually, MAX_ALLOWED_FROM_ACCOUNT, MAX_ALLOWED_BY_USER_TO_ACCOUNT, THRESHOLDS);

        // when
        final Iterator<Transaction> transactions = generator.generateIterator(NUMBER_OF_TRANSACTIONS);
        final Iterator<Transaction> suspicious = analyser.analyse(transactions, DUE_DAY);

        // then
        assertThat(newArrayList(suspicious), hasSize(EXPECTED_NUMBER_OF_ALL_SUSPICIOUS));
    }

    @Test
    public void concurrent_fraud_analyser() {
        // given
        final TransactionGenerator generator = new TransactionGenerator(CONFIG);

        final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
        final Predicate<Transaction> skipAnalysis = belongsTo(whitelisted).or(sameDate(DUE_DAY).negate());

        final List<Long> blacklisted = generator.chooseBlacklisted(BLACKLISTED_COUNT);
        final Predicate<Transaction> suspectIndividually = belongsTo(blacklisted);

        final FraudAnalyser analyser =
                new ConcurrentAnalyser(skipAnalysis, suspectIndividually, MAX_ALLOWED_FROM_ACCOUNT, MAX_ALLOWED_BY_USER_TO_ACCOUNT, THRESHOLDS);

        // when
        final Iterator<Transaction> transactions = generator.generateIterator(NUMBER_OF_TRANSACTIONS);
        final Iterator<Transaction> suspicious = analyser.analyse(transactions, DUE_DAY);

        // then
        assertThat(newArrayList(suspicious), hasSize(EXPECTED_NUMBER_OF_ALL_SUSPICIOUS));
    }

    private static JavaSparkContext CONTEXT;

    @BeforeClass
    public static void setUpClass() throws IOException {

        final SparkConf config = new SparkConf();
        config.setMaster("local");
        config.setAppName("Spark Analyser job");

        CONTEXT = new JavaSparkContext(config);
    }

    @AfterClass
    public static void tearDownClass() {

        CONTEXT.stop();
    }

    @Test
    public void spark_fraud_analyser() {
        // given
        final TransactionGenerator generator = new TransactionGenerator(CONFIG);

        final List<Long> whitelisted = generator.chooseWhitelisted(WHITELISTED_COUNT);
        final Function<Transaction, Boolean> allowAnalysis = new SparkAnalyser.AllowAnalysisPredicate(whitelisted, DUE_DAY);

        final List<Long> blacklisted = generator.chooseBlacklisted(BLACKLISTED_COUNT);
        final Function<Transaction, Boolean> suspectIndividually = new SparkAnalyser.SuspectIndividually(blacklisted);

        final FraudAnalyser analyser =
                new SparkAnalyser(CONTEXT, allowAnalysis, suspectIndividually, MAX_ALLOWED_FROM_ACCOUNT, MAX_ALLOWED_BY_USER_TO_ACCOUNT, THRESHOLDS);

        // when
        final Iterator<Transaction> transactions = generator.generateIterator(NUMBER_OF_TRANSACTIONS);
        final Iterator<Transaction> suspicious = analyser.analyse(transactions, DUE_DAY);

        // then
        assertThat(newArrayList(suspicious), hasSize(EXPECTED_NUMBER_OF_ALL_SUSPICIOUS));
    }
}
