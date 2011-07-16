/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.url.xml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.ParameterMap;
import org.jasig.portal.url.UrlType;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
class FailSafePortalUrlBuilder implements IPortalUrlBuilder {
    private final Map<String, String[]> parameters = new ParameterMap();
    private final Map<IPortletWindowId, IPortletUrlBuilder> portletUrlBuilders = new LinkedHashMap<IPortletWindowId, IPortletUrlBuilder>();

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlBuilder#setParameter(java.lang.String, java.lang.String[])
     */
    @Override
    public void setParameter(String name, String... values) {
        //NOOP
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlBuilder#setParameter(java.lang.String, java.util.List)
     */
    @Override
    public void setParameter(String name, List<String> values) {
        //NOOP
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlBuilder#addParameter(java.lang.String, java.lang.String[])
     */
    @Override
    public void addParameter(String name, String... values) {
        //NOOP
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlBuilder#setParameters(java.util.Map)
     */
    @Override
    public void setParameters(Map<String, List<String>> parameters) {
        //NOOP
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlBuilder#getParameters()
     */
    @Override
    public Map<String, String[]> getParameters() {
        return this.parameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlBuilder#getTargetFolderId()
     */
    @Override
    public String getTargetFolderId() {
        //NOOP
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlBuilder#getTargetPortletWindowId()
     */
    @Override
    public IPortletWindowId getTargetPortletWindowId() {
        //NOOP
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlBuilder#getUrlType()
     */
    @Override
    public UrlType getUrlType() {
        //NOOP
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlBuilder#getPortletUrlBuilder(org.jasig.portal.portlet.om.IPortletWindowId)
     */
    @Override
    public IPortletUrlBuilder getPortletUrlBuilder(IPortletWindowId portletWindowId) {
        IPortletUrlBuilder portletUrlBuilder;
        
        synchronized (this.portletUrlBuilders) {
            portletUrlBuilder = this.portletUrlBuilders.get(portletWindowId);
            if (portletUrlBuilder == null) {
                portletUrlBuilder = new FailSafePortletUrlBuilder(portletWindowId, this);
                this.portletUrlBuilders.put(portletWindowId, portletUrlBuilder);
            }
        }
        
        return portletUrlBuilder;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlBuilder#getTargetedPortletUrlBuilder()
     */
    @Override
    public IPortletUrlBuilder getTargetedPortletUrlBuilder() {
        //NOOP
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlBuilder#getPortletUrlBuilders()
     */
    @Override
    public Map<IPortletWindowId, IPortletUrlBuilder> getPortletUrlBuilders() {
        return Collections.unmodifiableMap(this.portletUrlBuilders);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlBuilder#getUrlString()
     */
    @Override
    public String getUrlString() {
        return "#";
    }
}
