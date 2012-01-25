/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.events.aggr;

import static org.junit.Assert.assertEquals;

import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class IntervalHelperImplTest {
    @InjectMocks private AggregationIntervalHelperImpl helperImpl = new AggregationIntervalHelperImpl();
    @Mock private DateDimensionDao dateDimensionDao;
    @Mock private TimeDimensionDao timeDimensionDao;
    
    @Test
    public void testGetIntervalInfo() {
        final DateTime instant = new DateTime(1325881376117l, DateTimeZone.UTC);
        
        assertEquals(2012, instant.getYear());
        assertEquals(1, instant.getMonthOfYear());
        assertEquals(6, instant.getDayOfMonth());
        assertEquals(1, instant.getWeekOfWeekyear());
        assertEquals(20, instant.getHourOfDay());
        assertEquals(22, instant.getMinuteOfHour());
        assertEquals(56, instant.getSecondOfMinute());
        assertEquals(117, instant.getMillisOfSecond());
        
        DateTime start, end;
        
        
        // TEST YEAR
        
        start = this.helperImpl.determineStart(AggregationInterval.YEAR, instant);
        assertEquals(2012, start.getYear());
        assertEquals(1, start.getMonthOfYear());
        assertEquals(1, start.getDayOfMonth());
        assertEquals(52, start.getWeekOfWeekyear());
        assertEquals(0, start.getHourOfDay());
        assertEquals(0, start.getMinuteOfHour());
        assertEquals(0, start.getSecondOfMinute());
        assertEquals(0, start.getMillisOfSecond());
        
        end = this.helperImpl.determineEnd(AggregationInterval.YEAR, start);
        assertEquals(2013, end.getYear());
        assertEquals(1, end.getMonthOfYear());
        assertEquals(1, end.getDayOfMonth());
        assertEquals(1, end.getWeekOfWeekyear());
        assertEquals(0, end.getHourOfDay());
        assertEquals(0, end.getMinuteOfHour());
        assertEquals(0, end.getSecondOfMinute());
        assertEquals(0, end.getMillisOfSecond());
        
        
        // TEST MONTH
        
        start = this.helperImpl.determineStart(AggregationInterval.MONTH, instant);
        assertEquals(2012, start.getYear());
        assertEquals(1, start.getMonthOfYear());
        assertEquals(1, start.getDayOfMonth());
        assertEquals(52, start.getWeekOfWeekyear());
        assertEquals(0, start.getHourOfDay());
        assertEquals(0, start.getMinuteOfHour());
        assertEquals(0, start.getSecondOfMinute());
        assertEquals(0, start.getMillisOfSecond());
        
        end = this.helperImpl.determineEnd(AggregationInterval.MONTH, start);
        assertEquals(2012, end.getYear());
        assertEquals(2, end.getMonthOfYear());
        assertEquals(1, end.getDayOfMonth());
        assertEquals(5, end.getWeekOfWeekyear());
        assertEquals(0, end.getHourOfDay());
        assertEquals(0, end.getMinuteOfHour());
        assertEquals(0, end.getSecondOfMinute());
        assertEquals(0, end.getMillisOfSecond());
        
        
        // TEST WEEK
        
        start = this.helperImpl.determineStart(AggregationInterval.WEEK, instant);
        assertEquals(2012, start.getYear());
        assertEquals(1, start.getMonthOfYear());
        assertEquals(2, start.getDayOfMonth());
        assertEquals(1, start.getWeekOfWeekyear());
        assertEquals(0, start.getHourOfDay());
        assertEquals(0, start.getMinuteOfHour());
        assertEquals(0, start.getSecondOfMinute());
        assertEquals(0, start.getMillisOfSecond());
        
        end = this.helperImpl.determineEnd(AggregationInterval.WEEK, start);
        assertEquals(2012, end.getYear());
        assertEquals(1, end.getMonthOfYear());
        assertEquals(9, end.getDayOfMonth());
        assertEquals(2, end.getWeekOfWeekyear());
        assertEquals(0, end.getHourOfDay());
        assertEquals(0, end.getMinuteOfHour());
        assertEquals(0, end.getSecondOfMinute());
        assertEquals(0, end.getMillisOfSecond());
        
        
        // TEST DAY
        
        start = this.helperImpl.determineStart(AggregationInterval.DAY, instant);
        assertEquals(2012, start.getYear());
        assertEquals(1, start.getMonthOfYear());
        assertEquals(6, start.getDayOfMonth());
        assertEquals(1, start.getWeekOfWeekyear());
        assertEquals(0, start.getHourOfDay());
        assertEquals(0, start.getMinuteOfHour());
        assertEquals(0, start.getSecondOfMinute());
        assertEquals(0, start.getMillisOfSecond());
        
        end = this.helperImpl.determineEnd(AggregationInterval.DAY, start);
        assertEquals(2012, end.getYear());
        assertEquals(1, end.getMonthOfYear());
        assertEquals(7, end.getDayOfMonth());
        assertEquals(1, end.getWeekOfWeekyear());
        assertEquals(0, end.getHourOfDay());
        assertEquals(0, end.getMinuteOfHour());
        assertEquals(0, end.getSecondOfMinute());
        assertEquals(0, end.getMillisOfSecond());
        
        
        // TEST HOUR
        
        start = this.helperImpl.determineStart(AggregationInterval.HOUR, instant);
        assertEquals(2012, start.getYear());
        assertEquals(1, start.getMonthOfYear());
        assertEquals(6, start.getDayOfMonth());
        assertEquals(1, start.getWeekOfWeekyear());
        assertEquals(20, start.getHourOfDay());
        assertEquals(0, start.getMinuteOfHour());
        assertEquals(0, start.getSecondOfMinute());
        assertEquals(0, start.getMillisOfSecond());
        
        end = this.helperImpl.determineEnd(AggregationInterval.HOUR, start);
        assertEquals(2012, end.getYear());
        assertEquals(1, end.getMonthOfYear());
        assertEquals(6, end.getDayOfMonth());
        assertEquals(1, end.getWeekOfWeekyear());
        assertEquals(21, end.getHourOfDay());
        assertEquals(0, end.getMinuteOfHour());
        assertEquals(0, end.getSecondOfMinute());
        assertEquals(0, end.getMillisOfSecond());
        
        
        // TEST FIVE_MINUTE
        
        start = this.helperImpl.determineStart(AggregationInterval.FIVE_MINUTE, instant);
        assertEquals(2012, start.getYear());
        assertEquals(1, start.getMonthOfYear());
        assertEquals(6, start.getDayOfMonth());
        assertEquals(1, start.getWeekOfWeekyear());
        assertEquals(20, start.getHourOfDay());
        assertEquals(20, start.getMinuteOfHour());
        assertEquals(0, start.getSecondOfMinute());
        assertEquals(0, start.getMillisOfSecond());
        
        end = this.helperImpl.determineEnd(AggregationInterval.FIVE_MINUTE, start);
        assertEquals(2012, end.getYear());
        assertEquals(1, end.getMonthOfYear());
        assertEquals(6, end.getDayOfMonth());
        assertEquals(1, end.getWeekOfWeekyear());
        assertEquals(20, end.getHourOfDay());
        assertEquals(25, end.getMinuteOfHour());
        assertEquals(0, end.getSecondOfMinute());
        assertEquals(0, end.getMillisOfSecond());
        
        
        // TEST MINUTE
        
        start = this.helperImpl.determineStart(AggregationInterval.MINUTE, instant);
        assertEquals(2012, start.getYear());
        assertEquals(1, start.getMonthOfYear());
        assertEquals(6, start.getDayOfMonth());
        assertEquals(1, start.getWeekOfWeekyear());
        assertEquals(20, start.getHourOfDay());
        assertEquals(22, start.getMinuteOfHour());
        assertEquals(0, start.getSecondOfMinute());
        assertEquals(0, start.getMillisOfSecond());
        
        end = this.helperImpl.determineEnd(AggregationInterval.MINUTE, start);
        assertEquals(2012, end.getYear());
        assertEquals(1, end.getMonthOfYear());
        assertEquals(6, end.getDayOfMonth());
        assertEquals(1, end.getWeekOfWeekyear());
        assertEquals(20, end.getHourOfDay());
        assertEquals(23, end.getMinuteOfHour());
        assertEquals(0, end.getSecondOfMinute());
        assertEquals(0, end.getMillisOfSecond());
                
    }
    
}
