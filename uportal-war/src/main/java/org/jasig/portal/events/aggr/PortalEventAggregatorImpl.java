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

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jasig.portal.concurrency.locking.IClusterLockService;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.handlers.db.IPortalEventDao;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalEventAggregatorImpl {
    private static final String AGGREGATION_LOCK_NAME = PortalEventAggregatorImpl.class.getName() + ".AGGREGATION_LOCK";
    private static final String PURGE_LOCK_NAME = PortalEventAggregatorImpl.class.getName() + ".PURGE_LOCK";
    
    private IClusterLockService clusterLockService;
    private IPortalEventDao portalEventDao;
    private Set<IPortalEventAggregator<PortalEvent>> portalEventAggregators;
    
    private long aggregationDelay = TimeUnit.MINUTES.toMillis(1);
    private long purgeDelay = TimeUnit.DAYS.toMillis(1);
    
    /*
     * TODO need aggregation mgmt DAO to track last aggregated event time
     * create PortalEventAggregationStatus bean
     *      Date lastAggrEvent
     *      String lastAggrHost
     * create PortalEventPurgeStatus bean
     *      Date lastPurgeEvent
     *      String lastPurgeHost
     */
    
    @Transactional(value="aggrrawEventsTransactionManager")
    public void aggregateStatistics() {
        this.clusterLockService.doInTryLock(AGGREGATION_LOCK_NAME, new Function<String, Object>() {
            @Override
            public Object apply(String input) {
                doAggregation();
                return null;
            }
        });
    }
    
    public void purgeStatistics() {
        this.clusterLockService.doInTryLock(PURGE_LOCK_NAME, new Function<String, Object>() {
            @Override
            public Object apply(String input) {
                final Date lastAggregated = new Date(0); //TODO get from aggrMgmtDao
                
                final Date purgeStart = new Date(lastAggregated.getTime() - purgeDelay);
                portalEventDao.deletePortalEvents(new Date(0), purgeStart);
                
                return null;
            }
        });
    }
    
    private void doAggregation() {
        final Date lastAggregated = new Date(0); //TODO get from aggrMgmtDao
        final Date endTime = new Date(System.currentTimeMillis() - this.aggregationDelay);
        
        portalEventDao.getPortalEvents(lastAggregated, endTime, new Function<PortalEvent, Object>() {
            @Override
            public Object apply(PortalEvent input) {
                for (final IPortalEventAggregator<PortalEvent> portalEventAggregator : portalEventAggregators) {
                    if (supportsEvent(portalEventAggregator, input.getClass())) {
                        portalEventAggregator.aggregateEvent(input);
                    }
                }
                
                return null;
            }
        });
    }
    
    
    protected boolean supportsEvent(IPortalEventAggregator<PortalEvent> portalEventAggregator, Class<? extends PortalEvent> eventType) {
        Class<?> typeArg = GenericTypeResolver.resolveTypeArgument(portalEventAggregator.getClass(), IPortalEventAggregator.class);
        if (typeArg == null || typeArg.equals(ApplicationEvent.class)) {
            Class<?> targetClass = AopUtils.getTargetClass(portalEventAggregator);
            if (targetClass != portalEventAggregator.getClass()) {
                typeArg = GenericTypeResolver.resolveTypeArgument(targetClass, ApplicationListener.class);
            }
        }
        return (typeArg == null || typeArg.isAssignableFrom(eventType));
    }
}
