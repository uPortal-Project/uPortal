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

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang.time.DateUtils;
import org.jasig.portal.IPortalInfoProvider;
import org.jasig.portal.concurrency.Time;
import org.jasig.portal.concurrency.locking.IClusterLockService;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.IEventAggregatorStatus.ProcessingType;
import org.jasig.portal.events.handlers.db.IPortalEventDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portalEventAggregationManager")
public class PortalEventAggregationManagerImpl implements IPortalEventAggregationManager {
    private static final String AGGREGATION_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".AGGREGATION_LOCK";
    private static final String PURGE_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".PURGE_LOCK";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private IEventAggregationManagementDao eventAggregationManagementDao;
    private IClusterLockService clusterLockService;
    private IPortalEventDao portalEventDao;
    private IPortalInfoProvider portalInfoProvider;
    private Set<IPortalEventAggregator<PortalEvent>> portalEventAggregators;
    
    private Time aggregationDelay = Time.getTime(1, TimeUnit.MINUTES);
    private Time purgeDelay = Time.getTime(1, TimeUnit.DAYS);
    
    @Autowired
    public void setPortalInfoProvider(IPortalInfoProvider portalInfoProvider) {
        this.portalInfoProvider = portalInfoProvider;
    }

    @Autowired
    public void setEventAggregationManagementDao(IEventAggregationManagementDao eventAggregationManagementDao) {
        this.eventAggregationManagementDao = eventAggregationManagementDao;
    }

    @Autowired
    public void setClusterLockService(IClusterLockService clusterLockService) {
        this.clusterLockService = clusterLockService;
    }

    @Autowired
    public void setPortalEventDao(IPortalEventDao portalEventDao) {
        this.portalEventDao = portalEventDao;
    }

    @Autowired
    public void setPortalEventAggregators(Set<IPortalEventAggregator<PortalEvent>> portalEventAggregators) {
        this.portalEventAggregators = portalEventAggregators;
    }

    @Value("${org.jasig.portal.event.aggr.PortalEventAggregationManager.aggregationDelay:1_MINUTES}")
    public void setAggregationDelay(Time aggregationDelay) {
        this.aggregationDelay = aggregationDelay;
    }

    @Value("${org.jasig.portal.event.aggr.PortalEventAggregationManager.purgeDelay:1_DAYS}")
    public void setPurgeDelay(Time purgeDelay) {
        this.purgeDelay = purgeDelay;
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public void aggregateRawEvents() {
        try {
            this.clusterLockService.doInTryLock(AGGREGATION_LOCK_NAME, new Function<String, Object>() {
                @Override
                public Object apply(String input) {
                    doAggregation();
                    return null;
                }
            });
        }
        catch (InterruptedException e) {
            logger.warn("Interrupted while aggregating", e);
            Thread.currentThread().interrupt();
            return;
        }
    }
    
    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public void purgeRawEvents() {
        try {
            this.clusterLockService.doInTryLock(PURGE_LOCK_NAME, new Function<String, Object>() {
                @Override
                public Object apply(String input) {
                    doPurge();
                    return null;
                }
            });
        }
        catch (InterruptedException e) {
            logger.warn("Interrupted while purging", e);
            Thread.currentThread().interrupt();
            return;
        }
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
    
    private void doAggregation() {
        if (!this.clusterLockService.isLockOwner(AGGREGATION_LOCK_NAME)) {
            throw new IllegalStateException("Can only be called when this thread owns cluster lock: " + AGGREGATION_LOCK_NAME);
        }

        final IEventAggregatorStatus eventAggregatorStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION);
        
        //Update status with current server name
        final String serverName = this.portalInfoProvider.getServerName();
        eventAggregatorStatus.setServerName(serverName);
        
        //Calculate date range for aggregation
        Date lastAggregated = eventAggregatorStatus.getLastEventDate();
        if (lastAggregated == null) {
            lastAggregated = new Date(0);
        }
        final Date newestEventTime = DateUtils.truncate(new Date(System.currentTimeMillis() - this.aggregationDelay.asMillis()), Calendar.MINUTE);
        eventAggregatorStatus.setLastEventDate(newestEventTime);
        
        logger.debug("Starting aggregation of events between {} (inc) and {} (exc)", lastAggregated, newestEventTime);
        final MutableInt events = new MutableInt();

        //Do aggregation, capturing the start and end dates
        eventAggregatorStatus.setLastStart(new Date());
        portalEventDao.getPortalEvents(lastAggregated, newestEventTime, new Function<PortalEvent, Object>() {
            @Override
            public Object apply(PortalEvent input) {
                events.increment();
                
                for (final IPortalEventAggregator<PortalEvent> portalEventAggregator : portalEventAggregators) {
                    if (supportsEvent(portalEventAggregator, input.getClass())) {
                        portalEventAggregator.aggregateEvent(input);
                    }
                }
                
                return null;
            }
        });
        eventAggregatorStatus.setLastEnd(new Date());
        
        logger.debug("Aggregated {} events between {} and {}", new Object[] { events, lastAggregated, newestEventTime });

        //Store the results of the aggregation
        eventAggregationManagementDao.updateEventAggregatorStatus(eventAggregatorStatus);
    }

    private void doPurge() {
        if (!this.clusterLockService.isLockOwner(PURGE_LOCK_NAME)) {
            throw new IllegalStateException("Can only be called when this thread owns cluster lock: " + PURGE_LOCK_NAME);
        }
        
        final IEventAggregatorStatus eventPurgerStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.PURGING);
        
        //Update status with current server name
        final String serverName = this.portalInfoProvider.getServerName();
        eventPurgerStatus.setServerName(serverName);
        eventPurgerStatus.setLastStart(new Date());
        
        //Determine date of most recently aggregated data
        final IEventAggregatorStatus eventAggregatorStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION);
        final Date lastAggregated = eventAggregatorStatus.getLastEventDate();
        if (lastAggregated == null) {
            //Nothing has been aggregated, skip purging
            
            eventPurgerStatus.setLastEnd(new Date());
            eventAggregationManagementDao.updateEventAggregatorStatus(eventPurgerStatus);
            
            return;
        }
        
        //Calculate purge end date from most recent aggregation minus the purge delay
        final Date purgeEnd = new Date(lastAggregated.getTime() - purgeDelay.asMillis());
        eventPurgerStatus.setLastEventDate(purgeEnd);
        
        //Purge events
        logger.debug("Starting purge of events before {}", purgeEnd);
        final int events = portalEventDao.deletePortalEventsBefore(purgeEnd);
        logger.debug("Purged {} events before {}", events, purgeEnd);
        
        //Update the status object and store it
        eventPurgerStatus.setLastEnd(new Date());
        eventAggregationManagementDao.updateEventAggregatorStatus(eventPurgerStatus);
    }
}
