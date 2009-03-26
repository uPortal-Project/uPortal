/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.container.services;

import javax.portlet.PortletContext;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.Validate;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.core.DefaultPortletEnvironmentService;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.spi.optional.PortletEnvironmentService;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.session.ScopingPortletSessionImpl;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provides custom portlet session instance to use a different scoping attribute value
 *
 * @author Eric Dalquist
 * @version $Revision$
 */
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
    @Required
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        Validate.notNull(portletWindowRegistry);
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
