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
import java.util.List;
import java.util.Locale;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.PortletExecutionEvent;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletLookupDao;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.apereo.portal.events.tincan.om.LocalizedString;
import org.apereo.portal.events.tincan.om.LrsObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 * Generic xApi converter for PortletExecutionEvents.
 *
 */
public class PortletExecutionEventConverter extends AbstractPortalEventToLrsStatementConverter {
    private AggregatedPortletLookupDao aggregatedPortletLookupDao;
    private List<Class<? extends PortletExecutionEvent>> supportedEventTypes;
    private List<String> filterFNames;
    private FNameFilterType fnameFilterType = FNameFilterType.Blacklist;

    @Required
    public void setSupportedEventTypes(
            List<Class<? extends PortletExecutionEvent>> supportedEventTypes) {
        this.supportedEventTypes = supportedEventTypes;
    }

    public void setFilterFNames(List<String> filterFNames) {
        this.filterFNames = filterFNames;
    }

    public void setFnameFilterType(FNameFilterType fnameFilterType) {
        this.fnameFilterType = fnameFilterType;
    }

    @Autowired
    public void setAggregatedPortletLookupDao(
            final AggregatedPortletLookupDao aggregatedPortletLookupDao) {
        this.aggregatedPortletLookupDao = aggregatedPortletLookupDao;
    }

    @Override
    public boolean supports(PortalEvent event) {
        boolean postEvent = false;
        for (Class<?> cls : supportedEventTypes) {
            if (cls.isAssignableFrom(event.getClass())) {
                postEvent = true;
                break;
            }
        }

        if (postEvent && filterFNames != null) {

            PortletExecutionEvent execEvent = (PortletExecutionEvent) event;
            boolean foundFName = false;
            for (String fname : filterFNames) {
                if (fname != null && fname.equalsIgnoreCase(execEvent.getFname())) {
                    foundFName = true;
                    break;
                }
            }

            postEvent = fnameFilterType == FNameFilterType.Whitelist ? foundFName : !foundFName;
        }

        return postEvent;
    }

    @Override
    protected LrsObject getLrsObject(PortalEvent event) {
        final String fname = ((PortletExecutionEvent) event).getFname();
        final AggregatedPortletMapping mappedPortletForFname =
                this.aggregatedPortletLookupDao.getMappedPortletForFname(fname);

        final Builder<String, LocalizedString> definitionBuilder = ImmutableMap.builder();
        definitionBuilder.put(
                "name", new LocalizedString(Locale.US, mappedPortletForFname.getName()));

        return new LrsObject(
                buildUrn("portlet", fname), getDefaultObjectType(), definitionBuilder.build());
    }

    public static enum FNameFilterType {
        Whitelist,
        Blacklist
    }
}
