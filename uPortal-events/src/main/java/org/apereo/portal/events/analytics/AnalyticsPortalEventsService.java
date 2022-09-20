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
package org.apereo.portal.events.analytics;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.events.IPortalAnalyticsEventFactory;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsPortalEventsService implements IAnalyticsPortalEventService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired private IPortalAnalyticsEventFactory portalEventFactory;
    @Autowired private IPersonManager personManager;

    @Override
    public void publishEvent(HttpServletRequest request, Map<String, Object> analyticsData) {
        final IPerson user = personManager.getPerson(request);
        portalEventFactory.publishAnalyticsPortalEvents(request, this, analyticsData, user);
    }
}
