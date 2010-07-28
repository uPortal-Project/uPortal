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

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletResourceRequestContext;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletResourceRequestContextImpl extends PortletRequestContextImpl implements PortletResourceRequestContext {

    public PortletResourceRequestContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager) {
        
        super(portletContainer, portletWindow, containerRequest, containerResponse, requestPropertiesManager);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResourceRequestContext#getCacheability()
     */
    @Override
    public String getCacheability() {
        // TODO See constants on javax.portlet.ResourceURL
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResourceRequestContext#getPrivateRenderParameterMap()
     */
    @Override
    public Map<String, String[]> getPrivateRenderParameterMap() {
        // TODO This probably needs to be provided by the window as well
        return Collections.emptyMap();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResourceRequestContext#getResourceID()
     */
    @Override
    public String getResourceID() {
        // TODO read the portlet spec about resource ids?
        return null;
    }

}
