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

import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.ParameterMap;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
class FailSafePortletUrlBuilder implements IPortletUrlBuilder {
    private final Map<String, String[]> parameters = new ParameterMap();
    private final Map<String, String[]> publicParameters = new ParameterMap();
    private final IPortletWindowId portletWindowId;
    private final IPortalUrlBuilder portalUrlBuilder;
    
    public FailSafePortletUrlBuilder(IPortletWindowId portletWindowId, IPortalUrlBuilder portalUrlBuilder) {
        this.portletWindowId = portletWindowId;
        this.portalUrlBuilder = portalUrlBuilder;
    }

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
     * @see org.jasig.portal.url.IPortletUrlBuilder#setCopyCurrentRenderParameters(boolean)
     */
    @Override
    public void setCopyCurrentRenderParameters(boolean copyCurrentRenderParameters) {
        //NOOP
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getCopyCurrentRenderParameters()
     */
    @Override
    public boolean getCopyCurrentRenderParameters() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlBuilder#getParameters()
     */
    @Override
    public Map<String, String[]> getParameters() {
        return parameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getPortletWindowId()
     */
    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindowId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getPortalUrlBuilder()
     */
    @Override
    public IPortalUrlBuilder getPortalUrlBuilder() {
        return this.portalUrlBuilder;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#setWindowState(javax.portlet.WindowState)
     */
    @Override
    public void setWindowState(WindowState windowState) {
        //NOOP
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getWindowState()
     */
    @Override
    public WindowState getWindowState() {
        //NOOP
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#setPortletMode(javax.portlet.PortletMode)
     */
    @Override
    public void setPortletMode(PortletMode portletMode) {
        //NOOP
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getPortletMode()
     */
    @Override
    public PortletMode getPortletMode() {
        //NOOP
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#setResourceId(java.lang.String)
     */
    @Override
    public void setResourceId(String resourceId) {
        //NOOP
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getResourceId()
     */
    @Override
    public String getResourceId() {
        //NOOP
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#setCacheability(java.lang.String)
     */
    @Override
    public void setCacheability(String cacheability) {
        //NOOP
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getCacheability()
     */
    @Override
    public String getCacheability() {
        //NOOP
        return null;
    }

    @Override
    public Map<String, String[]> getPublicRenderParameters() {
        //NOOP
        return publicParameters;
    }
}
