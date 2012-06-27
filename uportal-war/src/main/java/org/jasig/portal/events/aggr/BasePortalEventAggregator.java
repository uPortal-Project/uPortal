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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.hibernate.Cache;
import org.hibernate.Session;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.session.EventSession;
import org.jasig.portal.jpa.BaseAggrEventsJpaDao.AggrEventsTransactional;
import org.jasig.portal.utils.cache.CacheKey;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.UnmodifiableIterator;

/**
 * Base {@link PortalEvent} aggregator, useful for aggregations that extend from {@link BaseAggregationImpl} 
 * 
 * @author Eric Dalquist
 * @param <E> The {@link PortalEvent} type handled by this aggregator
 * @param <T> The {@link BaseAggregationImpl} subclass operated on by this aggregator 
 * @param <K> The {@link BaseAggregationKey} type used by this aggregator
 */
public abstract class BasePortalEventAggregator<
            E extends PortalEvent, 
            T extends BaseAggregationImpl<K>,
            K extends BaseAggregationKey> 
    implements IPortalEventAggregator<E> {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final String cacheKeySource = this.getClass().getName();
    private final String overallCacheKeySource = cacheKeySource + "_COMPOSITE";
    private AggregationIntervalHelper aggregationIntervalHelper;
//    private JpaCachePurger
    
    @Autowired
    public void setAggregationIntervalHelper(AggregationIntervalHelper aggregationIntervalHelper) {
        this.aggregationIntervalHelper = aggregationIntervalHelper;
    }

    /**
     * @return The private aggregation DAO to use
     */
    protected abstract BaseAggregationPrivateDao<T, K> getAggregationDao();

    /**
     * Called for each {@link BaseAggregationImpl} that needs to be updated 
     * 
     * @param e The {@link PortalEvent} to get the data from
     * @param intervalInfo The info about the interval the aggregation is for
     * @param aggregation The aggregation to update
     */
    protected abstract void updateAggregation(E e, EventAggregationContext eventAggregationContext, AggregationIntervalInfo intervalInfo, T aggregation);
    
    /**
     * Create a unique key that describes the aggregation.
     * 
     * @param intervalInfo The info about the interval the aggregation is for
     * @param aggregatedGroup The group the aggregation is for
     * @param event The event the aggregation is for
     */
    protected abstract K createAggregationKey(E e, EventAggregationContext eventAggregationContext, AggregationIntervalInfo intervalInfo, AggregatedGroupMapping aggregatedGroup);
    
    /**
     * Create a cache key with which to store the data returned by {@link BaseAggregationDao#getAggregationsForInterval(DateDimension, TimeDimension, AggregationInterval)}
     */
    protected CacheKey createSessionCacheKey(AggregationIntervalInfo intervalInfo, E event) {
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        
        return CacheKey.build(cacheKeySource, dateDimension, timeDimension, aggregationInterval);
    } 

    @AggrEventsTransactional
    @Override
    public final void aggregateEvent(E e, EventSession eventSession,
            EventAggregationContext eventAggregationContext,
            Map<AggregationInterval, AggregationIntervalInfo> currentIntervals) {
        
        final BaseAggregationPrivateDao<T, K> aggregationDao = this.getAggregationDao();
        
        for (Map.Entry<AggregationInterval, AggregationIntervalInfo> intervalInfoEntry : currentIntervals.entrySet()) {
            final AggregationIntervalInfo intervalInfo = intervalInfoEntry.getValue();

            //Get all data for this (date + time + interval)
            final Map<K, T> aggregationsForInterval = getOrLoadAggregations(eventAggregationContext, intervalInfo, e);

            //Groups this event is for
            final Set<AggregatedGroupMapping> groupMappings = eventSession.getGroupMappings();
            
            //For each group get/create then update the aggregation
            for (final AggregatedGroupMapping groupMapping : groupMappings) {
                final K key = this.createAggregationKey(e, eventAggregationContext, intervalInfo, groupMapping);
                T aggregation = aggregationsForInterval.get(key);
                if (aggregation == null) {
                    aggregation = aggregationDao.getAggregation(key);
                    if (aggregation == null) {
                        aggregation = aggregationDao.createAggregation(key);
                    }
                    aggregationsForInterval.put(key, aggregation);
                }
                updateAggregation(e, eventAggregationContext, intervalInfo, aggregation);
            }
        }
    }

    @AggrEventsTransactional
    @Override
    public final void handleIntervalBoundary(AggregationInterval interval, EventAggregationContext eventAggregationContext,
            Map<AggregationInterval, AggregationIntervalInfo> intervals) {
        
        final AggregationIntervalInfo intervalInfo = intervals.get(interval);
        
        final BaseAggregationPrivateDao<T, K> aggregationDao = this.getAggregationDao();
        
        //Complete all of the aggregations that have been touched by this session, can be null if no events of
        //the handled type have been seen so far in this session
        final Iterable<T> aggregations = this.getCachedAggregations(eventAggregationContext, intervalInfo);
        for (final T aggregation : aggregations) {
            final int duration = intervalInfo.getTotalDuration();
            aggregation.intervalComplete(duration);
        }
        aggregationDao.updateAggregations(aggregations, true);
    }

    @AggrEventsTransactional
    @Override
    public int cleanUnclosedAggregations(AggregationInterval interval, DateTime end) {
        final BaseAggregationPrivateDao<T, K> aggregationDao = this.getAggregationDao();
        final Collection<T> unclosedAggregations = aggregationDao.getUnclosedAggregations(end, interval);
        for (final T aggregation : unclosedAggregations) {
            final DateTime eventDate = aggregation.getTimeDimension().getTime().toDateTime(aggregation.getDateDimension().getDate());
            final AggregationIntervalInfo unclosedIntervalInfo = this.aggregationIntervalHelper.getIntervalInfo(interval, eventDate);
            aggregation.intervalComplete(unclosedIntervalInfo.getTotalDuration());
        }
        aggregationDao.updateAggregations(unclosedAggregations, true);
        
        return unclosedAggregations.size();
    }

    /**
     * Get the set of existing aggregations looking first in the aggregation session and then in the db
     */
    private Map<K, T> getOrLoadAggregations(EventAggregationContext eventAggregationContext, AggregationIntervalInfo intervalInfo, E event) {
        final CacheKey eventScopedCacheKey = this.createSessionCacheKey(intervalInfo, event);
        
        Map<K, T> aggregationsForInterval = eventAggregationContext.getAttribute(eventScopedCacheKey);
        if (aggregationsForInterval == null) {
//            final DateDimension dateDimension = intervalInfo.getDateDimension();
//            final TimeDimension timeDimension = intervalInfo.getTimeDimension();
//            final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
            
//            aggregationsForInterval = this.getAggregationDao().getAggregationsForInterval(dateDimension, timeDimension, aggregationInterval);
            aggregationsForInterval = new HashMap<K, T>();
            eventAggregationContext.setAttribute(eventScopedCacheKey, aggregationsForInterval);
            
            final CacheKey eventScopedKeysCacheKey = createAggregationSessionCacheKey(intervalInfo);
            Set<CacheKey> eventScopedKeys = eventAggregationContext.getAttribute(eventScopedKeysCacheKey);
            if (eventScopedKeys == null) {
                eventScopedKeys = new HashSet<CacheKey>();
                eventAggregationContext.setAttribute(eventScopedKeysCacheKey, eventScopedKeys);
            }
            eventScopedKeys.add(eventScopedCacheKey);
        }
        return aggregationsForInterval;
    }

    /**
     * Get the set of existing aggregations from the aggregation session
     */
    private Iterable<T> getCachedAggregations(final EventAggregationContext eventAggregationContext, AggregationIntervalInfo intervalInfo) {
        final CacheKey eventScopedKeysCacheKey = createAggregationSessionCacheKey(intervalInfo);
        final Set<CacheKey> eventScopedKeys = eventAggregationContext.getAttribute(eventScopedKeysCacheKey);
        if (eventScopedKeys == null || eventScopedKeys.isEmpty()) {
            return Collections.emptySet();
        }
        
        //Use a little iterator of iterator pattern to avoid extra trips over the collection of cached aggregations
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new UnmodifiableIterator<T>() {
                    final Iterator<CacheKey> eventScopedKeysItr = eventScopedKeys.iterator();
                    Iterator<T> cachedAggregationsItr;
                    
                    @Override
                    public boolean hasNext() {
                        checkState();
                        
                        return cachedAggregationsItr != null && cachedAggregationsItr.hasNext();
                    }

                    @Override
                    public T next() {
                        checkState();
                        
                        return cachedAggregationsItr.next();
                    }

                    private void checkState() {
                        while ((cachedAggregationsItr == null || !cachedAggregationsItr.hasNext()) && eventScopedKeysItr.hasNext()) {
                            final CacheKey cachedAggregationsCacheKey = eventScopedKeysItr.next();
                            final Map<K, T> aggregationsForInterval = eventAggregationContext.getAttribute(cachedAggregationsCacheKey);
                            cachedAggregationsItr = aggregationsForInterval.values().iterator();
                        }
                    }
                };
            }
        };
    }

    /**
     * Create the CacheKey used to store data in the aggregation session
     */
    private CacheKey createAggregationSessionCacheKey(AggregationIntervalInfo intervalInfo) {
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        
        return CacheKey.build(overallCacheKeySource, dateDimension, timeDimension, aggregationInterval);
    }
}
