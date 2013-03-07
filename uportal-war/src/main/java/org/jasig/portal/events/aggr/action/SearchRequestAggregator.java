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
package org.jasig.portal.events.aggr.action;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.PortletActionExecutionEvent;
import org.jasig.portal.events.PortletEventExecutionEvent;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalInfo;
import org.jasig.portal.events.aggr.BaseAggregationPrivateDao;
import org.jasig.portal.events.aggr.BasePortalEventAggregator;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.EventAggregationContext;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletLookupDao;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.jasig.portal.events.aggr.session.EventSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chris Waymire (chris@waymire.net)
 */
public class SearchRequestAggregator extends BasePortalEventAggregator<PortletActionExecutionEvent, SearchRequestAggregationImpl, SearchRequestAggregationKey> {
    private static final String TARGET_FNAME = "search";
    private static final String TARGET_PARAM = "query";

    private SearchRequestAggregationPrivateDao searchRequestAggregationDao;

    @Autowired
    public void setSearchRequestAggregationDao(SearchRequestAggregationPrivateDao searchRequestAggregationDao) {
        this.searchRequestAggregationDao = searchRequestAggregationDao;
    }

    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return PortletActionExecutionEvent.class.isAssignableFrom(type);
    }

    @Override
    public boolean supports(PortalEvent event)
    {
        if(event instanceof PortletActionExecutionEvent)
        {
            PortletActionExecutionEvent paee = (PortletActionExecutionEvent)event;
            if(paee.getFname().equals(TARGET_FNAME))
            {
                Map<String,List<String>> params = paee.getParameters();
                if(params.containsKey(TARGET_PARAM))
                {
                    if((params.get(TARGET_PARAM) != null) && !params.get(TARGET_PARAM).isEmpty())
                    {
                        if(!StringUtils.isEmpty(params.get(TARGET_PARAM).get(0)))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected BaseAggregationPrivateDao<SearchRequestAggregationImpl, SearchRequestAggregationKey> getAggregationDao() {
        return this.searchRequestAggregationDao;
    }

    @Override
    protected void updateAggregation(PortletActionExecutionEvent e, EventAggregationContext eventAggregationContext,
                                     AggregationIntervalInfo intervalInfo, SearchRequestAggregationImpl aggregation) {
        final int duration = intervalInfo.getDurationTo(e.getTimestampAsDate());
        aggregation.setDuration(duration);
        aggregation.increment();
    }

    @Override
    protected SearchRequestAggregationKey createAggregationKey(PortletActionExecutionEvent e,
                                                               EventAggregationContext eventAggregationContext, AggregationIntervalInfo intervalInfo,
                                                               AggregatedGroupMapping aggregatedGroup) {

        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        String query = e.getParameters().get(TARGET_PARAM).get(0);
        SearchRequestAggregationKey key = new SearchRequestAggregationKeyImpl(dateDimension,timeDimension,aggregationInterval,aggregatedGroup,query);
        return key;
    }
}
