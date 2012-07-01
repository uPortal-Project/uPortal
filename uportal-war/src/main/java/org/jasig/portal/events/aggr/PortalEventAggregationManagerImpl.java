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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jasig.portal.concurrency.FunctionWithoutResult;
import org.jasig.portal.concurrency.locking.ClusterMutex;
import org.jasig.portal.concurrency.locking.IClusterLockService;
import org.jasig.portal.concurrency.locking.IClusterLockService.TryLockFunctionResult;
import org.jasig.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.jasig.portal.jpa.BaseAggrEventsJpaDao;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;

/**
 * Service that handles the management of event aggregation & purging
 * 
 * @author Eric Dalquist
 */
@Service("portalEventAggregationManager")
public class PortalEventAggregationManagerImpl extends BaseAggrEventsJpaDao implements IPortalEventAggregationManager, HibernateCacheEvictor, DisposableBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private PortalEventDimensionPopulator portalEventDimensionPopulator;
    private PortalEventAggregator portalEventAggregator;
    private PortalEventPurger portalEventPurger;
    private PortalEventSessionPurger portalEventSessionPurger;
    private IClusterLockService clusterLockService;
    private IEventAggregationManagementDao eventAggregationManagementDao;
    
    private long aggregateRawEventsPeriod = 0;
    private int closeAggregationsPeriod = 0;
    private long purgeRawEventsPeriod = 0;
    private long purgeEventSessionsPeriod = 0;
    
    private final ThreadLocal<Map<Class<?>, Collection<Serializable>>> evictedEntitiesHolder = new ThreadLocal<Map<Class<?>, Collection<Serializable>>>() {
        @Override
        protected Map<Class<?>, Collection<Serializable>> initialValue() {
            return new HashMap<Class<?>, Collection<Serializable>>();
        }
    };
    private volatile boolean shutdown = false;
    
    @Autowired
    public void setEventAggregationManagementDao(IEventAggregationManagementDao eventAggregationManagementDao) {
        this.eventAggregationManagementDao = eventAggregationManagementDao;
    }

    @Autowired
    public void setClusterLockService(IClusterLockService clusterLockService) {
        this.clusterLockService = clusterLockService;
    }
    
    @Autowired
    public void setPortalEventDimensionPopulator(PortalEventDimensionPopulator portalEventDimensionPopulator) {
        this.portalEventDimensionPopulator = portalEventDimensionPopulator;
    }

    @Autowired
    public void setPortalEventAggregator(PortalEventAggregator portalEventAggregator) {
        this.portalEventAggregator = portalEventAggregator;
    }

    @Autowired
    public void setPortalEventPurger(PortalEventPurger portalEventPurger) {
        this.portalEventPurger = portalEventPurger;
    }

    @Autowired
    public void setPortalEventSessionPurger(PortalEventSessionPurger portalEventSessionPurger) {
        this.portalEventSessionPurger = portalEventSessionPurger;
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

    @Value("#{${org.jasig.portal.events.aggr.session.PortalEventAggregationManager.cleanUnclosedAggregationsPeriod:3670000} * 0.95}")
    public void setCloseAggregationsPeriod(int closeAggregationsPeriod) {
        this.closeAggregationsPeriod = closeAggregationsPeriod;
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
                
                //Set aggr period to 0 to allow immediate re-run locally
                aggregatePeriod = 0;
            }
            
            try {
                long start = System.nanoTime();
                
                //Try executing aggregation within lock
                result = clusterLockService.doInTryLockIfNotRunSince(PortalEventAggregator.AGGREGATION_LOCK_NAME,
                        aggregatePeriod,
                        new Function<ClusterMutex, EventProcessingResult>() {
                            @Override
                            public EventProcessingResult apply(final ClusterMutex input) {
                                return portalEventAggregator.doAggregateRawEvents();
                            }
                        });
                
                //Aggregation didn't run due to the lock either being owned or not old enough, return immediately
                if (!result.isExecuted()) {
                    return false;
                }
                
                aggrResult = result.getResult();
                
                //Check the result, warn if null
                if (result.isExecuted() && aggrResult == null) {
                    logger.warn("doAggregateRawEvents did not execute");
                }
                else if (aggrResult != null) {
                    if (logger.isInfoEnabled()) {
                        logResult("Aggregated {} events created at {} events/second between {} and {} in {}ms - {} e/s a {}x speedup", aggrResult, start);
                    }
                    
                    //If events were processed purge old aggregations from the cache and then clean unclosed aggregations
                    if (aggrResult.getProcessed() > 0) {
                        final Map<Class<?>, Collection<Serializable>> evictedEntities = evictedEntitiesHolder.get();
                        if (evictedEntities.size() > 0) {
                            portalEventAggregator.evictAggregates(evictedEntities);
                        }

                        //Update start time so logging is accurate
                        start = System.nanoTime();
                        
                        result = clusterLockService.doInTryLock(PortalEventAggregator.AGGREGATION_LOCK_NAME,
                                new Function<ClusterMutex, EventProcessingResult>() {
                                    @Override
                                    public EventProcessingResult apply(final ClusterMutex input) {
                                        return portalEventAggregator.doCloseAggregations();
                                    }
                                });
                        
                        final EventProcessingResult cleanResult = result.getResult();
                        if (result.isExecuted() && cleanResult == null) {
                            logger.warn("doCloseAggregations was not executed");
                        }
                        else if (cleanResult != null && logger.isInfoEnabled()) {
                            logResult("Clean {} unclosed agregations created at {} events/second between {} and {} in {}ms - {} e/s a {}x speedup", cleanResult, start);
                        }
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
        } while (result.isExecuted() && aggrResult != null && !aggrResult.isComplete());
        
        return result != null && result.isExecuted() && aggrResult != null && aggrResult.isComplete();
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
                purgeResult = result.getResult();
                
                //Check the result, warn if null
                if (result.isExecuted() && purgeResult == null) {
                    logger.warn("doPurgeRawEvents did not execute");
                }
                else if (purgeResult != null && logger.isInfoEnabled()) {
                    logResult("Purged {} events created at {} events/second between {} and {} in {}ms - {} e/s a {}x speedup", purgeResult, start);
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
        } while (result.isExecuted() && purgeResult != null && !purgeResult.isComplete());
        
        return result != null && result.isExecuted() && purgeResult != null && purgeResult.isComplete();
    }
    
    @Override
    public boolean purgeEventSessions() {
        if (shutdown) {
            logger.warn("purgeEventSessions called after shutdown, ignoring call");
            return false;
        }
        
        try {
            final long start = System.nanoTime();
            
            final TryLockFunctionResult<EventProcessingResult> result = clusterLockService
                    .doInTryLockIfNotRunSince(PortalEventSessionPurger.PURGE_EVENT_SESSION_LOCK_NAME,
                            this.purgeEventSessionsPeriod,
                            new Function<ClusterMutex, EventProcessingResult>() {
                                @Override
                                public EventProcessingResult apply(final ClusterMutex input) {
                                    return portalEventSessionPurger.doPurgeEventSessions();
                                }
                            });
            final EventProcessingResult purgeResult = result.getResult();
            
            //Check the result, warn if null
            if (result.isExecuted() && purgeResult == null) {
                logger.warn("doPurgeRawEvents did not execute");
            }
            else if (purgeResult != null && logger.isInfoEnabled()) {
                logResult("Purged {} event sessions created before {} in {}ms - {} sessions/second", purgeResult, start);
            }
            
            return result.isExecuted() && purgeResult != null && purgeResult.isComplete();
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

    protected void logResult(String message, EventProcessingResult aggrResult, long start) {
        final long runTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        final double processRate = aggrResult.getProcessed() / (runTime / 1000d);

        final DateTime startDate = aggrResult.getStart();
        if (startDate != null) {
            final double creationRate = aggrResult.getCreationRate();
            final double processSpeedUp = processRate / creationRate;
            logger.info(message,
                    new Object[] { aggrResult.getProcessed(), String.format("%.4f", creationRate), startDate,
                            aggrResult.getEnd(), runTime, String.format("%.4f", processRate),
                            String.format("%.4f", processSpeedUp) });
        }
        else {
            logger.info(message,
                    new Object[] { aggrResult.getProcessed(), aggrResult.getEnd(), runTime,
                            String.format("%.4f", processRate) });
        }
    }
}
