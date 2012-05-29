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

package org.jasig.portal.events.aggr.concuser;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalHelper;
import org.jasig.portal.events.aggr.AggregationIntervalInfo;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.EventAggregationContext;
import org.jasig.portal.events.aggr.IPortalEventAggregator;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.session.EventSession;
import org.jasig.portal.jpa.BaseAggrEventsJpaDao.AggrEventsTransactional;
import org.jasig.portal.utils.cache.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Event aggregator that uses {@link ConcurrentUserAggregationPrivateDao} to aggregate concurrent user data 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ConcurrentUserAggregator implements IPortalEventAggregator<PortalEvent> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private ConcurrentUserAggregationPrivateDao concurrentUserAggregationDao;
    private AggregationIntervalHelper aggregationIntervalHelper;

    @Autowired
    public void setConcurrentUserAggregationDao(ConcurrentUserAggregationPrivateDao concurrentUserAggregationDao) {
        this.concurrentUserAggregationDao = concurrentUserAggregationDao;
    }
    
    @Autowired
    public void setAggregationIntervalHelper(AggregationIntervalHelper aggregationIntervalHelper) {
        this.aggregationIntervalHelper = aggregationIntervalHelper;
    }

    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return true;
    }

    @AggrEventsTransactional
    @Override
    public void aggregateEvent(PortalEvent e, EventSession eventSession,
            EventAggregationContext eventAggregationContext,
            Map<AggregationInterval, AggregationIntervalInfo> currentIntervals) {
        
        for (Map.Entry<AggregationInterval, AggregationIntervalInfo> intervalInfoEntry : currentIntervals.entrySet()) {
            final AggregationInterval interval = intervalInfoEntry.getKey();
            final AggregationIntervalInfo intervalInfo = intervalInfoEntry.getValue();
            final DateDimension dateDimension = intervalInfo.getDateDimension();
            final TimeDimension timeDimension = intervalInfo.getTimeDimension();
            
            final Set<AggregatedGroupMapping> groupMappings = new LinkedHashSet<AggregatedGroupMapping>(eventSession.getGroupMappings());
            
            final Set<ConcurrentUserAggregationImpl> cachedConcurrentUserAggregations = getConcurrentUserAggregations(eventAggregationContext,
                    interval,
                    dateDimension,
                    timeDimension);
        
            for (final ConcurrentUserAggregationImpl concurrentUserAggregation : cachedConcurrentUserAggregations) {
                //Remove the aggregation from the group set to mark that it has been updated
                groupMappings.remove(concurrentUserAggregation.getAggregatedGroup());
                updateAggregation(e, intervalInfo, concurrentUserAggregation);
            }
            
            //Create any left over groups
            if (!groupMappings.isEmpty()) {
                for (final AggregatedGroupMapping aggregatedGroup : groupMappings) {
                    final ConcurrentUserAggregationImpl concurrentUserAggregation = concurrentUserAggregationDao.createConcurrentUserAggregation(dateDimension, timeDimension, interval, aggregatedGroup);
                    cachedConcurrentUserAggregations.add(concurrentUserAggregation);
                    updateAggregation(e, intervalInfo, concurrentUserAggregation);
                }
            }
        }
    }

    @AggrEventsTransactional
    @Override
    public void handleIntervalBoundary(AggregationInterval interval, EventAggregationContext eventAggregationContext,
            Map<AggregationInterval, AggregationIntervalInfo> intervals) {
        
        final AggregationIntervalInfo intervalInfo = intervals.get(interval);
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        
        //Complete all of the login aggregations that have been touched by this session
        final Set<ConcurrentUserAggregationImpl> concurrentUserAggregations = this.getConcurrentUserAggregations(eventAggregationContext, interval, dateDimension, timeDimension);
        for (final ConcurrentUserAggregationImpl concurrentUserAggregation : concurrentUserAggregations) {
            final int duration = intervalInfo.getTotalDuration();
            concurrentUserAggregation.intervalComplete(duration);
            logger.debug("Marked complete: " + concurrentUserAggregation);
            this.concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregation);
        }
        
        //Look for any uncomplete aggregations from the previous interval
        final AggregationIntervalInfo prevIntervalInfo = this.aggregationIntervalHelper.getIntervalInfo(interval, intervalInfo.getStart().minusMinutes(1));
        final Set<ConcurrentUserAggregationImpl> unclosedConcurrentUserAggregations = this.concurrentUserAggregationDao.getUnclosedConcurrentUserAggregations(prevIntervalInfo.getStart(), prevIntervalInfo.getEnd(), interval);
        for (final ConcurrentUserAggregationImpl concurrentUserAggregation : unclosedConcurrentUserAggregations) {
            final int duration = intervalInfo.getTotalDuration();
            concurrentUserAggregation.intervalComplete(duration);
            logger.debug("Marked complete previously missed: " + concurrentUserAggregation);
            this.concurrentUserAggregationDao.updateConcurrentUserAggregation(concurrentUserAggregation);
        }
    }

    /**
     * Get the set of existing concurrent user aggregations looking first in the aggregation session and then in the db
     */
    private Set<ConcurrentUserAggregationImpl> getConcurrentUserAggregations(EventAggregationContext eventAggregationContext,
            final AggregationInterval interval, final DateDimension dateDimension, final TimeDimension timeDimension) {
        
        final CacheKey key = CacheKey.build(this.getClass().getName(), dateDimension.getDate(), timeDimension.getTime(), interval);
        Set<ConcurrentUserAggregationImpl> cachedConcurrentUserAggregations = eventAggregationContext.getAttribute(key);
        if (cachedConcurrentUserAggregations == null) {
            //Nothing in the aggr session yet, cache the current set of concurrentUserAggregationDao.getConcurrentUserAggregationsForInterval() from the DB in the aggr session
            final Set<ConcurrentUserAggregationImpl> concurrentUserAggregations = this.concurrentUserAggregationDao.getConcurrentUserAggregationsForInterval(dateDimension, timeDimension, interval);
            cachedConcurrentUserAggregations = new HashSet<ConcurrentUserAggregationImpl>(concurrentUserAggregations);
            eventAggregationContext.setAttribute(key, cachedConcurrentUserAggregations);
        }
        
        return cachedConcurrentUserAggregations;
    }

    private void updateAggregation(PortalEvent e, final AggregationIntervalInfo intervalInfo, final ConcurrentUserAggregationImpl concurrentUserAggregation) {
        final String eventSessionId = e.getEventSessionId();
        final int duration = intervalInfo.getDurationTo(e.getTimestampAsDate());
        concurrentUserAggregation.setDuration(duration);
        concurrentUserAggregation.countSession(eventSessionId);
    }
}
