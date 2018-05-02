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
package org.apereo.portal.url;

import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.portlet.om.IPortletWindowId;

/** Provides portal URL builders */
public interface IPortalUrlProvider {
    /**
     * @param request the current request
     * @return The current portal action url builder, null if there is no portal action url builder
     *     for this requet
     */
    IPortalActionUrlBuilder getPortalActionUrlBuilder(HttpServletRequest request);

    /**
     * Converts a standard {@link IPortalUrlBuilder} to a {@link IPortalActionUrlBuilder}
     *
     * @param request The current request
     * @param portalUrlBuilder The url builder to convert
     * @return A portal action url builder
     */
    IPortalActionUrlBuilder convertToPortalActionUrlBuilder(
            HttpServletRequest request, IPortalUrlBuilder portalUrlBuilder);

    /**
     * Gets the default portal URL, this is equivalent to the first URL rendered by the portal when
     * a user logs in. Always a {@link UrlType#RENDER}
     *
     * @param request The current portal request
     * @return Default {@link IPortalUrlBuilder}
     */
    IPortalUrlBuilder getDefaultUrl(HttpServletRequest request);

    /**
     * Get a portal URL builder that targets the specified layout node.
     *
     * @param request The current portal request
     * @param layoutNodeId ID of the node in the user's layout that should be targeted by the URL.
     * @param urlType The type of the portal url to create
     * @return {@link IPortalUrlBuilder} targeting the specified node in the user's layout
     * @throws IllegalArgumentException If the specified ID doesn't exist for a folder in the users
     *     layout.
     */
    IPortalUrlBuilder getPortalUrlBuilderByLayoutNode(
            HttpServletRequest request, String layoutNodeId, UrlType urlType);

    /**
     * Get a portal URL builder that targets the specified portlet window.
     *
     * @param request The current portal request
     * @param portletWindowId ID of the portlet window that should be targeted by the URL.
     * @param urlType The type of the portal url to create
     * @return {@link IPortalUrlBuilder} targeting the specified node in the user's layout
     * @throws IllegalArgumentException If the specified ID doesn't exist for a folder in the users
     *     layout.
     */
    IPortalUrlBuilder getPortalUrlBuilderByPortletWindow(
            HttpServletRequest request, IPortletWindowId portletWindowId, UrlType urlType);

    /**
     * Get a portal URL builder that targets the specified portlet window.
     *
     * @param request The current portal request
     * @param portletFName The fname of the portlet that should be targeted by the URL
     * @param urlType The type of the portal url to create
     * @return {@link IPortalUrlBuilder} targeting the specified node in the user's layout
     * @throws IllegalArgumentException If the specified ID doesn't exist for a folder in the users
     *     layout.
     */
    IPortalUrlBuilder getPortalUrlBuilderByPortletFName(
            HttpServletRequest request, String portletFName, UrlType urlType);
}
