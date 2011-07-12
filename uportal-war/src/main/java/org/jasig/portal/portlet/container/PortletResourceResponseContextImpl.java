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

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletResourceResponseContext;
import org.jasig.portal.portlet.container.cache.IPortletCacheControlService;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.container.services.IPortletCookieService;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.IPortalUrlProvider;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletResourceResponseContextImpl extends PortletMimeResponseContextImpl implements PortletResourceResponseContext {
    
    public PortletResourceResponseContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortalUrlProvider portalUrlProvider,
            IPortletCookieService portletCookieService, IPortletCacheControlService portletCacheControlService) {

        super(portletContainer, portletWindow, containerRequest, containerResponse, requestPropertiesManager, portalUrlProvider, portletCookieService, portletCacheControlService);
        
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResourceResponseContext#setCharacterEncoding(java.lang.String)
     */
    @Override
    public void setCharacterEncoding(String charset) {
        this.checkContextStatus();
        this.servletResponse.setCharacterEncoding(charset);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResourceResponseContext#setContentLength(int)
     */
    @Override
    public void setContentLength(int len) {
        this.checkContextStatus();
        this.servletResponse.setContentLength(len);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResourceResponseContext#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale locale) {
        this.checkContextStatus();
        this.servletResponse.setLocale(locale);
    }
}
