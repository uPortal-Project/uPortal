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

import java.util.List;
import java.util.Set;

import javax.naming.CompositeName;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.test.BaseJpaDaoTest;
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
public class JpaLoginAggregationDaoTest extends BaseJpaDaoTest {
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
    
    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    private EntityManager entityManager;
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
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
                
                final LoginAggregationImpl loginAggregationFiveMinuteGroupA = loginAggregationDao.createLoginAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupA);
                final LoginAggregationImpl loginAggregationFiveMinuteGroupB = loginAggregationDao.createLoginAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupB);
                final LoginAggregationImpl loginAggregationHour = loginAggregationDao.createLoginAggregation(dateDimension, timeDimension, AggregationInterval.HOUR, groupA);

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
                
                loginAggregationDao.updateLoginAggregation(loginAggregationFiveMinuteGroupA);
                loginAggregationDao.updateLoginAggregation(loginAggregationHour);
            }
        });
        

        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                final Set<LoginAggregationImpl> loginAggregationsFiveMinute = loginAggregationDao.getLoginAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE);
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
                
                
                final Set<LoginAggregationImpl> loginAggregationsHour = loginAggregationDao.getLoginAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.HOUR);
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

                final LoginAggregationImpl loginAggregationFiveMinuteGroupA = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupA);
                final LoginAggregationImpl loginAggregationFiveMinuteGroupB = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupB);
                final LoginAggregationImpl loginAggregationHour = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension, AggregationInterval.HOUR, groupA);
                
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
                
                loginAggregationDao.updateLoginAggregation(loginAggregationFiveMinuteGroupA);
                loginAggregationDao.updateLoginAggregation(loginAggregationFiveMinuteGroupB);
                loginAggregationDao.updateLoginAggregation(loginAggregationHour);
            }
        });

        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");

                
                final Set<LoginAggregationImpl> loginAggregationsFiveMinute = loginAggregationDao.getLoginAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE);
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
                
                
                final Set<LoginAggregationImpl> loginAggregationsHour = loginAggregationDao.getLoginAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.HOUR);
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

                final LoginAggregationImpl loginAggregationFiveMinuteGroupA = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupA);
                final LoginAggregationImpl loginAggregationFiveMinuteGroupB = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE, groupB);
                final LoginAggregationImpl loginAggregationHour = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension, AggregationInterval.HOUR, groupA);
                
                loginAggregationFiveMinuteGroupA.intervalComplete(5);
                loginAggregationFiveMinuteGroupB.intervalComplete(5);
                loginAggregationHour.intervalComplete(60);
                
                loginAggregationDao.updateLoginAggregation(loginAggregationFiveMinuteGroupA);
                loginAggregationDao.updateLoginAggregation(loginAggregationFiveMinuteGroupB);
                loginAggregationDao.updateLoginAggregation(loginAggregationHour);
            }
        });

        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                final AggregatedGroupMapping groupA = aggregatedGroupLookupDao.getGroupMapping("local.0");
                
                final Set<LoginAggregationImpl> loginAggregationsFiveMinute = loginAggregationDao.getLoginAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.FIVE_MINUTE);
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
                
                
                final Set<LoginAggregationImpl> loginAggregationsHour = loginAggregationDao.getLoginAggregationsForInterval(dateDimension, timeDimension, AggregationInterval.HOUR);
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
                
                final List<LoginAggregation> loginAggregations = loginAggregationDao.getLoginAggregations(
                        instantDate.monthOfYear().roundFloorCopy(),
                        instantDate.monthOfYear().roundCeilingCopy(),
                        AggregationInterval.FIVE_MINUTE,
                        groupA);
                        
                        
                assertEquals(1, loginAggregations.size());
            }
        });
    }
}
