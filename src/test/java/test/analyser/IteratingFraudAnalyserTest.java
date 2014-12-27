package test.analyser;

import org.junit.Before;
import org.junit.Test;

public class IteratingFraudAnalyserTest {

    private FraudAnalyser analyser;

    @Before
    public void setup()
    {
	analyser = new IteratingFraudAnalyser();
    }

    @Test
    public void test()
    {
	analyser.analyse(null, null);
    }
}
