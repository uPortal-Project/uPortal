/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.events.aggr;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.apereo.portal.concurrency.FunctionWithoutResult;
import org.apereo.portal.concurrency.locking.ClusterMutex;
import org.apereo.portal.concurrency.locking.IClusterLockService;
import org.apereo.portal.concurrency.locking.IClusterLockService.LockStatus;
import org.apereo.portal.concurrency.locking.IClusterLockService.TryLockFunctionResult;
import org.apereo.portal.concurrency.locking.LockOptions;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao;
import org.apereo.portal.jpa.BaseRawEventsJpaDao;
import org.apereo.portal.version.dao.VersionDao;
import org.apereo.portal.version.om.Version;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("portalEventAggregationManager")
public class PortalEventProcessingManagerImpl
        implements IPortalEventProcessingManager, HibernateCacheEvictor, DisposableBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private PortalEventDimensionPopulator portalEventDimensionPopulator;
    private PortalRawEventsAggregator portalEventAggregator;
    private PortalEventPurger portalEventPurger;
    private PortalEventSessionPurger portalEventSessionPurger;
    private IClusterLockService clusterLockService;
    private Map<String, Version> requiredProductVersions = Collections.emptyMap();
    private VersionDao versionDao;

    private long aggregateRawEventsPeriod = 0;
    private long purgeRawEventsPeriod = 0;
    private long purgeEventSessionsPeriod = 0;

    private final ThreadLocal<Map<Class<?>, Collection<Serializable>>> evictedEntitiesHolder =
            new ThreadLocal<Map<Class<?>, Collection<Serializable>>>() {
                @Override
                protected Map<Class<?>, Collection<Serializable>> initialValue() {
                    return new HashMap<Class<?>, Collection<Serializable>>();
                }
            };
    private volatile boolean shutdown = false;

    @Autowired
    public void setClusterLockService(IClusterLockService clusterLockService) {
        this.clusterLockService = clusterLockService;
    }

    @Autowired
    public void setPortalEventDimensionPopulator(
            PortalEventDimensionPopulator portalEventDimensionPopulator) {
        this.portalEventDimensionPopulator = portalEventDimensionPopulator;
    }

    @Autowired
    public void setPortalEventAggregator(PortalRawEventsAggregator portalEventAggregator) {
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

    @Value(
            "${org.apereo.portal.events.aggr.PortalEventProcessingManagerImpl.aggregateRawEventsPeriod}")
    public void setAggregateRawEventsPeriod(long aggregateRawEventsPeriod) {
        this.aggregateRawEventsPeriod = aggregateRawEventsPeriod;
    }

    @Value("${org.apereo.portal.events.aggr.PortalEventProcessingManagerImpl.purgeRawEventsPeriod}")
    public void setPurgeRawEventsPeriod(long purgeRawEventsPeriod) {
        this.purgeRawEventsPeriod = purgeRawEventsPeriod;
    }

    @Value(
            "${org.apereo.portal.events.aggr.PortalEventProcessingManagerImpl.purgeEventSessionsPeriod}")
    public void setPurgeEventSessionsPeriod(long purgeEventSessionsPeriod) {
        this.purgeEventSessionsPeriod = purgeEventSessionsPeriod;
    }

    @Resource(name = "productVersions")
    public void setRequiredProductVersions(Map<String, Version> requiredProductVersions) {
        this.requiredProductVersions = ImmutableMap.copyOf(requiredProductVersions);
    }

    @Autowired
    public void setVersionDao(VersionDao versionDao) {
        this.versionDao = versionDao;
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

        if (!this.checkDatabaseVersion(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)) {
            logger.info(
                    "The database and software versions for "
                            + BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME
                            + " do not match. No dimension population will be done");
            return false;
        }

        try {
            final TryLockFunctionResult<Object> result =
                    this.clusterLockService.doInTryLock(
                            PortalEventDimensionPopulator.DIMENSION_LOCK_NAME,
                            new FunctionWithoutResult<ClusterMutex>() {
                                @Override
                                protected void applyWithoutResult(ClusterMutex input) {
                                    portalEventDimensionPopulator.doPopulateDimensions();
                                }
                            });

            return result.getLockStatus() == LockStatus.EXECUTED;
        } catch (InterruptedException e) {
            logger.warn("Interrupted while populating dimensions", e);
            Thread.currentThread().interrupt();
            return false;
        } catch (RuntimeException e) {
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

        if (!this.checkDatabaseVersion(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)) {
            logger.info(
                    "The database and software versions for "
                            + BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME
                            + " do not match. No event aggregation will be done");
            return false;
        }

        if (!this.checkDatabaseVersion(BaseRawEventsJpaDao.PERSISTENCE_UNIT_NAME)) {
            logger.info(
                    "The database and software versions for "
                            + BaseRawEventsJpaDao.PERSISTENCE_UNIT_NAME
                            + " do not match. No event aggregation will be done");
            return false;
        }

        long aggregateLastRunDelay = (long) (this.aggregateRawEventsPeriod * .95);
        final long aggregateServerBiasDelay = this.aggregateRawEventsPeriod * 4;
        TryLockFunctionResult<EventProcessingResult> result = null;
        EventProcessingResult aggrResult = null;
        do {
            if (result != null) {
                logger.info(
                        "doAggregateRawEvents signaled that not all eligible events were aggregated in a single transaction, running aggregation again.");

                //Set aggr period to 0 to allow immediate re-run locally
                aggregateLastRunDelay = 0;
            }

            try {
                long start = System.nanoTime();

                //Try executing aggregation within lock
                result =
                        clusterLockService.doInTryLock(
                                PortalRawEventsAggregator.AGGREGATION_LOCK_NAME,
                                LockOptions.builder()
                                        .lastRunDelay(aggregateLastRunDelay)
                                        .serverBiasDelay(aggregateServerBiasDelay),
                                new Function<ClusterMutex, EventProcessingResult>() {
                                    @Override
                                    public EventProcessingResult apply(final ClusterMutex input) {
                                        return portalEventAggregator.doAggregateRawEvents();
                                    }
                                });
                aggrResult = result.getResult();

                //Check the result, warn if null
                if (result.getLockStatus() == LockStatus.EXECUTED && aggrResult == null) {
                    logger.warn("doAggregateRawEvents did not execute");
                } else if (aggrResult != null) {
                    if (logger.isInfoEnabled()) {
                        logResult(
                                "Aggregated {} events created at {} events/second between {} and {} in {}ms - {} e/s a {}x speedup.",
                                aggrResult,
                                start);
                    }

                    //If events were processed purge old aggregations from the cache and then clean unclosed aggregations
                    if (aggrResult.getProcessed() > 0) {
                        final Map<Class<?>, Collection<Serializable>> evictedEntities =
                                evictedEntitiesHolder.get();
                        if (evictedEntities.size() > 0) {
                            portalEventAggregator.evictAggregates(evictedEntities);
                        }

                        TryLockFunctionResult<EventProcessingResult> cleanAggrResult;
                        EventProcessingResult cleanResult;
                        do {
                            //Update start time so logging is accurate
                            start = System.nanoTime();
                            cleanAggrResult =
                                    clusterLockService.doInTryLock(
                                            PortalRawEventsAggregator.AGGREGATION_LOCK_NAME,
                                            new Function<ClusterMutex, EventProcessingResult>() {
                                                @Override
                                                public EventProcessingResult apply(
                                                        final ClusterMutex input) {
                                                    return portalEventAggregator
                                                            .doCloseAggregations();
                                                }
                                            });

                            cleanResult = cleanAggrResult.getResult();
                            if (cleanAggrResult.getLockStatus() == LockStatus.EXECUTED
                                    && cleanResult == null) {
                                logger.warn("doCloseAggregations was not executed");
                            } else if (cleanResult != null && logger.isInfoEnabled()) {
                                logResult(
                                        "Clean {} unclosed agregations created at {} aggrs/second between {} and {} in {}ms - {} a/s a {}x speedup",
                                        cleanResult,
                                        start);
                            }

                            //Loop if doCloseAggregations returns false, that means there is more to clean
                        } while (cleanAggrResult.getLockStatus() == LockStatus.EXECUTED
                                && cleanResult != null
                                && !cleanResult.isComplete());
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while aggregating", e);
                Thread.currentThread().interrupt();
                return false;
            } catch (RuntimeException e) {
                logger.error("aggregateRawEvents failed", e);
                throw e;
            } finally {
                //Make sure we clean up the thread local
                evictedEntitiesHolder.remove();
            }

            //Loop if doAggregateRawEvents returns false, this means that there is more to aggregate
        } while (result.getLockStatus() == LockStatus.EXECUTED
                && aggrResult != null
                && !aggrResult.isComplete());

        return result != null
                && result.getLockStatus() == LockStatus.EXECUTED
                && aggrResult != null
                && aggrResult.isComplete();
    }

    @Override
    public boolean purgeRawEvents() {
        if (shutdown) {
            logger.warn("purgeRawEvents called after shutdown, ignoring call");
            return false;
        }

        if (!this.checkDatabaseVersion(BaseRawEventsJpaDao.PERSISTENCE_UNIT_NAME)) {
            logger.info(
                    "The database and software versions for "
                            + BaseRawEventsJpaDao.PERSISTENCE_UNIT_NAME
                            + " do not match. No event purging will be done");
            return false;
        }

        long purgeLastRunDelay = (long) (this.purgeRawEventsPeriod * .95);
        TryLockFunctionResult<EventProcessingResult> result = null;
        EventProcessingResult purgeResult = null;
        do {
            if (result != null) {
                logger.debug(
                        "doPurgeRawEvents signaled that not all eligibe events were purged in a single transaction, running purge again.");

                //Set purge period to 0 to allow immediate re-run locally
                purgeLastRunDelay = 0;
            }

            try {
                final long start = System.nanoTime();

                result =
                        clusterLockService.doInTryLock(
                                PortalEventPurger.PURGE_RAW_EVENTS_LOCK_NAME,
                                LockOptions.builder().lastRunDelay(purgeLastRunDelay),
                                new Function<ClusterMutex, EventProcessingResult>() {
                                    @Override
                                    public EventProcessingResult apply(final ClusterMutex input) {
                                        return portalEventPurger.doPurgeRawEvents();
                                    }
                                });
                purgeResult = result.getResult();

                //Check the result, warn if null
                if (result.getLockStatus() == LockStatus.EXECUTED && purgeResult == null) {
                    logger.warn("doPurgeRawEvents did not execute");
                } else if (purgeResult != null && logger.isInfoEnabled()) {
                    logResult(
                            "Purged {} events created at {} events/second between {} and {} in {}ms - {} e/s a {}x speedup",
                            purgeResult,
                            start);
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while purging raw events", e);
                Thread.currentThread().interrupt();
                return false;
            } catch (RuntimeException e) {
                logger.error("purgeRawEvents failed", e);
                throw e;
            }

            //Loop if doPurgeRawEvents returns false, this means that there is more to purge
        } while (result.getLockStatus() == LockStatus.EXECUTED
                && purgeResult != null
                && !purgeResult.isComplete());

        return result != null
                && result.getLockStatus() == LockStatus.EXECUTED
                && purgeResult != null
                && purgeResult.isComplete();
    }

    @Override
    public boolean purgeEventSessions() {
        if (shutdown) {
            logger.warn("purgeEventSessions called after shutdown, ignoring call");
            return false;
        }

        if (!this.checkDatabaseVersion(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)) {
            logger.info(
                    "The database and software versions for "
                            + BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME
                            + " do not match. No event session purging will be done");
            return false;
        }

        try {
            final long start = System.nanoTime();

            final long purgeLastRunDelay = (long) (this.purgeEventSessionsPeriod * .95);
            final TryLockFunctionResult<EventProcessingResult> result =
                    clusterLockService.doInTryLock(
                            PortalEventSessionPurger.PURGE_EVENT_SESSION_LOCK_NAME,
                            LockOptions.builder().lastRunDelay(purgeLastRunDelay),
                            new Function<ClusterMutex, EventProcessingResult>() {
                                @Override
                                public EventProcessingResult apply(final ClusterMutex input) {
                                    return portalEventSessionPurger.doPurgeEventSessions();
                                }
                            });
            final EventProcessingResult purgeResult = result.getResult();

            //Check the result, warn if null
            if (result.getLockStatus() == LockStatus.EXECUTED && purgeResult == null) {
                logger.warn("doPurgeRawEvents did not execute");
            } else if (purgeResult != null && logger.isInfoEnabled()) {
                logResult(
                        "Purged {} event sessions created before {} in {}ms - {} sessions/second",
                        purgeResult,
                        start);
            }

            return result.getLockStatus() == LockStatus.EXECUTED
                    && purgeResult != null
                    && purgeResult.isComplete();
        } catch (InterruptedException e) {
            logger.warn("Interrupted while purging event sessions", e);
            Thread.currentThread().interrupt();
            return false;
        } catch (RuntimeException e) {
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

    /** Check if the database and software versions match */
    private boolean checkDatabaseVersion(String databaseName) {
        final Version softwareVersion = this.requiredProductVersions.get(databaseName);
        if (softwareVersion == null) {
            throw new IllegalStateException("No version number is configured for: " + databaseName);
        }

        final Version databaseVersion = this.versionDao.getVersion(databaseName);
        if (databaseVersion == null) {
            throw new IllegalStateException(
                    "No version number is exists in the database for: " + databaseName);
        }

        return softwareVersion.equals(databaseVersion);
    }

    private void logResult(String message, EventProcessingResult aggrResult, long start) {
        final long runTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        final double processRate = aggrResult.getProcessed() / (runTime / 1000d);

        final DateTime startDate = aggrResult.getStart();
        if (startDate != null) {
            final double creationRate = aggrResult.getCreationRate();
            final double processSpeedUp = processRate / creationRate;
            logger.info(
                    message,
                    new Object[] {
                        aggrResult.getProcessed(),
                        String.format("%.4f", creationRate),
                        startDate,
                        aggrResult.getEnd(),
                        runTime,
                        String.format("%.4f", processRate),
                        String.format("%.4f", processSpeedUp)
                    });
        } else {
            logger.info(
                    message,
                    new Object[] {
                        aggrResult.getProcessed(),
                        aggrResult.getEnd(),
                        runTime,
                        String.format("%.4f", processRate)
                    });
        }
    }
}
