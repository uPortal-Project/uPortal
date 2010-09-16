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

import javax.portlet.PortletMode;
import javax.portlet.PortletSecurityException;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletPortalUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around a {@link IPortletPortalUrl} that makes it easier to use via XSLTC
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XsltPortletPortalUrl extends XsltBasePortalUrl {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final TYPE urlType;
    private final HttpServletRequest request;
    private final IPortalUrlProvider urlProvider;
    private final IPortletWindowRegistry portletWindowRegistry;
    private IPortletPortalUrl portletPortalUrl;

    public XsltPortletPortalUrl(HttpServletRequest request, IPortalUrlProvider urlProvider, IPortletWindowRegistry portletWindowRegistry, TYPE urlType) {
        this.request = request;
        this.urlProvider = urlProvider;
        this.portletWindowRegistry = portletWindowRegistry;
        this.urlType = urlType;
    }
    
    public void setTargetFname(String fname) {
        try {
            this.portletPortalUrl = this.urlProvider.getPortletUrlByFName(this.urlType, this.request, fname);
            this.basePortalUrl = this.portletPortalUrl;
        }
        catch (RuntimeException e) {
            logger.warn("Could not create portlet URL for fname '" + fname + "', an empty string will be returned.", e);
        }
    }
    public void setTargetSubscribeId(String subscribeId) {
        try {
            this.portletPortalUrl = this.urlProvider.getPortletUrlByNodeId(this.urlType, this.request, subscribeId);
            this.basePortalUrl = this.portletPortalUrl;
        }
        catch (RuntimeException e) {
            logger.warn("Could not create portlet URL for subscribeId '" + subscribeId + "', an empty string will be returned.", e);
        }
    }
    public void setTargetWindowId(String windowId) {
        try {
            final IPortletWindowId portletWindowId = this.portletWindowRegistry.getPortletWindowId(windowId);
            this.portletPortalUrl = this.urlProvider.getPortletUrl(this.urlType, this.request, portletWindowId);
            this.basePortalUrl = this.portletPortalUrl;
        }
        catch (RuntimeException e) {
            logger.warn("Could not create portlet URL for windowId '" + windowId + "', an empty string will be returned.", e);
        }
    }

    public void addPortletParameter(String name, String value) {
        if (this.portletPortalUrl == null) {
            return;
        }
        this.portletPortalUrl.addPortletParameter(name, value);
    }

    public void setPortletMode(String portletMode) {
        if (this.portletPortalUrl == null) {
            return;
        }
        if (portletMode != null && (portletMode = portletMode.trim()).length() > 0) {
            this.portletPortalUrl.setPortletMode(new PortletMode(portletMode));
        }
    }

    public void setPortletParameter(String name, String value) {
        if (this.portletPortalUrl == null) {
            return;
        }
        this.portletPortalUrl.setPortletParameter(name, value);
    }

    public void setSecure(String secure) throws PortletSecurityException {
        if (this.portletPortalUrl == null) {
            return;
        }
        if (secure != null && (secure = secure.trim()).length() > 0) {
            this.portletPortalUrl.setSecure(Boolean.parseBoolean(secure));
        }
    }

    public void setWindowState(String windowState) {
        if (this.portletPortalUrl == null) {
            return;
        }
        if (windowState != null && (windowState = windowState.trim()).length() > 0) {
            this.portletPortalUrl.setWindowState(new WindowState(windowState));
        }
    }

    @Override
    public int hashCode() {
        if (this.portletPortalUrl == null) {
            return 0;
        }

        return this.portletPortalUrl.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        XsltPortletPortalUrl other = (XsltPortletPortalUrl) obj;
        if (this.portletPortalUrl == null) {
            if (other.portletPortalUrl != null) {
                return false;
            }
        }
        else if (!this.portletPortalUrl.equals(other.portletPortalUrl)) {
            return false;
        }
        return true;
    }
}
