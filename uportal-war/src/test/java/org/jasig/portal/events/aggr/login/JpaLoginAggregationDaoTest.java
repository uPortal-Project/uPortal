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

package org.jasig.portal.events.aggr.login;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Random;

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

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaAggrEventsTestContext.xml")
public class JpaLoginAggregationDaoTest extends BaseAggrEventsJpaDaoTest {
    @Autowired
    private AggregatedGroupLookupDao aggregatedGroupLookupDao;
    @Autowired
    private LoginAggregationPrivateDao loginAggregationDao;
    @Autowired
    private TimeDimensionDao timeDimensionDao;
    @Autowired
    private DateDimensionDao dateDimensionDao;
    @Autowired
    private ICompositeGroupService compositeGroupService;
    
    
    @Test
    public void testLoginAggregationLifecycle() throws Exception {
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
                
                final LoginAggregationImpl loginAggregationFiveMinuteGroupA = loginAggregationDao.createAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupA);
                final LoginAggregationImpl loginAggregationFiveMinuteGroupB = loginAggregationDao.createAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupB);
                final LoginAggregationImpl loginAggregationHour = loginAggregationDao.createAggregation(dateDimension, timeDimension, AggregationInterval.HOUR, groupA);

                loginAggregationFiveMinuteGroupA.countUser("joe");
                loginAggregationFiveMinuteGroupA.countUser("john");
                loginAggregationFiveMinuteGroupA.countUser("levi");
                loginAggregationFiveMinuteGroupA.countUser("erin");
                loginAggregationFiveMinuteGroupA.countUser("john");
                loginAggregationFiveMinuteGroupA.setDuration(1);
                
                loginAggregationFiveMinuteGroupB.countUser("joe");
                loginAggregationFiveMinuteGroupB.countUser("john");
                loginAggregationFiveMinuteGroupB.setDuration(1);
                
                loginAggregationHour.countUser("joe");
                loginAggregationHour.countUser("john");
                loginAggregationHour.countUser("levi");
                loginAggregationHour.countUser("erin");
                loginAggregationHour.countUser("john");
                loginAggregationHour.setDuration(1);
                
                loginAggregationDao.updateAggregation(loginAggregationFiveMinuteGroupA);
                loginAggregationDao.updateAggregation(loginAggregationHour);
            }
        });
        

        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                final Collection<LoginAggregationImpl> loginAggregationsFiveMinute = loginAggregationDao.getAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE);
                assertEquals(2, loginAggregationsFiveMinute.size());
                
                for (final LoginAggregationImpl loginAggregation : loginAggregationsFiveMinute) {
                    if (loginAggregation.getAggregatedGroup().equals(groupA)) {
                        assertEquals(5, loginAggregation.getLoginCount());
                        assertEquals(4, loginAggregation.getUniqueLoginCount());
                    }
                    else {
                        assertEquals(2, loginAggregation.getLoginCount());
                        assertEquals(2, loginAggregation.getUniqueLoginCount());
                    }
                }
                
                
                final Collection<LoginAggregationImpl> loginAggregationsHour = loginAggregationDao.getAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.HOUR);
                assertEquals(1, loginAggregationsHour.size());
                
                final LoginAggregationImpl loginAggregation = loginAggregationsHour.iterator().next();
                assertEquals(5, loginAggregation.getLoginCount());
                assertEquals(4, loginAggregation.getUniqueLoginCount());
            }
        });
        

        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");

                final LoginAggregationImpl loginAggregationFiveMinuteGroupA = loginAggregationDao.getAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupA);
                final LoginAggregationImpl loginAggregationFiveMinuteGroupB = loginAggregationDao.getAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupB);
                final LoginAggregationImpl loginAggregationHour = loginAggregationDao.getAggregation(dateDimension, timeDimension, AggregationInterval.HOUR, groupA);
                
                loginAggregationFiveMinuteGroupA.countUser("john");
                loginAggregationFiveMinuteGroupA.countUser("elvira");
                loginAggregationFiveMinuteGroupA.countUser("levi");
                loginAggregationFiveMinuteGroupA.countUser("gretchen");
                loginAggregationFiveMinuteGroupA.countUser("erin");
                loginAggregationFiveMinuteGroupA.setDuration(2);
                
                loginAggregationFiveMinuteGroupB.countUser("gretchen");
                loginAggregationFiveMinuteGroupB.setDuration(2);
                
                loginAggregationHour.countUser("john");
                loginAggregationHour.countUser("elvira");
                loginAggregationHour.countUser("levi");
                loginAggregationHour.countUser("gretchen");
                loginAggregationHour.countUser("erin");
                loginAggregationHour.setDuration(2);
                
                loginAggregationDao.updateAggregation(loginAggregationFiveMinuteGroupA);
                loginAggregationDao.updateAggregation(loginAggregationFiveMinuteGroupB);
                loginAggregationDao.updateAggregation(loginAggregationHour);
            }
        });

        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");

                
                final Collection<LoginAggregationImpl> loginAggregationsFiveMinute = loginAggregationDao.getAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE);
                assertEquals(2, loginAggregationsFiveMinute.size());
                
                for (final LoginAggregationImpl loginAggregation : loginAggregationsFiveMinute) {
                    if (loginAggregation.getAggregatedGroup().equals(groupA)) {
                        assertEquals(10, loginAggregation.getLoginCount());
                        assertEquals(6, loginAggregation.getUniqueLoginCount());
                    }
                    else {
                        assertEquals(3, loginAggregation.getLoginCount());
                        assertEquals(3, loginAggregation.getUniqueLoginCount());
                    }
                }
                
                
                final Collection<LoginAggregationImpl> loginAggregationsHour = loginAggregationDao.getAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.HOUR);
                assertEquals(1, loginAggregationsHour.size());
                
                final LoginAggregationImpl loginAggregation = loginAggregationsHour.iterator().next();
                assertEquals(10, loginAggregation.getLoginCount());
                assertEquals(6, loginAggregation.getUniqueLoginCount());
            }
        });

        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");

                final LoginAggregationImpl loginAggregationFiveMinuteGroupA = loginAggregationDao.getAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupA);
                final LoginAggregationImpl loginAggregationFiveMinuteGroupB = loginAggregationDao.getAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupB);
                final LoginAggregationImpl loginAggregationHour = loginAggregationDao.getAggregation(dateDimension, timeDimension, AggregationInterval.HOUR, groupA);
                
                loginAggregationFiveMinuteGroupA.intervalComplete(5);
                loginAggregationFiveMinuteGroupB.intervalComplete(5);
                loginAggregationHour.intervalComplete(60);
                
                loginAggregationDao.updateAggregation(loginAggregationFiveMinuteGroupA);
                loginAggregationDao.updateAggregation(loginAggregationFiveMinuteGroupB);
                loginAggregationDao.updateAggregation(loginAggregationHour);
            }
        });

        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                final Collection<LoginAggregationImpl> loginAggregationsFiveMinute = loginAggregationDao.getAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE);
                assertEquals(2, loginAggregationsFiveMinute.size());
                
                for (final LoginAggregationImpl loginAggregation : loginAggregationsFiveMinute) {
                    if (loginAggregation.getAggregatedGroup().equals(groupA)) {
                        assertEquals(10, loginAggregation.getLoginCount());
                        assertEquals(6, loginAggregation.getUniqueLoginCount());
                    }
                    else {
                        assertEquals(3, loginAggregation.getLoginCount());
                        assertEquals(3, loginAggregation.getUniqueLoginCount());
                    }
                }
                
                
                final Collection<LoginAggregationImpl> loginAggregationsHour = loginAggregationDao.getAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.HOUR);
                assertEquals(1, loginAggregationsHour.size());
                
                final LoginAggregationImpl loginAggregation = loginAggregationsHour.iterator().next();
                assertEquals(10, loginAggregation.getLoginCount());
                assertEquals(6, loginAggregation.getUniqueLoginCount());
            }
        });

        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                final List<LoginAggregationImpl> loginAggregations = loginAggregationDao.getAggregations(
                        instantDate.monthOfYear().roundFloorCopy().toDateTime(),
                        instantDate.monthOfYear().roundCeilingCopy().toDateTime(),
                        AggregationInterval.FIVE_MINUTE,
                        groupA);
                        
                        
                assertEquals(1, loginAggregations.size());
            }
        });
    }
    

    @Test
    public void testLoginAggregationRangeQuery() throws Exception {
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
                             final LoginAggregationImpl loginAggregationA = loginAggregationDao.createAggregation(dd, td, interval, groupA);
                             final LoginAggregationImpl loginAggregationB = loginAggregationDao.createAggregation(dd, td, interval, groupB);
                             
                             for (int u = 0; u < r.nextInt(50); u++) {
                                 loginAggregationA.countUser(RandomStringUtils.random(8, 0, 0, true, true, null, r));
                                 loginAggregationB.countUser(RandomStringUtils.random(8, 0, 0, true, true, null, r));
                             }
                             
                             loginAggregationA.intervalComplete(5);
                             loginAggregationB.intervalComplete(5);
                             
                             loginAggregationDao.updateAggregation(loginAggregationA);
                             loginAggregationDao.updateAggregation(loginAggregationB);
                             
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
                
                final List<LoginAggregationImpl> loginAggregations = 
                        loginAggregationDao.getAggregations(start, end.plusDays(1), interval, groupA, groupB);
                
                assertEquals(1152, loginAggregations.size());
            }
        });

        //Find first days worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<LoginAggregationImpl> loginAggregations = 
                        loginAggregationDao.getAggregations(start, end, interval, groupA, groupB);
                
                assertEquals(576, loginAggregations.size());
            }
        });

        //Find second days worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<LoginAggregationImpl> loginAggregations = 
                        loginAggregationDao.getAggregations(start.plusDays(1), end.plusDays(1), interval, groupA, groupB);
                
                assertEquals(576, loginAggregations.size());
            }
        });

        //Find first 12 hours worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<LoginAggregationImpl> loginAggregations = 
                        loginAggregationDao.getAggregations(start, end.minusHours(12), interval, groupA, groupB);
                
                assertEquals(288, loginAggregations.size());
            }
        });

        //Find middle 24 hours worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<LoginAggregationImpl> loginAggregations = 
                        loginAggregationDao.getAggregations(start.plusHours(12), end.plusHours(12), interval, groupA, groupB);
                
                assertEquals(576, loginAggregations.size());
            }
        });

        //Find middle 24 hours worth for one group
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                final List<LoginAggregationImpl> loginAggregations = 
                        loginAggregationDao.getAggregations(start.plusHours(12), end.plusHours(12), interval, groupA);
                
                assertEquals(288, loginAggregations.size());
            }
        });

        //Find last 12 hours worth
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                final AggregatedGroupMapping groupB = aggregatedGroupLookupDao.getGroupMapping("local.1");
                
                final List<LoginAggregationImpl> loginAggregations = 
                        loginAggregationDao.getAggregations(start.plusHours(36), end.plusDays(1), interval, groupA, groupB);
                
                assertEquals(288, loginAggregations.size());
            }
        });
    }
    

    @Test
    public void testUnclosedLoginAggregationRangeQuery() throws Exception {
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
                             final LoginAggregationImpl loginAggregationA = loginAggregationDao.createAggregation(dd, td, interval, groupA);
                             final LoginAggregationImpl loginAggregationB = loginAggregationDao.createAggregation(dd, td, interval, groupB);
                             
                             for (int u = 0; u < r.nextInt(50); u++) {
                                 loginAggregationA.countUser(RandomStringUtils.random(8, 0, 0, true, true, null, r));
                                 loginAggregationB.countUser(RandomStringUtils.random(8, 0, 0, true, true, null, r));
                             }
                             
                             if (aggrs.intValue() % 4 == 0) {
                                 loginAggregationA.intervalComplete(5);
                             }
                             loginAggregationB.intervalComplete(5);
                             
                             loginAggregationDao.updateAggregation(loginAggregationA);
                             loginAggregationDao.updateAggregation(loginAggregationB);
                             
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
                final Collection<LoginAggregationImpl> loginAggregations = loginAggregationDao
                        .getUnclosedAggregations(start, end.plusDays(1), interval);
                
                assertEquals(1, loginAggregations.size());
                
                for (final LoginAggregationImpl loginAggregationImpl : loginAggregations) {
                    loginAggregationImpl.intervalComplete(5);
                    loginAggregationDao.updateAggregation(loginAggregationImpl);
                }
            }
        });

        //Find unclosed 0 aggr
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Collection<LoginAggregationImpl> loginAggregations = loginAggregationDao
                        .getUnclosedAggregations(start, end.plusDays(1), interval);
                
                assertEquals(0, loginAggregations.size());
            }
        });
    }

}
