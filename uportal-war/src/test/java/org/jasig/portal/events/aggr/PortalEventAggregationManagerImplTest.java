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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.IPortalInfoProvider;
import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.concurrency.FunctionWithoutResult;
import org.jasig.portal.concurrency.locking.IClusterLockService;
import org.jasig.portal.concurrency.locking.IClusterLockService.TryLockFunctionResult;
import org.jasig.portal.events.aggr.IEventAggregatorStatus.ProcessingType;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.jasig.portal.events.handlers.db.IPortalEventDao;
import org.jasig.portal.test.BaseJpaDaoTest;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Function;

/**
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaAggrEventsTestContext.xml")
public class PortalEventAggregationManagerImplTest extends BaseJpaDaoTest {
    private PortalEventAggregationManagerImpl portalEventAggregationManager;
    private IPortalEventDao portalEventDao;
    private IClusterLockService clusterLockService;
    private IEventAggregationManagementDao eventAggregationManagementDao;
    private IPortalInfoProvider portalInfoProvider;

    @Autowired
    private DateDimensionDao dateDimensionDao;
    @Autowired
    private TimeDimensionDao timeDimensionDao;
    @Autowired
    private AggregationIntervalHelper intervalHelper;
    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Before
    public void setup() {
        portalEventDao = mock(IPortalEventDao.class);
        clusterLockService = mock(IClusterLockService.class);
        eventAggregationManagementDao = mock(IEventAggregationManagementDao.class);
        portalInfoProvider = mock(IPortalInfoProvider.class);

        portalEventAggregationManager = new PortalEventAggregationManagerImpl() {
            @Override
            DateTime getNow() {
                return new DateTime(1325881376117l);
            }
        };
        
        when(eventAggregationManagementDao.getQuartersDetails()).thenReturn(EventDateTimeUtils.createStandardQuarters());
        
        portalEventAggregationManager.setPortalEventDao(portalEventDao);
        portalEventAggregationManager.setDateDimensionDao(dateDimensionDao);
        portalEventAggregationManager.setTimeDimensionDao(timeDimensionDao);
        portalEventAggregationManager.setIntervalHelper(intervalHelper);
        portalEventAggregationManager.setClusterLockService(clusterLockService);
        portalEventAggregationManager.setEventAggregationManagementDao(eventAggregationManagementDao);
        //        portalEventAggregationManager.setPortalEventAggregators(null);
        portalEventAggregationManager.setPortalInfoProvider(portalInfoProvider);
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
                assertEquals(60 * 24, timeDimensions.size());
            }
        });
    }

    @Test
    public void populateSomeTimeDimensions() {
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                timeDimensionDao.createTimeDimension(new LocalTime(0, 1));
                timeDimensionDao.createTimeDimension(new LocalTime(0, 2));
                timeDimensionDao.createTimeDimension(new LocalTime(0, 3));
                timeDimensionDao.createTimeDimension(new LocalTime(0, 4));
                timeDimensionDao.createTimeDimension(new LocalTime(0, 7));
                timeDimensionDao.createTimeDimension(new LocalTime(0, 8));
                timeDimensionDao.createTimeDimension(new LocalTime(0, 9));
                timeDimensionDao.createTimeDimension(new LocalTime(1, 23));
                timeDimensionDao.createTimeDimension(new LocalTime(23, 58));
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
                assertEquals(60 * 24, timeDimensions.size());
            }
        });
    }

    @Test
    public void populateDefaultDateDimensions() {
        final DateTime now = new DateTime(1325881376117l);
        
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
                assertEquals(731, dateDimensions.size());
                final DateDimension oldestDateDimension = dateDimensionDao.getOldestDateDimension();
                assertEquals(new DateMidnight(2011, 1, 1), oldestDateDimension.getDate());
                final DateDimension newestDateDimension = dateDimensionDao.getNewestDateDimension();
                assertEquals(new DateMidnight(2012, 12, 31), newestDateDimension.getDate());
            }
        });

        when(portalEventDao.getOldestPortalEventTimestamp()).thenReturn(now.minusYears(1));

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
                assertEquals(1096, dateDimensions.size());
                final DateDimension oldestDateDimension = dateDimensionDao.getOldestDateDimension();
                assertEquals(new DateMidnight(2010, 1, 1), oldestDateDimension.getDate());
                final DateDimension newestDateDimension = dateDimensionDao.getNewestDateDimension();
                assertEquals(new DateMidnight(2012, 12, 31), newestDateDimension.getDate());
            }
        });

        when(portalEventDao.getNewestPortalEventTimestamp()).thenReturn(now.plusYears(1));

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
                assertEquals(1461, dateDimensions.size());
                final DateDimension oldestDateDimension = dateDimensionDao.getOldestDateDimension();
                assertEquals(new DateMidnight(2010, 01, 01), oldestDateDimension.getDate());
                final DateDimension newestDateDimension = dateDimensionDao.getNewestDateDimension();
                assertEquals(new DateMidnight(2013, 12, 31), newestDateDimension.getDate());
            }
        });
    }

    @Test
    public void aggregateRawEvents()  throws Exception {
        final TryLockFunctionResult<?> tryLockFunctionResult = mock(TryLockFunctionResult.class);
        when(this.clusterLockService.doInTryLock(Mockito.anyString(), Mockito.any(Function.class))).thenReturn(tryLockFunctionResult);
        
        final IEventAggregatorStatus eventAggregatorStatus = mock(IEventAggregatorStatus.class);
        when(this.eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION, true)).thenReturn(eventAggregatorStatus);
        
        when(this.portalInfoProvider.getServerName()).thenReturn("TEST_SERVER_NAME");
        
        this.executeInTransaction(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                portalEventAggregationManager.doAggregateRawEvents();
            }
        });
        
        verify(portalEventDao).aggregatePortalEvents(Mockito.any(DateTime.class), Mockito.any(DateTime.class), Mockito.eq(5000), Mockito.any(FunctionWithoutResult.class));
    }
}
