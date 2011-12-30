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
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang.time.DateUtils;
import org.jasig.portal.IPortalInfoProvider;
import org.jasig.portal.concurrency.FunctionWithoutResult;
import org.jasig.portal.concurrency.Time;
import org.jasig.portal.concurrency.locking.IClusterLockService;
import org.jasig.portal.concurrency.locking.IClusterLockService.TryLockFunctionResult;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.IEventAggregatorStatus.ProcessingType;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
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
    private static final String DIMENSION_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".DIMENSION_LOCK";
    private static final String AGGREGATION_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".AGGREGATION_LOCK";
    private static final String PURGE_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".PURGE_LOCK";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final AtomicBoolean checkedDimensions = new AtomicBoolean(false);
    
    private IEventAggregationManagementDao eventAggregationManagementDao;
    private IClusterLockService clusterLockService;
    private IPortalEventDao portalEventDao;
    private TimeDimensionDao timeDimensionDao;
    private DateDimensionDao dateDimensionDao;
    private IPortalInfoProvider portalInfoProvider;
    private Set<IPortalEventAggregator<PortalEvent>> portalEventAggregators;
    
    private Time aggregationDelay = Time.getTime(1, TimeUnit.MINUTES);
    private Time purgeDelay = Time.getTime(1, TimeUnit.DAYS);
    private Time dimensionPreloadBuffer = Time.getTime(30, TimeUnit.DAYS);

    @Autowired
    public void setTimeDimensionDao(TimeDimensionDao timeDimensionDao) {
        this.timeDimensionDao = timeDimensionDao;
    }

    @Autowired
    public void setDateDimensionDao(DateDimensionDao dateDimensionDao) {
        this.dateDimensionDao = dateDimensionDao;
    }

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
    
    @Value("${org.jasig.portal.event.aggr.PortalEventAggregationManager.dimensionPreloadBuffer:30_DAYS}")
    public void setDimensionPreloadBuffer(Time dimensionPreloadBuffer) {
        if (dimensionPreloadBuffer.asDays() < 1) {
            throw new IllegalArgumentException("dimensionPreloadBuffer must be at least 1 day. Is: " + dimensionPreloadBuffer);
        }
        this.dimensionPreloadBuffer = dimensionPreloadBuffer;
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public boolean populateDimensions() {
        try {
            final TryLockFunctionResult<Object> result = this.clusterLockService.doInTryLock(DIMENSION_LOCK_NAME, new FunctionWithoutResult<String>() {
                @Override
                protected void applyWithoutResult(String input) {
                    doPopulateDimensions();
                }
            });
            
            return result.isExecuted();
        }
        catch (InterruptedException e) {
            logger.warn("Interrupted while aggregating", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public boolean aggregateRawEvents() {
        try {
            final TryLockFunctionResult<Object> result = this.clusterLockService.doInTryLock(AGGREGATION_LOCK_NAME, new FunctionWithoutResult<String>() {
                @Override
                protected void applyWithoutResult(String input) {
                    doAggregateRawEvents();
                }
            });
            
            return result.isExecuted();
        }
        catch (InterruptedException e) {
            logger.warn("Interrupted while aggregating", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public boolean purgeRawEvents() {
        try {
            final TryLockFunctionResult<Object> result = this.clusterLockService.doInTryLock(PURGE_LOCK_NAME, new FunctionWithoutResult<String>() {
                @Override
                protected void applyWithoutResult(String input) {
                    doPurgeRawEvents();
                }
            });
            
            return result.isExecuted();
        }
        catch (InterruptedException e) {
            logger.warn("Interrupted while purging", e);
            Thread.currentThread().interrupt();
            return false;
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
    
    //use local flag to run on first call to doAggregation
    private void doPopulateDimensions() {
        if (!this.clusterLockService.isLockOwner(DIMENSION_LOCK_NAME)) {
            throw new IllegalStateException("Can only be called when this thread owns cluster lock: " + DIMENSION_LOCK_NAME);
        }
        
        final List<TimeDimension> timeDimensions = this.timeDimensionDao.getTimeDimensions();
        if (timeDimensions.size() != (24 * 60)) {
            this.logger.info("There are only " + timeDimensions.size() + " time dimensions in the database, there should be " + (24 * 60) + " creating missing dimensions");
            
            for (int hour = 0; hour <= 23; hour++) {
                for (int minute = 0; minute <= 59; minute++) {
                    //Create any missing time dimensions
                    final TimeDimension timeDimension = this.timeDimensionDao.getTimeDimensionByHourMinute(hour, minute);
                    if (timeDimension == null) {
                        this.timeDimensionDao.createTimeDimension(hour, minute);
                    }
                }
            }
            
        }
    
        /* this.dimensionPreloadBuffer
         * 
         * verify that all 24 * 60 time dimensions exist
         * get newest date dimension
         * if no date dimension get oldest and newest persistent events and create all dimensions in that range
         * get newest date dimension
         * create date dimensions until now + dimensionPreloadBuffer
         * 
         */
        
    }
    
    private void doAggregateRawEvents() {
        if (!this.clusterLockService.isLockOwner(AGGREGATION_LOCK_NAME)) {
            throw new IllegalStateException("Can only be called when this thread owns cluster lock: " + AGGREGATION_LOCK_NAME);
        }
        
        if (!this.checkedDimensions.get() && this.checkedDimensions.compareAndSet(false, true)) {
            //First time aggregation has happened, run populateDimensions to ensure enough dimension data exists
            final boolean populatedDimensions = this.populateDimensions();
            if (!populatedDimensions) {
                this.logger.warn("First time doAggregateRawEvents has run and populateDimensions returned false, assuming current dimension data is available");
            }
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
                
                /*
                 * TODO interval tracking
                 * 
     * If the next event crosses a time interval boundary, the boundary crossed along with its start and end
     * dates are passed. Called before {@link #doWrite(PortalEvent)} is called on the event that crosses
     * the boundary
    public void handleIntervalBoundry(Interval interval, Map<Interval, IntervalInfo> intervals);
                 */
                
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

    private void doPurgeRawEvents() {
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
