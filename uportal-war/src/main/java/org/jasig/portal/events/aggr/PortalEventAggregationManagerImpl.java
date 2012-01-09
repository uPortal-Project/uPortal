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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.mutable.MutableInt;
import org.jasig.portal.IPortalInfoProvider;
import org.jasig.portal.concurrency.FunctionWithoutResult;
import org.jasig.portal.concurrency.locking.IClusterLockService;
import org.jasig.portal.concurrency.locking.IClusterLockService.TryLockFunctionResult;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.IEventAggregatorStatus.ProcessingType;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.jasig.portal.events.handlers.db.IPortalEventDao;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

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
    private IntervalHelper intervalHelper;
    private IPortalInfoProvider portalInfoProvider;
    private Set<IPortalEventAggregator<PortalEvent>> portalEventAggregators;
    private TransactionOperations aggrEventsTransactionOperations;
    
    private int eventAggregationBatchSize = 5000;
    private ReadablePeriod aggregationDelay = Period.seconds(30);
    private ReadablePeriod purgeDelay = Period.days(1);
    private ReadablePeriod dimensionBuffer = Period.days(30);
    
    @Autowired
    public void setAggrEventsPlatformTransactionManager(@Qualifier("aggrEvents") PlatformTransactionManager transactionManager) {
        final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.afterPropertiesSet();
        this.aggrEventsTransactionOperations = transactionTemplate;
    }

    @Autowired
    public void setIntervalHelper(IntervalHelper intervalHelper) {
        this.intervalHelper = intervalHelper;
    }

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

    @Value("${org.jasig.portal.event.aggr.PortalEventAggregationManager.aggregationDelay:PT30S}")
    public void setAggregationDelay(ReadablePeriod aggregationDelay) {
        this.aggregationDelay = aggregationDelay;
    }

    @Value("${org.jasig.portal.event.aggr.PortalEventAggregationManager.purgeDelay:P1D}")
    public void setPurgeDelay(ReadablePeriod purgeDelay) {
        this.purgeDelay = purgeDelay;
    }
    
    @Value("${org.jasig.portal.event.aggr.PortalEventAggregationManager.eventAggregationBatchSize:5}")
    public void setEventAggregationBatchSize(int eventAggregationBatchSize) {
        this.eventAggregationBatchSize = eventAggregationBatchSize;
    }

    @Value("${org.jasig.portal.event.aggr.PortalEventAggregationManager.dimensionBuffer:P30D}")
    public void setDimensionPreloadBuffer(ReadablePeriod dimensionBuffer) {
        if (new Period(dimensionBuffer).toStandardDays().getDays() < 1) {
            throw new IllegalArgumentException("dimensionBuffer must be at least 1 day. Is: " + new Period(dimensionBuffer).toStandardDays().getDays());
        }
        this.dimensionBuffer = dimensionBuffer;
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
    public boolean aggregateRawEvents() {
        TryLockFunctionResult<Boolean> result = null;
        do {
            if (result != null) {
                logger.debug("doAggregateRawEvents signaled that not all events were aggregated in a single transaction, running again.");
            }
            
            result = aggrEventsTransactionOperations.execute(new TransactionCallback<TryLockFunctionResult<Boolean>>() {
                @Override
                public TryLockFunctionResult<Boolean> doInTransaction(TransactionStatus status) {
                    try {
                        return clusterLockService.doInTryLock(AGGREGATION_LOCK_NAME, new Function<String, Boolean>() {
                            @Override
                            public Boolean apply(String input) {
                                return doAggregateRawEvents();
                            }
                        });
                    }
                    catch (InterruptedException e) {
                        logger.warn("Interrupted while aggregating", e);
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            });
            
            //Loop if doAggregateRawEvents returns false, this means that there is more to aggregate 
        } while (result != null && result.isExecuted() && !result.getResult());
        
        return result != null && result.isExecuted();
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
        }
        else if (timeDimensions.size() != (24 * 60)) {
            this.logger.info("There are only " + timeDimensions.size() + " time dimensions in the database, there should be " + (24 * 60) + " creating missing dimensions");
        }
        else {
            this.logger.debug("Found expected " + timeDimensions.size() + " time dimensions");
            return;
        }
            
        LocalTime nextTime = new LocalTime(0, 0);
        final LocalTime lastTime = new LocalTime(23, 59);
        
        for (final TimeDimension timeDimension : timeDimensions) {
            LocalTime dimensionTime = timeDimension.getTime();
            if (nextTime.isBefore(dimensionTime)) {
                do {
                    this.timeDimensionDao.createTimeDimension(nextTime);
                    nextTime = nextTime.plusMinutes(1);
                } while (nextTime.isBefore(dimensionTime));
            }
            else if (nextTime.isAfter(dimensionTime)) {
                do {
                    this.timeDimensionDao.createTimeDimension(dimensionTime);
                    dimensionTime = dimensionTime.plusMinutes(1);
                } while (nextTime.isAfter(dimensionTime));
            }
            
            nextTime = dimensionTime.plusMinutes(1);
        }
        
        //Add any missing times from the tail
        while (nextTime.isBefore(lastTime) || nextTime.equals(lastTime)) {
            this.timeDimensionDao.createTimeDimension(nextTime);
            if (nextTime.equals(lastTime)) {
                break;
            }
            nextTime = nextTime.plusMinutes(1);
        }
    }

    void doPopulateDateDimensions() {
        final DateTime now = getNow();
        
        final IntervalInfo startIntervalInfo;
        final DateTime oldestPortalEventTimestamp = this.portalEventDao.getOldestPortalEventTimestamp();
        if (oldestPortalEventTimestamp == null || now.isBefore(oldestPortalEventTimestamp)) {
            startIntervalInfo = this.intervalHelper.getIntervalInfo(Interval.YEAR, now.minus(this.dimensionBuffer));
        }
        else {
            startIntervalInfo = this.intervalHelper.getIntervalInfo(Interval.YEAR, oldestPortalEventTimestamp.minus(this.dimensionBuffer));
        }
        
        final IntervalInfo endIntervalInfo;
        final DateTime newestPortalEventTimestamp = this.portalEventDao.getNewestPortalEventTimestamp();
        if (newestPortalEventTimestamp == null || now.isAfter(newestPortalEventTimestamp)) {
            endIntervalInfo = this.intervalHelper.getIntervalInfo(Interval.YEAR, now.plus(this.dimensionBuffer));
        }
        else {
            endIntervalInfo = this.intervalHelper.getIntervalInfo(Interval.YEAR, newestPortalEventTimestamp.plus(this.dimensionBuffer));
        }
        
        final DateMidnight start = startIntervalInfo.getStart().toDateMidnight();
        final DateMidnight end = endIntervalInfo.getEnd().toDateMidnight();
        
        doPopulateDateDimensions(start, end);
    }

    /**
     * Exists to make this class testable
     */
    DateTime getNow() {
        return DateTime.now();
    }
    
    void doPopulateDateDimensions(final DateMidnight start, final DateMidnight end) {
        final List<DateDimension> dateDimensions = this.dateDimensionDao.getDateDimensionsBetween(start, end);
        
        DateMidnight nextDate = start;
        for (final DateDimension dateDimension : dateDimensions) {
            DateMidnight dimensionDate = dateDimension.getFullDate();
            if (nextDate.isBefore(dimensionDate)) {
                do {
                    this.dateDimensionDao.createDateDimension(nextDate);
                    nextDate = nextDate.plusDays(1);
                } while (nextDate.isBefore(dimensionDate));
            }
            else if (nextDate.isAfter(dimensionDate)) {
                do {
                    this.dateDimensionDao.createDateDimension(dimensionDate);
                    dimensionDate = dimensionDate.plusDays(1);
                } while (nextDate.isAfter(dimensionDate));
            }
            
            nextDate = dimensionDate.plusDays(1);
        }
        
        //Add any missing dates from the tail
        while (nextDate.isBefore(end)) {
            this.dateDimensionDao.createDateDimension(nextDate);
            nextDate = nextDate.plusDays(1);
        }
    }
    
    /**
     * @return true if all events for the time period were aggregated, false if not
     */
    boolean doAggregateRawEvents() {
        if (!this.checkedDimensions.get() && this.checkedDimensions.compareAndSet(false, true)) {
            //First time aggregation has happened, run populateDimensions to ensure enough dimension data exists
            final boolean populatedDimensions = this.populateDimensions();
            if (!populatedDimensions) {
                this.logger.warn("First time doAggregateRawEvents has run and populateDimensions returned false, assuming current dimension data is available");
            }
        }

        IEventAggregatorStatus eventAggregatorStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION);
        if (eventAggregatorStatus == null) {
            eventAggregatorStatus = eventAggregationManagementDao.createEventAggregatorStatus(ProcessingType.AGGREGATION);
        }
        
        //Update status with current server name
        final String serverName = this.portalInfoProvider.getServerName();
        eventAggregatorStatus.setServerName(serverName);
        
        //Calculate date range for aggregation
        DateTime lastAggregated = eventAggregatorStatus.getLastEventDate();
        if (lastAggregated == null) {
            lastAggregated = new DateTime(0);
        }
        
        DateTime newestEventTime = DateTime.now().minus(this.aggregationDelay).secondOfMinute().roundFloorCopy();
        
        logger.debug("Starting aggregation of events between {} (inc) and {} (exc)", lastAggregated, newestEventTime);
        final MutableInt events = new MutableInt();
        
        //Do aggregation, capturing the start and end dates
        eventAggregatorStatus.setLastStart(DateTime.now());
        final long start = System.nanoTime();
        portalEventDao.getPortalEvents(lastAggregated, newestEventTime, this.eventAggregationBatchSize, new AggregateEventsHandler(events, eventAggregatorStatus));
        eventAggregatorStatus.setLastEnd(new DateTime());
        
        logger.debug("Aggregated {} events between {} and {} in {}ms", new Object[] { events, lastAggregated, newestEventTime, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) });

        //Store the results of the aggregation
        eventAggregationManagementDao.updateEventAggregatorStatus(eventAggregatorStatus);
        
        return this.eventAggregationBatchSize <= 0 || events.intValue() < this.eventAggregationBatchSize;
    }

    void doPurgeRawEvents() {
        IEventAggregatorStatus eventPurgerStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.PURGING);
        if (eventPurgerStatus == null) {
            eventPurgerStatus = eventAggregationManagementDao.createEventAggregatorStatus(ProcessingType.PURGING);
        }
        
        
        //Update status with current server name
        final String serverName = this.portalInfoProvider.getServerName();
        eventPurgerStatus.setServerName(serverName);
        eventPurgerStatus.setLastStart(new DateTime());
        
        //Determine date of most recently aggregated data
        final IEventAggregatorStatus eventAggregatorStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION);
        if (eventAggregatorStatus == null || eventAggregatorStatus.getLastEventDate() == null) {
            //Nothing has been aggregated, skip purging
            
            eventPurgerStatus.setLastEnd(new DateTime());
            eventAggregationManagementDao.updateEventAggregatorStatus(eventPurgerStatus);
            
            return;
        }
        
        //Calculate purge end date from most recent aggregation minus the purge delay
        final DateTime lastAggregated = eventAggregatorStatus.getLastEventDate();
        final DateTime purgeEnd = lastAggregated.minus(this.purgeDelay);
        
        //Purge events
        logger.debug("Starting purge of events before {}", purgeEnd);
        final long start = System.nanoTime();
        final int events = portalEventDao.deletePortalEventsBefore(purgeEnd);
        logger.debug("Purged {} events before {} in {}ms", new Object[] {events, purgeEnd, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) });
        
        //Update the status object and store it
        eventPurgerStatus.setLastEventDate(purgeEnd.minusMillis(1)); //decrement by 1ms since deletePortalEventsBefore uses lessThan and not lessThanEqualTo 
        eventPurgerStatus.setLastEnd(new DateTime());
        eventAggregationManagementDao.updateEventAggregatorStatus(eventPurgerStatus);
    }
    
    private final class AggregateEventsHandler extends FunctionWithoutResult<PortalEvent> {
        private final MutableInt eventCounter;
        private final IEventAggregatorStatus eventAggregatorStatus;
        
        private final Map<Interval, IntervalInfo> currentIntervalInfo = new HashMap<Interval, IntervalInfo>();
        
        private AggregateEventsHandler(MutableInt eventCounter, IEventAggregatorStatus eventAggregatorStatus) {
            this.eventCounter = eventCounter;
            this.eventAggregatorStatus = eventAggregatorStatus;
        }

        @Override
        protected void applyWithoutResult(PortalEvent event) {
            final DateTime lastEventDate = eventAggregatorStatus.getLastEventDate();
            
            //Handle crossing interval boundaries
            if (lastEventDate != null) {
                if (this.currentIntervalInfo.isEmpty()) {
                    //If first execution with a lastEventDate populate the current IntervalInfo for that previous date
                    for (final Interval interval : Interval.values()) {
                        final IntervalInfo intervalInfo = intervalHelper.getIntervalInfo(interval, lastEventDate);
                        this.currentIntervalInfo.put(interval, intervalInfo);
                    }
                }
                
                for (final Interval interval : Interval.values()) {
                    IntervalInfo intervalInfo = this.currentIntervalInfo.get(interval);
                    if (!intervalInfo.getEnd().isAfter(lastEventDate)) {
                        logger.debug("Crossing {} Interval, triggerd by {}", interval, event);
                        this.doHandleIntervalBoundry(interval, this.currentIntervalInfo);
                        
                        intervalInfo = intervalHelper.getIntervalInfo(interval, lastEventDate); 
                        this.currentIntervalInfo.put(interval, intervalInfo);
                    }
                }
            }
            
            //Aggregate the event
            this.doAggregateEvent(event);
            
            //Update the status object with the event date
            eventAggregatorStatus.setLastEventDate(event.getTimestampAsDate());
        }

        private void doAggregateEvent(PortalEvent item) {
            eventCounter.increment();
            for (final IPortalEventAggregator<PortalEvent> portalEventAggregator : portalEventAggregators) {
                if (supportsEvent(portalEventAggregator, item.getClass())) {
                    portalEventAggregator.aggregateEvent(item);
                }
            }
        }
        
        private void doHandleIntervalBoundry(Interval interval, Map<Interval, IntervalInfo> intervals) {
            for (final IPortalEventAggregator<PortalEvent> portalEventAggregator : portalEventAggregators) {
                portalEventAggregator.handleIntervalBoundry(interval, intervals);
            }
        }
    }
}
