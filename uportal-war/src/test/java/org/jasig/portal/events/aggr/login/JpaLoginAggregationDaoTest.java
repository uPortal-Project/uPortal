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

import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.Interval;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.jasig.portal.test.BaseJpaDaoTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

/**
 * TODO nuke all tables between test runs
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaStatsAggregationTestContext.xml")
public class JpaLoginAggregationDaoTest extends BaseJpaDaoTest {
    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    private EntityManager entityManager;
    @Autowired
    private LoginAggregationPrivateDao loginAggregationDao;
    @Autowired
    private TimeDimensionDao timeDimensionDao;
    @Autowired
    private DateDimensionDao dateDimensionDao;
    
    @Before
    public void setup() {
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                transactionOperations.execute(new TransactionCallbackWithoutResult(){
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        final Query deleteTimeDimensionsQuery = entityManager.createQuery("delete from org.jasig.portal.events.aggr.dao.jpa.TimeDimensionImpl");
                        deleteTimeDimensionsQuery.executeUpdate();
                        
                        final Query deleteDateDimensionsQuery = entityManager.createQuery("delete from org.jasig.portal.events.aggr.dao.jpa.DateDimensionImpl");
                        deleteDateDimensionsQuery.executeUpdate();
                    }
                });
            }
        });
    }
    
    @Test
    public void testLoginAggregationLifecycle() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(1326734644000l); //just a random time
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                dateDimensionDao.createDateDimension(calendar);
                timeDimensionDao.createTimeDimension(calendar);
            }
        });
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionForCalendar(calendar);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionForCalendar(calendar);
                
                final LoginAggregationImpl loginAggregation = loginAggregationDao.createLoginAggregation(dateDimension, timeDimension, Interval.FIVE_MINUTE, null);

                loginAggregation.countUser("joe");
                loginAggregation.countUser("john");
                loginAggregation.countUser("levi");
                loginAggregation.countUser("erin");
                loginAggregation.countUser("john");
                loginAggregation.setDuration(1);
                
                loginAggregationDao.updateLoginAggregation(loginAggregation);
            }
        });
        

        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionForCalendar(calendar);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionForCalendar(calendar);
                
                final LoginAggregationImpl loginAggregation = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension);
                
                assertEquals(5, loginAggregation.getLoginCount());
                assertEquals(4, loginAggregation.getUniqueLoginCount());
            }
        });
        

        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionForCalendar(calendar);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionForCalendar(calendar);
                
                final LoginAggregationImpl loginAggregation = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension);
                
                loginAggregation.countUser("john");
                loginAggregation.countUser("elvira");
                loginAggregation.countUser("levi");
                loginAggregation.countUser("erin");
                loginAggregation.countUser("gretchen");
                loginAggregation.setDuration(3);
                
                loginAggregationDao.updateLoginAggregation(loginAggregation);
            }
        });
        

        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionForCalendar(calendar);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionForCalendar(calendar);
                
                final LoginAggregationImpl loginAggregation = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension);
                
                assertEquals(10, loginAggregation.getLoginCount());
                assertEquals(6, loginAggregation.getUniqueLoginCount());
            }
        });
        

        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionForCalendar(calendar);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionForCalendar(calendar);
                
                final LoginAggregationImpl loginAggregation = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension);
                
                loginAggregation.setDuration(5);
                loginAggregation.intervalComplete();
                
                loginAggregationDao.updateLoginAggregation(loginAggregation);
            }
        });
        

        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final DateDimension dateDimension = dateDimensionDao.getDateDimensionForCalendar(calendar);
                final TimeDimension timeDimension = timeDimensionDao.getTimeDimensionForCalendar(calendar);
                
                final LoginAggregationImpl loginAggregation = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension);
                
                assertEquals(10, loginAggregation.getLoginCount());
                assertEquals(6, loginAggregation.getUniqueLoginCount());
            }
        });
    }
}
