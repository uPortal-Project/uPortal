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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jasig.portal.concurrency.locking.IClusterLockService;
import org.junit.Ignore;
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

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class PortalRawEventsAggregatorImplTest {
    @InjectMocks private PortalRawEventsAggregatorImpl portalEventAggregator = new PortalRawEventsAggregatorImpl();
    @Mock private TransactionOperations transactionOperations;
    @Mock private IClusterLockService clusterLockService;
    @Mock private PortalEventDimensionPopulator portalEventDimensionPopulator;
    
    //TODO rename PortalEventAggregator ... IPortalEventAggregator already exists :(
    
    @Test
    public void aggregateRawEvents()  throws Exception {
        when(transactionOperations.execute(any(TransactionCallback.class))).then(new Answer<EventProcessingResult>() {
            @Override
            public EventProcessingResult answer(InvocationOnMock invocation) throws Throwable {
                final TransactionStatus status = mock(TransactionStatus.class);
                return ((TransactionCallback<EventProcessingResult>)invocation.getArguments()[0]).doInTransaction(status);
            }
        });
        when(clusterLockService.isLockOwner(PortalRawEventsAggregator.AGGREGATION_LOCK_NAME)).thenReturn(true);
        when(portalEventDimensionPopulator.isCheckedDimensions()).thenReturn(true);
        
        portalEventAggregator.doAggregateRawEvents();
    }
}
