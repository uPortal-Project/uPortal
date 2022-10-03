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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsPortalEventsService implements IAnalyticsPortalEventService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String ANALYTICS_LOG_LEVEL_NONE = "NONE";
    private static final String ANALYTICS_LOG_LEVEL_AUTHENTICATED = "AUTHENTICATED";
    private static final String ANALYTICS_LOG_LEVEL_ALL = "ALL";

    @Autowired private IPortalAnalyticsEventFactory portalEventFactory;
    @Autowired private IPersonManager personManager;

    @Value("${events.analytics.log.level:NONE}")
    private String eventLogLevel;

    @Override
    public String getLogLevel() {
        return eventLogLevel;
    }

    @Override
    public void publishEvent(HttpServletRequest request, Map<String, Object> analyticsData) {
        final IPerson user = personManager.getPerson(request);
        if (isLogEventEnabled(user)) {
            portalEventFactory.publishAnalyticsPortalEvents(request, this, analyticsData, user);
        }
    }

    private boolean isLogEventEnabled(IPerson user) {
        if (ANALYTICS_LOG_LEVEL_ALL.equals(eventLogLevel)) {
            return true;
        }
        if (ANALYTICS_LOG_LEVEL_NONE.equals(eventLogLevel)) {
            return false;
        }
        if (ANALYTICS_LOG_LEVEL_AUTHENTICATED.equals(eventLogLevel)) {
            return !user.isGuest();
        }
        logger.warn(
                "events.analytics.log.level is set to "
                        + eventLogLevel
                        + " which is invalid.  "
                        + "Use "
                        + ANALYTICS_LOG_LEVEL_NONE
                        + ", "
                        + ANALYTICS_LOG_LEVEL_AUTHENTICATED
                        + ", or "
                        + ANALYTICS_LOG_LEVEL_ALL
                        + " instead.  No analytics events will be captured");
        return false;
    }
}
