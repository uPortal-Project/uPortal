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
package org.apereo.portal.events.tincan.converters;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Locale;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.PortalRenderEvent;
import org.apereo.portal.events.aggr.tabs.AggregatedTabLookupDao;
import org.apereo.portal.events.aggr.tabs.AggregatedTabMapping;
import org.apereo.portal.events.tincan.om.LocalizedString;
import org.apereo.portal.events.tincan.om.LrsObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Generic class to convert from PortletRenderEvent to LrsStatement.
 *
 */
public class PortletRenderEventConverter extends AbstractPortalEventToLrsStatementConverter {
    private AggregatedTabLookupDao aggregatedTabLookupDao;

    @Autowired
    public void setAggregatedTabLookupDao(AggregatedTabLookupDao aggregatedTabLookupDao) {
        this.aggregatedTabLookupDao = aggregatedTabLookupDao;
    }

    @Override
    public boolean supports(PortalEvent event) {
        return event instanceof PortalRenderEvent;
    }

    @Override
    protected LrsObject getLrsObject(PortalEvent event) {
        final String targetedLayoutNodeId = ((PortalRenderEvent) event).getTargetedLayoutNodeId();
        final AggregatedTabMapping aggregatedTabMapping =
                aggregatedTabLookupDao.getMappedTabForLayoutId(targetedLayoutNodeId);

        final Builder<String, LocalizedString> definitionBuilder = ImmutableMap.builder();
        definitionBuilder.put(
                "name", new LocalizedString(Locale.US, aggregatedTabMapping.getDisplayString()));

        return new LrsObject(
                buildUrn("tab", aggregatedTabMapping.getFragmentName()),
                getDefaultObjectType(),
                definitionBuilder.build());
    }
}
