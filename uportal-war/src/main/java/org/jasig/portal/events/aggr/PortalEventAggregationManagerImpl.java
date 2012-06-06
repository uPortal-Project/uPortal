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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.persistence.FlushModeType;

import org.apache.commons.lang.mutable.MutableInt;
import org.hibernate.Session;
import org.jasig.portal.IPortalInfoProvider;
import org.jasig.portal.concurrency.FunctionWithoutResult;
import org.jasig.portal.concurrency.locking.ClusterMutex;
import org.jasig.portal.concurrency.locking.IClusterLockService;
import org.jasig.portal.concurrency.locking.IClusterLockService.TryLockFunctionResult;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.IEventAggregatorStatus.ProcessingType;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.jasig.portal.events.aggr.session.EventSession;
import org.jasig.portal.events.aggr.session.EventSessionDao;
import org.jasig.portal.events.handlers.db.IPortalEventDao;
import org.jasig.portal.jpa.BaseAggrEventsJpaDao;
import org.jasig.portal.jpa.BaseRawEventsJpaDao;
import org.jasig.portal.spring.context.ApplicationEventFilter;
import org.jasig.portal.utils.cache.CacheKey;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portalEventAggregationManager")
public class PortalEventAggregationManagerImpl extends BaseAggrEventsJpaDao implements IPortalEventAggregationManager, DisposableBean {
    private static final String EVENT_SESSION_CACHE_KEY_SOURCE = AggregateEventsHandler.class.getName() + "-EventSession";
    
    private static final String DIMENSION_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".DIMENSION_LOCK";
    private static final String AGGREGATION_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".AGGREGATION_LOCK";
    private static final String PURGE_RAW_EVENTS_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".PURGE_RAW_EVENTS_LOCK";
    private static final String PURGE_EVENT_SESSION_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".PURGE_EVENT_SESSION_LOCK_NAME";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private volatile boolean checkedDimensions = false;
    private volatile boolean shutdown = false;
    
    private TransactionOperations rawEventsTransactionOperations;
    private IPortalInfoProvider portalInfoProvider;
    private IClusterLockService clusterLockService;
    private IEventAggregationManagementDao eventAggregationManagementDao;
    private IPortalEventDao portalEventDao;
    private TimeDimensionDao timeDimensionDao;
    private DateDimensionDao dateDimensionDao;
    private AggregationIntervalHelper intervalHelper;
    private EventSessionDao eventSessionDao;
    private Set<IPortalEventAggregator<PortalEvent>> portalEventAggregators = Collections.emptySet();
    private List<ApplicationEventFilter<PortalEvent>> applicationEventFilters = Collections.emptyList();
    
    private int eventAggregationBatchSize = 10000;
    private ReadablePeriod aggregationDelay = Period.seconds(30);
    private ReadablePeriod purgeDelay = Period.days(1);
    private ReadablePeriod dimensionBuffer = Period.days(30);
    

    @Autowired
    @Qualifier(BaseRawEventsJpaDao.PERSISTENCE_UNIT_NAME)
    public final void setRawEventsTransactionOperations(TransactionOperations rawEventsTransactionOperations) {
        this.rawEventsTransactionOperations = rawEventsTransactionOperations;
    }
    
    /**
     * @param applicationEventFilters The list of filters to test each event with
     */
    @Resource(name="aggregatorEventFilters")
    public void setApplicationEventFilters(List<ApplicationEventFilter<PortalEvent>> applicationEventFilters) {
        applicationEventFilters = new ArrayList<ApplicationEventFilter<PortalEvent>>(applicationEventFilters);
        Collections.sort(applicationEventFilters, OrderComparator.INSTANCE);
        this.applicationEventFilters = ImmutableList.copyOf(applicationEventFilters);
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
    
    @Value("${org.jasig.portal.event.aggr.PortalEventAggregationManager.eventAggregationBatchSize:10000}")
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
    public void destroy() throws Exception {
        this.shutdown = true;
    }

    @Override
    public boolean populateDimensions() {
        if (shutdown) {
            logger.warn("populateDimensions called after shutdown, ignoring call");
            return false;
        }
        
        try {
            final TryLockFunctionResult<Object> result = this.clusterLockService.doInTryLock(DIMENSION_LOCK_NAME, new FunctionWithoutResult<ClusterMutex>() {
                @Override
                protected void applyWithoutResult(ClusterMutex input) {
                    getTransactionOperations().execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                            doPopulateDimensions();
                        }
                    });
                }
            });
            
            return result.isExecuted();
        }
        catch (InterruptedException e) {
            logger.warn("Interrupted while populating dimensions", e);
            Thread.currentThread().interrupt();
            return false;
        }
        catch (RuntimeException e) {
            logger.error("populateDimensions failed", e);
            throw e;
        }
    }

    @Override
    public boolean aggregateRawEvents() {
        //TODO eventually consider JTA/XA for this http://docs.codehaus.org/display/BTM/Home
        if (shutdown) {
            logger.warn("aggregateRawEvents called after shutdown, ignoring call");
            return false;
        }
        
        TryLockFunctionResult<Boolean> result = null;
        do {
            if (result != null) {
                logger.debug("doAggregateRawEvents signaled that not all events were aggregated in a single transaction, running again.");
            }
            
            try {
                result = clusterLockService.doInTryLock(AGGREGATION_LOCK_NAME, new Function<ClusterMutex, Boolean>() {
                    @Override
                    public Boolean apply(final ClusterMutex input) {
                        //Executing within lock
                        final long start = System.nanoTime();
                        
                        //Do RawTX around AggrTX. The AggrTX is MUCH more likely to fail than the RawTX and this results in both rolling back
                        final EventProcessingResult result = rawEventsTransactionOperations.execute(new TransactionCallback<EventProcessingResult>() {
                            @Override
                            public EventProcessingResult doInTransaction(TransactionStatus status) {
                                return getTransactionOperations().execute(new TransactionCallback<EventProcessingResult>() {
                                        @Override
                                        public EventProcessingResult doInTransaction(TransactionStatus status) {
                                            return doAggregateRawEvents();
                                        }
                                    });
                            }
                        });
                        
                        if (result == null) {
                            logger.warn("doAggregateRawEvents did not execute");
                            return null;
                        }
                        
                        if (logger.isInfoEnabled()) {
                            final long runTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                            logger.info("Aggregated {} events between {} and {} in {}ms - {} events/second", 
                                    new Object[] { result.eventCount, result.start, result.end, runTime, result.eventCount/(runTime/1000d) });
                        }
                        
                        return result.complete;
                    }
                });
            }
            catch (InterruptedException e) {
                logger.warn("Interrupted while aggregating", e);
                Thread.currentThread().interrupt();
                return false;
            }
            catch (RuntimeException e) {
                logger.error("aggregateRawEvents failed", e);
                throw e;
            }
            
        //Loop if doAggregateRawEvents returns false, this means that there is more to aggregate 
        } while (result != null && result.isExecuted() && !result.getResult());
        
        return result != null && result.isExecuted();
    }
    
    @Override
    public boolean purgeRawEvents() {
        if (shutdown) {
            logger.warn("purgeRawEvents called after shutdown, ignoring call");
            return false;
        }
        
        try {
            final TryLockFunctionResult<Object> result = this.clusterLockService.doInTryLock(PURGE_RAW_EVENTS_LOCK_NAME, new FunctionWithoutResult<ClusterMutex>() {
                @Override
                protected void applyWithoutResult(ClusterMutex input) {
                    final long start = System.nanoTime();
                    
                    final EventProcessingResult result = getTransactionOperations().execute(new TransactionCallback<EventProcessingResult>() {
                        @Override
                        public EventProcessingResult doInTransaction(TransactionStatus status) {
                            return doPurgeRawEvents();
                        }
                    });
                    
                    if (logger.isInfoEnabled()) {
                        final long runTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                        logger.info("Purged {} events before {} in {}ms - {} events/second", 
                                new Object[] { result.eventCount, result.end, runTime, result.eventCount/(runTime/1000d) });
                    }
                }
            });
            
            return result.isExecuted();
        }
        catch (InterruptedException e) {
            logger.warn("Interrupted while purging raw events", e);
            Thread.currentThread().interrupt();
            return false;
        }
        catch (RuntimeException e) {
            logger.error("purgeRawEvents failed", e);
            throw e;
        }
    }
    
    @Override
    public boolean purgeEventSessions() {
        if (shutdown) {
            logger.warn("purgeEventSessions called after shutdown, ignoring call");
            return false;
        }
        
        try {
            final TryLockFunctionResult<Object> result = this.clusterLockService.doInTryLock(PURGE_EVENT_SESSION_LOCK_NAME, new FunctionWithoutResult<ClusterMutex>() {
                @Override
                protected void applyWithoutResult(ClusterMutex input) {
                    getTransactionOperations().execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                            final IEventAggregatorStatus eventAggregatorStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION, false);
                            if (eventAggregatorStatus != null) {
                                final long start = System.nanoTime();
                                
                                final DateTime lastEventDate = eventAggregatorStatus.getLastEventDate();
                                if (lastEventDate != null) {
                                    final int purgeCount = eventSessionDao.purgeExpiredEventSessions(lastEventDate);
                                    
                                    if (logger.isInfoEnabled()) {
                                        final long runTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                                        logger.info("Purged {} event sessions before {} in {}ms - {} sessions/second", 
                                                new Object[] { purgeCount, lastEventDate, runTime, purgeCount/(runTime/1000d) });
                                    }
                                }
                            }
                        }
                    });
                }
            });
            
            return result.isExecuted();
        }
        catch (InterruptedException e) {
            logger.warn("Interrupted while purging event sessions", e);
            Thread.currentThread().interrupt();
            return false;
        }
        catch (RuntimeException e) {
            logger.error("purgeEventSessions failed", e);
            throw e;
        }
        finally {
        }
    }

    private void checkShutdown() {
        if (shutdown) {
            //Mark ourselves as interupted and throw an exception
            Thread.currentThread().interrupt();
            throw new RuntimeException("uPortal is shutting down, throwing an exeption to stop processing");
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
                    checkShutdown();
                    this.timeDimensionDao.createTimeDimension(nextTime);
                    nextTime = nextTime.plusMinutes(1);
                } while (nextTime.isBefore(dimensionTime));
            }
            else if (nextTime.isAfter(dimensionTime)) {
                do {
                    checkShutdown();
                    this.timeDimensionDao.createTimeDimension(dimensionTime);
                    dimensionTime = dimensionTime.plusMinutes(1);
                } while (nextTime.isAfter(dimensionTime));
            }
            
            nextTime = dimensionTime.plusMinutes(1);
        }
        
        //Add any missing times from the tail
        while (nextTime.isBefore(lastTime) || nextTime.equals(lastTime)) {
            checkShutdown();
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
                    checkShutdown();
                    createDateDimension(quartersDetails, academicTermDetails, nextDate);
                    nextDate = nextDate.plusDays(1);
                } while (nextDate.isBefore(dimensionDate));
            }
            else if (nextDate.isAfter(dimensionDate)) {
                do {
                    checkShutdown();
                    createDateDimension(quartersDetails, academicTermDetails, dimensionDate);
                    dimensionDate = dimensionDate.plusDays(1);
                } while (nextDate.isAfter(dimensionDate));
            }
            
            nextDate = dimensionDate.plusDays(1);
        }
        
        //Add any missing dates from the tail
        while (nextDate.isBefore(end)) {
            checkShutdown();
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
    EventProcessingResult doAggregateRawEvents() {
        if (!this.checkedDimensions) {
            //First time aggregation has happened, run populateDimensions to ensure enough dimension data exists
            final boolean populatedDimensions = this.populateDimensions();
            if (!populatedDimensions) {
                this.logger.warn("Aborting raw event aggregation, populateDimensions returned false so the state of date/time dimensions is unknown");
                return null;
            }
            
            this.checkedDimensions = true;
        }
        
        //Flush any dimension creation before aggregation
        this.getEntityManager().flush();
        this.getEntityManager().setFlushMode(FlushModeType.COMMIT);

        final IEventAggregatorStatus eventAggregatorStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION, true);
        
        //Update status with current server name
        final String serverName = this.portalInfoProvider.getUniqueServerName();
        final String previousServerName = eventAggregatorStatus.getServerName();
        if (previousServerName != null && !serverName.equals(previousServerName)) {
            this.logger.debug("Last aggregation run on {} clearing all aggregation caches", previousServerName);
            final Session session = getEntityManager().unwrap(Session.class);
            session.getSessionFactory().getCache().evictEntityRegions();
        }
        
        eventAggregatorStatus.setServerName(serverName);
        
        //Calculate date range for aggregation
        DateTime lastAggregated = eventAggregatorStatus.getLastEventDate();
        if (lastAggregated == null) {
            lastAggregated = new DateTime(0);
        }
        
        final DateTime newestEventTime = DateTime.now().minus(this.aggregationDelay).secondOfMinute().roundFloorCopy();
        
        final Thread currentThread = Thread.currentThread();
        final String currentName = currentThread.getName();
        final MutableInt events = new MutableInt();
        
        try {
            currentThread.setName(currentName + "-" + lastAggregated + "_" + newestEventTime);
        
            logger.debug("Starting aggregation of events between {} (inc) and {} (exc)", lastAggregated, newestEventTime);
            
            //Do aggregation, capturing the start and end dates
            eventAggregatorStatus.setLastStart(DateTime.now());
            portalEventDao.aggregatePortalEvents(lastAggregated, newestEventTime, this.eventAggregationBatchSize, new AggregateEventsHandler(events, eventAggregatorStatus));
            eventAggregatorStatus.setLastEnd(new DateTime());
        }
        finally {
            currentThread.setName(currentName);
        }
        
        //Store the results of the aggregation
        eventAggregationManagementDao.updateEventAggregatorStatus(eventAggregatorStatus);
        
        final boolean complete = this.eventAggregationBatchSize <= 0 || events.intValue() < this.eventAggregationBatchSize;
        return new EventProcessingResult(events.intValue(), lastAggregated, newestEventTime, complete);
    }
    
    static class EventProcessingResult {
        private final int eventCount;
        private final DateTime start;
        private final DateTime end;
        private final boolean complete;
        
        private EventProcessingResult(int eventCount, DateTime lastAggregated, DateTime newestEventTime, boolean complete) {
            this.eventCount = eventCount;
            this.start = lastAggregated;
            this.end = newestEventTime;
            this.complete = complete;
        }
    }

    EventProcessingResult doPurgeRawEvents() {
        final IEventAggregatorStatus eventPurgerStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.PURGING, true);
        
        //Update status with current server name
        final String serverName = this.portalInfoProvider.getUniqueServerName();
        eventPurgerStatus.setServerName(serverName);
        eventPurgerStatus.setLastStart(new DateTime());
        
        //Determine date of most recently aggregated data
        final IEventAggregatorStatus eventAggregatorStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION, false);
        if (eventAggregatorStatus == null || eventAggregatorStatus.getLastEventDate() == null) {
            //Nothing has been aggregated, skip purging
            
            eventPurgerStatus.setLastEnd(new DateTime());
            eventAggregationManagementDao.updateEventAggregatorStatus(eventPurgerStatus);
            
            return new EventProcessingResult(0, null, null, true);
        }
        
        //Calculate purge end date from most recent aggregation minus the purge delay
        final DateTime lastAggregated = eventAggregatorStatus.getLastEventDate();
        final DateTime purgeEnd = lastAggregated.minus(this.purgeDelay);
        
        final Thread currentThread = Thread.currentThread();
        final String currentName = currentThread.getName();
        final int events;
        try {
            currentThread.setName(currentName + "-" + purgeEnd);
        
            //Purge events
            logger.debug("Starting purge of events before {}", purgeEnd);
            events = portalEventDao.deletePortalEventsBefore(purgeEnd);
        }
        finally {
            currentThread.setName(currentName);
        }
        
        //Update the status object and store it
        eventPurgerStatus.setLastEventDate(purgeEnd.minusMillis(1)); //decrement by 1ms since deletePortalEventsBefore uses lessThan and not lessThanEqualTo 
        eventPurgerStatus.setLastEnd(new DateTime());
        eventAggregationManagementDao.updateEventAggregatorStatus(eventPurgerStatus);
        
        return new EventProcessingResult(events, null, purgeEnd, true);
    }
    
    private final class AggregateEventsHandler extends FunctionWithoutResult<PortalEvent> {
        //Event Aggregation Context - used by aggregators to track state
        private final EventAggregationContext eventAggregationContext = new EventAggregationContextImpl(); 
        private final MutableInt eventCounter;
        private final IEventAggregatorStatus eventAggregatorStatus;

        //pre-compute the set of intervals that our event aggregators support and only bother tracking those
        private final Set<AggregationInterval> handledIntervals;
        
        //Local tracking of the current aggregation interval and info about said interval
        private final Map<AggregationInterval, AggregationIntervalInfo> currentIntervalInfo = new EnumMap<AggregationInterval, AggregationIntervalInfo>(AggregationInterval.class);
        private final Map<AggregationInterval, AggregationIntervalInfo> readOnlyIntervalInfo = Collections.unmodifiableMap(currentIntervalInfo);
        
        //Local caches of per-aggregator config data, shouldn't ever change for the duration of an aggregation run
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
            
            //Create the set of intervals that are actually being aggregated
            final Set<AggregationInterval> handledIntervalsNotIncluded = EnumSet.allOf(AggregationInterval.class);
            final Set<AggregationInterval> handledIntervalsBuilder = EnumSet.noneOf(AggregationInterval.class);
            for (final IPortalEventAggregator<PortalEvent> portalEventAggregator : portalEventAggregators) {
                final Class<? extends IPortalEventAggregator> aggregatorType = getClass(portalEventAggregator);
                
                //Get aggregator specific interval info config
                final AggregatedIntervalConfig aggregatorIntervalConfig = this.getAggregatorIntervalConfig(aggregatorType);
                
                for (final Iterator<AggregationInterval> intervalsIterator = handledIntervalsNotIncluded.iterator(); intervalsIterator.hasNext(); ) {
                    final AggregationInterval interval = intervalsIterator.next();
                    if (aggregatorIntervalConfig.isIncluded(interval)) {
                        handledIntervalsBuilder.add(interval);
                        intervalsIterator.remove();
                    }
                }
            }
            this.handledIntervals = Sets.immutableEnumSet(handledIntervalsBuilder);
        }

        @Override
        protected void applyWithoutResult(PortalEvent event) {
            if (shutdown) {
                //Mark ourselves as interupted and throw an exception
                Thread.currentThread().interrupt();
                throw new RuntimeException("uPortal is shutting down, throwing an exeption to stop aggregation");
            }
            
            final DateTime eventDate = event.getTimestampAsDate();
            
            //If no interval data yet populate it.
            if (this.currentIntervalInfo.isEmpty()) {
                initializeIntervalInfo(eventDate);
            }
            
            //Check each interval to see if an interval boundary has been crossed
            for (final AggregationInterval interval : handledIntervals) {
                AggregationIntervalInfo intervalInfo = this.currentIntervalInfo.get(interval);
                if (intervalInfo != null && !intervalInfo.getEnd().isAfter(eventDate)) { //if there is no IntervalInfo that interval must not be supported in the current environment 
                    logger.debug("Crossing {} Interval, triggered by {}", interval, event);
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

        private void initializeIntervalInfo(final DateTime eventDate) {
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
            
            for (final AggregationInterval interval : this.handledIntervals) {
                final AggregationIntervalInfo intervalInfo = intervalHelper.getIntervalInfo(interval, intervalDate);
                if (intervalInfo != null) {
                    this.currentIntervalInfo.put(interval, intervalInfo);
                }
                else {
                    this.currentIntervalInfo.remove(interval);
                }
            }
        }
        
        private void doAggregateEvent(PortalEvent item) {
            checkShutdown();
            
            eventCounter.increment();

            for (final ApplicationEventFilter<PortalEvent> applicationEventFilter : applicationEventFilters) {
                if (!applicationEventFilter.supports(item)) {
                    logger.trace("Skipping event {} - {} excluded by filter {}", new Object[] { eventCounter, item,
                            applicationEventFilter });
                    return;
                }
            }
            logger.trace("Aggregating event {} - {}", eventCounter, item);
            
            //Load or create the event session
            EventSession eventSession = getEventSession(item);
            
            //Give each aggregator a chance at the event
            for (final IPortalEventAggregator<PortalEvent> portalEventAggregator : portalEventAggregators) {
                if (portalEventAggregator.supports(item.getClass())) {
                    final Class<? extends IPortalEventAggregator> aggregatorType = getClass(portalEventAggregator);
                    
                    //Get aggregator specific interval info map
                    final Map<AggregationInterval, AggregationIntervalInfo> aggregatorIntervalInfo = this.getAggregatorIntervalInfo(aggregatorType);
                    
                    //If there is an event session get the aggregator specific version of it
                    if (eventSession != null) {
                        final AggregatedGroupConfig aggregatorGroupConfig = getAggregatorGroupConfig(aggregatorType);
                        
                        final CacheKey key = CacheKey.build(EVENT_SESSION_CACHE_KEY_SOURCE, eventSession, aggregatorGroupConfig);
                        EventSession filteredEventSession = this.eventAggregationContext.getAttribute(key);
                        if (filteredEventSession == null) {
                            filteredEventSession = new FilteredEventSession(eventSession, aggregatorGroupConfig);
                            this.eventAggregationContext.setAttribute(key, filteredEventSession);
                        }
                        eventSession = filteredEventSession;
                    }
                    
                    //Aggregation magic happens here!
                    portalEventAggregator.aggregateEvent(item, eventSession, eventAggregationContext, aggregatorIntervalInfo);
                }
            }
        }

        protected EventSession getEventSession(PortalEvent item) {
            final String eventSessionId = item.getEventSessionId();
            
            //First check the aggregation context for a cached session event, fall back
            //to asking the DAO if nothing in the context, cache the result
            final CacheKey key = CacheKey.build(EVENT_SESSION_CACHE_KEY_SOURCE, eventSessionId);
            EventSession eventSession = this.eventAggregationContext.getAttribute(key);
            if (eventSession == null) {
                eventSession = eventSessionDao.getEventSession(item);
                this.eventAggregationContext.setAttribute(key, eventSession);
            }
            
            //Record the session access
            eventSession.recordAccess(item.getTimestampAsDate());
            eventSessionDao.storeEventSession(eventSession);
            
            return eventSession;
        }
        
        private void doHandleIntervalBoundary(AggregationInterval interval, Map<AggregationInterval, AggregationIntervalInfo> intervals) {
            for (final IPortalEventAggregator<PortalEvent> portalEventAggregator : portalEventAggregators) {
                
                final Class<? extends IPortalEventAggregator> aggregatorType = getClass(portalEventAggregator);
                final AggregatedIntervalConfig aggregatorIntervalConfig = this.getAggregatorIntervalConfig(aggregatorType);
                
                //If the aggreagator is configured to use the interval notify it of the interval boundary
                if (aggregatorIntervalConfig.isIncluded(interval)) {
                    final Map<AggregationInterval, AggregationIntervalInfo> aggregatorIntervalInfo = this.getAggregatorIntervalInfo(aggregatorType);
                    portalEventAggregator.handleIntervalBoundary(interval, eventAggregationContext, aggregatorIntervalInfo);
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
                aggregatorReadOnlyIntervalInfo.put(aggregatorType, intervalInfo);
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
        
        protected <T> Class<T> getClass(T object) {
            return (Class<T>)AopProxyUtils.ultimateTargetClass(object);
        }
    }
}
