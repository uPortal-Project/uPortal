/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.events.aggr;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.SecondMoment;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;
import org.jasig.portal.test.BaseAggrEventsJpaDaoTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaAggrEventsTestContext.xml")
public class JpaStatisticalSummaryTest extends BaseAggrEventsJpaDaoTest {
    
//    @Test
//    public void testJpaStatisticalSummary() {
//        final long id = this.executeInTransaction(new Callable<Long>() {
//            @Override
//            public Long call() throws Exception {
//                final JpaStatisticalSummary jpaStatisticalSummary = new JpaStatisticalSummary();
//                
//                final Random r = new Random(0);
//                for (int i = 0; i < 10; i++) {
//                    final int nextInt = r.nextInt(100000000);
//                    jpaStatisticalSummary.addValue(nextInt);
//                }
//                
//                getEntityManager().persist(jpaStatisticalSummary);
//                
//                System.out.println(jpaStatisticalSummary);
//                
//                return jpaStatisticalSummary.getStatSummaryId();
//            }
//        });
//        
//        System.out.println(id);
//        
//        this.executeInTransaction(new CallableWithoutResult() {
//            @Override
//            protected void callWithoutResult() {
//                final JpaStatisticalSummary jpaStatisticalSummary = getEntityManager().find(JpaStatisticalSummary.class, id);
//                
//                System.out.println(jpaStatisticalSummary);
//            }
//        });
//    }

    
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
        mapper.findAndRegisterModules();
        
        //Configure Jackson to just use fields
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.CREATOR, Visibility.NONE);
        
        mapper.addMixInAnnotations(Object.class, IgnoreTypeMixIn.class);
        
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
