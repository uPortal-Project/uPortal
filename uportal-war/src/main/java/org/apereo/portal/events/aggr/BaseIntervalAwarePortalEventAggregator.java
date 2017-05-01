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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.session.EventSession;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao.AggrEventsTransactional;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base {@link PortalEvent} aggregator, useful for aggregations that extend from {@link
 * BaseAggregationImpl}
 *
 * @param <E> The {@link PortalEvent} type handled by this aggregator
 * @param <T> The {@link BaseAggregationImpl} subclass operated on by this aggregator
 * @param <K> The {@link BaseAggregationKey} type used by this aggregator
 */
public abstract class BaseIntervalAwarePortalEventAggregator<
                E extends PortalEvent,
                T extends BaseAggregationImpl<K, ?>,
                K extends BaseAggregationKey>
        extends BasePortalEventAggregator<E> implements IntervalAwarePortalEventAggregator<E> {

    private final String aggregationsCacheKey =
            this.getClass().getName() + ".AGGREGATIONS_FOR_INTERVAL";
    private AggregationIntervalHelper aggregationIntervalHelper;

    @Autowired
    public void setAggregationIntervalHelper(AggregationIntervalHelper aggregationIntervalHelper) {
        this.aggregationIntervalHelper = aggregationIntervalHelper;
    }

    /** @return The private aggregation DAO to use */
    protected abstract BaseAggregationPrivateDao<T, K> getAggregationDao();

    /**
     * Called for each {@link BaseAggregationImpl} that needs to be updated
     *
     * @param e The {@link PortalEvent} to get the data from
     * @param intervalInfo The info about the interval the aggregation is for
     * @param aggregation The aggregation to update
     */
    protected abstract void updateAggregation(
            E e,
            EventAggregationContext eventAggregationContext,
            AggregationIntervalInfo intervalInfo,
            T aggregation);

    /**
     * Create a unique key that describes the aggregation.
     *
     * @param intervalInfo The info about the interval the aggregation is for
     * @param aggregatedGroup The group the aggregation is for
     * @param e The event the aggregation is for
     */
    protected abstract K createAggregationKey(
            E e,
            EventAggregationContext eventAggregationContext,
            AggregationIntervalInfo intervalInfo,
            AggregatedGroupMapping aggregatedGroup);

    @AggrEventsTransactional
    @Override
    public final void aggregateEvent(
            E e,
            EventSession eventSession,
            EventAggregationContext eventAggregationContext,
            Map<AggregationInterval, AggregationIntervalInfo> currentIntervals) {

        final BaseAggregationPrivateDao<T, K> aggregationDao = this.getAggregationDao();

        for (Map.Entry<AggregationInterval, AggregationIntervalInfo> intervalInfoEntry :
                currentIntervals.entrySet()) {
            final AggregationIntervalInfo intervalInfo = intervalInfoEntry.getValue();

            //Map used to cache aggregations locally after loading
            Map<K, T> aggregationsCache =
                    eventAggregationContext.getAttribute(this.aggregationsCacheKey);
            if (aggregationsCache == null) {
                aggregationsCache = new HashMap<K, T>();
                eventAggregationContext.setAttribute(this.aggregationsCacheKey, aggregationsCache);
            }

            //Groups this event is for
            final Set<AggregatedGroupMapping> groupMappings = eventSession.getGroupMappings();

            //For each group get/create then update the aggregation
            for (final AggregatedGroupMapping groupMapping : groupMappings) {
                final K key =
                        this.createAggregationKey(
                                e, eventAggregationContext, intervalInfo, groupMapping);

                //Load the aggregation, try from the cache first
                T aggregation = aggregationsCache.get(key);
                if (aggregation == null) {
                    //Then try loading from the db
                    aggregation = aggregationDao.getAggregation(key);
                    if (aggregation == null) {
                        //Finally create the aggregation
                        aggregation = aggregationDao.createAggregation(key);
                    }

                    //Store the loaded/created aggregation in the local cache
                    aggregationsCache.put(key, aggregation);
                }

                //Update the aggregation with the event
                updateAggregation(e, eventAggregationContext, intervalInfo, aggregation);
            }
        }
    }

    @AggrEventsTransactional
    @Override
    public final void handleIntervalBoundary(
            AggregationInterval interval,
            EventAggregationContext eventAggregationContext,
            Map<AggregationInterval, AggregationIntervalInfo> intervals) {

        final AggregationIntervalInfo intervalInfo = intervals.get(interval);

        final BaseAggregationPrivateDao<T, K> aggregationDao = this.getAggregationDao();

        //Complete all of the aggregations that have been touched by this session, can be null if no events of
        //the handled type have been seen so far in this session
        Map<K, T> aggregationsForInterval =
                eventAggregationContext.getAttribute(this.aggregationsCacheKey);
        if (aggregationsForInterval == null) {
            //No aggregations have been seen in this interval, nothing to do
            return;
        }

        //Tracks the aggregations that need to be updated, estimate size based on intervals/aggregations ratio
        final Collection<T> updatedAggregations =
                new ArrayList<T>(aggregationsForInterval.size() / intervals.size());

        //Mark each aggregation that matches the interval complete and remove it from the map of tracked aggregations
        final Collection<T> aggregations = aggregationsForInterval.values();
        for (final Iterator<T> aggregationItr = aggregations.iterator();
                aggregationItr.hasNext();
                ) {
            final T aggregation = aggregationItr.next();
            if (aggregation.getInterval() == interval) {
                final int duration = intervalInfo.getTotalDuration();
                aggregation.intervalComplete(duration);
                aggregationItr.remove();
                updatedAggregations.add(aggregation);
            }
        }

        //Instruct the DAO to remove the aggregation from cache after updating, once closed it will never be visited again
        aggregationDao.updateAggregations(updatedAggregations, true);
    }

    @AggrEventsTransactional
    @Override
    public int cleanUnclosedAggregations(
            DateTime start, DateTime end, AggregationInterval interval) {
        final BaseAggregationPrivateDao<T, K> aggregationDao = this.getAggregationDao();
        final Collection<T> unclosedAggregations =
                aggregationDao.getUnclosedAggregations(start, end, interval);
        for (final T aggregation : unclosedAggregations) {
            final DateTime eventDate = aggregation.getDateTime();
            final AggregationIntervalInfo unclosedIntervalInfo =
                    this.aggregationIntervalHelper.getIntervalInfo(interval, eventDate);
            aggregation.intervalComplete(unclosedIntervalInfo.getTotalDuration());
        }
        aggregationDao.updateAggregations(unclosedAggregations, true);

        return unclosedAggregations.size();
    }
}
