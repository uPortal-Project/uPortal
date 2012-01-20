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

package org.jasig.portal.events.aggr.login;

import java.util.Map;
import java.util.Set;

import org.jasig.portal.events.LoginEvent;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.IPortalEventAggregator;
import org.jasig.portal.events.aggr.Interval;
import org.jasig.portal.events.aggr.IntervalInfo;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.session.EventSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event aggregator that uses {@link LoginAggregationPrivateDao} to aggregate login events 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class LoginPortalEventAggregator implements IPortalEventAggregator<LoginEvent> {
    private LoginAggregationPrivateDao loginAggregationDao;
    private AggregatedGroupLookupDao aggregatedGroupLookupDao;

    @Autowired
    public void setAggregatedGroupLookupDao(AggregatedGroupLookupDao aggregatedGroupLookupDao) {
        this.aggregatedGroupLookupDao = aggregatedGroupLookupDao;
    }

    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return LoginEvent.class.isAssignableFrom(type);
    }

    @Autowired
    public void setLoginAggregationDao(LoginAggregationPrivateDao loginAggregationDao) {
        this.loginAggregationDao = loginAggregationDao;
    }

    @Transactional("aggrEvents")
    @Override
    public void aggregateEvent(LoginEvent e, EventSession eventSession, Map<Interval, IntervalInfo> currentIntervals) {
        final Set<String> groups = e.getGroups();
        final String userName = e.getUserName();
        
        for (final String groupKey : groups) {
            final AggregatedGroupMapping aggregatedGroup = this.aggregatedGroupLookupDao.getGroupMapping(groupKey);
        
            for (Map.Entry<Interval, IntervalInfo> intervalInfoEntry : currentIntervals.entrySet()) {
                final Interval interval = intervalInfoEntry.getKey();
                final IntervalInfo intervalInfo = intervalInfoEntry.getValue();
                final DateDimension dateDimension = intervalInfo.getDateDimension();
                final TimeDimension timeDimension = intervalInfo.getTimeDimension();
            
                LoginAggregationImpl loginAggregation = loginAggregationDao.getLoginAggregation(dateDimension, timeDimension, interval, aggregatedGroup);
                if (loginAggregation == null) {
                    loginAggregation = loginAggregationDao.createLoginAggregation(dateDimension, timeDimension, interval, aggregatedGroup);
                    final int duration = intervalInfo.getDuration();
                    loginAggregation.setDuration(duration);
                }
                
                loginAggregation.countUser(userName);
            }
        }
    }

    @Transactional("aggrEvents")
    @Override
    public void handleIntervalBoundary(Interval interval, Map<Interval, IntervalInfo> intervals) {
        final IntervalInfo intervalInfo = intervals.get(interval);
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        
        final Set<LoginAggregationImpl> loginAggregations = this.loginAggregationDao.getLoginAggregationsForInterval(dateDimension, timeDimension, interval);
        for (final LoginAggregationImpl loginAggregation : loginAggregations) {
            final int duration = intervalInfo.getDuration();
            loginAggregation.intervalComplete(duration);
            this.loginAggregationDao.updateLoginAggregation(loginAggregation);
        }
    }
}
