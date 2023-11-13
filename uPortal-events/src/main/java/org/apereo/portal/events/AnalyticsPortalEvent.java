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

import java.time.ZonedDateTime;
import java.util.Date;
import org.apereo.portal.security.IPerson;

public class AnalyticsPortalEvent extends PortalEvent {
    private static final long serialVersionUID = 1L;

    private final String user;
    private final Date eventDate;
    private final String type;
    private final String url;
    private final String tabName;
    private final String portletId;
    private final String portletName;

    AnalyticsPortalEvent() {
        super();
        user = "UNKNOWN";
        eventDate = Date.from(ZonedDateTime.now().toInstant());
        type = "UNKNOWN";
        url = "";
        tabName = "";
        portletId = "";
        portletName = "";
    }

    AnalyticsPortalEvent(
            PortalEventBuilder portalEventBuilder,
            IPerson user,
            Date eventDate,
            String type,
            String url,
            String tabName,
            String portletId,
            String portletName) {
        super(portalEventBuilder);
        this.user = user.getUserName();
        this.eventDate = eventDate;
        this.type = type;
        this.url = url;
        this.tabName = tabName;
        this.portletId = portletId;
        this.portletName = portletName;
    }

    public String getUser() {
        return this.user;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getTabName() {
        return tabName;
    }

    public String getPortletId() {
        return portletId;
    }

    public String getPortletName() {
        return portletName;
    }

    @Override
    public String toString() {
        return super.toString()
                + ", user="
                + user
                + ", eventDate="
                + this.eventDate
                + ", type="
                + type
                + ", url="
                + url
                + ", tabName="
                + tabName
                + ", portletId="
                + portletId
                + ", portletName="
                + portletName;
    }
}
