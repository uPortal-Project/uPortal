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
package org.apereo.portal.events;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.url.IPortalRequestUtils;
import org.apereo.portal.utils.web.PortalWebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class RequestScopedEventsTrackerImpl
        implements ApplicationListener<PortalEvent>, RequestScopedEventsTracker {
    private static final String REQUEST_EVENTS =
            RequestScopedEventsTrackerImpl.class.getName() + ".REQUEST_EVENTS";

    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Override
    public void onApplicationEvent(PortalEvent event) {
        final HttpServletRequest portalRequest = event.getPortalRequest();
        if (portalRequest != null) {
            final ConcurrentMap<PortalEvent, Boolean> renderEvents =
                    PortalWebUtils.getMapRequestAttribute(portalRequest, REQUEST_EVENTS);
            renderEvents.put(event, true);
        }
    }

    @Override
    public Set<PortalEvent> getRequestEvents(PortletRequest portletRequest) {
        final HttpServletRequest portletHttpRequest =
                this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        return this.getRequestEvents(portletHttpRequest);
    }

    @Override
    public Set<PortalEvent> getRequestEvents(HttpServletRequest request) {
        request = this.portalRequestUtils.getOriginalPortalRequest(request);

        final ConcurrentMap<PortalEvent, Boolean> renderEvents =
                PortalWebUtils.getMapRequestAttribute(request, REQUEST_EVENTS, false);
        if (renderEvents == null) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(renderEvents.keySet());
    }
}
