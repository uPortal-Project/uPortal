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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.session.EventSession;
import org.jasig.portal.jpa.BaseAggrEventsJpaDao.AggrEventsTransactional;
import org.jasig.portal.utils.cache.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base {@link PortalEvent} aggregator, useful for aggregations that extend from {@link BaseAggregationImpl} 
 * 
 * @author Eric Dalquist
 * @param <E>
 * @param <T>
 */
public abstract class BasePortalEventAggregator<E extends PortalEvent, T extends BaseAggregationImpl> implements IPortalEventAggregator<E> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private AggregationIntervalHelper aggregationIntervalHelper;

    @Autowired
    public final void setAggregationIntervalHelper(AggregationIntervalHelper aggregationIntervalHelper) {
        this.aggregationIntervalHelper = aggregationIntervalHelper;
    }
    
    protected abstract BaseAggregationPrivateDao<T> getAggregationDao();

    @AggrEventsTransactional
    @Override
    public final void aggregateEvent(E e, EventSession eventSession,
            EventAggregationContext eventAggregationContext,
            Map<AggregationInterval, AggregationIntervalInfo> currentIntervals) {
        
        final BaseAggregationPrivateDao<T> aggregationDao = this.getAggregationDao();
        
        for (Map.Entry<AggregationInterval, AggregationIntervalInfo> intervalInfoEntry : currentIntervals.entrySet()) {
            final AggregationInterval interval = intervalInfoEntry.getKey();
            final AggregationIntervalInfo intervalInfo = intervalInfoEntry.getValue();
            final DateDimension dateDimension = intervalInfo.getDateDimension();
            final TimeDimension timeDimension = intervalInfo.getTimeDimension();
            
            final Collection<T> cachedAggregations = getAggregations(eventAggregationContext, interval, dateDimension, timeDimension);
            
            //If the number of cached aggregations != number of group mappings we need to figure out which groups don't have an
            //aggregation yet. This is done by cloning the groupMappings map and removing groups we see from it as the cached
            //aggregations are parsed
            final Set<AggregatedGroupMapping> groupMappings = eventSession.getGroupMappings();
            final Set<AggregatedGroupMapping> mutableGroupMappings;
            if (groupMappings.size() != cachedAggregations.size()) {
                mutableGroupMappings = new LinkedHashSet<AggregatedGroupMapping>(groupMappings);
            }
            else {
                mutableGroupMappings = null;
            }
        
            //Update the aggregation for each cached aggr
            for (final T aggregation : cachedAggregations) {
                //Remove the aggregation from the group set to mark that it has been updated
                if (mutableGroupMappings != null) {
                    mutableGroupMappings.remove(aggregation.getAggregatedGroup());
                }
                
                updateAggregation(e, intervalInfo, aggregation);
            }
            
            //Create aggregations for any left over groups
            if (mutableGroupMappings != null && !mutableGroupMappings.isEmpty()) {
                for (final AggregatedGroupMapping aggregatedGroup : mutableGroupMappings) {
                    final T aggregation = aggregationDao.createAggregation(dateDimension, timeDimension, interval, aggregatedGroup);
                    cachedAggregations.add(aggregation);
                    updateAggregation(e, intervalInfo, aggregation);
                }
            }
        }
    }

    @AggrEventsTransactional
    @Override
    public final void handleIntervalBoundary(AggregationInterval interval, EventAggregationContext eventAggregationContext,
            Map<AggregationInterval, AggregationIntervalInfo> intervals) {
        
        final AggregationIntervalInfo intervalInfo = intervals.get(interval);
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        
        final BaseAggregationPrivateDao<T> aggregationDao = this.getAggregationDao();
        
        //Complete all of the aggregations that have been touched by this session
        final Collection<T> aggregations = this.getAggregations(eventAggregationContext, interval, dateDimension, timeDimension);
        for (final T loginAggregation : aggregations) {
            final int duration = intervalInfo.getTotalDuration();
            loginAggregation.intervalComplete(duration);
            logger.debug("Marked complete: " + loginAggregation);
            aggregationDao.updateAggregation(loginAggregation);
        }
        
        //Look for any uncomplete aggregations from the previous interval
        final AggregationIntervalInfo prevIntervalInfo = this.aggregationIntervalHelper.getIntervalInfo(interval, intervalInfo.getStart().minusMinutes(1));
        final Collection<T> unclosedLoginAggregations = aggregationDao.getUnclosedAggregations(prevIntervalInfo.getStart(), prevIntervalInfo.getEnd(), interval);
        for (final T aggregation : unclosedLoginAggregations) {
            final int duration = intervalInfo.getTotalDuration();
            aggregation.intervalComplete(duration);
            logger.debug("Marked complete previously missed: " + aggregation);
            aggregationDao.updateAggregation(aggregation);
        }
    }

    /**
     * Get the set of existing aggregations looking first in the aggregation session and then in the db
     */
    private Collection<T> getAggregations(EventAggregationContext eventAggregationContext,
            final AggregationInterval interval, final DateDimension dateDimension, final TimeDimension timeDimension) {
        
        final CacheKey key = CacheKey.build(this.getClass().getName(), dateDimension.getDate(), timeDimension.getTime(), interval);
        Collection<T> cachedAggregations = eventAggregationContext.getAttribute(key);
        if (cachedAggregations == null) {
            //Nothing in the aggr session yet, cache the current set of aggregations from the DB in the aggr session
            cachedAggregations = this.getAggregationDao().getAggregationsForInterval(dateDimension, timeDimension, interval);
            eventAggregationContext.setAttribute(key, cachedAggregations);
        }
        
        return cachedAggregations;
    }

    /**
     * Called for each {@link BaseAggregationImpl} that needs to be updated 
     * 
     * @param e The {@link PortalEvent} to get the data from
     * @param intervalInfo The info about the interval the aggregation is for
     * @param aggregation The aggregation to update
     */
    protected abstract void updateAggregation(E e, AggregationIntervalInfo intervalInfo, T aggregation);
}
