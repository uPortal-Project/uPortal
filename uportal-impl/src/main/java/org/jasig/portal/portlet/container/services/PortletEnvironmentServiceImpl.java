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

package org.jasig.portal.portlet.container.services;

import javax.portlet.PortletContext;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.pluto.PortletContainer;
import org.apache.pluto.core.DefaultPortletEnvironmentService;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.spi.optional.PortletEnvironmentService;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.session.ScopingPortletSessionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides custom portlet session instance to use a different scoping attribute value
 *
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portletEnvironmentService")
public class PortletEnvironmentServiceImpl extends DefaultPortletEnvironmentService implements PortletEnvironmentService {
    private IPortletWindowRegistry portletWindowRegistry;
    
    /**
     * @return the portletWindowRegistry
     */
    public IPortletWindowRegistry getPortletWindowRegistry() {
        return portletWindowRegistry;
    }
    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    @Autowired(required=true)
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }


    @Override
    public PortletSession createPortletSession(PortletContainer container, 
                                               HttpServletRequest servletRequest,
                                               PortletContext portletContext, 
                                               HttpSession httpSession, 
                                               InternalPortletWindow internalPortletWindow) {
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(servletRequest, internalPortletWindow);
        final IPortletEntityId portletEntityId = portletWindow.getPortletEntityId();
        return new ScopingPortletSessionImpl(portletEntityId, portletContext, internalPortletWindow, httpSession);
    }
}
