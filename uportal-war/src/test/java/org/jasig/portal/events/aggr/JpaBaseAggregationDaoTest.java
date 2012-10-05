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

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import javax.naming.CompositeName;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang.mutable.MutableObject;
import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.concurrency.FunctionWithoutResult;
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
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class JpaBaseAggregationDaoTest<
            T extends BaseAggregationImpl<K>,
            K extends BaseAggregationKey> 
        extends BaseAggrEventsJpaDaoTest {
    
    @Autowired
    protected TimeDimensionDao timeDimensionDao;
    @Autowired
    protected  DateDimensionDao dateDimensionDao;
    @Autowired
    protected AggregatedGroupLookupDao aggregatedGroupLookupDao;
    @Autowired
    protected ICompositeGroupService compositeGroupService;
    @Autowired
    protected AggregationIntervalHelper aggregationIntervalHelper;
    

    /**
     * @return The private aggregation DAO to use
     */
    protected abstract BaseAggregationPrivateDao<T, K> getAggregationDao();
    
    /**
     * Populate some data in the aggregation, use the Random source if needed for the population (it will be consistent across test runs)
     */
    protected abstract void updateAggregation(AggregationIntervalInfo intervalInfo, T aggregation, Random r);
    
    /**
     * Create an aggregation key
     */
    protected abstract K createAggregationKey(AggregationIntervalInfo intervalInfo, AggregatedGroupMapping aggregatedGroup); 
    
    /**
     * Create an aggregation key
     */
    protected abstract K createAggregationKey(AggregationInterval interval, AggregatedGroupMapping aggregatedGroup);
    
    
    /**
     * Create a list of aggregations for use in the lifecycle test
     */
    protected abstract Map<K, T> createAggregations(AggregationIntervalInfo intervalInfo, AggregatedGroupMapping aggregatedGroup);
    
    
    @Test
    public final void testBaseAggregationLifecycle() throws Exception {
        final IEntityGroup entityGroupA = mock(IEntityGroup.class);
        when(entityGroupA.getServiceName()).thenReturn(new CompositeName("local"));
        when(entityGroupA.getName()).thenReturn("Group A");
        when(compositeGroupService.findGroup("local.0")).thenReturn(entityGroupA);
        
        final IEntityGroup entityGroupB = mock(IEntityGroup.class);
        when(entityGroupB.getServiceName()).thenReturn(new CompositeName("local"));
        when(entityGroupB.getName()).thenReturn("Group B");
        when(compositeGroupService.findGroup("local.1")).thenReturn(entityGroupB);
        
        final DateTime instant = new DateTime(1326734644000l, DateTimeZone.UTC); //just a random time
        
        //Create required date and time dimensions
        populateDateTimeDimensions(instant.minusHours(2), instant.plusHours(2), null);
        
        //Create aggregations
        final Map<K, T> createdAggrs = this.executeInTransaction(new Callable<Map<K, T>>() {
            @Override
            public Map<K, T> call() throws Exception {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final AggregationIntervalInfo fiveMinuteInfo = aggregationIntervalHelper.getIntervalInfo(AggregationInterval.FIVE_MINUTE, instant);
                final AggregationIntervalInfo hourInfo = aggregationIntervalHelper.getIntervalInfo(AggregationInterval.HOUR, instant);
                
                final Map<K, T> fiveMinGroupA = createAggregations(fiveMinuteInfo, groupA);
                final Map<K, T> fiveMinGroupB = createAggregations(fiveMinuteInfo, groupB);
                final Map<K, T> hourGroupA = createAggregations(hourInfo, groupA);
                
                final Map<K, T> aggrs = new HashMap<K, T>(fiveMinGroupA);
                aggrs.putAll(fiveMinGroupB);
                aggrs.putAll(hourGroupA);
                
                return aggrs;
            }
        });
        
        //Verify aggregations were created
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregationIntervalInfo fiveMinuteInfo = aggregationIntervalHelper.getIntervalInfo(AggregationInterval.FIVE_MINUTE, instant);
                final AggregationIntervalInfo hourInfo = aggregationIntervalHelper.getIntervalInfo(AggregationInterval.HOUR, instant);
                
                final Map<K, T> fiveMinGroup = getAggregationDao().getAggregationsForInterval(
                        fiveMinuteInfo.getDateDimension(), 
                        fiveMinuteInfo.getTimeDimension(), 
                        fiveMinuteInfo.getAggregationInterval());
                
                final Map<K, T> hourGroup = getAggregationDao().getAggregationsForInterval(
                        hourInfo.getDateDimension(), 
                        hourInfo.getTimeDimension(), 
                        hourInfo.getAggregationInterval());
                
                final Map<K, T> foundAggrs = new HashMap<K, T>(fiveMinGroup);
                foundAggrs.putAll(hourGroup);
                
                assertEquals("Aggregations not created as expected", createdAggrs, foundAggrs);
            }
        });

        //Update Aggregations
        final Map<K, T> updatedAggrs = this.executeInTransaction(new Callable<Map<K, T>>() {
            @Override
            public Map<K, T> call() throws Exception {
                final Random r = new Random(0);
                
                final AggregationIntervalInfo fiveMinuteInfo = aggregationIntervalHelper.getIntervalInfo(AggregationInterval.FIVE_MINUTE, instant);
                final AggregationIntervalInfo hourInfo = aggregationIntervalHelper.getIntervalInfo(AggregationInterval.HOUR, instant);
                
                final Map<K, T> fiveMinGroup = getAggregationDao().getAggregationsForInterval(
                        fiveMinuteInfo.getDateDimension(), 
                        fiveMinuteInfo.getTimeDimension(), 
                        fiveMinuteInfo.getAggregationInterval());
                
                final Map<K, T> hourGroup = getAggregationDao().getAggregationsForInterval(
                        hourInfo.getDateDimension(), 
                        hourInfo.getTimeDimension(), 
                        hourInfo.getAggregationInterval());
                
                final Map<K, T> updatedAggrs = new HashMap<K, T>();
                
                for (final Entry<K, T> aggrEntry : fiveMinGroup.entrySet()) {
                    final T aggr = aggrEntry.getValue();
                    updateAggregation(fiveMinuteInfo, aggr, r);
                    getAggregationDao().updateAggregation(aggr);
                    updatedAggrs.put(aggrEntry.getKey(), aggr);
                }
                for (final Entry<K, T> aggrEntry : hourGroup.entrySet()) {
                    final T aggr = aggrEntry.getValue();
                    updateAggregation(hourInfo, aggr, r);
                    getAggregationDao().updateAggregation(aggr);
                    updatedAggrs.put(aggrEntry.getKey(), aggr);
                }
                
                return updatedAggrs;
            }
        });
        
        //Verify aggregations were updated
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregationIntervalInfo fiveMinuteInfo = aggregationIntervalHelper.getIntervalInfo(AggregationInterval.FIVE_MINUTE, instant);
                final AggregationIntervalInfo hourInfo = aggregationIntervalHelper.getIntervalInfo(AggregationInterval.HOUR, instant);
                
                final Map<K, T> fiveMinGroup = getAggregationDao().getAggregationsForInterval(
                        fiveMinuteInfo.getDateDimension(), 
                        fiveMinuteInfo.getTimeDimension(), 
                        fiveMinuteInfo.getAggregationInterval());
                
                final Map<K, T> hourGroup = getAggregationDao().getAggregationsForInterval(
                        hourInfo.getDateDimension(), 
                        hourInfo.getTimeDimension(), 
                        hourInfo.getAggregationInterval());
                
                final Map<K, T> foundAggrs = new HashMap<K, T>(fiveMinGroup);
                foundAggrs.putAll(hourGroup);
                
                assertEquals("Aggregations not updated as expected", updatedAggrs, foundAggrs);
            }
        });

        //Complete intervals
        final Map<K, T> completeAggrs = this.executeInTransaction(new Callable<Map<K, T>>() {
            @Override
            public Map<K, T> call() throws Exception {
                final AggregationIntervalInfo fiveMinuteInfo = aggregationIntervalHelper.getIntervalInfo(AggregationInterval.FIVE_MINUTE, instant);
                final AggregationIntervalInfo hourInfo = aggregationIntervalHelper.getIntervalInfo(AggregationInterval.HOUR, instant);
                
                final Map<K, T> fiveMinGroup = getAggregationDao().getAggregationsForInterval(
                        fiveMinuteInfo.getDateDimension(), 
                        fiveMinuteInfo.getTimeDimension(), 
                        fiveMinuteInfo.getAggregationInterval());
                
                final Map<K, T> hourGroup = getAggregationDao().getAggregationsForInterval(
                        hourInfo.getDateDimension(), 
                        hourInfo.getTimeDimension(), 
                        hourInfo.getAggregationInterval());
                
                final Map<K, T> completeAggrs = new HashMap<K, T>();
                
                for (final Entry<K, T> aggrEntry : fiveMinGroup.entrySet()) {
                    final T aggr = aggrEntry.getValue();
                    aggr.intervalComplete(5);
                    getAggregationDao().updateAggregation(aggr);
                    completeAggrs.put(aggrEntry.getKey(), aggr);
                }
                for (final Entry<K, T> aggrEntry : hourGroup.entrySet()) {
                    final T aggr = aggrEntry.getValue();
                    aggr.intervalComplete(60);
                    getAggregationDao().updateAggregation(aggr);
                    completeAggrs.put(aggrEntry.getKey(), aggr);
                }
                
                return completeAggrs;
            }
        });
        
        //Verify aggregations were completed
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregationIntervalInfo fiveMinuteInfo = aggregationIntervalHelper.getIntervalInfo(AggregationInterval.FIVE_MINUTE, instant);
                final AggregationIntervalInfo hourInfo = aggregationIntervalHelper.getIntervalInfo(AggregationInterval.HOUR, instant);
                
                final Map<K, T> fiveMinGroup = getAggregationDao().getAggregationsForInterval(
                        fiveMinuteInfo.getDateDimension(), 
                        fiveMinuteInfo.getTimeDimension(), 
                        fiveMinuteInfo.getAggregationInterval());
                
                final Map<K, T> hourGroup = getAggregationDao().getAggregationsForInterval(
                        hourInfo.getDateDimension(), 
                        hourInfo.getTimeDimension(), 
                        hourInfo.getAggregationInterval());
                
                final Map<K, T> foundAggrs = new HashMap<K, T>(fiveMinGroup);
                foundAggrs.putAll(hourGroup);
                
                assertEquals("Aggregations not completed as expected", completeAggrs, foundAggrs);
            }
        });
    }

    @Test
    public final void testBaseAggregationRangeQuery() throws Exception {
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
        final DateTime start = new DateTime(1326734644000l, DateTimeZone.UTC).minuteOfDay().roundFloorCopy();
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
                            final AggregationIntervalInfo intervalInfo = aggregationIntervalHelper.getIntervalInfo(interval, instant);
                            
                            final T baseAggregationA = getAggregationDao().createAggregation(createAggregationKey(intervalInfo, groupA));
                            final T baseAggregationB = getAggregationDao().createAggregation(createAggregationKey(intervalInfo, groupB));
                             
                            for (int u = 0; u < r.nextInt(50); u++) {
                                updateAggregation(intervalInfo, baseAggregationA, r);
                                updateAggregation(intervalInfo, baseAggregationB, r);
                            }
                            
                            baseAggregationA.intervalComplete(5);
                            baseAggregationB.intervalComplete(5);
                            
                            getAggregationDao().updateAggregation(baseAggregationA);
                            getAggregationDao().updateAggregation(baseAggregationB);
                            
                            aggrs.add(2);
                        }
                    }
                });
            }
        });
        
        //Verify all aggrs created
        assertEquals(1152, aggrs.intValue());
        


        //Find aggrs for one day
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final DateTime queryStart = start.toDateMidnight().toDateTime();
                final DateTime queryEnd = queryStart.plusDays(1).minusSeconds(1);
                
                final List<T> baseAggregations = 
                        getAggregationDao().getAggregations(queryStart, queryEnd, createAggregationKey(interval, groupA), groupB);
                
                assertEquals(158, baseAggregations.size());
            }
        });
        
      //Find aggrs for second day
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final DateTime queryStart = start.toDateMidnight().minusDays(1).toDateTime();
                final DateTime queryEnd = queryStart.plusDays(2).minusSeconds(1);
                
                final List<T> baseAggregations = 
                        getAggregationDao().getAggregations(queryStart, queryEnd, createAggregationKey(interval, groupA), groupB);
                
                assertEquals(158, baseAggregations.size());
            }
        });

        //Find all aggrs
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<T> baseAggregations = 
                        getAggregationDao().getAggregations(start, end.plusDays(1), createAggregationKey(interval, groupA), groupB);
                
                assertEquals(1152, baseAggregations.size());
            }
        });

        //Find first days worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<T> baseAggregations = 
                        getAggregationDao().getAggregations(start, end, createAggregationKey(interval, groupA), groupB);
                
                assertEquals(1152, baseAggregations.size());
            }
        });

        //Find second days worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<T> baseAggregations = 
                        getAggregationDao().getAggregations(start.plusDays(1), end.plusDays(1), createAggregationKey(interval, groupA), groupB);
                
                assertEquals(576, baseAggregations.size());
            }
        });

        //Find first 12 hours worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<T> baseAggregations = 
                        getAggregationDao().getAggregations(start, start.plusHours(12), createAggregationKey(interval, groupA), groupB);
                
                assertEquals(288, baseAggregations.size());
            }
        });

        //Find middle 24 hours worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<T> baseAggregations = 
                        getAggregationDao().getAggregations(start.plusHours(12), end.plusHours(12), createAggregationKey(interval, groupA), groupB);
                
                assertEquals(864, baseAggregations.size());
            }
        });

        //Find middle 24 hours worth for one group
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                final List<T> baseAggregations = 
                        getAggregationDao().getAggregations(start.plusHours(12), end.plusHours(12), createAggregationKey(interval, groupA));
                
                assertEquals(432, baseAggregations.size());
            }
        });

        //Find last 12 hours worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<T> baseAggregations = 
                        getAggregationDao().getAggregations(start.plusHours(36), end.plusDays(1), createAggregationKey(interval, groupA), groupB);
                
                assertEquals(288, baseAggregations.size());
            }
        });
        
        //TODO Query for intervals that are stored
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Set<AggregatedGroupMapping> aggregatedGroupMappings = getAggregationDao().getAggregatedGroupMappings();
                assertEquals(2, aggregatedGroupMappings.size());
                
                final Set<AggregationInterval> aggregationIntervals = getAggregationDao().getAggregationIntervals();
                assertEquals(1, aggregationIntervals.size());
            }
        });
    }
    
    @Test
    public final void testModifyingClosedAggregationRangeQuery() throws Exception {
        final IEntityGroup entityGroupA = mock(IEntityGroup.class);
        when(entityGroupA.getServiceName()).thenReturn(new CompositeName("local"));
        when(entityGroupA.getName()).thenReturn("Group A");
        when(compositeGroupService.findGroup("local.0")).thenReturn(entityGroupA);
        
        final MutableInt aggrs = new MutableInt();
        
        //Create 10 minutes of aggregations
        final DateTime start = new DateTime(1326734644000l, DateTimeZone.UTC).minuteOfDay().roundFloorCopy();
        final DateTime end = start.plusMinutes(10);
        final AggregationInterval interval = AggregationInterval.FIVE_MINUTE;
        
        final MutableObject startObj = new MutableObject();
        final MutableObject endObj = new MutableObject();
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Random r = new Random(0);
                
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
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
                            final AggregationIntervalInfo intervalInfo = aggregationIntervalHelper.getIntervalInfo(interval, instant);
                            
                            final T baseAggregationA = getAggregationDao().createAggregation(createAggregationKey(intervalInfo, groupA));
                             
                            for (int u = 0; u < r.nextInt(50); u++) {
                                updateAggregation(intervalInfo, baseAggregationA, r);
                            }
                             
                            baseAggregationA.intervalComplete(5);
                             
                            getAggregationDao().updateAggregation(baseAggregationA);
                             
                            aggrs.add(1);
                        }
                    }
                });
            }
        });
        
        //Verify all aggrs created
        assertEquals(2, aggrs.intValue());

        //Find unclosed 1 aggr
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Random r = new Random(0);
                
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                final K key = createAggregationKey(interval, groupA);
                final List<T> aggregations = getAggregationDao().getAggregations(start.minusDays(1), end.plusDays(1), key);
                
                assertEquals(2, aggregations.size());
                
                for (final T baseAggregationImpl : aggregations) {
                    final DateTime instant = baseAggregationImpl.getDateTime();
                    final AggregationIntervalInfo intervalInfo = aggregationIntervalHelper.getIntervalInfo(interval, instant);
                    try {
                        updateAggregation(intervalInfo, baseAggregationImpl, r);
                        fail("Expected aggregation update to throw IllegalStateException");
                    }
                    catch (IllegalStateException e) {
                        //expected
                    }
                }
            }
        });
    }
    

    @Test
    public final void testUnclosedBaseAggregationRangeQuery() throws Exception {
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
        final DateTime start = new DateTime(1326734644000l, DateTimeZone.UTC).minuteOfDay().roundFloorCopy();
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
                            final AggregationIntervalInfo intervalInfo = aggregationIntervalHelper.getIntervalInfo(interval, instant);
                            
                            final T baseAggregationA = getAggregationDao().createAggregation(createAggregationKey(intervalInfo, groupA));
                            final T baseAggregationB = getAggregationDao().createAggregation(createAggregationKey(intervalInfo, groupB));
                             
                            for (int u = 0; u < r.nextInt(50); u++) {
                                updateAggregation(intervalInfo, baseAggregationA, r);
                                updateAggregation(intervalInfo, baseAggregationB, r);
                            }
                             
                            if (aggrs.intValue() % 4 == 0) {
                                baseAggregationA.intervalComplete(5);
                            }
                            baseAggregationB.intervalComplete(5);
                             
                            getAggregationDao().updateAggregation(baseAggregationA);
                            getAggregationDao().updateAggregation(baseAggregationB);
                             
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
                final Collection<T> baseAggregations = getAggregationDao()
                        .getUnclosedAggregations(start.minusDays(1), end.plusDays(1), interval);
                
                assertEquals(1, baseAggregations.size());
                
                for (final T baseAggregationImpl : baseAggregations) {
                    baseAggregationImpl.intervalComplete(5);
                    getAggregationDao().updateAggregation(baseAggregationImpl);
                }
            }
        });

        //Find unclosed 0 aggr
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Collection<T> baseAggregations = getAggregationDao()
                        .getUnclosedAggregations(start.minusDays(1), end.plusDays(1), interval);
                
                assertEquals(0, baseAggregations.size());
            }
        });
    }

    
    /**
     * Populate date & time dimensions in an interval range executing a callback for each pair
     */
    public final <RT> List<RT> populateDateTimeDimensions(final DateTime start, final DateTime end,
            final Function<Tuple<DateDimension, TimeDimension>, RT> newDimensionHandler) {
        
        return this.executeInTransaction(new Callable<List<RT>>() {
            @Override
            public List<RT> call() throws Exception {
                final List<RT> results = new LinkedList<RT>();
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
                        final RT result = newDimensionHandler.apply(new Tuple<DateDimension, TimeDimension>(dd, td));
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
}
