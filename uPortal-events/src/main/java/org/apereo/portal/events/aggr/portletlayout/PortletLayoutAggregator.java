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
package org.apereo.portal.events.aggr.portletlayout;

import java.util.HashMap;
import java.util.Map;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.PortletAddedToLayoutPortalEvent;
import org.apereo.portal.events.PortletDeletedFromLayoutPortalEvent;
import org.apereo.portal.events.PortletLayoutPortalEvent;
import org.apereo.portal.events.PortletMovedInLayoutPortalEvent;
import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.AggregationIntervalInfo;
import org.apereo.portal.events.aggr.BaseAggregationPrivateDao;
import org.apereo.portal.events.aggr.BaseIntervalAwarePortalEventAggregator;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.EventAggregationContext;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletLookupDao;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Event aggregator that uses {@link PortletLayoutAggregationPrivateDao} to aggregate portlet adds
 *
 */
public class PortletLayoutAggregator
        extends BaseIntervalAwarePortalEventAggregator<
                PortletLayoutPortalEvent, PortletLayoutAggregationImpl,
                PortletLayoutAggregationKey> {
    private static final String MAPPED_PORTLETS_CACHE_KEY =
            PortletLayoutAggregator.class.getName() + "_MAPPED_PORTLETS";

    private PortletLayoutAggregationPrivateDao portletLayoutAggregationDao;
    private AggregatedPortletLookupDao aggregatedPortletLookupDao;

    @Autowired
    public void setAggregatedPortletLookupDao(
            AggregatedPortletLookupDao aggregatedPortletLookupDao) {
        this.aggregatedPortletLookupDao = aggregatedPortletLookupDao;
    }

    @Autowired
    public void setPortletAddAggregationDao(
            PortletLayoutAggregationPrivateDao portletAddAggregationDao) {
        this.portletLayoutAggregationDao = portletAddAggregationDao;
    }

    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return PortletLayoutPortalEvent.class.isAssignableFrom(type);
    }

    @Override
    protected BaseAggregationPrivateDao<PortletLayoutAggregationImpl, PortletLayoutAggregationKey>
            getAggregationDao() {
        return this.portletLayoutAggregationDao;
    }

    @Override
    protected void updateAggregation(
            PortletLayoutPortalEvent e,
            EventAggregationContext eventAggregationContext,
            AggregationIntervalInfo intervalInfo,
            PortletLayoutAggregationImpl aggregation) {
        final int duration = intervalInfo.getDurationTo(e.getTimestampAsDate());
        aggregation.setDuration(duration);

        if (e instanceof PortletAddedToLayoutPortalEvent) {
            aggregation.countPortletAdd();
            return;
        }
        if (e instanceof PortletDeletedFromLayoutPortalEvent) {
            aggregation.countPortletDelete();
            return;
        }
        if (e instanceof PortletMovedInLayoutPortalEvent) {
            aggregation.countPortletMove();
            return;
        }
    }

    @Override
    protected PortletLayoutAggregationKey createAggregationKey(
            PortletLayoutPortalEvent e,
            EventAggregationContext eventAggregationContext,
            AggregationIntervalInfo intervalInfo,
            AggregatedGroupMapping aggregatedGroup) {

        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();

        Map<String, AggregatedPortletMapping> mappedPortlets =
                eventAggregationContext.getAttribute(MAPPED_PORTLETS_CACHE_KEY);
        if (mappedPortlets == null) {
            mappedPortlets = new HashMap<String, AggregatedPortletMapping>();
            eventAggregationContext.setAttribute(MAPPED_PORTLETS_CACHE_KEY, mappedPortlets);
        }

        final String fname = e.getFname();
        AggregatedPortletMapping mappedPortlet = mappedPortlets.get(fname);
        if (mappedPortlet == null) {
            mappedPortlet = this.aggregatedPortletLookupDao.getMappedPortletForFname(fname);
            mappedPortlets.put(fname, mappedPortlet);
        }

        return new PortletLayoutAggregationKeyImpl(
                dateDimension, timeDimension, aggregationInterval, aggregatedGroup, mappedPortlet);
    }
}
