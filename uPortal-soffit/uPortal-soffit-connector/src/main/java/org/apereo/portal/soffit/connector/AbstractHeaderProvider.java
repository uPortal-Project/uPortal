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
package org.apereo.portal.soffit.connector;

import java.util.Date;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/** @since 5.0 */
public abstract class AbstractHeaderProvider implements IHeaderProvider {

    @Value("${org.apereo.portal.security.PersonFactory.guest_user_name:guest}")
    private String guestUserName;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final boolean isIncluded(RenderRequest renderRequest, String preferenceName) {
        final PortletPreferences preferences = renderRequest.getPreferences();
        final String result = preferences.getValue(preferenceName, Boolean.FALSE.toString());
        return Boolean.valueOf(result);
    }

    protected final String getUsername(RenderRequest renderRequest) {
        final String result =
                renderRequest.getRemoteUser() != null
                        ? renderRequest.getRemoteUser()
                        : guestUserName;
        return result;
    }

    /** Point at which the JWT expires */
    protected final Date getExpiration(RenderRequest renderRequest) {
        // Expiration of the JWT
        final PortletSession portletSession = renderRequest.getPortletSession();
        final Date result =
                new Date(
                        portletSession.getLastAccessedTime()
                                + ((long) portletSession.getMaxInactiveInterval() * 1000L));
        return result;
    }
}
