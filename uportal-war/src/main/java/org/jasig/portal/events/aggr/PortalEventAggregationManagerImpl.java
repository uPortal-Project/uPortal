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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.persistence.FlushModeType;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang.mutable.MutableObject;
import org.hibernate.Cache;
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;

/**
 * Service that handles the management of event aggregation & purging
 * 
 * @author Eric Dalquist
 */
@Service("portalEventAggregationManager")
public class PortalEventAggregationManagerImpl extends BaseAggrEventsJpaDao implements IPortalEventAggregationManager, HibernateCacheEvictor, DisposableBean {
    private static final String EVENT_SESSION_CACHE_KEY_SOURCE = AggregateEventsHandler.class.getName() + "-EventSession";
    
    private static final String DIMENSION_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".DIMENSION_LOCK";
    private static final String AGGREGATION_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".AGGREGATION_LOCK";
    private static final String PURGE_RAW_EVENTS_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".PURGE_RAW_EVENTS_LOCK";
    private static final String PURGE_EVENT_SESSION_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".PURGE_EVENT_SESSION_LOCK";
    private static final String CLEAN_UNCLOSED_AGGREGATIONS_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".CLEAN_UNCLOSED_AGGREGATIONS_LOCK";
    
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
    private ReadablePeriod eventSessionDuration = Period.days(1);
    private long aggregateRawEventsPeriod = 0;
    private long purgeRawEventsPeriod = 0;
    private long purgeEventSessionsPeriod = 0;
    private long cleanUnclosedAggregationsPeriod = 0;
    
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
    
    @Value("${org.jasig.portal.events.aggr.session.JpaEventSessionDao.eventSessionDuration:P1D}")
    public void setEventSessionDuration(ReadablePeriod eventSessionDuration) {
        this.eventSessionDuration = eventSessionDuration;
    }
    
    @Value("#{${org.jasig.portal.events.aggr.session.PortalEventAggregationManager.aggregateRawEventsPeriod:60700} * 0.95}")
    public void setAggregateRawEventsPeriod(long aggregateRawEventsPeriod) {
        this.aggregateRawEventsPeriod = aggregateRawEventsPeriod;
    }

    @Value("#{${org.jasig.portal.events.aggr.session.PortalEventAggregationManager.purgeRawEventsPeriod:61300} * 0.95}")
    public void setPurgeRawEventsPeriod(long purgeRawEventsPeriod) {
        this.purgeRawEventsPeriod = purgeRawEventsPeriod;
    }

    @Value("#{${org.jasig.portal.events.aggr.session.PortalEventAggregationManager.purgeEventSessionsPeriod:61700} * 0.95}")
    public void setPurgeEventSessionsPeriod(long purgeEventSessionsPeriod) {
        this.purgeEventSessionsPeriod = purgeEventSessionsPeriod;
    }

    @Value("#{${org.jasig.portal.events.aggr.session.PortalEventAggregationManager.cleanUnclosedAggregationsPeriod:} * 0.95}")
    public void setCleanUnclosedAggregationsPeriod(long cleanUnclosedAggregationsPeriod) {
        this.cleanUnclosedAggregationsPeriod = cleanUnclosedAggregationsPeriod;
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
            final TryLockFunctionResult<Object> result = this.clusterLockService
                    .doInTryLock(DIMENSION_LOCK_NAME,
                            new FunctionWithoutResult<ClusterMutex>() {
                                @Override
                                protected void applyWithoutResult(ClusterMutex input) {
                                    getTransactionOperations().execute(new TransactionCallbackWithoutResult() {
                                        @Override
                                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                                            doPopulateDimensions();
                                        }
                                    });
                                }
                            }
                    );
            
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
        if (shutdown) {
            logger.warn("aggregateRawEvents called after shutdown, ignoring call");
            return false;
        }

        long aggregatePeriod = this.aggregateRawEventsPeriod;
        TryLockFunctionResult<EventProcessingResult> result = null;
        final EventProcessingResult aggrResult = result.getResult();
        do {
            if (result != null) {
                logger.debug("doAggregateRawEvents signaled that not all events were aggregated in a single transaction, running again.");
                
                //Set purge period to 0 to allow immediate re-run locally
                aggregatePeriod = 0;
            }
            
            try {
                result = clusterLockService.doInTryLockIfNotRunSince(AGGREGATION_LOCK_NAME,
                        aggregatePeriod,
                        new Function<ClusterMutex, EventProcessingResult>() {
                            @Override
                            public EventProcessingResult apply(final ClusterMutex input) {
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
                                
                                return result;
                            }
                        }
                );
                
                if (aggrResult != null && aggrResult.eventCount > 0) {
                    //Clear all caches again, needs to happen in TX so we can get to the unwrapped Session
                    getTransactionOperations().execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                            int evictedEntities = 0;
                            int evictedCollections = 0;
                            
                            final Session session = getEntityManager().unwrap(Session.class);
                            final Cache cache = session.getSessionFactory().getCache();
                            for (final Entry<Class, Collection<Serializable>> evictedEntityEntry : evictedEntitiesHolder.get().entrySet()) {
                                final Class entityClass = evictedEntityEntry.getKey();
                                for (final Serializable id : evictedEntityEntry.getValue()) {
                                    cache.evictEntity(entityClass, id);
                                    evictedEntities++;
                                }
                            }
                            for (final Entry<String, Collection<Serializable>> evictedCollectionEntry : evictedCollectionsHolder.get().entrySet()) {
                                final String role = evictedCollectionEntry.getKey();
                                for (final Serializable id : evictedCollectionEntry.getValue()) {
                                    cache.evictCollection(role, id);
                                    evictedCollections++;
                                }
                            }
                            
                            logger.debug("Evicted {} entities and {} collections from hibernate caches", evictedEntities, evictedCollections);
                        }
                    });
                }
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
            finally {
                evictedEntitiesHolder.remove();
                evictedCollectionsHolder.remove();
            }
            
        //Loop if doAggregateRawEvents returns false, this means that there is more to aggregate 
        } while (result != null && result.isExecuted() && !aggrResult.complete);
        
        return result != null && result.isExecuted();
    }
    
    @Override
    public boolean cleanUnclosedAggregations() {
        if (shutdown) {
            logger.warn("cleanUnclosedAggregations called after shutdown, ignoring call");
            return false;
        }
        
        //Need to own two locks to do this ... is this even possible?
        try {
            final TryLockFunctionResult<Boolean> result = clusterLockService.doInTryLockIfNotRunSince(CLEAN_UNCLOSED_AGGREGATIONS_LOCK_NAME,
                    0, //TODO
                    new Function<ClusterMutex, Boolean>() {
                        @Override
                        public Boolean apply(ClusterMutex input) {
                            try {
                                final TryLockFunctionResult<Boolean> result = clusterLockService.doInTryLock(AGGREGATION_LOCK_NAME,
                                        new Function<ClusterMutex, Boolean>() {
                                            @Override
                                            public Boolean apply(final ClusterMutex input) {
                                                //Executing within lock
                                                final long start = System.nanoTime();
                                                
                                                final EventProcessingResult result = getTransactionOperations().execute(new TransactionCallback<EventProcessingResult>() {
                                                    @Override
                                                    public EventProcessingResult doInTransaction(TransactionStatus status) {
                                                        return doCleanUnclosedAggregations();
                                                    }
                                                });
                                                
                                                if (result == null) {
                                                    logger.warn("doCleanUnclosedAggregations did not execute");
                                                    return null;
                                                }
                                                
                                                if (logger.isInfoEnabled()) {
                                                    final long runTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                                                    logger.info("Cleaned {} unclosed aggregations between {} and {} in {}ms - {} aggregations/second", 
                                                            new Object[] { result.eventCount, result.start, result.end, runTime, result.eventCount/(runTime/1000d) });
                                                }
                                                
                                                return result.complete;
                                            }
                                        }
                                );
    
                                return result.isExecuted() && result.getResult();
                            }
                            catch (InterruptedException e) {
                                logger.warn("Interrupted while cleaning unclosed aggregations", e);
                                Thread.currentThread().interrupt();
                                return false;
                            }
                        }
                    }
            );
            
            return result.isExecuted() && result.getResult();
        }
        catch (InterruptedException e) {
            logger.warn("Interrupted while cleaning unclosed aggregations", e);
            Thread.currentThread().interrupt();
            return false;
        }
        catch (RuntimeException e) {
            logger.error("cleanUnclosedAggregations failed", e);
            throw e;
        }
    }
    
    @Override
    public boolean purgeRawEvents() {
        if (shutdown) {
            logger.warn("purgeRawEvents called after shutdown, ignoring call");
            return false;
        }
        
        long purgePeriod = this.purgeRawEventsPeriod;
        TryLockFunctionResult<Boolean> result = null;
        do {
            if (result != null) {
                logger.debug("doPurgeRawEvents signaled that not all events were purged in a single transaction, running again.");
                
                //Set purge period to 0 to allow immediate re-run locally
                purgePeriod = 0;
            }
            
            try {
                result = this.clusterLockService
                        .doInTryLockIfNotRunSince(PURGE_RAW_EVENTS_LOCK_NAME,
                                purgePeriod,
                                new Function<ClusterMutex, Boolean>() {
                                    @Override
                                    public Boolean apply(ClusterMutex input) {
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
                                        
                                        return result.complete;
                                    }
                                }
                        );
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
            
        //Loop if doPurgeRawEvents returns false, this means that there is more to purge 
        } while (result != null && result.isExecuted() && !result.getResult());
        
        return result != null && result.isExecuted();
    }
    
    @Override
    public boolean purgeEventSessions() {
        if (shutdown) {
            logger.warn("purgeEventSessions called after shutdown, ignoring call");
            return false;
        }
        
        try {
            final TryLockFunctionResult<Object> result = this.clusterLockService
                    .doInTryLockIfNotRunSince(PURGE_EVENT_SESSION_LOCK_NAME,
                            this.purgeEventSessionsPeriod,
                            new FunctionWithoutResult<ClusterMutex>() {
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
                                                    final DateTime sessionPurgeDate = lastEventDate.minus(eventSessionDuration);
                                                    final int purgeCount = eventSessionDao.purgeEventSessionsBefore(sessionPurgeDate);
                                                    
                                                    if (logger.isInfoEnabled()) {
                                                        final long runTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                                                        logger.info("Purged {} event sessions before {} in {}ms - {} sessions/second", 
                                                                new Object[] { purgeCount, sessionPurgeDate, runTime, purgeCount/(runTime/1000d) });
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                    );
            
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
    }
    
    private final ThreadLocal<Map<Class, Collection<Serializable>>> evictedEntitiesHolder = new ThreadLocal<Map<Class, Collection<Serializable>>>() {
        @Override
        protected Map<Class, Collection<Serializable>> initialValue() {
            return new HashMap<Class, Collection<Serializable>>();
        }
    };
    private final ThreadLocal<Map<String, Collection<Serializable>>> evictedCollectionsHolder = new ThreadLocal<Map<String, Collection<Serializable>>>() {
        @Override
        protected Map<String, Collection<Serializable>> initialValue() {
            return new HashMap<String, Collection<Serializable>>();
        }
    };
    
    @Override
    public void evictEntity(Class entityClass, Serializable identifier) {
        final Map<Class, Collection<Serializable>> evictedEntities = evictedEntitiesHolder.get();
        Collection<Serializable> ids = evictedEntities.get(entityClass);
        if (ids == null) {
            ids = new ArrayList<Serializable>();
            evictedEntities.put(entityClass, ids);
        }
        ids.add(identifier);
    }

    @Override
    public void evictCollection(String role, Serializable ownerIdentifier) {
        final Map<String, Collection<Serializable>> evictedCollections = evictedCollectionsHolder.get();
        Collection<Serializable> ids = evictedCollections.get(role);
        if (ids == null) {
            ids = new ArrayList<Serializable>();
            evictedCollections.put(role, ids);
        }
        ids.add(role);
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
        doUpdateDateDimensions();
        this.checkedDimensions = true;
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
     * Populate the term/quarter data for the list of dimensions
     */
    void doUpdateDateDimensions() {
        final List<DateDimension> dateDimensions = this.dateDimensionDao.getDateDimensionsWithoutTerm();
        final List<AcademicTermDetail> academicTermDetails = this.eventAggregationManagementDao.getAcademicTermDetails();
        
        for (final DateDimension dateDimension : dateDimensions) {
            final DateMidnight date = dateDimension.getDate();
            final AcademicTermDetail termDetail = EventDateTimeUtils.findDateRangeSorted(date, academicTermDetails);
            if (termDetail != null) {
                dateDimension.setTerm(termDetail.getTermName());
                this.dateDimensionDao.updateDateDimension(dateDimension);
            }
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
            final Cache cache = session.getSessionFactory().getCache();
            cache.evictEntityRegions();
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
        final MutableObject lastEventDate = new MutableObject();
        
        try {
            currentThread.setName(currentName + "-" + lastAggregated + "_" + newestEventTime);
        
            logger.debug("Starting aggregation of events between {} (inc) and {} (exc)", lastAggregated, newestEventTime);
            
            //Do aggregation, capturing the start and end dates
            eventAggregatorStatus.setLastStart(DateTime.now());
            portalEventDao.aggregatePortalEvents(lastAggregated, newestEventTime, this.eventAggregationBatchSize, new AggregateEventsHandler(events, lastEventDate, eventAggregatorStatus));
            eventAggregatorStatus.setLastEnd(new DateTime());
        }
        finally {
            currentThread.setName(currentName);
        }
        
        //Store the results of the aggregation
        eventAggregationManagementDao.updateEventAggregatorStatus(eventAggregatorStatus);
        
        final boolean complete = this.eventAggregationBatchSize <= 0 || events.intValue() < this.eventAggregationBatchSize;
        return new EventProcessingResult(events.intValue(), lastAggregated, (DateTime)lastEventDate.getValue(), complete);
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

    /**
     * @return true if all events to be purged have been. false if there are more events to purge
     */
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
        boolean complete = true;
        
        //Calculate purge end date from most recent aggregation minus the purge delay
        final DateTime lastAggregated = eventAggregatorStatus.getLastEventDate();
        DateTime purgeEnd = lastAggregated.minus(this.purgeDelay);
        
        //Determine the DateTime of the oldest event
        DateTime oldestEventDate = eventPurgerStatus.getLastEventDate();
        if (oldestEventDate == null) {
            oldestEventDate = this.portalEventDao.getOldestPortalEventTimestamp();
        }

        //Make sure purgeEnd is no more than 1 hour after the oldest event date to limit delete scope
        oldestEventDate = oldestEventDate.plusHours(1);
        if (oldestEventDate.isBefore(purgeEnd)) {
            purgeEnd = oldestEventDate;
            complete = false;
        }
        
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
        
        return new EventProcessingResult(events, null, purgeEnd, complete);
    }

    /**
     * @return true if all old unclosed aggregations are closed
     */
    EventProcessingResult doCleanUnclosedAggregations() {
        final IEventAggregatorStatus cleanUnclosedStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.CLEAN_UNCLOSED, true);
        
        //Update status with current server name
        final String serverName = this.portalInfoProvider.getUniqueServerName();
        cleanUnclosedStatus.setServerName(serverName);
        cleanUnclosedStatus.setLastStart(new DateTime());
        
        //Determine date of most recently aggregated data
        final IEventAggregatorStatus eventAggregatorStatus = eventAggregationManagementDao.getEventAggregatorStatus(ProcessingType.AGGREGATION, false);
        if (eventAggregatorStatus == null || eventAggregatorStatus.getLastEventDate() == null) {
            //Nothing has been aggregated, skip unclosed cleanup
            
            cleanUnclosedStatus.setLastEnd(new DateTime());
            eventAggregationManagementDao.updateEventAggregatorStatus(cleanUnclosedStatus);
            
            return new EventProcessingResult(0, null, null, true);
        }
        
        //Calculate clean end date from most recent aggregation minus the purge delay
        final DateTime lastAggregatedDate = eventAggregatorStatus.getLastEventDate();
        final DateTime lastCleanUnclosedDate = cleanUnclosedStatus.getLastEventDate();
        
        if (lastAggregatedDate.equals(lastCleanUnclosedDate)) {
            logger.debug("No events aggregated since last unclosed aggregation cleaning, skipping clean: {}", lastAggregatedDate);
            return new EventProcessingResult(0, null, null, true);
        }
        
        int closedAggregations = 0;
        final Thread currentThread = Thread.currentThread();
        final String currentName = currentThread.getName();
        try {
            currentThread.setName(currentName + "-" + lastAggregatedDate);
            
            //For each interval the aggregator supports, cleanup the unclosed aggregations
            for (final AggregationInterval interval : AggregationInterval.values()) {
                //Determine the current interval
                final AggregationIntervalInfo currentInterval = intervalHelper.getIntervalInfo(interval, lastAggregatedDate);
                if (currentInterval != null) {
                    //Use the start of the current interval as the end of the current cleanup range
                    final DateTime cleanBeforeDate = currentInterval.getStart();
                    
                    //Determine the end of the cleanup to use as the start of the cleanup range
                    final DateTime cleanAfterDate;
                    if (lastCleanUnclosedDate == null) {
                        cleanAfterDate = new DateTime(0);
                    }
                    else {
                        final AggregationIntervalInfo previousInterval = intervalHelper.getIntervalInfo(interval, lastCleanUnclosedDate);
                        if (previousInterval == null) {
                            cleanAfterDate = new DateTime(0);
                        }
                        else {
                            cleanAfterDate = previousInterval.getStart();
                        }
                    }
                    
                    logger.debug("Cleaning unclosed {} aggregations between {} and {}",  new Object[] { interval, cleanAfterDate, cleanBeforeDate});

                    for (final IPortalEventAggregator<PortalEvent> portalEventAggregator : portalEventAggregators) {
                        final Class<? extends IPortalEventAggregator> aggregatorType = getClass(portalEventAggregator);
                        
                        //Get aggregator specific interval info config
                        AggregatedIntervalConfig aggregatorIntervalConfig = eventAggregationManagementDao.getAggregatedIntervalConfig(aggregatorType);
                        if (aggregatorIntervalConfig == null) {
                            aggregatorIntervalConfig = eventAggregationManagementDao.getDefaultAggregatedIntervalConfig();
                        }
                        
                        //If the aggregator is being used for the specified interval call cleanUnclosedAggregations
                        if (aggregatorIntervalConfig.isIncluded(interval)) {
                            closedAggregations += portalEventAggregator.cleanUnclosedAggregations(cleanAfterDate, cleanBeforeDate, interval);
                        }
                    }
                }
            }
        }
        finally {
            currentThread.setName(currentName);
        }
        
        //Update the status object and store it
        cleanUnclosedStatus.setLastEventDate(lastAggregatedDate); 
        cleanUnclosedStatus.setLastEnd(new DateTime());
        eventAggregationManagementDao.updateEventAggregatorStatus(cleanUnclosedStatus);
        
        return new EventProcessingResult(closedAggregations, lastCleanUnclosedDate, lastAggregatedDate, true);
    }
    
    @SuppressWarnings("unchecked")
    protected <T> Class<T> getClass(T object) {
        return (Class<T>)AopProxyUtils.ultimateTargetClass(object);
    }
    
    private final class AggregateEventsHandler extends FunctionWithoutResult<PortalEvent> {
        //Event Aggregation Context - used by aggregators to track state
        private final EventAggregationContext eventAggregationContext = new EventAggregationContextImpl(); 
        private final MutableInt eventCounter;
        private final MutableObject lastEventDate;
        private final IEventAggregatorStatus eventAggregatorStatus;

        //pre-compute the set of intervals that our event aggregators support and only bother tracking those
        private final Set<AggregationInterval> handledIntervals;
        
        //Local tracking of the current aggregation interval and info about said interval
        private final Map<AggregationInterval, AggregationIntervalInfo> currentIntervalInfo = new EnumMap<AggregationInterval, AggregationIntervalInfo>(AggregationInterval.class);
        
        //Local caches of per-aggregator config data, shouldn't ever change for the duration of an aggregation run
        private final Map<Class<? extends IPortalEventAggregator>, AggregatedGroupConfig> aggregatorGroupConfigs = new HashMap<Class<? extends IPortalEventAggregator>, AggregatedGroupConfig>();
        private final Map<Class<? extends IPortalEventAggregator>, AggregatedIntervalConfig> aggregatorIntervalConfigs = new HashMap<Class<? extends IPortalEventAggregator>, AggregatedIntervalConfig>();
        private final Map<Class<? extends IPortalEventAggregator>, Map<AggregationInterval, AggregationIntervalInfo>> aggregatorReadOnlyIntervalInfo = new HashMap<Class<? extends IPortalEventAggregator>, Map<AggregationInterval,AggregationIntervalInfo>>();
        private final AggregatedGroupConfig defaultAggregatedGroupConfig;
        private final AggregatedIntervalConfig defaultAggregatedIntervalConfig;
        
        private AggregateEventsHandler(MutableInt eventCounter, MutableObject lastEventDate, IEventAggregatorStatus eventAggregatorStatus) {
            this.eventCounter = eventCounter;
            this.lastEventDate = lastEventDate;
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
            this.lastEventDate.setValue(eventDate);
            
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
                    
                    this.aggregatorReadOnlyIntervalInfo.clear(); //Clear out cached per-aggregator interval info whenever a current interval info changes
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
                final Builder<AggregationInterval, AggregationIntervalInfo> intervalInfoBuilder = ImmutableMap.builder();
                
                for (Map.Entry<AggregationInterval, AggregationIntervalInfo> intervalInfoEntry : this.currentIntervalInfo.entrySet()) {
                    final AggregationInterval key = intervalInfoEntry.getKey();
                    if (aggregatorIntervalConfig.isIncluded(key)) {
                        intervalInfoBuilder.put(key, intervalInfoEntry.getValue());
                    }
                }
                
                intervalInfo = intervalInfoBuilder.build();
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
        
        @SuppressWarnings("unchecked")
        protected <T> Class<T> getClass(T object) {
            return (Class<T>)AopProxyUtils.ultimateTargetClass(object);
        }
    }
}
