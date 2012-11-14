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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;

import org.jasig.portal.IPortalInfoProvider;
import org.jasig.portal.concurrency.locking.IClusterLockService;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.IEventAggregatorStatus.ProcessingType;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.events.aggr.session.EventSession;
import org.jasig.portal.events.aggr.session.EventSessionDao;
import org.jasig.portal.events.handlers.db.IPortalEventDao;
import org.jasig.portal.security.IPerson;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.base.Function;

@RunWith(MockitoJUnitRunner.class)
public class PortalRawEventsAggregatorImplTest {
    @InjectMocks private PortalRawEventsAggregatorImpl portalEventAggregator = new PortalRawEventsAggregatorImpl();
    @Mock private TransactionOperations transactionOperations;
    @Mock private IClusterLockService clusterLockService;
    @Mock private PortalEventDimensionPopulator portalEventDimensionPopulator;
    @Mock private EntityManager entityManager;
    @Mock private IEventAggregationManagementDao eventAggregationManagementDao;
    @Mock private IPortalInfoProvider portalInfoProvider;
    @Mock private IPortalEventDao portalEventDao;
    @Mock private EventSessionDao eventSessionDao;
    
    @Mock private IEventAggregatorStatus eventAggregatorStatus;
    @Mock private IPerson person;
    @Mock private EventSession eventSession;
    
    @Test
    public void aggregateRawEventsComplete()  throws Exception {
        when(transactionOperations.execute(any(TransactionCallback.class))).then(new Answer<EventProcessingResult>() {
            @Override
            public EventProcessingResult answer(InvocationOnMock invocation) throws Throwable {
                final TransactionStatus status = mock(TransactionStatus.class);
                return ((TransactionCallback<EventProcessingResult>)invocation.getArguments()[0]).doInTransaction(status);
            }
        });
        when(clusterLockService.isLockOwner(PortalRawEventsAggregator.AGGREGATION_LOCK_NAME)).thenReturn(true);
        when(portalEventDimensionPopulator.isCheckedDimensions()).thenReturn(true);
        when(eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION, true)).thenReturn(eventAggregatorStatus);
        when(portalInfoProvider.getUniqueServerName()).thenReturn("serverName_abcd");
        when(eventAggregatorStatus.getLastEventDate()).thenReturn(new DateTime(1325881376117l));
        when(portalEventDao.aggregatePortalEvents(any(DateTime.class), any(DateTime.class), (int)any(Integer.TYPE), (Function<PortalEvent, Boolean>)any(Function.class))).then(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return true;
            }
        });
        
        final EventProcessingResult result = portalEventAggregator.doAggregateRawEvents();
        assertNotNull(result);
        assertEquals(0, result.getProcessed());
        assertEquals(true, result.isComplete());
    }
    
    @Test
    public void aggregateRawEventsIncompleteByReturn()  throws Exception {
        when(transactionOperations.execute(any(TransactionCallback.class))).then(new Answer<EventProcessingResult>() {
            @Override
            public EventProcessingResult answer(InvocationOnMock invocation) throws Throwable {
                final TransactionStatus status = mock(TransactionStatus.class);
                return ((TransactionCallback<EventProcessingResult>)invocation.getArguments()[0]).doInTransaction(status);
            }
        });
        when(clusterLockService.isLockOwner(PortalRawEventsAggregator.AGGREGATION_LOCK_NAME)).thenReturn(true);
        when(portalEventDimensionPopulator.isCheckedDimensions()).thenReturn(true);
        when(eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION, true)).thenReturn(eventAggregatorStatus);
        when(portalInfoProvider.getUniqueServerName()).thenReturn("serverName_abcd");
        when(eventAggregatorStatus.getLastEventDate()).thenReturn(new DateTime(1325881376117l));
        when(portalEventDao.aggregatePortalEvents(any(DateTime.class), any(DateTime.class), (int)any(Integer.TYPE), (Function<PortalEvent, Boolean>)any(Function.class))).then(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return false;
            }
        });
        
        final EventProcessingResult result = portalEventAggregator.doAggregateRawEvents();
        assertNotNull(result);
        assertEquals(0, result.getProcessed());
        assertEquals(false, result.isComplete());
    }
    
    @Test
    public void aggregateRawEventsIncompleteByProcessCount()  throws Exception {
        when(transactionOperations.execute(any(TransactionCallback.class))).then(new Answer<EventProcessingResult>() {
            @Override
            public EventProcessingResult answer(InvocationOnMock invocation) throws Throwable {
                final TransactionStatus status = mock(TransactionStatus.class);
                return ((TransactionCallback<EventProcessingResult>)invocation.getArguments()[0]).doInTransaction(status);
            }
        });
        when(clusterLockService.isLockOwner(PortalRawEventsAggregator.AGGREGATION_LOCK_NAME)).thenReturn(true);
        when(portalEventDimensionPopulator.isCheckedDimensions()).thenReturn(true);
        when(eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION, true)).thenReturn(eventAggregatorStatus);
        when(portalInfoProvider.getUniqueServerName()).thenReturn("serverName_abcd");
        when(eventAggregatorStatus.getLastEventDate()).thenReturn(new DateTime(1325881376117l));
        when(portalEventDao.aggregatePortalEvents(any(DateTime.class), any(DateTime.class), (int)any(Integer.TYPE), (Function<PortalEvent, Boolean>)any(Function.class))).then(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                ((Function<PortalEvent, Boolean>)invocation.getArguments()[3]).apply(new MockPortalEvent(this, "serverName", "eventSessionId", person));
                
                return false;
            }
        });
        when(eventSessionDao.getEventSession(any(PortalEvent.class))).thenReturn(eventSession);
        
        this.portalEventAggregator.setEventAggregationBatchSize(1);
        final EventProcessingResult result = portalEventAggregator.doAggregateRawEvents();
        assertNotNull(result);
        assertEquals(1, result.getProcessed());
        assertEquals(false, result.isComplete());
        
        this.portalEventAggregator.setEventAggregationBatchSize(1000);
    }
    
    private static class MockPortalEvent extends PortalEvent {
        public MockPortalEvent(Object source, String serverName, String eventSessionId, IPerson person) {
            super(new MockPortalEventBuilder(source, serverName, eventSessionId, person));
        }
        
        private static class MockPortalEventBuilder extends PortalEventBuilder {
            public MockPortalEventBuilder(Object source, String serverName, String eventSessionId, IPerson person) {
                super(source, serverName, eventSessionId, person);
            }
        }
    }
}
