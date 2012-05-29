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

package org.jasig.portal.events.aggr.concuser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import javax.naming.CompositeName;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang.mutable.MutableObject;
import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.concurrency.FunctionWithoutResult;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.test.BaseAggrEventsJpaDaoTest;
import org.jasig.portal.utils.Tuple;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Function;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaAggrEventsTestContext.xml")
public class JpaConcurrentUserAggregationDaoTest extends BaseAggrEventsJpaDaoTest {
    @Autowired
    private AggregatedGroupLookupDao aggregatedGroupLookupDao;
    @Autowired
    private ConcurrentUserAggregationPrivateDao concurrentUserAggregationDao;
    @Autowired
    private TimeDimensionDao timeDimensionDao;
    @Autowired
    private DateDimensionDao dateDimensionDao;
    @Autowired
    private ICompositeGroupService compositeGroupService;
    
    
    @Test
    public void testConcurrentUserAggregationLifecycle() throws Exception {
        final IEntityGroup entityGroupA = mock(IEntityGroup.class);
        when(entityGroupA.getServiceName()).thenReturn(new CompositeName("local"));
        when(entityGroupA.getName()).thenReturn("Group A");
        when(compositeGroupService.findGroup("local.0")).thenReturn(entityGroupA);
        
        final IEntityGroup entityGroupB = mock(IEntityGroup.class);
        when(entityGroupB.getServiceName()).thenReturn(new CompositeName("local"));
        when(entityGroupB.getName()).thenReturn("Group B");
        when(compositeGroupService.findGroup("local.1")).thenReturn(entityGroupB);
        
        
        final DateTime instant = new DateTime(1326734644000l); //just a random time
        final DateMidnight instantDate = instant.toDateMidnight();
        final LocalTime instantTime = instant.toLocalTime();
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                dateDimensionDao.createDateDimension(instantDate, 0, null);
                timeDimensionDao.createTimeDimension(instantTime);
            }
        });
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final ConcurrentUserAggregationImpl concurrentUserAggregationFiveMinuteGroupA = concurrentUserAggregationDao.createConcurrentUserAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupA);
                final ConcurrentUserAggregationImpl concurrentUserAggregationFiveMinuteGroupB = concurrentUserAggregationDao.createConcurrentUserAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupB);
                final ConcurrentUserAggregationImpl concurrentUserAggregationHour = concurrentUserAggregationDao.createConcurrentUserAggregation(dateDimension, timeDimension, AggregationInterval.HOUR, groupA);

                concurrentUserAggregationFiveMinuteGroupA.countSession("joe");
                concurrentUserAggregationFiveMinuteGroupA.countSession("john");
                concurrentUserAggregationFiveMinuteGroupA.countSession("levi");
                concurrentUserAggregationFiveMinuteGroupA.countSession("erin");
                concurrentUserAggregationFiveMinuteGroupA.countSession("john");
                concurrentUserAggregationFiveMinuteGroupA.setDuration(1);
                
                concurrentUserAggregationFiveMinuteGroupB.countSession("joe");
                concurrentUserAggregationFiveMinuteGroupB.countSession("john");
                concurrentUserAggregationFiveMinuteGroupB.setDuration(1);
                
                concurrentUserAggregationHour.countSession("joe");
                concurrentUserAggregationHour.countSession("john");
                concurrentUserAggregationHour.countSession("levi");
                concurrentUserAggregationHour.countSession("erin");
                concurrentUserAggregationHour.countSession("john");
                concurrentUserAggregationHour.setDuration(1);
                
                concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregationFiveMinuteGroupA);
                concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregationHour);
            }
        });
        

        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                final Set<ConcurrentUserAggregationImpl> concurrentUserAggregationsFiveMinute = concurrentUserAggregationDao.getConcurrentUserAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE);
                assertEquals(2, concurrentUserAggregationsFiveMinute.size());
                
                for (final ConcurrentUserAggregationImpl concurrentUserAggregation : concurrentUserAggregationsFiveMinute) {
                    if (concurrentUserAggregation.getAggregatedGroup().equals(groupA)) {
                        assertEquals(4, concurrentUserAggregation.getConcurrentUsers());
                    }
                    else {
                        assertEquals(2, concurrentUserAggregation.getConcurrentUsers());
                    }
                }
                
                
                final Set<ConcurrentUserAggregationImpl> concurrentUserAggregationsHour = concurrentUserAggregationDao.getConcurrentUserAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.HOUR);
                assertEquals(1, concurrentUserAggregationsHour.size());
                
                final ConcurrentUserAggregationImpl concurrentUserAggregation = concurrentUserAggregationsHour.iterator().next();
                assertEquals(4, concurrentUserAggregation.getConcurrentUsers());
            }
        });
        

        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");

                final ConcurrentUserAggregationImpl concurrentUserAggregationFiveMinuteGroupA = concurrentUserAggregationDao.getConcurrentUserAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupA);
                final ConcurrentUserAggregationImpl concurrentUserAggregationFiveMinuteGroupB = concurrentUserAggregationDao.getConcurrentUserAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupB);
                final ConcurrentUserAggregationImpl concurrentUserAggregationHour = concurrentUserAggregationDao.getConcurrentUserAggregation(dateDimension, timeDimension, AggregationInterval.HOUR, groupA);
                
                concurrentUserAggregationFiveMinuteGroupA.countSession("john");
                concurrentUserAggregationFiveMinuteGroupA.countSession("elvira");
                concurrentUserAggregationFiveMinuteGroupA.countSession("levi");
                concurrentUserAggregationFiveMinuteGroupA.countSession("gretchen");
                concurrentUserAggregationFiveMinuteGroupA.countSession("erin");
                concurrentUserAggregationFiveMinuteGroupA.setDuration(2);
                
                concurrentUserAggregationFiveMinuteGroupB.countSession("gretchen");
                concurrentUserAggregationFiveMinuteGroupB.setDuration(2);
                
                concurrentUserAggregationHour.countSession("john");
                concurrentUserAggregationHour.countSession("elvira");
                concurrentUserAggregationHour.countSession("levi");
                concurrentUserAggregationHour.countSession("gretchen");
                concurrentUserAggregationHour.countSession("erin");
                concurrentUserAggregationHour.setDuration(2);
                
                concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregationFiveMinuteGroupA);
                concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregationFiveMinuteGroupB);
                concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregationHour);
            }
        });

        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");

                
                final Set<ConcurrentUserAggregationImpl> concurrentUserAggregationsFiveMinute = concurrentUserAggregationDao.getConcurrentUserAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE);
                assertEquals(2, concurrentUserAggregationsFiveMinute.size());
                
                for (final ConcurrentUserAggregationImpl concurrentUserAggregation : concurrentUserAggregationsFiveMinute) {
                    if (concurrentUserAggregation.getAggregatedGroup().equals(groupA)) {
                        assertEquals(6, concurrentUserAggregation.getConcurrentUsers());
                    }
                    else {
                        assertEquals(3, concurrentUserAggregation.getConcurrentUsers());
                    }
                }
                
                
                final Set<ConcurrentUserAggregationImpl> concurrentUserAggregationsHour = concurrentUserAggregationDao.getConcurrentUserAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.HOUR);
                assertEquals(1, concurrentUserAggregationsHour.size());
                
                final ConcurrentUserAggregationImpl concurrentUserAggregation = concurrentUserAggregationsHour.iterator().next();
                assertEquals(6, concurrentUserAggregation.getConcurrentUsers());
            }
        });

        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");

                final ConcurrentUserAggregationImpl concurrentUserAggregationFiveMinuteGroupA = concurrentUserAggregationDao.getConcurrentUserAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupA);
                final ConcurrentUserAggregationImpl concurrentUserAggregationFiveMinuteGroupB = concurrentUserAggregationDao.getConcurrentUserAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupB);
                final ConcurrentUserAggregationImpl concurrentUserAggregationHour = concurrentUserAggregationDao.getConcurrentUserAggregation(dateDimension, timeDimension, AggregationInterval.HOUR, groupA);
                
                concurrentUserAggregationFiveMinuteGroupA.intervalComplete(5);
                concurrentUserAggregationFiveMinuteGroupB.intervalComplete(5);
                concurrentUserAggregationHour.intervalComplete(60);
                
                concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregationFiveMinuteGroupA);
                concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregationFiveMinuteGroupB);
                concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregationHour);
            }
        });

        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                final Set<ConcurrentUserAggregationImpl> concurrentUserAggregationsFiveMinute = concurrentUserAggregationDao.getConcurrentUserAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE);
                assertEquals(2, concurrentUserAggregationsFiveMinute.size());
                
                for (final ConcurrentUserAggregationImpl concurrentUserAggregation : concurrentUserAggregationsFiveMinute) {
                    if (concurrentUserAggregation.getAggregatedGroup().equals(groupA)) {
                        assertEquals(6, concurrentUserAggregation.getConcurrentUsers());
                    }
                    else {
                        assertEquals(3, concurrentUserAggregation.getConcurrentUsers());
                    }
                }
                
                
                final Set<ConcurrentUserAggregationImpl> concurrentUserAggregationsHour = concurrentUserAggregationDao.getConcurrentUserAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.HOUR);
                assertEquals(1, concurrentUserAggregationsHour.size());
                
                final ConcurrentUserAggregationImpl concurrentUserAggregation = concurrentUserAggregationsHour.iterator().next();
                assertEquals(6, concurrentUserAggregation.getConcurrentUsers());
            }
        });

        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                final List<ConcurrentUserAggregationImpl> concurrentUserAggregations = concurrentUserAggregationDao.getConcurrentUserAggregations(
                        instantDate.monthOfYear().roundFloorCopy().toDateTime(),
                        instantDate.monthOfYear().roundCeilingCopy().toDateTime(),
                        AggregationInterval.FIVE_MINUTE,
                        groupA);
                        
                        
                assertEquals(1, concurrentUserAggregations.size());
            }
        });
    }
    
    /**
     * Populate date & time dimensions in an interval range executing a callback for each pair
     */
    public final <T> List<T> populateDateTimeDimensions(final DateTime start, final DateTime end,
            final Function<Tuple<DateDimension, TimeDimension>, T> newDimensionHandler) {
        
        return this.executeInTransaction(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                final List<T> results = new LinkedList<T>();
                final SortedMap<LocalTime, TimeDimension> times = new TreeMap<LocalTime, TimeDimension>();
                final SortedMap<DateMidnight, DateDimension> dates = new TreeMap<DateMidnight, DateDimension>();
                
                DateTime nextDateTime = start.minuteOfDay().roundFloorCopy();
                while (nextDateTime.isBefore(end)) {
                    
                    //get/create TimeDimension
                    final LocalTime localTime = nextDateTime.toLocalTime();
                    TimeDimension td = times.get(localTime);
                    if (td == null) {
                        td = timeDimensionDao.createTimeDimension(localTime);
                        times.put(localTime, td);
                    }
                    
                    //get/create DateDimension
                    final DateMidnight dateMidnight = nextDateTime.toDateMidnight();
                    DateDimension dd = dates.get(dateMidnight);
                    if (dd == null) {
                        dd = dateDimensionDao.createDateDimension(dateMidnight, 0, null);
                        dates.put(dateMidnight, dd);
                    }
                    
                    //Let callback do work
                    if (newDimensionHandler != null) {
                        final T result = newDimensionHandler.apply(new Tuple<DateDimension, TimeDimension>(dd, td));
                        if (result != null) {
                            results.add(result);
                        }
                    }
                    
                    nextDateTime = nextDateTime.plusMinutes(1);
                }
                
                return results;
            }
        });
    }
    

    @Test
    public void testConcurrentUserAggregationRangeQuery() throws Exception {
        final IEntityGroup entityGroupA = mock(IEntityGroup.class);
        when(entityGroupA.getServiceName()).thenReturn(new CompositeName("local"));
        when(entityGroupA.getName()).thenReturn("Group A");
        when(compositeGroupService.findGroup("local.0")).thenReturn(entityGroupA);
        
        final IEntityGroup entityGroupB = mock(IEntityGroup.class);
        when(entityGroupB.getServiceName()).thenReturn(new CompositeName("local"));
        when(entityGroupB.getName()).thenReturn("Group B");
        when(compositeGroupService.findGroup("local.1")).thenReturn(entityGroupB);
        
        final MutableInt aggrs = new MutableInt();
        
        //Create 2 days of login aggregates ... every 5 minutes
        final DateTime start = new DateTime(1326734644000l).minuteOfDay().roundFloorCopy();
        final DateTime end = start.plusDays(2);
        final AggregationInterval interval = AggregationInterval.FIVE_MINUTE;
        
        final MutableObject startObj = new MutableObject();
        final MutableObject endObj = new MutableObject();
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Random r = new Random(0);
                
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                populateDateTimeDimensions(start, end, new FunctionWithoutResult<Tuple<DateDimension, TimeDimension>>() {
                    @Override
                    protected void applyWithoutResult(Tuple<DateDimension, TimeDimension> input) {
                        final TimeDimension td = input.second;
                        final DateDimension dd = input.first;
                        final DateTime instant = td.getTime().toDateTime(dd.getDate());
                        
                        if (startObj.getValue() == null) {
                            startObj.setValue(instant);
                        }
                        endObj.setValue(instant);
                        
                        if (instant.equals(interval.determineStart(instant))) {
                             final ConcurrentUserAggregationImpl concurrentUserAggregationA = concurrentUserAggregationDao.createConcurrentUserAggregation(dd, td, interval, groupA);
                             final ConcurrentUserAggregationImpl concurrentUserAggregationB = concurrentUserAggregationDao.createConcurrentUserAggregation(dd, td, interval, groupB);
                             
                             for (int u = 0; u < r.nextInt(50); u++) {
                                 concurrentUserAggregationA.countSession(RandomStringUtils.random(8, 0, 0, true, true, null, r));
                                 concurrentUserAggregationB.countSession(RandomStringUtils.random(8, 0, 0, true, true, null, r));
                             }
                             
                             concurrentUserAggregationA.intervalComplete(5);
                             concurrentUserAggregationB.intervalComplete(5);
                             
                             concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregationA);
                             concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregationB);
                             
                             aggrs.add(2);
                         }
                    }
                });
            }
        });
        
        //Verify all aggrs created
        assertEquals(1152, aggrs.intValue());

        //Find all aggrs
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<ConcurrentUserAggregationImpl> concurrentUserAggregations = 
                        concurrentUserAggregationDao.getConcurrentUserAggregations(start, end.plusDays(1), interval, groupA, groupB);
                
                assertEquals(1152, concurrentUserAggregations.size());
            }
        });

        //Find first days worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<ConcurrentUserAggregationImpl> concurrentUserAggregations = 
                        concurrentUserAggregationDao.getConcurrentUserAggregations(start, end, interval, groupA, groupB);
                
                assertEquals(576, concurrentUserAggregations.size());
            }
        });

        //Find second days worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<ConcurrentUserAggregationImpl> concurrentUserAggregations = 
                        concurrentUserAggregationDao.getConcurrentUserAggregations(start.plusDays(1), end.plusDays(1), interval, groupA, groupB);
                
                assertEquals(576, concurrentUserAggregations.size());
            }
        });

        //Find first 12 hours worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<ConcurrentUserAggregationImpl> concurrentUserAggregations = 
                        concurrentUserAggregationDao.getConcurrentUserAggregations(start, end.minusHours(12), interval, groupA, groupB);
                
                assertEquals(288, concurrentUserAggregations.size());
            }
        });

        //Find middle 24 hours worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<ConcurrentUserAggregationImpl> concurrentUserAggregations = 
                        concurrentUserAggregationDao.getConcurrentUserAggregations(start.plusHours(12), end.plusHours(12), interval, groupA, groupB);
                
                assertEquals(576, concurrentUserAggregations.size());
            }
        });

        //Find middle 24 hours worth for one group
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                final List<ConcurrentUserAggregationImpl> concurrentUserAggregations = 
                        concurrentUserAggregationDao.getConcurrentUserAggregations(start.plusHours(12), end.plusHours(12), interval, groupA);
                
                assertEquals(288, concurrentUserAggregations.size());
            }
        });

        //Find last 12 hours worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<ConcurrentUserAggregationImpl> concurrentUserAggregations = 
                        concurrentUserAggregationDao.getConcurrentUserAggregations(start.plusHours(36), end.plusDays(1), interval, groupA, groupB);
                
                assertEquals(288, concurrentUserAggregations.size());
            }
        });
    }
    

    @Test
    public void testUnclosedConcurrentUserAggregationRangeQuery() throws Exception {
        final IEntityGroup entityGroupA = mock(IEntityGroup.class);
        when(entityGroupA.getServiceName()).thenReturn(new CompositeName("local"));
        when(entityGroupA.getName()).thenReturn("Group A");
        when(compositeGroupService.findGroup("local.0")).thenReturn(entityGroupA);
        
        final IEntityGroup entityGroupB = mock(IEntityGroup.class);
        when(entityGroupB.getServiceName()).thenReturn(new CompositeName("local"));
        when(entityGroupB.getName()).thenReturn("Group B");
        when(compositeGroupService.findGroup("local.1")).thenReturn(entityGroupB);
        
        final MutableInt aggrs = new MutableInt();
        
        //Create 10 minutes of aggregations
        final DateTime start = new DateTime(1326734644000l).minuteOfDay().roundFloorCopy();
        final DateTime end = start.plusMinutes(10);
        final AggregationInterval interval = AggregationInterval.FIVE_MINUTE;
        
        final MutableObject startObj = new MutableObject();
        final MutableObject endObj = new MutableObject();
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Random r = new Random(0);
                
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                populateDateTimeDimensions(start, end, new FunctionWithoutResult<Tuple<DateDimension, TimeDimension>>() {
                    @Override
                    protected void applyWithoutResult(Tuple<DateDimension, TimeDimension> input) {
                        final TimeDimension td = input.second;
                        final DateDimension dd = input.first;
                        final DateTime instant = td.getTime().toDateTime(dd.getDate());
                        
                        if (startObj.getValue() == null) {
                            startObj.setValue(instant);
                        }
                        endObj.setValue(instant);
                        
                        if (instant.equals(interval.determineStart(instant))) {
                             final ConcurrentUserAggregationImpl concurrentUserAggregationA = concurrentUserAggregationDao.createConcurrentUserAggregation(dd, td, interval, groupA);
                             final ConcurrentUserAggregationImpl concurrentUserAggregationB = concurrentUserAggregationDao.createConcurrentUserAggregation(dd, td, interval, groupB);
                             
                             for (int u = 0; u < r.nextInt(50); u++) {
                                 concurrentUserAggregationA.countSession(RandomStringUtils.random(8, 0, 0, true, true, null, r));
                                 concurrentUserAggregationB.countSession(RandomStringUtils.random(8, 0, 0, true, true, null, r));
                             }
                             
                             if (aggrs.intValue() % 4 == 0) {
                                 concurrentUserAggregationA.intervalComplete(5);
                             }
                             concurrentUserAggregationB.intervalComplete(5);
                             
                             concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregationA);
                             concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregationB);
                             
                             aggrs.add(2);
                         }
                    }
                });
            }
        });
        
        //Verify all aggrs created
        assertEquals(4, aggrs.intValue());

        //Find unclosed 1 aggr
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Set<ConcurrentUserAggregationImpl> concurrentUserAggregations = concurrentUserAggregationDao
                        .getUnclosedConcurrentUserAggregations(start, end.plusDays(1), interval);
                
                assertEquals(1, concurrentUserAggregations.size());
                
                for (final ConcurrentUserAggregationImpl concurrentUserAggregationImpl : concurrentUserAggregations) {
                    concurrentUserAggregationImpl.intervalComplete(5);
                    concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregationImpl);
                }
            }
        });

        //Find unclosed 0 aggr
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Set<ConcurrentUserAggregationImpl> concurrentUserAggregations = concurrentUserAggregationDao
                        .getUnclosedConcurrentUserAggregations(start, end.plusDays(1), interval);
                
                assertEquals(0, concurrentUserAggregations.size());
            }
        });
    }

}
