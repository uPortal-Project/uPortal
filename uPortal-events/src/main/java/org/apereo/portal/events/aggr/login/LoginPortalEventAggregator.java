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
package org.apereo.portal.events.aggr.login;

import org.apereo.portal.events.LoginEvent;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.AggregationIntervalInfo;
import org.apereo.portal.events.aggr.BaseAggregationPrivateDao;
import org.apereo.portal.events.aggr.BaseIntervalAwarePortalEventAggregator;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.EventAggregationContext;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Event aggregator that uses {@link LoginAggregationPrivateDao} to aggregate login events
 *
 */
public class LoginPortalEventAggregator
        extends BaseIntervalAwarePortalEventAggregator<
                LoginEvent, LoginAggregationImpl, LoginAggregationKey> {
    private LoginAggregationPrivateDao loginAggregationDao;

    @Autowired
    public void setLoginAggregationDao(LoginAggregationPrivateDao loginAggregationDao) {
        this.loginAggregationDao = loginAggregationDao;
    }

    @Override
    protected BaseAggregationPrivateDao<LoginAggregationImpl, LoginAggregationKey>
            getAggregationDao() {
        return this.loginAggregationDao;
    }

    @Override
    protected LoginAggregationKey createAggregationKey(
            LoginEvent e,
            EventAggregationContext eventAggregationContext,
            AggregationIntervalInfo intervalInfo,
            AggregatedGroupMapping aggregatedGroup) {
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        return new LoginAggregationKeyImpl(
                dateDimension, timeDimension, aggregationInterval, aggregatedGroup);
    }

    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return LoginEvent.class.isAssignableFrom(type);
    }

    @Override
    protected void updateAggregation(
            LoginEvent e,
            EventAggregationContext eventAggregationContext,
            AggregationIntervalInfo intervalInfo,
            LoginAggregationImpl aggregation) {
        final String userName = e.getUserName();
        final int duration = intervalInfo.getDurationTo(e.getTimestampAsDate());
        aggregation.setDuration(duration);
        aggregation.countUser(userName);
    }
}
