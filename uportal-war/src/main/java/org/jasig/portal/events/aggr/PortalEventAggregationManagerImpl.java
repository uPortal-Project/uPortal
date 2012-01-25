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

import java.util.Collections;
import java.util.EnumMap;
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
import org.jasig.portal.events.LoginEvent;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.IEventAggregatorStatus.ProcessingType;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.jasig.portal.events.aggr.session.EventSession;
import org.jasig.portal.events.aggr.session.EventSessionDao;
import org.jasig.portal.events.handlers.db.IPortalEventDao;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portalEventAggregationManager")
public class PortalEventAggregationManagerImpl implements IPortalEventAggregationManager {
    private static final String DIMENSION_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".DIMENSION_LOCK";
    private static final String AGGREGATION_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".AGGREGATION_LOCK";
    private static final String PURGE_RAW_EVENTS_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".PURGE_RAW_EVENTS_LOCK";
    private static final String PURGE_EVENT_SESSION_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".PURGE_EVENT_SESSION_LOCK_NAME";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final AtomicBoolean checkedDimensions = new AtomicBoolean(false);
    
    private IPortalInfoProvider portalInfoProvider;
    private IClusterLockService clusterLockService;
    private IEventAggregationManagementDao eventAggregationManagementDao;
    private IPortalEventDao portalEventDao;
    private TimeDimensionDao timeDimensionDao;
    private DateDimensionDao dateDimensionDao;
    private AggregationIntervalHelper intervalHelper;
    private EventSessionDao eventSessionDao;
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
    public void setEventSessionDao(EventSessionDao eventSessionDao) {
        this.eventSessionDao = eventSessionDao;
    }

    @Autowired
    public void setIntervalHelper(AggregationIntervalHelper intervalHelper) {
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
    
    @Value("${org.jasig.portal.event.aggr.PortalEventAggregationManager.eventAggregationBatchSize:5000}")
    public void setEventAggregationBatchSize(int eventAggregationBatchSize) {
        this.eventAggregationBatchSize = eventAggregationBatchSize;
    }

    @Value("${org.jasig.portal.event.aggr.PortalEventAggregationManager.dimensionBuffer:P30D}")
    public void setDimensionBuffer(ReadablePeriod dimensionBuffer) {
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
            final TryLockFunctionResult<Object> result = this.clusterLockService.doInTryLock(PURGE_RAW_EVENTS_LOCK_NAME, new FunctionWithoutResult<String>() {
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
    
    @Override
    @Transactional(value="aggrEventsTransactionManager")
    public boolean purgeEventSessions() {
        try {
            final TryLockFunctionResult<Object> result = this.clusterLockService.doInTryLock(PURGE_EVENT_SESSION_LOCK_NAME, new FunctionWithoutResult<String>() {
                @Override
                protected void applyWithoutResult(String input) {
                    eventSessionDao.purgeExpiredEventSessions();
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
        
        final AggregationIntervalInfo startIntervalInfo;
        final DateTime oldestPortalEventTimestamp = this.portalEventDao.getOldestPortalEventTimestamp();
        if (oldestPortalEventTimestamp == null || now.isBefore(oldestPortalEventTimestamp)) {
            startIntervalInfo = this.intervalHelper.getIntervalInfo(AggregationInterval.YEAR, now.minus(this.dimensionBuffer));
        }
        else {
            startIntervalInfo = this.intervalHelper.getIntervalInfo(AggregationInterval.YEAR, oldestPortalEventTimestamp.minus(this.dimensionBuffer));
        }
        
        final AggregationIntervalInfo endIntervalInfo;
        final DateTime newestPortalEventTimestamp = this.portalEventDao.getNewestPortalEventTimestamp();
        if (newestPortalEventTimestamp == null || now.isAfter(newestPortalEventTimestamp)) {
            endIntervalInfo = this.intervalHelper.getIntervalInfo(AggregationInterval.YEAR, now.plus(this.dimensionBuffer));
        }
        else {
            endIntervalInfo = this.intervalHelper.getIntervalInfo(AggregationInterval.YEAR, newestPortalEventTimestamp.plus(this.dimensionBuffer));
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
        logger.info("Populating DateDimensions between {} and {}", start, end);
        
        final List<QuarterDetail> quartersDetails = this.eventAggregationManagementDao.getQuartersDetails();
        final List<AcademicTermDetail> academicTermDetails = this.eventAggregationManagementDao.getAcademicTermDetails();
        
        final List<DateDimension> dateDimensions = this.dateDimensionDao.getDateDimensionsBetween(start, end);
        
        DateMidnight nextDate = start;
        for (final DateDimension dateDimension : dateDimensions) {
            DateMidnight dimensionDate = dateDimension.getDate();
            if (nextDate.isBefore(dimensionDate)) {
                do {
                    createDateDimension(quartersDetails, academicTermDetails, nextDate);
                    nextDate = nextDate.plusDays(1);
                } while (nextDate.isBefore(dimensionDate));
            }
            else if (nextDate.isAfter(dimensionDate)) {
                do {
                    createDateDimension(quartersDetails, academicTermDetails, dimensionDate);
                    dimensionDate = dimensionDate.plusDays(1);
                } while (nextDate.isAfter(dimensionDate));
            }
            
            nextDate = dimensionDate.plusDays(1);
        }
        
        //Add any missing dates from the tail
        while (nextDate.isBefore(end)) {
            createDateDimension(quartersDetails, academicTermDetails, nextDate);
            nextDate = nextDate.plusDays(1);
        }
    }

    /**
     * Creates a date dimension, handling the quarter and term lookup logic
     */
    protected void createDateDimension(
            List<QuarterDetail> quartersDetails,
            List<AcademicTermDetail> academicTermDetails, 
            DateMidnight date) {

        final QuarterDetail quarterDetail = EventDateTimeUtils.findDateRangeSorted(date, quartersDetails);
        final AcademicTermDetail termDetail = EventDateTimeUtils.findDateRangeSorted(date, academicTermDetails);
        this.dateDimensionDao.createDateDimension(date, quarterDetail.getQuarterId(), termDetail != null ? termDetail.getTermName() : null);
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

        final IEventAggregatorStatus eventAggregatorStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION, true);
        
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
        portalEventDao.aggregatePortalEvents(lastAggregated, newestEventTime, this.eventAggregationBatchSize, new AggregateEventsHandler(events, eventAggregatorStatus));
        eventAggregatorStatus.setLastEnd(new DateTime());
        
        logger.debug("Aggregated {} events between {} and {} in {}ms", new Object[] { events, lastAggregated, newestEventTime, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) });

        //Store the results of the aggregation
        eventAggregationManagementDao.updateEventAggregatorStatus(eventAggregatorStatus);
        
        return this.eventAggregationBatchSize <= 0 || events.intValue() < this.eventAggregationBatchSize;
    }

    void doPurgeRawEvents() {
        final IEventAggregatorStatus eventPurgerStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.PURGING, true);
        
        //Update status with current server name
        final String serverName = this.portalInfoProvider.getServerName();
        eventPurgerStatus.setServerName(serverName);
        eventPurgerStatus.setLastStart(new DateTime());
        
        //Determine date of most recently aggregated data
        final IEventAggregatorStatus eventAggregatorStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION, false);
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
        
        private final Map<AggregationInterval, AggregationIntervalInfo> currentIntervalInfo = new EnumMap<AggregationInterval, AggregationIntervalInfo>(AggregationInterval.class);
        private final Map<AggregationInterval, AggregationIntervalInfo> readOnlyIntervalInfo = Collections.unmodifiableMap(currentIntervalInfo);

        //Local caches of aggregator config data, shouldn't ever change for the duration of an aggregation run
        private final Map<Class<? extends IPortalEventAggregator>, AggregatedGroupConfig> aggregatorGroupConfigs = new HashMap<Class<? extends IPortalEventAggregator>, AggregatedGroupConfig>();
        private final Map<Class<? extends IPortalEventAggregator>, AggregatedIntervalConfig> aggregatorIntervalConfigs = new HashMap<Class<? extends IPortalEventAggregator>, AggregatedIntervalConfig>();
        private final Map<Class<? extends IPortalEventAggregator>, Map<AggregationInterval, AggregationIntervalInfo>> aggregatorReadOnlyIntervalInfo = new HashMap<Class<? extends IPortalEventAggregator>, Map<AggregationInterval,AggregationIntervalInfo>>();
        private final AggregatedGroupConfig defaultAggregatedGroupConfig;
        private final AggregatedIntervalConfig defaultAggregatedIntervalConfig;
        
        
        private AggregateEventsHandler(MutableInt eventCounter, IEventAggregatorStatus eventAggregatorStatus) {
            this.eventCounter = eventCounter;
            this.eventAggregatorStatus = eventAggregatorStatus;
            this.defaultAggregatedGroupConfig = eventAggregationManagementDao.getDefaultAggregatedGroupConfig();
            this.defaultAggregatedIntervalConfig = eventAggregationManagementDao.getDefaultAggregatedIntervalConfig();
        }

        @Override
        protected void applyWithoutResult(PortalEvent event) {
            final DateTime eventDate = event.getTimestampAsDate();
            
            //If no interval data yet populate it.
            if (this.currentIntervalInfo.isEmpty()) {
                final DateTime intervalDate;
                final DateTime lastEventDate = eventAggregatorStatus.getLastEventDate();
                if (lastEventDate != null) {
                    //If there was a previously aggregated event use that date to make sure an interval is not missed
                    intervalDate = lastEventDate;
                }
                else {
                    //Otherwise just use the current event date
                    intervalDate = eventDate;
                }
                
                for (final AggregationInterval interval : AggregationInterval.values()) {
                    final AggregationIntervalInfo intervalInfo = intervalHelper.getIntervalInfo(interval, intervalDate);
                    if (intervalInfo != null) {
                        this.currentIntervalInfo.put(interval, intervalInfo);
                    }
                    else {
                        this.currentIntervalInfo.remove(interval);
                    }
                }
            }
            
            //Check each interval to see if an interval boundary has been crossed
            for (final AggregationInterval interval : AggregationInterval.values()) {
                AggregationIntervalInfo intervalInfo = this.currentIntervalInfo.get(interval);
                if (intervalInfo != null && !intervalInfo.getEnd().isAfter(eventDate)) { //if there is no IntervalInfo that interval must not be supported in the current environment 
                    logger.debug("Crossing {} Interval, triggerd by {}", interval, event);
                    this.doHandleIntervalBoundary(interval, this.currentIntervalInfo);
                    
                    intervalInfo = intervalHelper.getIntervalInfo(interval, eventDate); 
                    this.currentIntervalInfo.put(interval, intervalInfo);
                }
            }
            
            //Aggregate the event
            this.doAggregateEvent(event);
            
            //Update the status object with the event date
            eventAggregatorStatus.setLastEventDate(eventDate);
        }
        

        private void doAggregateEvent(PortalEvent item) {
            eventCounter.increment();
            
            //Load or create the event session
            EventSession eventSession;
            if (item instanceof LoginEvent) {
                eventSession = eventSessionDao.createEventSession((LoginEvent)item);
            }
            else {
                eventSession = eventSessionDao.getEventSession(item.getEventSessionId());
            }
            
            //Give each aggregator a chance at the event
            for (final IPortalEventAggregator<PortalEvent> portalEventAggregator : portalEventAggregators) {
                if (portalEventAggregator.supports(item.getClass())) {
                    final Class<? extends IPortalEventAggregator> aggregatorType = portalEventAggregator.getClass();
                    
                    //Get aggregator specific interval info map
                    final Map<AggregationInterval, AggregationIntervalInfo> aggregatorIntervalInfo = this.getAggregatorIntervalInfo(aggregatorType);
                    
                    //If there is an event session get the aggregator specific version of it
                    if (eventSession != null) {
                        final AggregatedGroupConfig aggregatorGroupConfig = getAggregatorGroupConfig(aggregatorType);
                        eventSession = eventSession.getFilteredEventSession(aggregatorGroupConfig);
                    }
                    
                    //Aggregation magic happens here!
                    portalEventAggregator.aggregateEvent(item, eventSession, aggregatorIntervalInfo);
                }
            }
        }
        
        private void doHandleIntervalBoundary(AggregationInterval interval, Map<AggregationInterval, AggregationIntervalInfo> intervals) {
            for (final IPortalEventAggregator<PortalEvent> portalEventAggregator : portalEventAggregators) {
                
                final Class<? extends IPortalEventAggregator> aggregatorType = portalEventAggregator.getClass();
                final AggregatedIntervalConfig aggregatorIntervalConfig = this.getAggregatorIntervalConfig(aggregatorType);
                
                //If the aggreagator is configured to use the interval notify it of the interval boundary
                if (aggregatorIntervalConfig.isIncluded(interval)) {
                    final Map<AggregationInterval, AggregationIntervalInfo> aggregatorIntervalInfo = this.getAggregatorIntervalInfo(aggregatorType);
                    portalEventAggregator.handleIntervalBoundary(interval, aggregatorIntervalInfo);
                }
            }
        }
        
        /**
         * @return The interval info map for the aggregator
         */
        protected Map<AggregationInterval, AggregationIntervalInfo> getAggregatorIntervalInfo(final Class<? extends IPortalEventAggregator> aggregatorType) {
            final AggregatedIntervalConfig aggregatorIntervalConfig = this.getAggregatorIntervalConfig(aggregatorType);
            
            Map<AggregationInterval, AggregationIntervalInfo> intervalInfo = this.aggregatorReadOnlyIntervalInfo.get(aggregatorType);
            if (intervalInfo == null) {
                intervalInfo = Maps.filterKeys(this.readOnlyIntervalInfo, new Predicate<AggregationInterval>() {
                    @Override
                    public boolean apply(AggregationInterval input) {
                        return aggregatorIntervalConfig.isIncluded(input);
                    }
                });
            }
            
            return intervalInfo;
        }

        /**
         * @return The group config for the aggregator, returns the default config if no aggregator specific config is set
         */
        protected AggregatedGroupConfig getAggregatorGroupConfig(final Class<? extends IPortalEventAggregator> aggregatorType) {
            AggregatedGroupConfig config = this.aggregatorGroupConfigs.get(aggregatorType);
            if (config == null) {
                config = eventAggregationManagementDao.getAggregatedGroupConfig(aggregatorType);
                if (config == null) {
                    config = this.defaultAggregatedGroupConfig;
                }
                this.aggregatorGroupConfigs.put(aggregatorType, config);
            }
            return config;
        }
        
        /**
         * @return The interval config for the aggregator, returns the default config if no aggregator specific config is set
         */
        protected AggregatedIntervalConfig getAggregatorIntervalConfig(final Class<? extends IPortalEventAggregator> aggregatorType) {
            AggregatedIntervalConfig config = this.aggregatorIntervalConfigs.get(aggregatorType);
            if (config == null) {
                config = eventAggregationManagementDao.getAggregatedIntervalConfig(aggregatorType);
                if (config == null) {
                    config = this.defaultAggregatedIntervalConfig;
                }
                this.aggregatorIntervalConfigs.put(aggregatorType, config);
            }
            return config;
        }
    }
}
