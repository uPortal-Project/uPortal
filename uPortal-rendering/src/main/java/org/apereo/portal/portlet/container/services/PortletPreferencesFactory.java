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
package org.apereo.portal.portlet.container.services;

import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequest;
import org.apache.pluto.container.PortletRequestContext;
import org.apereo.portal.portlet.om.IPortletEntity;

/** Creates {@link PortletPreferences} objects */
public interface PortletPreferencesFactory {

    /** Create portlet preferences for the specified portlet request context */
    PortletPreferences createPortletPreferences(
            final PortletRequestContext requestContext, boolean render);

    /**
     * Create portlet preferences for the specified portlet request context for the REST API calls
     * (since they don't have access to the portletRequestContext (and technically don't need it))
     */
    PortletPreferences createAPIPortletPreferences(
            final HttpServletRequest requestContext,
            IPortletEntity portletEntity,
            boolean render,
            boolean configMode);
}
