/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.rendering;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.PortalException;
import org.jasig.portal.user.IUserInstance;

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
    
    /**
     * Clear the system character cache.
     */
    public void clearSystemCharacterCache();
}
