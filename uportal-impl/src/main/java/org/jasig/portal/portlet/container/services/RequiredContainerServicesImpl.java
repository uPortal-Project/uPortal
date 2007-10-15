/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.services;

import javax.portlet.PortalContext;

import org.apache.pluto.RequiredContainerServices;
import org.apache.pluto.spi.PortalCallbackService;

/**
 * Required service locator bean which is provided to Pluto for access to the
 * callbacks needed to render portlets.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RequiredContainerServicesImpl implements RequiredContainerServices {
    private PortalCallbackService portalCallbackService;
    private PortalContext portalContext;
    
    /**
     * @param portalCallbackService the portalCallbackService to set
     */
    public void setPortalCallbackService(PortalCallbackService portalCallbackService) {
        this.portalCallbackService = portalCallbackService;
    }

    /**
     * @param portalContext the portalContext to set
     */
    public void setPortalContext(PortalContext portalContext) {
        this.portalContext = portalContext;
    }

    
    /* (non-Javadoc)
     * @see org.apache.pluto.RequiredContainerServices#getPortalCallbackService()
     */
    public PortalCallbackService getPortalCallbackService() {
        return this.portalCallbackService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.RequiredContainerServices#getPortalContext()
     */
    public PortalContext getPortalContext() {
        return this.portalContext;
    }

}
