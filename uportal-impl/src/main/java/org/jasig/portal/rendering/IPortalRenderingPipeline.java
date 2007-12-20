/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.rendering;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.IUserInstance;
import org.jasig.portal.PortalException;

/**
 * Describes the entry point into the uPortal rendering pipeline.
 * 
 * TODO move statistics methods into this interface
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalRenderingPipeline {
    /**
     * <code>renderState</code> method orchestrates the rendering pipeline which includes worker dispatching, and the
     * rendering process from layout access, to channel rendering, to writing content to the browser.
     * 
     * @param req the <code>HttpServletRequest</code>
     * @param res the <code>HttpServletResponse</code>
     * @param userInstance The data object containing all information needed to rendering content for the current user
     * @exception PortalException if an error occurs
     */
    public void renderState(HttpServletRequest req, HttpServletResponse res, IUserInstance userInstance) throws PortalException;
}
