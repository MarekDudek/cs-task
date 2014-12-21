package test.analyser;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import test.transactions.Transaction;

public class FraudAnalyserTest {

    FraudAnalyser analyser;

    @Before
    public void setup()
    {
	analyser = new FraudAnalyser();
    }

    @Test
    public void test()
    {
	// when
	final Iterator<Transaction> suspictious = analyser.analyse(null, null);

	// then
	assertThat(suspictious, notNullValue());
    }
}
