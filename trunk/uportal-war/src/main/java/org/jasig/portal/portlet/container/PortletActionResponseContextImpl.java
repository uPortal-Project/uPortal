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

package org.jasig.portal.portlet.container;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletActionResponseContext;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.driver.PortletContextService;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.container.services.IPortletCookieService;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.IPortalActionUrlBuilder;
import org.jasig.portal.url.IPortletUrlBuilder;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletActionResponseContextImpl extends PortletStateAwareResponseContextImpl implements PortletActionResponseContext {
    private final IPortalActionUrlBuilder portalActionUrlBuilder;
    
    public PortletActionResponseContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortalActionUrlBuilder portalActionUrlBuilder,
            IPortletUrlBuilder portletUrlBuider, PortletContextService portletContextService,
            IPortletCookieService portletCookieService) {
        
        super(portletContainer, portletWindow, containerRequest, containerResponse, 
                requestPropertiesManager, portletUrlBuider, portletContextService, portletCookieService);
        
        this.portalActionUrlBuilder = portalActionUrlBuilder;
    }

    @Override
    public String getResponseURL() {
        return this.portalActionUrlBuilder.getUrlString();
    }

    @Override
    public boolean isRedirect() {
        return this.portalActionUrlBuilder.getRedirectLocation() != null;
    }

    @Override
    public void setRedirect(String location) {
        setRedirect(location, null);
    }

    @Override
    public void setRedirect(String location, String renderUrlParamName) {
        this.checkContextStatus();
        
        this.portalActionUrlBuilder.setRedirectLocation(location, renderUrlParamName);
    }
}
