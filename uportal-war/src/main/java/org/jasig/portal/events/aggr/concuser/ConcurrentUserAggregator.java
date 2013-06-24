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

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalInfo;
import org.jasig.portal.events.aggr.BaseAggregationPrivateDao;
import org.jasig.portal.events.aggr.BaseIntervalAwarePortalEventAggregator;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.EventAggregationContext;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Event aggregator that uses {@link ConcurrentUserAggregationPrivateDao} to aggregate concurrent user data 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ConcurrentUserAggregator extends BaseIntervalAwarePortalEventAggregator<PortalEvent, ConcurrentUserAggregationImpl, ConcurrentUserAggregationKey> {
    private ConcurrentUserAggregationPrivateDao concurrentUserAggregationDao;

    @Autowired
    public void setConcurrentUserAggregationDao(ConcurrentUserAggregationPrivateDao concurrentUserAggregationDao) {
        this.concurrentUserAggregationDao = concurrentUserAggregationDao;
    }

    @Override
    protected BaseAggregationPrivateDao<ConcurrentUserAggregationImpl, ConcurrentUserAggregationKey> getAggregationDao() {
        return this.concurrentUserAggregationDao;
    }
    
    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return true;
    }
    
    @Override
    protected void updateAggregation(PortalEvent e, EventAggregationContext eventAggregationContext,
            AggregationIntervalInfo intervalInfo, ConcurrentUserAggregationImpl aggregation) {
        final String eventSessionId = e.getEventSessionId();
        final int duration = intervalInfo.getDurationTo(e.getTimestampAsDate());
        aggregation.setDuration(duration);
        aggregation.countSession(eventSessionId);
    }
    
    @Override
    protected ConcurrentUserAggregationKey createAggregationKey(PortalEvent e,
            EventAggregationContext eventAggregationContext, AggregationIntervalInfo intervalInfo,
            AggregatedGroupMapping aggregatedGroup) {
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        return new ConcurrentUserAggregationKeyImpl(dateDimension, timeDimension, aggregationInterval, aggregatedGroup);
    }
}
