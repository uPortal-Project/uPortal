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
package org.apereo.portal.events.aggr.portletexec;

import java.util.HashMap;
import java.util.Map;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.PortletExecutionEvent;
import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.AggregationIntervalInfo;
import org.apereo.portal.events.aggr.BaseAggregationPrivateDao;
import org.apereo.portal.events.aggr.BaseIntervalAwarePortalEventAggregator;
import org.apereo.portal.events.aggr.DateDimension;
import org.apereo.portal.events.aggr.EventAggregationContext;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.portletexec.PortletExecutionAggregationKey.ExecutionType;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletLookupDao;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Event aggregator that uses {@link PortletExecutionAggregationPrivateDao} to aggregate portlet
 * executions
 *
 */
public class PortletExecutionAggregator
        extends BaseIntervalAwarePortalEventAggregator<
                PortletExecutionEvent, PortletExecutionAggregationImpl,
                PortletExecutionAggregationKey> {
    private static final String MAPPED_PORTLETS_CACHE_KEY =
            PortletExecutionAggregator.class.getName() + "_MAPPED_PORTLETS";

    private PortletExecutionAggregationPrivateDao portletExecutionAggregationDao;
    private AggregatedPortletLookupDao aggregatedPortletLookupDao;
    private ExecutionType executionType = ExecutionType.ALL;

    @Autowired
    public void setPortletExecutionAggregationDao(
            PortletExecutionAggregationPrivateDao portletExecutionAggregationDao) {
        this.portletExecutionAggregationDao = portletExecutionAggregationDao;
    }

    @Autowired
    public void setAggregatedPortletLookupDao(
            AggregatedPortletLookupDao aggregatedPortletLookupDao) {
        this.aggregatedPortletLookupDao = aggregatedPortletLookupDao;
    }

    /** Set the type of portlet execution this aggregator works on. */
    public void setExecutionType(ExecutionType executionType) {
        this.executionType = executionType;
    }

    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return executionType.supports(type);
    }

    @Override
    public boolean supports(PortalEvent event) {
        return super.supports(event);
    }

    @Override
    protected BaseAggregationPrivateDao<
                    PortletExecutionAggregationImpl, PortletExecutionAggregationKey>
            getAggregationDao() {
        return this.portletExecutionAggregationDao;
    }

    @Override
    protected void updateAggregation(
            PortletExecutionEvent e,
            EventAggregationContext eventAggregationContext,
            AggregationIntervalInfo intervalInfo,
            PortletExecutionAggregationImpl aggregation) {
        final long executionTime = e.getExecutionTimeNano();
        final int duration = intervalInfo.getDurationTo(e.getTimestampAsDate());
        aggregation.setDuration(duration);
        aggregation.addValue(executionTime);
    }

    @Override
    protected PortletExecutionAggregationKey createAggregationKey(
            PortletExecutionEvent e,
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

        return new PortletExecutionAggregationKeyImpl(
                dateDimension,
                timeDimension,
                aggregationInterval,
                aggregatedGroup,
                mappedPortlet,
                executionType);
    }
}
