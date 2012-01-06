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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
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
    void doPopulateDimensions() {
        doPopulateTimeDimensions();
        doPopulateDateDimensions();
    }

    /**
     * Populate the time dimensions 
     */
    void doPopulateTimeDimensions() {
        final List<TimeDimension> timeDimensions = this.timeDimensionDao.getTimeDimensions();
        if (timeDimensions.isEmpty()) {
            logger.info("No TimeDimensions exist, creating them");
            //Create all time dimension 
            for (int hour = 0; hour <= 23; hour++) {
                for (int minute = 0; minute <= 59; minute++) {
                    this.timeDimensionDao.createTimeDimension(hour, minute);
                }
            }
        }
        else if (timeDimensions.size() != (24 * 60)) {
            this.logger.info("There are only " + timeDimensions.size() + " time dimensions in the database, there should be " + (24 * 60) + " creating missing dimensions");
            
            Calendar nextCal = Calendar.getInstance();
            nextCal.setLenient(false);
            nextCal.clear();
            nextCal.set(Calendar.HOUR_OF_DAY, 0);
            nextCal.set(Calendar.MINUTE, 0);
            
            for (final TimeDimension timeDimension : timeDimensions) {
                final Calendar tdCal = timeDimension.getCalendar();
                if (nextCal.before(tdCal)) {
                    do {
                        this.timeDimensionDao.createTimeDimension(nextCal.get(Calendar.HOUR_OF_DAY), nextCal.get(Calendar.MINUTE));
                        nextCal.add(Calendar.MINUTE, 1);
                    } while (nextCal.before(tdCal));
                }
                else if (nextCal.after(tdCal)) {
                    do {
                        this.timeDimensionDao.createTimeDimension(tdCal.get(Calendar.HOUR_OF_DAY), tdCal.get(Calendar.MINUTE));
                        tdCal.add(Calendar.MINUTE, 1);
                    } while (nextCal.after(tdCal));
                }
                
                nextCal = tdCal;
                nextCal.add(Calendar.MINUTE, 1);
            }
            
            //Add any missing calendars from the tail
            final Calendar lastCal = Calendar.getInstance();
            lastCal.setLenient(false);
            lastCal.clear();
            lastCal.set(Calendar.HOUR_OF_DAY, 23);
            lastCal.set(Calendar.MINUTE, 59);
            
            while (nextCal.before(lastCal) || nextCal.equals(lastCal)) {
                this.timeDimensionDao.createTimeDimension(nextCal.get(Calendar.HOUR_OF_DAY), nextCal.get(Calendar.MINUTE));
                nextCal.add(Calendar.MINUTE, 1);
            }
        }
        else {
            this.logger.debug("Found expected " + timeDimensions.size() + " time dimensions");
        }
    }

    void doPopulateDateDimensions() {
        final Calendar now = Calendar.getInstance();
        
        final Calendar minEventDate = Calendar.getInstance();
        minEventDate.setLenient(false);
        minEventDate.clear();
        
        final Calendar maxEventDate = Calendar.getInstance();
        maxEventDate.setLenient(false);
        maxEventDate.clear();

        // min(oldestPortalEventTimestamp - 1, now -1)
        final Date oldestPortalEventTimestamp = this.portalEventDao.getOldestPortalEventTimestamp();
        if (oldestPortalEventTimestamp == null || oldestPortalEventTimestamp.getTime() >= now.getTimeInMillis()) {
            //no portal events or oldest event is after now, start at now - 1 day
            minEventDate.set(Calendar.YEAR, now.get(Calendar.YEAR));
            minEventDate.set(Calendar.MONTH, now.get(Calendar.MONTH));
            minEventDate.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
            minEventDate.add(Calendar.DAY_OF_MONTH, -1);
        }
        else {
            //portal events exist, start at oldest event - 1 day
            final Calendar oldestEvent = Calendar.getInstance();
            oldestEvent.setTime(oldestPortalEventTimestamp);
            
            minEventDate.set(Calendar.YEAR, oldestEvent.get(Calendar.YEAR));
            minEventDate.set(Calendar.MONTH, oldestEvent.get(Calendar.MONTH));
            minEventDate.set(Calendar.DAY_OF_MONTH, oldestEvent.get(Calendar.DAY_OF_MONTH));
            minEventDate.add(Calendar.DAY_OF_MONTH, -1);
        }
        
        //max(newestPortalEventTimestamp + dimensionPreloadBuffer, now + dimensionPreloadBuffer)
        final Date newestPortalEventTimestamp = this.portalEventDao.getNewestPortalEventTimestamp();
        if (newestPortalEventTimestamp == null || newestPortalEventTimestamp.getTime() <= now.getTimeInMillis()) {
            //no portal events or newest event is before now, end at now + dimensionPreloadBuffer
            maxEventDate.set(Calendar.YEAR, now.get(Calendar.YEAR));
            maxEventDate.set(Calendar.MONTH, now.get(Calendar.MONTH));
            maxEventDate.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
            maxEventDate.add(Calendar.DAY_OF_MONTH, (int)this.dimensionPreloadBuffer.asDays());
        }
        else {
            //portal events exist, end at newest event + dimensionPreloadBuffer
            final Calendar oldestEvent = Calendar.getInstance();
            oldestEvent.setTime(newestPortalEventTimestamp);
            
            maxEventDate.set(Calendar.YEAR, oldestEvent.get(Calendar.YEAR));
            maxEventDate.set(Calendar.MONTH, oldestEvent.get(Calendar.MONTH));
            maxEventDate.set(Calendar.DAY_OF_MONTH, oldestEvent.get(Calendar.DAY_OF_MONTH));
            maxEventDate.add(Calendar.DAY_OF_MONTH, (int)this.dimensionPreloadBuffer.asDays());
        }
        
        final DateDimension oldestDateDimension = this.dateDimensionDao.getOldestDateDimension();
        if (oldestDateDimension == null) {
            //No date dimensions, create from minEventDate to maxEventDate
            doPopulateDateDimensions(minEventDate, maxEventDate);
        }
        else {
            final Calendar oldestDimensionCal = oldestDateDimension.getCalendar();
            if (oldestDimensionCal.after(minEventDate)) {
                //the oldest event is after the oldest date dimension, create date dimensions to compensate
                oldestDimensionCal.add(Calendar.DAY_OF_MONTH, -1);
                doPopulateDateDimensions(minEventDate, oldestDimensionCal);
            }
            
            final DateDimension newestDateDimension = this.dateDimensionDao.getNewestDateDimension();
            final Calendar newestDimensionCal = newestDateDimension.getCalendar();
            if (newestDimensionCal.before(maxEventDate)) {
                //the newest dimension is before either now or the newest event plus the dimensionPreloadBuffer, create date dimensions to pad
                newestDimensionCal.add(Calendar.DAY_OF_MONTH, 1);
                doPopulateDateDimensions(newestDimensionCal, maxEventDate);
            }
        }
    }
    
    void doPopulateDateDimensions(Calendar start, Calendar end) {
        //don't assume we can modify the caller's object
        start = (Calendar)start.clone();
        
        logger.info("Creating date dimensions from " + start.getTime() + " to " + end.getTime() + "  (inclusive)");
        
        while (start.before(end) || start.equals(end)) {
            this.dateDimensionDao.createDateDimension(start);
            start.add(Calendar.DAY_OF_MONTH, 1);
        }
    }
    
    void doAggregateRawEvents() {
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
        
        final Map<Interval, IntervalInfo> intervalInfo = new HashMap<Interval, IntervalInfo>();

        //Do aggregation, capturing the start and end dates
        eventAggregatorStatus.setLastStart(new Date());
        portalEventDao.getPortalEvents(lastAggregated, newestEventTime, new Function<PortalEvent, Object>() {
            @Override
            public Object apply(PortalEvent input) {
                events.increment();
                
                
                
                /*
                 * TODO interval tracking
                 *
                 * https://source.jasig.org/sandbox/StatsAggregator/trunk/stats-aggr/src/main/java/org/jasig/portal/stats/item/write/BaseEventWriter.java
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

    void doPurgeRawEvents() {
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
