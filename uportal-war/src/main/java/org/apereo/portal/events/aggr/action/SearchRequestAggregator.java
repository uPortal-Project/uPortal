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
package org.apereo.portal.events.aggr.action;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.PortletActionExecutionEvent;
import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.AggregationIntervalInfo;
import org.apereo.portal.events.aggr.BaseAggregationPrivateDao;
import org.apereo.portal.events.aggr.BaseIntervalAwarePortalEventAggregator;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.EventAggregationContext;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.springframework.beans.factory.annotation.Autowired;

public class SearchRequestAggregator
        extends BaseIntervalAwarePortalEventAggregator<
                PortletActionExecutionEvent, SearchRequestAggregationImpl,
                SearchRequestAggregationKey> {
    private static final String TARGET_FNAME = "search";
    private static final String TARGET_PARAM = "query";

    private SearchRequestAggregationPrivateDao searchRequestAggregationDao;

    @Autowired
    public void setSearchRequestAggregationDao(
            SearchRequestAggregationPrivateDao searchRequestAggregationDao) {
        this.searchRequestAggregationDao = searchRequestAggregationDao;
    }

    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return PortletActionExecutionEvent.class.isAssignableFrom(type);
    }

    @Override
    public boolean supports(PortalEvent event) {
        if (event instanceof PortletActionExecutionEvent) {
            PortletActionExecutionEvent paee = (PortletActionExecutionEvent) event;
            if (paee.getFname().equals(TARGET_FNAME)) {
                Map<String, List<String>> params = paee.getParameters();
                if (params.containsKey(TARGET_PARAM)) {
                    if ((params.get(TARGET_PARAM) != null) && !params.get(TARGET_PARAM).isEmpty()) {
                        if (!StringUtils.isBlank(params.get(TARGET_PARAM).get(0))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected BaseAggregationPrivateDao<SearchRequestAggregationImpl, SearchRequestAggregationKey>
            getAggregationDao() {
        return this.searchRequestAggregationDao;
    }

    @Override
    protected void updateAggregation(
            PortletActionExecutionEvent e,
            EventAggregationContext eventAggregationContext,
            AggregationIntervalInfo intervalInfo,
            SearchRequestAggregationImpl aggregation) {
        final int duration = intervalInfo.getDurationTo(e.getTimestampAsDate());
        aggregation.setDuration(duration);
        aggregation.increment();
    }

    @Override
    protected SearchRequestAggregationKey createAggregationKey(
            PortletActionExecutionEvent e,
            EventAggregationContext eventAggregationContext,
            AggregationIntervalInfo intervalInfo,
            AggregatedGroupMapping aggregatedGroup) {

        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        String query = e.getParameters().get(TARGET_PARAM).get(0);
        SearchRequestAggregationKey key =
                new SearchRequestAggregationKeyImpl(
                        dateDimension, timeDimension, aggregationInterval, aggregatedGroup, query);
        return key;
    }
}
