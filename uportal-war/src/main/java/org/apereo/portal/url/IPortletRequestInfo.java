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

import java.util.List;
import java.util.Map;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import org.apereo.portal.portlet.om.IPortletWindowId;

/**
 * Information from the request that targets a particular portlet
 *
 */
public interface IPortletRequestInfo {
    /** @return The {@link IPortletWindowId} this request info is for, will never return null */
    public IPortletWindowId getPortletWindowId();
    /** @return The portlet parameters from the request, will never return null. */
    public Map<String, List<String>> getPortletParameters();

    /**
     * @return The delegation parent window id, null if the portlet is not a delegate
     * @throws IllegalStateException If parent {@link IPortalRequestInfo#getUrlType()} returns
     *     {@link UrlType#RESOURCE}
     */
    public IPortletWindowId getDelegateParentWindowId();

    /**
     * @return The requested WindowState, null if no change to the WindowState is requested
     * @throws IllegalStateException If parent {@link IPortalRequestInfo#getUrlType()} returns
     *     {@link UrlType#RESOURCE}
     */
    public WindowState getWindowState();
    /**
     * @return The requested PortletMode, null if no change to the PortletMode is requested
     * @throws IllegalStateException If parent {@link IPortalRequestInfo#getUrlType()} returns
     *     {@link UrlType#RESOURCE}
     */
    public PortletMode getPortletMode();
    /**
     * @return The requested resource ID for a resource request
     * @throws IllegalStateException If parent {@link IPortalRequestInfo#getUrlType()} does not
     *     return {@link UrlType#RESOURCE}
     */
    public String getResourceId();
    /**
     * @return The cacheability requested for the resource request
     * @throws IllegalStateException If parent {@link IPortalRequestInfo#getUrlType()} does not
     *     return {@link UrlType#RESOURCE}
     */
    public String getCacheability();
}
