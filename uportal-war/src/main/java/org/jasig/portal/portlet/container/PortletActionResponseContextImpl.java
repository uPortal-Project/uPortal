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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletActionResponseContext;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.driver.PortletContextService;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.container.services.IPortletCookieService;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.IPortalUrlProvider;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletActionResponseContextImpl extends PortletStateAwareResponseContextImpl implements PortletActionResponseContext {
    private boolean redirect;
    private String redirectLocation;
    private String renderURLParamName;
    
    public PortletActionResponseContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortalUrlProvider portalUrlProvider,
            PortletContextService portletContextService, IPortletCookieService portletCookieService) {
        super(portletContainer, portletWindow, containerRequest, containerResponse, 
                requestPropertiesManager, portalUrlProvider, portletContextService, portletCookieService);
    }

    @Override
    public String getResponseURL() {
        if (!isReleased()) {
            this.close();
            
            //if not redirect or there is a render url parameter name
            if (!redirect || renderURLParamName != null) {
                if (redirect) {
                    try {
                        return this.redirectLocation + "?" + 
                            URLEncoder.encode(renderURLParamName, "UTF-8") + "=" + 
                            URLEncoder.encode(this.portalUrlBuilder.getUrlString(), "UTF-8");
                    }
                    catch (UnsupportedEncodingException e) {
                        // Cannot happen: UTF-8 is a built-in/required encoder
                        return null;
                    }
                }

                return portalUrlBuilder.getUrlString();
            }

            return this.redirectLocation;
        }
        
        return null;
    }

    @Override
    public boolean isRedirect() {
        return redirect;
    }

    @Override
    public void setRedirect(String location) {
        setRedirect(location, null);
    }

    @Override
    public void setRedirect(String location, String renderURLParamName) {
        this.checkContextStatus();
        
        this.redirectLocation = location;
        this.renderURLParamName = renderURLParamName;
        this.redirect = true;
    }
}
