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

import java.util.Collection;

import javax.portlet.PortletMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletRenderResponseContext;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.container.services.IPortletCookieService;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.url.IPortalUrlProvider;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRenderResponseContextImpl extends PortletMimeResponseContextImpl implements PortletRenderResponseContext {
    
    public PortletRenderResponseContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortalUrlProvider portalUrlProvider,
            IPortletCookieService portletCookieService) {

        super(portletContainer, portletWindow, containerRequest, containerResponse, requestPropertiesManager, portalUrlProvider, portletCookieService);

    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRenderResponseContext#setNextPossiblePortletModes(java.util.Collection)
     */
    @Override
    public void setNextPossiblePortletModes(Collection<PortletMode> portletModes) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRenderResponseContext#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {
        this.checkContextStatus();
        this.servletRequest.setAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_TITLE, title);
    }
}
