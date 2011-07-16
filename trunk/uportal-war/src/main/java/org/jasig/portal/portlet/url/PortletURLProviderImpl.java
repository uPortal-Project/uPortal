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

package org.jasig.portal.portlet.url;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.PortletSecurityException;
import javax.portlet.WindowState;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.pluto.container.PortletURLProvider;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.UrlType;

/**
 * Wraps the portal {@link IPortletUrlBuilder} API to work with Pluto's API
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletURLProviderImpl implements PortletURLProvider {
    private final IPortletUrlBuilder portletUrlBuilder;
    
    public PortletURLProviderImpl(IPortletUrlBuilder portletUrlBuilder) {
        this.portletUrlBuilder = portletUrlBuilder;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#getCacheability()
     */
    @Override
    public String getCacheability() {
        return this.portletUrlBuilder.getCacheability();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#getPortletMode()
     */
    @Override
    public PortletMode getPortletMode() {
        return this.portletUrlBuilder.getPortletMode();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#getProperties()
     */
    @Override
    public Map<String, List<String>> getProperties() {
        //TODO
        return Collections.emptyMap();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#getPublicRenderParameters()
     */
    @Override
    public Map<String, String[]> getPublicRenderParameters() {
        //TODO
        return Collections.emptyMap();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#getRenderParameters()
     */
    @Override
    public Map<String, String[]> getRenderParameters() {
        return this.portletUrlBuilder.getParameters();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#getResourceID()
     */
    @Override
    public String getResourceID() {
        return this.portletUrlBuilder.getResourceId();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#getType()
     */
    @Override
    public TYPE getType() {
        final IPortalUrlBuilder portalUrlBuilder = this.portletUrlBuilder.getPortalUrlBuilder();
        final UrlType urlType = portalUrlBuilder.getUrlType();
        return urlType.getPortletUrlType();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#getWindowState()
     */
    @Override
    public WindowState getWindowState() {
        return this.portletUrlBuilder.getWindowState();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#isSecure()
     */
    @Override
    public boolean isSecure() {
        // TODO not supported yet
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#setCacheability(java.lang.String)
     */
    @Override
    public void setCacheability(String cacheability) {
        this.portletUrlBuilder.setCacheability(cacheability);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#setPortletMode(javax.portlet.PortletMode)
     */
    @Override
    public void setPortletMode(PortletMode mode) {
        this.portletUrlBuilder.setPortletMode(mode);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#setResourceID(java.lang.String)
     */
    @Override
    public void setResourceID(String resourceId) {
        this.portletUrlBuilder.setResourceId(resourceId);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#setSecure(boolean)
     */
    @Override
    public void setSecure(boolean secure) throws PortletSecurityException {
        // TODO not supported yet
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#setWindowState(javax.portlet.WindowState)
     */
    @Override
    public void setWindowState(WindowState state) {
        this.portletUrlBuilder.setWindowState(state);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#toURL()
     */
    @Override
    public String toURL() {
        final IPortalUrlBuilder portalUrlBuilder = this.portletUrlBuilder.getPortalUrlBuilder();
        return portalUrlBuilder.getUrlString();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletURLProvider#write(java.io.Writer, boolean)
     */
    @Override
    public void write(Writer out, boolean escapeXML) throws IOException {
        final String url = this.toURL();
        if (escapeXML) {
            StringEscapeUtils.escapeXml(out, url);
        }
        else {
            out.write(url);
        }
    }
}
