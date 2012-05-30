package org.jasig.portal.events.aggr;

import static junit.framework.Assert.assertEquals;

import java.util.Random;

import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.SecondMoment;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.test.BaseAggrEventsJpaDaoTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaAggrEventsTestContext.xml")
public class JpaStatisticalSummaryTest extends BaseAggrEventsJpaDaoTest {
    
    @Test
    public void testJpaStatisticalSummary() {
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final JpaStatisticalSummary jpaStatisticalSummary = new JpaStatisticalSummary();
                
                final Random r = new Random(0);
                for (int i = 0; i < 10; i++) {
                    final int nextInt = r.nextInt(100000000);
                    jpaStatisticalSummary.addValue(nextInt);
                }
                
                getEntityManager().persist(jpaStatisticalSummary);
            }
        });
    }

    
    @Ignore
    @Test
    public void testSummaryStatisticsJson() throws Exception {
        final SecondMoment secondMoment = new SecondMoment();
        final Sum sum = new Sum();
        final SumOfSquares sumsq = new SumOfSquares();
        final Min min = new Min();
        final Max max = new Max();
        final SumOfLogs sumLog = new SumOfLogs();
        
        final Random r = new Random(0);
        for (int i = 0; i < 10; i++) {
            final int nextInt = r.nextInt(100000000);
            secondMoment.increment(nextInt);
            sum.increment(nextInt);
            sumsq.increment(nextInt);
            min.increment(nextInt);
            max.increment(nextInt);
            sumLog.increment(nextInt);
        }

        testStorelessUnivariateStatistic(secondMoment, 7.513432791665536E15);
        testStorelessUnivariateStatistic(sum, 6.01312177E8);
        testStorelessUnivariateStatistic(sumsq, 4.3671066212513456E16);
        testStorelessUnivariateStatistic(min, 2116447.0);
        testStorelessUnivariateStatistic(max, 8.5505948E7);
        testStorelessUnivariateStatistic(sumLog, 175.91713800250577);
    }
    
    public void testStorelessUnivariateStatistic(StorelessUnivariateStatistic sus, double expected) throws Exception {

        assertEquals(expected, sus.getResult(), 0.1);
        
        final ObjectMapper mapper = new ObjectMapper();
        
        //Configure Jackson to just use fields
        mapper.setVisibility(JsonMethod.FIELD, Visibility.ANY);
        mapper.setVisibility(JsonMethod.GETTER, Visibility.NONE);
        mapper.setVisibility(JsonMethod.IS_GETTER, Visibility.NONE);
        mapper.setVisibility(JsonMethod.SETTER, Visibility.NONE);
        mapper.setVisibility(JsonMethod.CREATOR, Visibility.NONE);
        
        mapper.getSerializationConfig().addMixInAnnotations(Object.class, IgnoreTypeMixIn.class);
        
        final FilterProvider filters = new SimpleFilterProvider().addFilter("storedDataFilter", SimpleBeanPropertyFilter.serializeAllExcept("storedData"));

        final ObjectWriter ssWriter = mapper.writer(filters);
        final ObjectReader ssReader = mapper.reader(sus.getClass());
        
        final String susString = ssWriter.writeValueAsString(sus);
        System.out.println(susString);
        final StorelessUnivariateStatistic newSus = ssReader.readValue(susString);
        
        assertEquals(expected, newSus.getResult(), 0.1);
    }
    
    @JsonFilter("storedDataFilter")
    private interface IgnoreTypeMixIn {
    }
}
