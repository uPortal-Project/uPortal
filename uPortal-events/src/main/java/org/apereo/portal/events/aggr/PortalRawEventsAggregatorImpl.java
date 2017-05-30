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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.Serializable;
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
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang.mutable.MutableObject;
import org.apereo.portal.IPortalInfoProvider;
import org.apereo.portal.concurrency.locking.IClusterLockService;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.aggr.dao.DateDimensionDao;
import org.apereo.portal.events.aggr.dao.IEventAggregationManagementDao;
import org.apereo.portal.events.aggr.session.EventSession;
import org.apereo.portal.events.aggr.session.EventSessionDao;
import org.apereo.portal.events.handlers.db.IPortalEventDao;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao;
import org.apereo.portal.jpa.BaseRawEventsJpaDao.RawEventsTransactional;
import org.apereo.portal.spring.context.ApplicationEventFilter;
import org.apereo.portal.utils.cache.CacheKey;
import org.hibernate.Cache;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

@Service
public class PortalRawEventsAggregatorImpl extends BaseAggrEventsJpaDao
        implements PortalRawEventsAggregator, DisposableBean {
    private static final String EVENT_SESSION_CACHE_KEY_SOURCE =
            AggregateEventsHandler.class.getName() + "-EventSession";

    private IClusterLockService clusterLockService;
    private IPortalEventProcessingManager portalEventAggregationManager;
    private PortalEventDimensionPopulator portalEventDimensionPopulator;
    private IEventAggregationManagementDao eventAggregationManagementDao;
    private IPortalInfoProvider portalInfoProvider;
    private IPortalEventDao portalEventDao;
    private AggregationIntervalHelper intervalHelper;
    private EventSessionDao eventSessionDao;
    private DateDimensionDao dateDimensionDao;
    private Set<IntervalAwarePortalEventAggregator<PortalEvent>>
            intervalAwarePortalEventAggregators = Collections.emptySet();
    private Set<SimplePortalEventAggregator<PortalEvent>> simplePortalEventAggregators =
            Collections.emptySet();
    private List<ApplicationEventFilter<PortalEvent>> applicationEventFilters =
            Collections.emptyList();

    private int eventAggregationBatchSize = 10000;
    private int intervalAggregationBatchSize = 5;
    private int cleanUnclosedAggregationsBatchSize = 1000;
    private int cleanUnclosedIntervalsBatchSize = 315;
    private ReadablePeriod aggregationDelay = Period.seconds(30);

    private final Map<Class<?>, List<String>> entityCollectionRoles =
            new HashMap<Class<?>, List<String>>();
    private volatile boolean shutdown = false;

    @Autowired
    public void setDateDimensionDao(DateDimensionDao dateDimensionDao) {
        this.dateDimensionDao = dateDimensionDao;
    }

    @Autowired
    public void setPortalEventAggregationManager(
            IPortalEventProcessingManager portalEventAggregationManager) {
        this.portalEventAggregationManager = portalEventAggregationManager;
    }

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
    public void setEventAggregationManagementDao(
            IEventAggregationManagementDao eventAggregationManagementDao) {
        this.eventAggregationManagementDao = eventAggregationManagementDao;
    }

    @Autowired
    public void setPortalInfoProvider(IPortalInfoProvider portalInfoProvider) {
        this.portalInfoProvider = portalInfoProvider;
    }

    @Autowired
    public void setPortalEventDao(IPortalEventDao portalEventDao) {
        this.portalEventDao = portalEventDao;
    }

    @Autowired
    public void setIntervalHelper(AggregationIntervalHelper intervalHelper) {
        this.intervalHelper = intervalHelper;
    }

    @Autowired
    public void setEventSessionDao(EventSessionDao eventSessionDao) {
        this.eventSessionDao = eventSessionDao;
    }

    @Autowired
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setPortalEventAggregators(
            Set<IPortalEventAggregator<PortalEvent>> portalEventAggregators) {
        final com.google.common.collect.ImmutableSet.Builder<
                        IntervalAwarePortalEventAggregator<PortalEvent>>
                intervalAwarePortalEventAggregatorsBuilder = ImmutableSet.builder();
        final com.google.common.collect.ImmutableSet.Builder<
                        SimplePortalEventAggregator<PortalEvent>>
                simplePortalEventAggregatorsBuilder = ImmutableSet.builder();

        for (final IPortalEventAggregator<PortalEvent> portalEventAggregator :
                portalEventAggregators) {
            if (portalEventAggregator instanceof IntervalAwarePortalEventAggregator) {
                intervalAwarePortalEventAggregatorsBuilder.add(
                        (IntervalAwarePortalEventAggregator) portalEventAggregator);
            } else if (portalEventAggregator instanceof SimplePortalEventAggregator) {
                simplePortalEventAggregatorsBuilder.add(
                        (SimplePortalEventAggregator) portalEventAggregator);
            }
        }

        this.intervalAwarePortalEventAggregators =
                intervalAwarePortalEventAggregatorsBuilder.build();
        this.simplePortalEventAggregators = simplePortalEventAggregatorsBuilder.build();
    }

    @Resource(name = "aggregatorEventFilters")
    public void setApplicationEventFilters(
            List<ApplicationEventFilter<PortalEvent>> applicationEventFilters) {
        this.applicationEventFilters = applicationEventFilters;
    }

    @Value("${org.apereo.portal.events.aggr.PortalRawEventsAggregatorImpl.aggregationDelay:PT30S}")
    public void setAggregationDelay(ReadablePeriod aggregationDelay) {
        this.aggregationDelay = aggregationDelay;
    }

    @Value(
            "${org.apereo.portal.events.aggr.PortalRawEventsAggregatorImpl.eventAggregationBatchSize:10000}")
    public void setEventAggregationBatchSize(int eventAggregationBatchSize) {
        this.eventAggregationBatchSize = eventAggregationBatchSize;
    }

    @Value(
            "${org.apereo.portal.events.aggr.PortalRawEventsAggregatorImpl.intervalAggregationBatchSize:5}")
    public void setIntervalAggregationBatchSize(int intervalAggregationBatchSize) {
        this.intervalAggregationBatchSize = intervalAggregationBatchSize;
    }

    @Value(
            "${org.apereo.portal.events.aggr.PortalRawEventsAggregatorImpl.cleanUnclosedAggregationsBatchSize:1000}")
    public void setCleanUnclosedAggregationsBatchSize(int cleanUnclosedAggregationsBatchSize) {
        this.cleanUnclosedAggregationsBatchSize = cleanUnclosedAggregationsBatchSize;
    }

    @Value(
            "${org.apereo.portal.events.aggr.PortalRawEventsAggregatorImpl.cleanUnclosedIntervalsBatchSize:300}")
    public void setCleanUnclosedIntervalsBatchSize(int cleanUnclosedIntervalsBatchSize) {
        this.cleanUnclosedIntervalsBatchSize = cleanUnclosedIntervalsBatchSize;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    @Override
    public void destroy() throws Exception {
        this.shutdown = true;
    }

    private void checkShutdown() {
        if (shutdown) {
            //Mark ourselves as interupted and throw an exception
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "uPortal is shutting down, throwing an exception to stop processing");
        }
    }

    @RawEventsTransactional
    @Override
    public EventProcessingResult doAggregateRawEvents() {
        //Do RawTX around AggrTX. The AggrTX is MUCH more likely to fail than the RawTX and this results in both rolling back
        return this.getTransactionOperations()
                .execute(
                        new TransactionCallback<EventProcessingResult>() {
                            @Override
                            public EventProcessingResult doInTransaction(TransactionStatus status) {
                                return doAggregateRawEventsInternal();
                            }
                        });
    }

    @AggrEventsTransactional
    @Override
    public void evictAggregates(Map<Class<?>, Collection<Serializable>> entitiesToEvict) {
        int evictedEntities = 0;
        int evictedCollections = 0;

        final Session session = getEntityManager().unwrap(Session.class);
        final SessionFactory sessionFactory = session.getSessionFactory();
        final Cache cache = sessionFactory.getCache();

        for (final Entry<Class<?>, Collection<Serializable>> evictedEntityEntry :
                entitiesToEvict.entrySet()) {
            final Class<?> entityClass = evictedEntityEntry.getKey();
            final List<String> collectionRoles = getCollectionRoles(sessionFactory, entityClass);

            for (final Serializable id : evictedEntityEntry.getValue()) {
                cache.evictEntity(entityClass, id);
                evictedEntities++;

                for (final String collectionRole : collectionRoles) {
                    cache.evictCollection(collectionRole, id);
                    evictedCollections++;
                }
            }
        }

        logger.debug(
                "Evicted {} entities and {} collections from hibernate caches",
                evictedEntities,
                evictedCollections);
    }

    @Override
    @AggrEventsTransactional
    public EventProcessingResult doCloseAggregations() {
        if (!this.clusterLockService.isLockOwner(AGGREGATION_LOCK_NAME)) {
            throw new IllegalStateException(
                    "The cluster lock "
                            + AGGREGATION_LOCK_NAME
                            + " must be owned by the current thread and server");
        }

        final IEventAggregatorStatus cleanUnclosedStatus =
                eventAggregationManagementDao.getEventAggregatorStatus(
                        IEventAggregatorStatus.ProcessingType.CLEAN_UNCLOSED, true);

        //Update status with current server name
        final String serverName = this.portalInfoProvider.getUniqueServerName();
        cleanUnclosedStatus.setServerName(serverName);
        cleanUnclosedStatus.setLastStart(new DateTime());

        //Determine date of most recently aggregated data
        final IEventAggregatorStatus eventAggregatorStatus =
                eventAggregationManagementDao.getEventAggregatorStatus(
                        IEventAggregatorStatus.ProcessingType.AGGREGATION, false);
        if (eventAggregatorStatus == null || eventAggregatorStatus.getLastEventDate() == null) {
            //Nothing has been aggregated, skip unclosed cleanup

            cleanUnclosedStatus.setLastEnd(new DateTime());
            eventAggregationManagementDao.updateEventAggregatorStatus(cleanUnclosedStatus);

            return new EventProcessingResult(0, null, null, true);
        }

        final DateTime lastAggregatedDate = eventAggregatorStatus.getLastEventDate();

        //If lastCleanUnclosedDate is null use the oldest date dimension as there can be
        //no aggregations that exist before it
        final DateTime lastCleanUnclosedDate;
        if (cleanUnclosedStatus.getLastEventDate() == null) {
            final DateDimension oldestDateDimension =
                    this.dateDimensionDao.getOldestDateDimension();
            lastCleanUnclosedDate = oldestDateDimension.getDate().toDateTime();
        } else {
            lastCleanUnclosedDate = cleanUnclosedStatus.getLastEventDate();
        }

        if (!(lastCleanUnclosedDate.isBefore(lastAggregatedDate))) {
            logger.debug(
                    "No events aggregated since last unclosed aggregation cleaning, skipping clean: {}",
                    lastAggregatedDate);
            return new EventProcessingResult(0, lastCleanUnclosedDate, lastAggregatedDate, true);
        }

        //Switch to flush on commit to avoid flushes during queries
        final EntityManager entityManager = this.getEntityManager();
        entityManager.flush();
        entityManager.setFlushMode(FlushModeType.COMMIT);

        //Track the number of closed aggregations and the last date of a cleaned interval
        int closedAggregations = 0;
        int cleanedIntervals = 0;
        DateTime cleanUnclosedEnd;

        final Thread currentThread = Thread.currentThread();
        final String currentName = currentThread.getName();
        try {
            currentThread.setName(
                    currentName + "-" + lastCleanUnclosedDate + "-" + lastAggregatedDate);

            //Local caches used to reduce db io
            final IntervalsForAggregatorHelper intervalsForAggregatorHelper =
                    new IntervalsForAggregatorHelper();
            final Map<AggregationInterval, AggregationIntervalInfo> previousIntervals =
                    new HashMap<AggregationInterval, AggregationIntervalInfo>();

            //A DateTime within the next interval to close aggregations in
            DateTime nextIntervalDate = lastCleanUnclosedDate;
            do {
                //Reset our goal of catching up to the last aggregated event on every iteration
                cleanUnclosedEnd = lastAggregatedDate;

                //For each interval the aggregator supports, cleanup the unclosed aggregations
                for (final AggregationInterval interval :
                        intervalsForAggregatorHelper.getHandledIntervals()) {
                    final AggregationIntervalInfo previousInterval =
                            previousIntervals.get(interval);
                    if (previousInterval != null
                            && nextIntervalDate.isBefore(previousInterval.getEnd())) {
                        logger.debug(
                                "{} interval before {} has already been cleaned during this execution, ignoring",
                                interval,
                                previousInterval.getEnd());
                        continue;
                    }

                    //The END date of the last clean session will find us the next interval to clean
                    final AggregationIntervalInfo nextIntervalToClean =
                            intervalHelper.getIntervalInfo(interval, nextIntervalDate);
                    previousIntervals.put(interval, nextIntervalToClean);
                    if (nextIntervalToClean == null) {
                        continue;
                    }

                    final DateTime start = nextIntervalToClean.getStart();
                    final DateTime end = nextIntervalToClean.getEnd();
                    if (!end.isBefore(lastAggregatedDate)) {
                        logger.debug(
                                "{} interval between {} and {} is still active, ignoring",
                                new Object[] {interval, start, end});
                        continue;
                    }

                    //Track the oldest interval end, this ensures that nothing is missed
                    if (end.isBefore(cleanUnclosedEnd)) {
                        cleanUnclosedEnd = end;
                    }

                    logger.debug(
                            "Cleaning unclosed {} aggregations between {} and {}",
                            new Object[] {interval, start, end});

                    for (final IntervalAwarePortalEventAggregator<PortalEvent>
                            portalEventAggregator : intervalAwarePortalEventAggregators) {
                        checkShutdown();

                        final Class<? extends IPortalEventAggregator<?>> aggregatorType =
                                getClass(portalEventAggregator);

                        //Get aggregator specific interval info config
                        final AggregatedIntervalConfig aggregatorIntervalConfig =
                                intervalsForAggregatorHelper.getAggregatorIntervalConfig(
                                        aggregatorType);

                        //If the aggregator is being used for the specified interval call cleanUnclosedAggregations
                        if (aggregatorIntervalConfig.isIncluded(interval)) {
                            closedAggregations +=
                                    portalEventAggregator.cleanUnclosedAggregations(
                                            start, end, interval);
                        }
                    }

                    cleanedIntervals++;
                }

                //Set the next interval to the end date from the last aggregation run
                nextIntervalDate = cleanUnclosedEnd;

                logger.debug(
                        "Closed {} aggregations across {} interval before {} with goal of {}",
                        new Object[] {
                            closedAggregations,
                            cleanedIntervals,
                            cleanUnclosedEnd,
                            lastAggregatedDate
                        });
                //Loop until either the batchSize of cleaned aggregations has been reached or no aggregation work is done
            } while (closedAggregations <= cleanUnclosedAggregationsBatchSize
                    && cleanedIntervals <= cleanUnclosedIntervalsBatchSize
                    && cleanUnclosedEnd.isBefore(lastAggregatedDate));
        } finally {
            currentThread.setName(currentName);
        }

        //Update the status object and store it
        cleanUnclosedStatus.setLastEventDate(cleanUnclosedEnd);
        cleanUnclosedStatus.setLastEnd(new DateTime());
        eventAggregationManagementDao.updateEventAggregatorStatus(cleanUnclosedStatus);

        return new EventProcessingResult(
                closedAggregations,
                lastCleanUnclosedDate,
                lastAggregatedDate,
                !cleanUnclosedEnd.isBefore(lastAggregatedDate));
    }

    @SuppressWarnings("unchecked")
    protected final <T> Class<T> getClass(T object) {
        return (Class<T>) AopProxyUtils.ultimateTargetClass(object);
    }

    private List<String> getCollectionRoles(
            final SessionFactory sessionFactory, final Class<?> entityClass) {
        List<String> collectionRoles = entityCollectionRoles.get(entityClass);
        if (collectionRoles != null) {
            return collectionRoles;
        }

        final com.google.common.collect.ImmutableList.Builder<String> collectionRolesBuilder =
                ImmutableList.builder();
        final ClassMetadata classMetadata = sessionFactory.getClassMetadata(entityClass);
        for (final Type type : classMetadata.getPropertyTypes()) {
            if (type.isCollectionType()) {
                collectionRolesBuilder.add(((CollectionType) type).getRole());
            }
        }

        collectionRoles = collectionRolesBuilder.build();
        entityCollectionRoles.put(entityClass, collectionRoles);

        return collectionRoles;
    }

    private EventProcessingResult doAggregateRawEventsInternal() {
        if (!this.clusterLockService.isLockOwner(AGGREGATION_LOCK_NAME)) {
            throw new IllegalStateException(
                    "The cluster lock "
                            + AGGREGATION_LOCK_NAME
                            + " must be owned by the current thread and server");
        }

        if (!this.portalEventDimensionPopulator.isCheckedDimensions()) {
            //First time aggregation has happened, run populateDimensions to ensure enough dimension data exists
            final boolean populatedDimensions =
                    this.portalEventAggregationManager.populateDimensions();
            if (!populatedDimensions) {
                this.logger.warn(
                        "Aborting raw event aggregation, populateDimensions returned false so the state of date/time dimensions is unknown");
                return null;
            }
        }

        //Flush any dimension creation before aggregation
        final EntityManager entityManager = this.getEntityManager();
        entityManager.flush();
        entityManager.setFlushMode(FlushModeType.COMMIT);

        final IEventAggregatorStatus eventAggregatorStatus =
                eventAggregationManagementDao.getEventAggregatorStatus(
                        IEventAggregatorStatus.ProcessingType.AGGREGATION, true);

        //Update status with current server name
        final String serverName = this.portalInfoProvider.getUniqueServerName();
        final String previousServerName = eventAggregatorStatus.getServerName();
        if (previousServerName != null && !serverName.equals(previousServerName)) {
            this.logger.debug(
                    "Last aggregation run on {} clearing all aggregation caches",
                    previousServerName);
            final Session session = getEntityManager().unwrap(Session.class);
            final Cache cache = session.getSessionFactory().getCache();
            cache.evictEntityRegions();
        }

        eventAggregatorStatus.setServerName(serverName);

        //Calculate date range for aggregation
        DateTime lastAggregated = eventAggregatorStatus.getLastEventDate();
        if (lastAggregated == null) {
            lastAggregated = portalEventDao.getOldestPortalEventTimestamp();

            //No portal events to aggregate, skip aggregation
            if (lastAggregated == null) {
                return new EventProcessingResult(0, null, null, true);
            }

            //First time aggregation has run, initialize the CLEAN_UNCLOSED status to save catch-up time
            final IEventAggregatorStatus cleanUnclosedStatus =
                    eventAggregationManagementDao.getEventAggregatorStatus(
                            IEventAggregatorStatus.ProcessingType.CLEAN_UNCLOSED, true);
            AggregationIntervalInfo oldestMinuteInterval =
                    this.intervalHelper.getIntervalInfo(AggregationInterval.MINUTE, lastAggregated);
            cleanUnclosedStatus.setLastEventDate(oldestMinuteInterval.getStart().minusMinutes(1));
            eventAggregationManagementDao.updateEventAggregatorStatus(cleanUnclosedStatus);
        }

        final DateTime newestEventTime =
                DateTime.now().minus(this.aggregationDelay).secondOfMinute().roundFloorCopy();

        final Thread currentThread = Thread.currentThread();
        final String currentName = currentThread.getName();
        final MutableInt events = new MutableInt();
        final MutableObject lastEventDate = new MutableObject(newestEventTime);

        boolean complete;
        try {
            currentThread.setName(currentName + "-" + lastAggregated + "_" + newestEventTime);

            logger.debug(
                    "Starting aggregation of events between {} (inc) and {} (exc)",
                    lastAggregated,
                    newestEventTime);

            //Do aggregation, capturing the start and end dates
            eventAggregatorStatus.setLastStart(DateTime.now());

            complete =
                    portalEventDao.aggregatePortalEvents(
                            lastAggregated,
                            newestEventTime,
                            this.eventAggregationBatchSize,
                            new AggregateEventsHandler(
                                    events, lastEventDate, eventAggregatorStatus));

            eventAggregatorStatus.setLastEventDate((DateTime) lastEventDate.getValue());
            eventAggregatorStatus.setLastEnd(DateTime.now());
        } finally {
            currentThread.setName(currentName);
        }

        //Store the results of the aggregation
        eventAggregationManagementDao.updateEventAggregatorStatus(eventAggregatorStatus);

        complete =
                complete
                        && (this.eventAggregationBatchSize <= 0
                                || events.intValue() < this.eventAggregationBatchSize);
        return new EventProcessingResult(
                events.intValue(),
                lastAggregated,
                eventAggregatorStatus.getLastEventDate(),
                complete);
    }

    /**
     * Helper class that loads and caches the interval configuration for each aggregator as well as
     * the union of intervals handled by the set of aggregators.
     */
    private final class IntervalsForAggregatorHelper {
        private final Map<Class<? extends IPortalEventAggregator<?>>, AggregatedIntervalConfig>
                aggregatorIntervalConfigsCache =
                        new HashMap<
                                Class<? extends IPortalEventAggregator<?>>,
                                AggregatedIntervalConfig>();
        private final AggregatedIntervalConfig defaultAggregatedIntervalConfig;
        private final Set<AggregationInterval> handledIntervals;

        public IntervalsForAggregatorHelper() {
            this.defaultAggregatedIntervalConfig =
                    eventAggregationManagementDao.getDefaultAggregatedIntervalConfig();

            //Create the set of intervals that are actually being aggregated
            final Set<AggregationInterval> handledIntervalsNotIncluded =
                    EnumSet.allOf(AggregationInterval.class);
            final Set<AggregationInterval> handledIntervalsBuilder =
                    EnumSet.noneOf(AggregationInterval.class);
            for (final IntervalAwarePortalEventAggregator<PortalEvent> portalEventAggregator :
                    intervalAwarePortalEventAggregators) {
                final Class<? extends IPortalEventAggregator<?>> aggregatorType =
                        PortalRawEventsAggregatorImpl.this.getClass(portalEventAggregator);

                //Get aggregator specific interval info config
                final AggregatedIntervalConfig aggregatorIntervalConfig =
                        this.getAggregatorIntervalConfig(aggregatorType);

                for (final Iterator<AggregationInterval> intervalsIterator =
                                handledIntervalsNotIncluded.iterator();
                        intervalsIterator.hasNext();
                        ) {
                    final AggregationInterval interval = intervalsIterator.next();
                    if (aggregatorIntervalConfig.isIncluded(interval)) {
                        handledIntervalsBuilder.add(interval);
                        intervalsIterator.remove();
                    }
                }
            }

            handledIntervals = Sets.immutableEnumSet(handledIntervalsBuilder);
        }

        /**
         * @return All of the intervals that are actually handled by the current set of aggregators
         */
        public Set<AggregationInterval> getHandledIntervals() {
            return handledIntervals;
        }

        /**
         * @return The interval config for the aggregator, returns the default config if no
         *     aggregator specific config is set
         */
        public AggregatedIntervalConfig getAggregatorIntervalConfig(
                final Class<? extends IPortalEventAggregator<?>> aggregatorType) {
            AggregatedIntervalConfig config = aggregatorIntervalConfigsCache.get(aggregatorType);
            if (config != null) {
                return config;
            }

            config = eventAggregationManagementDao.getAggregatedIntervalConfig(aggregatorType);
            if (config == null) {
                config = defaultAggregatedIntervalConfig;
            }
            aggregatorIntervalConfigsCache.put(aggregatorType, config);
            return config;
        }
    }

    private final class AggregateEventsHandler implements Function<PortalEvent, Boolean> {
        //Event Aggregation Context - used by aggregators to track state
        private final EventAggregationContext eventAggregationContext =
                new EventAggregationContextImpl();
        private final MutableInt eventCounter;
        private final MutableObject lastEventDate;
        private final IEventAggregatorStatus eventAggregatorStatus;
        private int intervalsCrossed = 0;

        //Local tracking of the current aggregation interval and info about said interval
        private final Map<AggregationInterval, AggregationIntervalInfo> currentIntervalInfo =
                new EnumMap<AggregationInterval, AggregationIntervalInfo>(
                        AggregationInterval.class);

        //Local caches of per-aggregator config data, shouldn't ever change for the duration of an aggregation run
        private final IntervalsForAggregatorHelper intervalsForAggregatorHelper =
                new IntervalsForAggregatorHelper();
        private final Map<Class<? extends IPortalEventAggregator<?>>, AggregatedGroupConfig>
                aggregatorGroupConfigs =
                        new HashMap<
                                Class<? extends IPortalEventAggregator<?>>,
                                AggregatedGroupConfig>();
        private final Map<
                        Class<? extends IPortalEventAggregator<?>>,
                        Map<AggregationInterval, AggregationIntervalInfo>>
                aggregatorReadOnlyIntervalInfo =
                        new HashMap<
                                Class<? extends IPortalEventAggregator<?>>,
                                Map<AggregationInterval, AggregationIntervalInfo>>();
        private final AggregatedGroupConfig defaultAggregatedGroupConfig;

        private AggregateEventsHandler(
                MutableInt eventCounter,
                MutableObject lastEventDate,
                IEventAggregatorStatus eventAggregatorStatus) {
            this.eventCounter = eventCounter;
            this.lastEventDate = lastEventDate;
            this.eventAggregatorStatus = eventAggregatorStatus;
            this.defaultAggregatedGroupConfig =
                    eventAggregationManagementDao.getDefaultAggregatedGroupConfig();
        }

        @Override
        public Boolean apply(PortalEvent event) {
            if (shutdown) {
                //Mark ourselves as interupted and throw an exception
                Thread.currentThread().interrupt();
                throw new RuntimeException(
                        "uPortal is shutting down, throwing an exeption to stop aggregation");
            }

            final DateTime eventDate = event.getTimestampAsDate();
            this.lastEventDate.setValue(eventDate);

            //If no interval data yet populate it.
            if (this.currentIntervalInfo.isEmpty()) {
                initializeIntervalInfo(eventDate);
            }

            //Check each interval to see if an interval boundary has been crossed
            boolean intervalCrossed = false;
            for (final AggregationInterval interval :
                    this.intervalsForAggregatorHelper.getHandledIntervals()) {
                AggregationIntervalInfo intervalInfo = this.currentIntervalInfo.get(interval);
                if (intervalInfo != null
                        && !intervalInfo
                                .getEnd()
                                .isAfter(
                                        eventDate)) { //if there is no IntervalInfo that interval must not be supported in the current environment
                    logger.debug("Crossing {} Interval, triggered by {}", interval, event);
                    this.doHandleIntervalBoundary(interval, this.currentIntervalInfo);

                    intervalInfo = intervalHelper.getIntervalInfo(interval, eventDate);
                    this.currentIntervalInfo.put(interval, intervalInfo);

                    this.aggregatorReadOnlyIntervalInfo
                            .clear(); //Clear out cached per-aggregator interval info whenever a current interval info changes

                    intervalCrossed = true;
                }
            }
            if (intervalCrossed) {
                this.intervalsCrossed++;

                //If we have crossed more intervals than the interval batch size return false to stop aggregation before handling the triggering event
                if (this.intervalsCrossed >= intervalAggregationBatchSize) {
                    return false;
                }
            }

            //Aggregate the event
            this.doAggregateEvent(event);

            //Update the status object with the event date
            this.lastEventDate.setValue(eventDate);

            //Continue processing
            return true;
        }

        private void initializeIntervalInfo(final DateTime eventDate) {
            final DateTime intervalDate;
            final DateTime lastEventDate = this.eventAggregatorStatus.getLastEventDate();
            if (lastEventDate != null) {
                //If there was a previously aggregated event use that date to make sure an interval is not missed
                intervalDate = lastEventDate;
            } else {
                //Otherwise just use the current event date
                intervalDate = eventDate;
            }

            for (final AggregationInterval interval :
                    this.intervalsForAggregatorHelper.getHandledIntervals()) {
                final AggregationIntervalInfo intervalInfo =
                        intervalHelper.getIntervalInfo(interval, intervalDate);
                if (intervalInfo != null) {
                    this.currentIntervalInfo.put(interval, intervalInfo);
                } else {
                    this.currentIntervalInfo.remove(interval);
                }
            }
        }

        private void doAggregateEvent(PortalEvent item) {
            checkShutdown();

            eventCounter.increment();

            for (final ApplicationEventFilter<PortalEvent> applicationEventFilter :
                    applicationEventFilters) {
                if (!applicationEventFilter.supports(item)) {
                    logger.trace(
                            "Skipping event {} - {} excluded by filter {}",
                            new Object[] {eventCounter, item, applicationEventFilter});
                    return;
                }
            }
            logger.trace("Aggregating event {} - {}", eventCounter, item);

            //Load or create the event session
            EventSession eventSession = getEventSession(item);

            //Give each interval aware aggregator a chance at the event
            for (final IntervalAwarePortalEventAggregator<PortalEvent> portalEventAggregator :
                    intervalAwarePortalEventAggregators) {
                if (checkSupports(portalEventAggregator, item)) {
                    final Class<? extends IPortalEventAggregator<?>> aggregatorType =
                            PortalRawEventsAggregatorImpl.this.getClass(portalEventAggregator);

                    //Get aggregator specific interval info map
                    final Map<AggregationInterval, AggregationIntervalInfo> aggregatorIntervalInfo =
                            this.getAggregatorIntervalInfo(aggregatorType);

                    //If there is an event session get the aggregator specific version of it
                    if (eventSession != null) {
                        final AggregatedGroupConfig aggregatorGroupConfig =
                                getAggregatorGroupConfig(aggregatorType);

                        final CacheKey key =
                                CacheKey.build(
                                        EVENT_SESSION_CACHE_KEY_SOURCE,
                                        eventSession,
                                        aggregatorGroupConfig);
                        EventSession filteredEventSession =
                                this.eventAggregationContext.getAttribute(key);
                        if (filteredEventSession == null) {
                            filteredEventSession =
                                    new FilteredEventSession(eventSession, aggregatorGroupConfig);
                            this.eventAggregationContext.setAttribute(key, filteredEventSession);
                        }
                        eventSession = filteredEventSession;
                    }

                    //Aggregation magic happens here!
                    portalEventAggregator.aggregateEvent(
                            item, eventSession, eventAggregationContext, aggregatorIntervalInfo);
                }
            }

            //Give each simple aggregator a chance at the event
            for (final SimplePortalEventAggregator<PortalEvent> portalEventAggregator :
                    simplePortalEventAggregators) {
                if (checkSupports(portalEventAggregator, item)) {
                    portalEventAggregator.aggregateEvent(item, eventSession);
                }
            }
        }

        /**
         * @deprecated This method exists until uPortal 4.1 when
         *     IPortalEventAggregator#supports(Class) can be deleted
         */
        @Deprecated
        protected boolean checkSupports(
                IPortalEventAggregator<PortalEvent> portalEventAggregator, PortalEvent item) {
            try {
                return portalEventAggregator.supports(item);
            } catch (AbstractMethodError e) {
                return portalEventAggregator.supports(item.getClass());
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

        private void doHandleIntervalBoundary(
                AggregationInterval interval,
                Map<AggregationInterval, AggregationIntervalInfo> intervals) {
            for (final IntervalAwarePortalEventAggregator<PortalEvent> portalEventAggregator :
                    intervalAwarePortalEventAggregators) {

                final Class<? extends IPortalEventAggregator<?>> aggregatorType =
                        PortalRawEventsAggregatorImpl.this.getClass(portalEventAggregator);
                final AggregatedIntervalConfig aggregatorIntervalConfig =
                        this.intervalsForAggregatorHelper.getAggregatorIntervalConfig(
                                aggregatorType);

                //If the aggreagator is configured to use the interval notify it of the interval boundary
                if (aggregatorIntervalConfig.isIncluded(interval)) {
                    final Map<AggregationInterval, AggregationIntervalInfo> aggregatorIntervalInfo =
                            this.getAggregatorIntervalInfo(aggregatorType);
                    portalEventAggregator.handleIntervalBoundary(
                            interval, eventAggregationContext, aggregatorIntervalInfo);
                }
            }
        }

        /** @return The interval info map for the aggregator */
        protected Map<AggregationInterval, AggregationIntervalInfo> getAggregatorIntervalInfo(
                final Class<? extends IPortalEventAggregator<?>> aggregatorType) {
            final AggregatedIntervalConfig aggregatorIntervalConfig =
                    this.intervalsForAggregatorHelper.getAggregatorIntervalConfig(aggregatorType);

            Map<AggregationInterval, AggregationIntervalInfo> intervalInfo =
                    this.aggregatorReadOnlyIntervalInfo.get(aggregatorType);
            if (intervalInfo == null) {
                final Builder<AggregationInterval, AggregationIntervalInfo> intervalInfoBuilder =
                        ImmutableMap.builder();

                for (Map.Entry<AggregationInterval, AggregationIntervalInfo> intervalInfoEntry :
                        this.currentIntervalInfo.entrySet()) {
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
         * @return The group config for the aggregator, returns the default config if no aggregator
         *     specific config is set
         */
        protected AggregatedGroupConfig getAggregatorGroupConfig(
                final Class<? extends IPortalEventAggregator<?>> aggregatorType) {
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
    }
}
