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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jasig.portal.IPortalInfoProvider;
import org.jasig.portal.concurrency.FunctionWithoutResult;
import org.jasig.portal.concurrency.locking.ClusterMutex;
import org.jasig.portal.concurrency.locking.IClusterLockService;
import org.jasig.portal.concurrency.locking.IClusterLockService.TryLockFunctionResult;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.IEventAggregatorStatus.ProcessingType;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.events.aggr.session.EventSessionDao;
import org.jasig.portal.jpa.BaseAggrEventsJpaDao;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.google.common.base.Function;

/**
 * Service that handles the management of event aggregation & purging
 * 
 * @author Eric Dalquist
 */
@Service("portalEventAggregationManager")
public class PortalEventAggregationManagerImpl extends BaseAggrEventsJpaDao implements IPortalEventAggregationManager, HibernateCacheEvictor, DisposableBean {
    private static final String PURGE_EVENT_SESSION_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".PURGE_EVENT_SESSION_LOCK";
    private static final String CLEAN_UNCLOSED_AGGREGATIONS_LOCK_NAME = PortalEventAggregationManagerImpl.class.getName() + ".CLEAN_UNCLOSED_AGGREGATIONS_LOCK";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private volatile boolean shutdown = false;
    
    private PortalEventDimensionPopulator portalEventDimensionPopulator;
    private PortalEventAggregator portalEventAggregator;
    private PortalEventPurger portalEventPurger;
    
    private IPortalInfoProvider portalInfoProvider;
    private IClusterLockService clusterLockService;
    private IEventAggregationManagementDao eventAggregationManagementDao;
    private AggregationIntervalHelper intervalHelper;
    private EventSessionDao eventSessionDao;
    private Set<IPortalEventAggregator<PortalEvent>> portalEventAggregators = Collections.emptySet();
    
    private ReadablePeriod eventSessionDuration = Period.days(1);
    private long aggregateRawEventsPeriod = 0;
    private long purgeRawEventsPeriod = 0;
    private long purgeEventSessionsPeriod = 0;
    private long cleanUnclosedAggregationsPeriod = 0;
    
    @Autowired
    public void setEventSessionDao(EventSessionDao eventSessionDao) {
        this.eventSessionDao = eventSessionDao;
    }

    @Autowired
    public void setIntervalHelper(AggregationIntervalHelper intervalHelper) {
        this.intervalHelper = intervalHelper;
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
    public void setPortalEventAggregators(Set<IPortalEventAggregator<PortalEvent>> portalEventAggregators) {
        this.portalEventAggregators = portalEventAggregators;
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
                    .doInTryLock(PortalEventDimensionPopulator.DIMENSION_LOCK_NAME,
                            new FunctionWithoutResult<ClusterMutex>() {
                                @Override
                                protected void applyWithoutResult(ClusterMutex input) {
                                    portalEventDimensionPopulator.doPopulateDimensions();
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
        if (shutdown) {
            logger.warn("aggregateRawEvents called after shutdown, ignoring call");
            return false;
        }

        long aggregatePeriod = this.aggregateRawEventsPeriod;
        TryLockFunctionResult<EventProcessingResult> result = null;
        EventProcessingResult aggrResult = null;
        do {
            if (result != null) {
                logger.debug("doAggregateRawEvents signaled that not all events were aggregated in a single transaction, running again.");
                
                //Set purge period to 0 to allow immediate re-run locally
                aggregatePeriod = 0;
            }
            
            try {
                final long start = System.nanoTime();
                
                //Try executing aggregation within lock
                result = clusterLockService.doInTryLockIfNotRunSince(PortalEventAggregator.AGGREGATION_LOCK_NAME,
                        aggregatePeriod,
                        new Function<ClusterMutex, EventProcessingResult>() {
                            @Override
                            public EventProcessingResult apply(final ClusterMutex input) {
                                return portalEventAggregator.doAggregateRawEvents();
                            }
                        });
                
                //Check the result, warn if null
                if (result == null) {
                    aggrResult = null;
                    logger.warn("doAggregateRawEvents did not execute");
                }
                else {
                    //Report on non-null result
                    aggrResult = result.getResult();
                    
                    if (logger.isInfoEnabled()) {
                        final long runTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                        logger.info("Aggregated {} events between {} and {} in {}ms - {} events/second", 
                                new Object[] { aggrResult.getProcessed(), aggrResult.getStart(), aggrResult.getEnd(), runTime, aggrResult.getProcessed()/(runTime/1000d) });
                    }
                    
                    //If events were processed purge old aggregations from the cache
                    if (aggrResult != null && aggrResult.getProcessed() > 0) {
                        final Map<Class<?>, Collection<Serializable>> evictedEntities = evictedEntitiesHolder.get();
                        portalEventAggregator.evictAggregates(evictedEntities);
                    }
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
            }
            
        //Loop if doAggregateRawEvents returns false, this means that there is more to aggregate 
        } while (result != null && result.isExecuted() && aggrResult != null && !aggrResult.isComplete());
        
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
                    this.cleanUnclosedAggregationsPeriod,
                    new Function<ClusterMutex, Boolean>() {
                        @Override
                        public Boolean apply(ClusterMutex input) {
                            try {
                                final TryLockFunctionResult<Boolean> result = clusterLockService.doInTryLock(PortalEventAggregator.AGGREGATION_LOCK_NAME,
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
                                                            new Object[] { result.getProcessed(), result.getStart(), result.getEnd(), runTime, result.getProcessed()/(runTime/1000d) });
                                                }
                                                
                                                return result.isComplete();
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
        TryLockFunctionResult<EventProcessingResult> result = null;
        EventProcessingResult purgeResult = null;
        do {
            if (result != null) {
                logger.debug("doPurgeRawEvents signaled that not all events were purged in a single transaction, running again.");
                
                //Set purge period to 0 to allow immediate re-run locally
                purgePeriod = 0;
            }
            
            try {
                final long start = System.nanoTime();
                
                result = clusterLockService.doInTryLockIfNotRunSince(PortalEventPurger.PURGE_RAW_EVENTS_LOCK_NAME,
                        purgePeriod,
                        new Function<ClusterMutex, EventProcessingResult>() {
                            @Override
                            public EventProcessingResult apply(final ClusterMutex input) {
                                return portalEventPurger.doPurgeRawEvents();
                            }
                        });
                
                //Check the result, warn if null
                if (result == null) {
                    purgeResult = null;
                    logger.warn("doPurgeRawEvents did not execute");
                }
                else {
                    //Report on non-null result
                    purgeResult = result.getResult();
                    
                    if (logger.isInfoEnabled()) {
                        final long runTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                        logger.info("Purged {} events before {} in {}ms - {} events/second", 
                                new Object[] { purgeResult.getProcessed(), purgeResult.getEnd(), runTime, purgeResult.getProcessed()/(runTime/1000d) });
                    }
                }
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
        } while (result != null && result.isExecuted() && purgeResult != null && !purgeResult.isComplete());
        
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
    
    private final ThreadLocal<Map<Class<?>, Collection<Serializable>>> evictedEntitiesHolder = new ThreadLocal<Map<Class<?>, Collection<Serializable>>>() {
        @Override
        protected Map<Class<?>, Collection<Serializable>> initialValue() {
            return new HashMap<Class<?>, Collection<Serializable>>();
        }
    };
    
    @Override
    public void evictEntity(Class<?> entityClass, Serializable identifier) {
        final Map<Class<?>, Collection<Serializable>> evictedEntities = evictedEntitiesHolder.get();
        Collection<Serializable> ids = evictedEntities.get(entityClass);
        if (ids == null) {
            ids = new ArrayList<Serializable>();
            evictedEntities.put(entityClass, ids);
        }
        ids.add(identifier);
    }

    private void checkShutdown() {
        if (shutdown) {
            //Mark ourselves as interupted and throw an exception
            Thread.currentThread().interrupt();
            throw new RuntimeException("uPortal is shutting down, throwing an exeption to stop processing");
        }
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
}
