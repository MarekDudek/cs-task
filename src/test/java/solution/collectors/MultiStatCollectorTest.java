package solution.collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import test.transactions.Transaction;

@RunWith(MockitoJUnitRunner.class)
public class MultiStatCollectorTest {

    // given
    @Mock
    private StatsCollector collector1;
    @Mock
    private StatsCollector collector2;
    @Mock
    private StatsCollector collector3;

    private Transaction transaction1 = new Transaction();
    private Transaction transaction2 = new Transaction();
    private Transaction transaction3 = new Transaction();
    private Transaction transaction4 = new Transaction();
    private Transaction transaction5 = new Transaction();

    @Before
    public void setup()
    {
        given(collector1.suspicious()).willReturn(newArrayList(transaction1, transaction2, transaction3));
        given(collector2.suspicious()).willReturn(newArrayList(transaction2, transaction3, transaction4));
        given(collector3.suspicious()).willReturn(newArrayList(transaction3, transaction4, transaction5));
    }

    @Test
    public void elements_are_not_duplicated()
    {
        // given
        final MultiStatCollector collector = new MultiStatCollector(collector1, collector2, collector3);

        // when
        final Collection<Transaction> suspicious = collector.suspicious();

        // then
        assertThat(suspicious, hasSize(5));
    }
}
