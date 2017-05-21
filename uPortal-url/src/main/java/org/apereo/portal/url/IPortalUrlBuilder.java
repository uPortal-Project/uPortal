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

import java.util.Map;
import org.apereo.portal.portlet.om.IPortletWindowId;

/**
 * Builds a portal URL
 *
 */
public interface IPortalUrlBuilder extends IUrlBuilder {
    /**
     * @return The layout folder this URL targets, if null {@link #getTargetPortletWindowId()} will
     *     not be null.
     */
    public String getTargetFolderId();

    /**
     * @return The portlet window ID this URL targets, if null {@link #getTargetFolderId()} will not
     *     be null.
     */
    public IPortletWindowId getTargetPortletWindowId();

    /** @return The type of URL that will be generated, will never return null. */
    public UrlType getUrlType();

    //get/set secure

    /**
     * Get the {@link IPortletUrlBuilder} for the specified {@link IPortletWindowId}. Multiple calls
     * to this method with the same id will likely return the same object
     *
     * @param portletWindowId The id of the portlet window to get the url builder for, not null.
     * @return The url builder for the portlet window, not null.
     */
    public IPortletUrlBuilder getPortletUrlBuilder(IPortletWindowId portletWindowId);

    /**
     * If {@link #getTargetPortletWindowId()} does not return null this will return the {@link
     * IPortletUrlBuilder} for the returned window id.
     *
     * @throws IllegalStateException if {@link #getTargetPortletWindowId()} returns null
     */
    public IPortletUrlBuilder getTargetedPortletUrlBuilder();

    /**
     * @return All of the portlet url builders that have been created for this portal url.
     *     Unmodifiable Map
     */
    public Map<IPortletWindowId, IPortletUrlBuilder> getPortletUrlBuilders();

    /**
     * @return Generate a URL to be used in markup or as a redirect. The URL will be absolute,
     *     starting with a / or with a protocol such as http://
     */
    public String getUrlString();
}
