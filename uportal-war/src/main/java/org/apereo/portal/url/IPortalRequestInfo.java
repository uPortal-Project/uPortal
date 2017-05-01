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
import org.apereo.portal.portlet.om.IPortletWindowId;

/**
 * Provides information about the portal request.
 *
 */
public interface IPortalRequestInfo {
    public static final String URL_TYPE_HEADER = UrlType.class.getName();
    public static final String URL_STATE_HEADER = UrlState.class.getName();

    /** @return The state rendered by the URL */
    public UrlState getUrlState();

    /** @return Type of url this request is for */
    public UrlType getUrlType();

    /** @return Parameters targeting the portal itself */
    public Map<String, List<String>> getPortalParameters();

    /**
     * @return The layout node being targeted by the request. If the request isn't targeting a
     *     particular layout node null is returned.
     */
    public String getTargetedLayoutNodeId();

    /**
     * @return The portlet window id targeted by the request. If the request isn't targeting a
     *     particular portlet null is returned.
     */
    public IPortletWindowId getTargetedPortletWindowId();

    /**
     * @return The {@link IPortletRequestInfo} for the targeted portlet. If the request isn't
     *     targeting a particular portlet null is returned.
     */
    public IPortletRequestInfo getTargetedPortletRequestInfo();

    /**
     * @param portletWindowId The portlet window id.
     * @return The {@link IPortletRequestInfo} for the window id, null if no request info for the
     *     window
     */
    public IPortletRequestInfo getPortletRequestInfo(IPortletWindowId portletWindowId);

    /**
     * @return Information for each of the portlets that has data on the request. The returned map
     *     is read only and will never be null.
     */
    public Map<IPortletWindowId, ? extends IPortletRequestInfo> getPortletRequestInfoMap();

    //    /**
    //     * @return The public portlet parameters for the request
    //     */
    //    public Map<String, List<String>> getPublicPortletParameters();
}
