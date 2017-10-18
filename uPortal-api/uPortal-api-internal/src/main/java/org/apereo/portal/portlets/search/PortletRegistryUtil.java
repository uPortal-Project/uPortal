/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.portal.portlets.search;

import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.portlet.PortletUtils;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.portlets.groupselector.EntityEnum;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.url.IPortalUrlBuilder;
import org.apereo.portal.url.IPortalUrlProvider;
import org.apereo.portal.url.IPortletUrlBuilder;
import org.apereo.portal.url.UrlType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Collection of useful methods for searching portlet definitions.
 *
 * <p>- matches(query, portlet) tests the query against portlet title, name and description
 *
 * <p>- buildPortletUrl(servletRequest, portletDef) builds a portal URL to the specific portlet if
 * the remote user in the request has browse permissions
 *
 * @since 5.0
 */
@Component
public class PortletRegistryUtil {

    @Autowired private IPortalUrlProvider portalUrlProvider;

    @Autowired private IPortletWindowRegistry portletWindowRegistry;

    @Autowired private IAuthorizationService authorizationService;

    /**
     * Performs a case-insensitive comparison of the user's query against several important fields
     * from the {@link IPortletDefinition}.
     *
     * @param query The user's search terms, which seem to be forced lower-case
     * @param portlet Definition of portlet to check name, title, and description
     * @return true if the query string is contained in the above 3 attributes; otherwise, false
     */
    public static boolean matches(final String query, final IPortletDefinition portlet) {
        /*
         * The query parameter is coming in lower case always (even when upper
         * or mixed case is entered by the user).  We really want a case-
         * insensitive comparison here anyway;  for safety, we will make certain
         * it is insensitive.
         */
        final String lcQuery = query.toLowerCase();
        final boolean titleMatch = portlet.getTitle().toLowerCase().contains(lcQuery);
        final boolean nameMatch = portlet.getName().toLowerCase().contains(lcQuery);
        final boolean descMatch =
                portlet.getDescription() != null
                        && portlet.getDescription().toLowerCase().contains(lcQuery);
        final boolean fnameMatch = portlet.getFName().toLowerCase().contains(lcQuery);
        return titleMatch || nameMatch || descMatch || fnameMatch;
    }

    /**
     * Builds a portal URL to the specific portlet definition {@link IPortletDefinition}, if the
     * remote user in the request has browse permissions to the portlet.
     *
     * @param httpServletRequest Web request with requester as remote user
     * @param portlet Definition of portlet to build a maximized portal URL
     * @return URL string if user has access; otherwise, return null
     */
    public String buildPortletUrl(
            HttpServletRequest httpServletRequest, IPortletDefinition portlet) {
        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(
                        httpServletRequest, portlet.getFName());

        // portletWindow is null if user does not have access to portlet.
        if (portletWindow == null) {
            return null;
        }
        // If user does not have browse permission, exclude the portlet.
        if (!this.authorizationService.canPrincipalBrowse(
                this.authorizationService.newPrincipal(
                        httpServletRequest.getRemoteUser(), EntityEnum.PERSON.getClazz()),
                portlet)) {
            return null;
        }

        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
        final IPortalUrlBuilder portalUrlBuilder =
                this.portalUrlProvider.getPortalUrlBuilderByPortletFName(
                        httpServletRequest, portlet.getFName(), UrlType.RENDER);
        final IPortletUrlBuilder portletUrlBuilder =
                portalUrlBuilder.getPortletUrlBuilder(portletWindowId);
        portletUrlBuilder.setWindowState(PortletUtils.getWindowState("maximized"));
        return portalUrlBuilder.getUrlString();
    }
}
