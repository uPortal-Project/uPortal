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

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.Interval;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
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
    private LoginAggregationPrivateDao loginAggregationDao;
    @Autowired
    private TimeDimensionDao timeDimensionDao;
    @Autowired
    private DateDimensionDao dateDimensionDao;
    
    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    private EntityManager entityManager;
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    @Test
    public void testLoginAggregationLifecycle() {
        final DateTime instant = new DateTime(1326734644000l); //just a random time
        final DateMidnight instantDate = instant.toDateMidnight();
        final LocalTime instantTime = instant.toLocalTime();
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                dateDimensionDao.createDateDimension(instantDate);
                timeDimensionDao.createTimeDimension(instantTime);
            }
        });
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                
                final LoginAggregationImpl loginAggregationFiveMinute = loginAggregationDao.createLoginAggregation(dateDimension, timeDimension, Interval.FIVE_MINUTE, null);
                final LoginAggregationImpl loginAggregationFiveMinuteGroup = loginAggregationDao.createLoginAggregation(dateDimension, timeDimension, Interval.FIVE_MINUTE, "groupA");
                final LoginAggregationImpl loginAggregationHour = loginAggregationDao.createLoginAggregation(dateDimension, timeDimension, Interval.HOUR, null);

                loginAggregationFiveMinute.countUser("joe");
                loginAggregationFiveMinute.countUser("john");
                loginAggregationFiveMinute.countUser("levi");
                loginAggregationFiveMinute.countUser("erin");
                loginAggregationFiveMinute.countUser("john");
                loginAggregationFiveMinute.setDuration(1);
                
                loginAggregationFiveMinuteGroup.countUser("joe");
                loginAggregationFiveMinuteGroup.countUser("john");
                loginAggregationFiveMinuteGroup.setDuration(1);
                
                loginAggregationHour.countUser("joe");
                loginAggregationHour.countUser("john");
                loginAggregationHour.countUser("levi");
                loginAggregationHour.countUser("erin");
                loginAggregationHour.countUser("john");
                loginAggregationHour.setDuration(1);
                
                loginAggregationDao.updateLoginAggregation(loginAggregationFiveMinute);
                loginAggregationDao.updateLoginAggregation(loginAggregationHour);
            }
        });
        

        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                
                final Set<LoginAggregationImpl> loginAggregationsFiveMinute = loginAggregationDao.getLoginAggregationsForInterval(dateDimension, timeDimension, Interval.FIVE_MINUTE);
                assertEquals(2, loginAggregationsFiveMinute.size());
                
                for (final LoginAggregationImpl loginAggregation : loginAggregationsFiveMinute) {
                    if (loginAggregation.getGroupName() == null) {
                        assertEquals(5, loginAggregation.getLoginCount());
                        assertEquals(4, loginAggregation.getUniqueLoginCount());
                    }
                    else {
                        assertEquals(2, loginAggregation.getLoginCount());
                        assertEquals(2, loginAggregation.getUniqueLoginCount());
                    }
                }
                
                
                final Set<LoginAggregationImpl> loginAggregationsHour = loginAggregationDao.getLoginAggregationsForInterval(dateDimension, timeDimension, Interval.HOUR);
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
                
                final LoginAggregationImpl loginAggregationFiveMinute = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension, Interval.FIVE_MINUTE, null);
                final LoginAggregationImpl loginAggregationFiveMinuteGroup = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension, Interval.FIVE_MINUTE, "groupA");
                final LoginAggregationImpl loginAggregationHour = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension, Interval.HOUR, null);
                
                loginAggregationFiveMinute.countUser("john");
                loginAggregationFiveMinute.countUser("elvira");
                loginAggregationFiveMinute.countUser("levi");
                loginAggregationFiveMinute.countUser("gretchen");
                loginAggregationFiveMinute.countUser("erin");
                loginAggregationFiveMinute.setDuration(2);
                
                loginAggregationFiveMinuteGroup.countUser("gretchen");
                loginAggregationFiveMinuteGroup.setDuration(2);
                
                loginAggregationHour.countUser("john");
                loginAggregationHour.countUser("elvira");
                loginAggregationHour.countUser("levi");
                loginAggregationHour.countUser("gretchen");
                loginAggregationHour.countUser("erin");
                loginAggregationHour.setDuration(2);
                
                loginAggregationDao.updateLoginAggregation(loginAggregationFiveMinute);
                loginAggregationDao.updateLoginAggregation(loginAggregationFiveMinuteGroup);
                loginAggregationDao.updateLoginAggregation(loginAggregationHour);
            }
        });

        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                
                final Set<LoginAggregationImpl> loginAggregationsFiveMinute = loginAggregationDao.getLoginAggregationsForInterval(dateDimension, timeDimension, Interval.FIVE_MINUTE);
                assertEquals(2, loginAggregationsFiveMinute.size());
                
                for (final LoginAggregationImpl loginAggregation : loginAggregationsFiveMinute) {
                    if (loginAggregation.getGroupName() == null) {
                        assertEquals(10, loginAggregation.getLoginCount());
                        assertEquals(6, loginAggregation.getUniqueLoginCount());
                    }
                    else {
                        assertEquals(3, loginAggregation.getLoginCount());
                        assertEquals(3, loginAggregation.getUniqueLoginCount());
                    }
                }
                
                
                final Set<LoginAggregationImpl> loginAggregationsHour = loginAggregationDao.getLoginAggregationsForInterval(dateDimension, timeDimension, Interval.HOUR);
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
                
                final LoginAggregationImpl loginAggregationFiveMinute = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension, Interval.FIVE_MINUTE, null);
                final LoginAggregationImpl loginAggregationFiveMinuteGroup = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension, Interval.FIVE_MINUTE, "groupA");
                final LoginAggregationImpl loginAggregationHour = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension, Interval.HOUR, null);
                
                loginAggregationFiveMinute.intervalComplete(5);
                loginAggregationFiveMinuteGroup.intervalComplete(5);
                loginAggregationHour.intervalComplete(60);
                
                loginAggregationDao.updateLoginAggregation(loginAggregationFiveMinute);
                loginAggregationDao.updateLoginAggregation(loginAggregationFiveMinuteGroup);
                loginAggregationDao.updateLoginAggregation(loginAggregationHour);
            }
        });

        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionByDate(instantDate);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionByTime(instantTime);
                
                final Set<LoginAggregationImpl> loginAggregationsFiveMinute = loginAggregationDao.getLoginAggregationsForInterval(dateDimension, timeDimension, Interval.FIVE_MINUTE);
                assertEquals(2, loginAggregationsFiveMinute.size());
                
                for (final LoginAggregationImpl loginAggregation : loginAggregationsFiveMinute) {
                    if (loginAggregation.getGroupName() == null) {
                        assertEquals(10, loginAggregation.getLoginCount());
                        assertEquals(6, loginAggregation.getUniqueLoginCount());
                    }
                    else {
                        assertEquals(3, loginAggregation.getLoginCount());
                        assertEquals(3, loginAggregation.getUniqueLoginCount());
                    }
                }
                
                
                final Set<LoginAggregationImpl> loginAggregationsHour = loginAggregationDao.getLoginAggregationsForInterval(dateDimension, timeDimension, Interval.HOUR);
                assertEquals(1, loginAggregationsHour.size());
                
                final LoginAggregationImpl loginAggregation = loginAggregationsHour.iterator().next();
                assertEquals(10, loginAggregation.getLoginCount());
                assertEquals(6, loginAggregation.getUniqueLoginCount());
            }
        });
    }
}
