/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.services;

import javax.portlet.PortalContext;

import org.apache.commons.lang.Validate;
import org.apache.pluto.RequiredContainerServices;
import org.apache.pluto.spi.PortalCallbackService;
import org.springframework.beans.factory.annotation.Required;

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
    @Required
    public void setPortalCallbackService(PortalCallbackService portalCallbackService) {
        Validate.notNull(portalCallbackService, "portalCallbackService can not be null");
        this.portalCallbackService = portalCallbackService;
    }

    /**
     * @param portalContext the portalContext to set
     */
    @Required
    public void setPortalContext(PortalContext portalContext) {
        Validate.notNull(portalContext, "portalContext can not be null");
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
