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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.jasig.portal.events.handlers.db.IPortalEventDao;
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
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaAggrEventsTestContext.xml")
public class PortalEventAggregationManagerImplTest extends BaseJpaDaoTest {
    private PortalEventAggregationManagerImpl portalEventAggregationManager = new PortalEventAggregationManagerImpl();
    private IPortalEventDao portalEventDao;

    @Autowired
    private DateDimensionDao dateDimensionDao;
    @Autowired
    private TimeDimensionDao timeDimensionDao;
    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    private EntityManager entityManager;
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    @Before
    public void setup() {
        portalEventDao = mock(IPortalEventDao.class);
        portalEventAggregationManager.setPortalEventDao(portalEventDao);
        portalEventAggregationManager.setDateDimensionDao(dateDimensionDao);
        portalEventAggregationManager.setTimeDimensionDao(timeDimensionDao);
        
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
    public void populateAllTimeDimensions() {
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<TimeDimension> timeDimensions = timeDimensionDao.getTimeDimensions();
                assertEquals(Collections.EMPTY_LIST, timeDimensions);
            }
        });
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                portalEventAggregationManager.doPopulateTimeDimensions();
            }
        });
     
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<TimeDimension> timeDimensions = timeDimensionDao.getTimeDimensions();
                assertEquals(60*24, timeDimensions.size());
            }
        });
    }

    
    @Test
    public void populateSomeTimeDimensions() {
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                timeDimensionDao.createTimeDimension(0, 1);
                timeDimensionDao.createTimeDimension(0, 2);
                timeDimensionDao.createTimeDimension(0, 3);
                timeDimensionDao.createTimeDimension(0, 4);
                timeDimensionDao.createTimeDimension(0, 7);
                timeDimensionDao.createTimeDimension(0, 8);
                timeDimensionDao.createTimeDimension(0, 9);
                timeDimensionDao.createTimeDimension(1, 23);
                timeDimensionDao.createTimeDimension(23, 58);
            }
        });
                
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<TimeDimension> timeDimensions = timeDimensionDao.getTimeDimensions();
                assertEquals(9, timeDimensions.size());
            }
        });
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                portalEventAggregationManager.doPopulateTimeDimensions();
            }
        });
     
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<TimeDimension> timeDimensions = timeDimensionDao.getTimeDimensions();
                assertEquals(60*24, timeDimensions.size());
            }
        });
    }

    
    @Test
    public void populateDefaultDateDimensions() {
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<DateDimension> dateDimensions = dateDimensionDao.getDateDimensions();
                assertEquals(Collections.EMPTY_LIST, dateDimensions);
            }
        });
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                portalEventAggregationManager.doPopulateDateDimensions();
            }
        });
     
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<DateDimension> dateDimensions = dateDimensionDao.getDateDimensions();
                assertEquals(32, dateDimensions.size());
            }
        });
        
        when(portalEventDao.getOldestPortalEventTimestamp()).thenReturn(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(12)));
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                portalEventAggregationManager.doPopulateDateDimensions();
            }
        });
     
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<DateDimension> dateDimensions = dateDimensionDao.getDateDimensions();
                assertEquals(44, dateDimensions.size());
            }
        });
        
        when(portalEventDao.getNewestPortalEventTimestamp()).thenReturn(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(12)));
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                portalEventAggregationManager.doPopulateDateDimensions();
            }
        });
     
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final List<DateDimension> dateDimensions = dateDimensionDao.getDateDimensions();
                assertEquals(56, dateDimensions.size());
            }
        });
    }
}
