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
import javax.portlet.PortletMode;
import javax.portlet.PortletURL;
import javax.portlet.ResourceURL;
import javax.portlet.WindowState;
import org.apereo.portal.portlet.om.IPortletWindowId;

/**
 * Builds a URL for a specific portlet.
 *
 */
public interface IPortletUrlBuilder extends IUrlBuilder {
    /** @return The portlet window this url builder is for */
    public IPortletWindowId getPortletWindowId();

    /** @return The parent {@link IPortalUrlBuilder} of this {@link IPortletUrlBuilder} */
    public IPortalUrlBuilder getPortalUrlBuilder();

    /**
     * @see PortletURL#setWindowState(WindowState)
     * @throws IllegalStateException If parent {@link IPortalUrlBuilder#getUrlType()} returns {@link
     *     UrlType#RESOURCE}
     */
    public void setWindowState(WindowState windowState);
    /**
     * @see PortletURL#getWindowState()
     * @throws IllegalStateException If parent {@link IPortalUrlBuilder#getUrlType()} returns {@link
     *     UrlType#RESOURCE}
     */
    public WindowState getWindowState();

    /**
     * @see PortletURL#setPortletMode(PortletMode)
     * @throws IllegalStateException If parent {@link IPortalUrlBuilder#getUrlType()} returns {@link
     *     UrlType#RESOURCE}
     */
    public void setPortletMode(PortletMode portletMode);
    /**
     * @see PortletURL#getPortletMode()
     * @throws IllegalStateException If parent {@link IPortalUrlBuilder#getUrlType()} returns {@link
     *     UrlType#RESOURCE}
     */
    public PortletMode getPortletMode();

    /**
     * @see ResourceURL#setResourceID(String)
     * @throws IllegalStateException If parent {@link IPortalUrlBuilder#getUrlType()} does not
     *     return {@link UrlType#RESOURCE}
     */
    public void setResourceId(String resourceId);
    /**
     * @see ResourceURL#setResourceID(String)
     * @throws IllegalStateException If parent {@link IPortalUrlBuilder#getUrlType()} does not
     *     return {@link UrlType#RESOURCE}
     */
    public String getResourceId();

    /**
     * @see ResourceURL#setCacheability(String)
     * @throws IllegalStateException If parent {@link IPortalUrlBuilder#getUrlType()} does not
     *     return {@link UrlType#RESOURCE}
     */
    public void setCacheability(String cacheability);
    /**
     * @see ResourceURL#getCacheability()
     * @throws IllegalStateException If parent {@link IPortalUrlBuilder#getUrlType()} does not
     *     return {@link UrlType#RESOURCE}
     */
    public String getCacheability();

    /**
     * Get the public render parameters set by this portlet. The Map is mutable and making changes
     * to the Map will affect the public render parameters on the URL.
     *
     * @return Map containing currently set parameters.
     */
    public Map<String, String[]> getPublicRenderParameters();

    /**
     * @param copyCurrentRenderParameters If set to true all current private render parameters are
     *     copied to the URL
     */
    public void setCopyCurrentRenderParameters(boolean copyCurrentRenderParameters);

    /** @return If the current private render parameters should be copied to the generared URL */
    public boolean getCopyCurrentRenderParameters();
}
