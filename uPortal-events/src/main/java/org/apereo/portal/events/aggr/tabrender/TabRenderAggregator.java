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
package org.apereo.portal.events.aggr.tabrender;

import java.util.HashMap;
import java.util.Map;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.PortalRenderEvent;
import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.AggregationIntervalInfo;
import org.apereo.portal.events.aggr.BaseAggregationPrivateDao;
import org.apereo.portal.events.aggr.BaseIntervalAwarePortalEventAggregator;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.EventAggregationContext;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.tabs.AggregatedTabLookupDao;
import org.apereo.portal.events.aggr.tabs.AggregatedTabMapping;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Event aggregator that uses {@link TabRenderAggregationPrivateDao} to aggregate tab renders
 *
 */
public class TabRenderAggregator
        extends BaseIntervalAwarePortalEventAggregator<
                PortalRenderEvent, TabRenderAggregationImpl, TabRenderAggregationKey> {
    private static final String MAPPED_TABS_CACHE_KEY =
            TabRenderAggregator.class.getName() + "_MAPPED_TABS";

    private TabRenderAggregationPrivateDao tabRenderAggregationDao;
    private AggregatedTabLookupDao aggregatedTabLookupDao;

    @Autowired
    public void setAggregatedTabLookupDao(AggregatedTabLookupDao aggregatedTabLookupDao) {
        this.aggregatedTabLookupDao = aggregatedTabLookupDao;
    }

    @Autowired
    public void setTabRenderAggregationDao(TabRenderAggregationPrivateDao tabRenderAggregationDao) {
        this.tabRenderAggregationDao = tabRenderAggregationDao;
    }

    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return PortalRenderEvent.class.isAssignableFrom(type);
    }

    @Override
    protected BaseAggregationPrivateDao<TabRenderAggregationImpl, TabRenderAggregationKey>
            getAggregationDao() {
        return this.tabRenderAggregationDao;
    }

    @Override
    protected void updateAggregation(
            PortalRenderEvent e,
            EventAggregationContext eventAggregationContext,
            AggregationIntervalInfo intervalInfo,
            TabRenderAggregationImpl aggregation) {
        final long executionTime = e.getExecutionTimeNano();
        final int duration = intervalInfo.getDurationTo(e.getTimestampAsDate());
        aggregation.setDuration(duration);
        aggregation.addValue(executionTime);
    }

    @Override
    protected TabRenderAggregationKey createAggregationKey(
            PortalRenderEvent e,
            EventAggregationContext eventAggregationContext,
            AggregationIntervalInfo intervalInfo,
            AggregatedGroupMapping aggregatedGroup) {

        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();

        Map<String, AggregatedTabMapping> mappedTabs =
                eventAggregationContext.getAttribute(MAPPED_TABS_CACHE_KEY);
        if (mappedTabs == null) {
            mappedTabs = new HashMap<String, AggregatedTabMapping>();
            eventAggregationContext.setAttribute(MAPPED_TABS_CACHE_KEY, mappedTabs);
        }

        final String targetedLayoutNodeId = e.getTargetedLayoutNodeId();
        AggregatedTabMapping mappedTab = mappedTabs.get(targetedLayoutNodeId);
        if (mappedTab == null) {
            mappedTab = this.aggregatedTabLookupDao.getMappedTabForLayoutId(targetedLayoutNodeId);
            mappedTabs.put(targetedLayoutNodeId, mappedTab);
        }

        return new TabRenderAggregationKeyImpl(
                dateDimension, timeDimension, aggregationInterval, aggregatedGroup, mappedTab);
    }
}
