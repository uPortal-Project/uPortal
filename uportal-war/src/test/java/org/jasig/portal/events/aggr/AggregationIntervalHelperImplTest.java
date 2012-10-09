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
import static org.mockito.Mockito.when;

import java.util.List;

import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.jasig.portal.events.aggr.dao.jpa.AcademicTermDetailImpl;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class AggregationIntervalHelperImplTest {
	@InjectMocks private AggregationIntervalHelperImpl helper = new AggregationIntervalHelperImpl();
	@Mock private IEventAggregationManagementDao eventAggregationManagementDao;
	@Mock private TimeDimensionDao timeDimensionDao;
	@Mock private DateDimensionDao dateDimensionDao;
	@Mock private AggregatedGroupMapping groupMapping;
	
	@Before
	public void setUp() {
	    when(eventAggregationManagementDao.getQuartersDetails()).thenReturn(EventDateTimeUtils.createStandardQuarters());
	    
	    final List<AcademicTermDetail> terms = ImmutableList.<AcademicTermDetail>of(
	            new AcademicTermDetailImpl(new DateMidnight(2012, 1, 12), new DateMidnight(2012, 4, 1), "spring 2"),
	            new AcademicTermDetailImpl(new DateMidnight(2012, 4, 1), new DateMidnight(2012, 7, 1), "summer 2"),
                new AcademicTermDetailImpl(new DateMidnight(2012, 7, 1), new DateMidnight(2012, 12, 15), "fall 2"),
                new AcademicTermDetailImpl(new DateMidnight(2012, 12, 15), new DateMidnight(2013, 1, 14), "winter 2"),
                new AcademicTermDetailImpl(new DateMidnight(2015, 12, 15), new DateMidnight(2016, 1, 14), "winter 5")
        );
        when(eventAggregationManagementDao.getAcademicTermDetails()).thenReturn(terms);
	}
	
    
    @Test
    public void testIntervalsBetween() {
        DateTime start = new DateTime(2012, 2, 2, 2, 2, 2);
        DateTime end = start.plusYears(2);
        
        assertEquals(1052640, helper.intervalsBetween(AggregationInterval.MINUTE, start, end));
        assertEquals(210528, helper.intervalsBetween(AggregationInterval.FIVE_MINUTE, start, end));
        assertEquals(17544, helper.intervalsBetween(AggregationInterval.HOUR, start, end));
        assertEquals(731, helper.intervalsBetween(AggregationInterval.DAY, start, end));
        assertEquals(104, helper.intervalsBetween(AggregationInterval.WEEK, start, end));
        assertEquals(24, helper.intervalsBetween(AggregationInterval.MONTH, start, end));
        assertEquals(2, helper.intervalsBetween(AggregationInterval.YEAR, start, end));
        assertEquals(3, helper.intervalsBetween(AggregationInterval.ACADEMIC_TERM, start, end));
        assertEquals(8, helper.intervalsBetween(AggregationInterval.CALENDAR_QUARTER, start, end));
    }
    
    
    @Test
    public void testIntervalsBetweens() {
        DateTime start = new DateTime(2012, 2, 2, 2, 2, 2);
        DateTime end = start.plusYears(2);
        
        assertEquals(1052640, helper.getIntervalStartDateTimesBetween(AggregationInterval.MINUTE, start, end).size());
        assertEquals(210528, helper.getIntervalStartDateTimesBetween(AggregationInterval.FIVE_MINUTE, start, end).size());
        assertEquals(17544, helper.getIntervalStartDateTimesBetween(AggregationInterval.HOUR, start, end).size());
        assertEquals(731, helper.getIntervalStartDateTimesBetween(AggregationInterval.DAY, start, end).size());
        assertEquals(104, helper.getIntervalStartDateTimesBetween(AggregationInterval.WEEK, start, end).size());
        assertEquals(24, helper.getIntervalStartDateTimesBetween(AggregationInterval.MONTH, start, end).size());
        assertEquals(2, helper.getIntervalStartDateTimesBetween(AggregationInterval.YEAR, start, end).size());
        assertEquals(3, helper.getIntervalStartDateTimesBetween(AggregationInterval.ACADEMIC_TERM, start, end).size());
        assertEquals(8, helper.getIntervalStartDateTimesBetween(AggregationInterval.CALENDAR_QUARTER, start, end).size());
    }
}
