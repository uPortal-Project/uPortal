/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.channels.portlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Provides easy API for executing methods on portlets. Takes care of all of the uPortal specific setup and tear down around portlet calls.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletRenderer {
    /**
     * Initializes the portlet within the portlet container
     * 
     * @param portletEntity The portlet entity to base the window on
     * @param portletWindowId Window ID of the portlet to inititalize, may be null
     * @param httpServletRequest The portal's request
     * @param httpServletResponse The portal's response (nothing will be written to the response)
     * @return The correct portlet window ID, this ID should be used to track the portlet that was initialized
     */
    public IPortletWindowId doInit(final IPortletEntity portletEntity, final IPortletWindowId portletWindowId, final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse);
    
    /**
     * Executes an action in a portlet, handles all the request and response setup and teardown
     * 
     * @param portletWindowId Portlet to target with the action
     * @param httpServletRequest The portal's request
     * @param httpServletResponse The portal's response (nothing will be written to the response)
     */
    public void doAction(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
    
    /**
     * Executes a render in a portlet, handles all the request and response setup and teardown
     * 
     * @param portletWindowId Portlet to target with the render
     * @param httpServletRequest The portal's request
     * @param httpServletResponse The portal's response (nothing will be written to the response)
     * @param printWriter The writer to write the portlet's output to
     */
    public PortletRenderResult doRender(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, PrintWriter printWriter);
    
    /**
     * Resets a portlet's window data to the defaults and clears all portlet scoped session data
     * 
     * @param portletWindowId Portlet to target with the render
     * @param httpServletRequest The portal's request
     * @param httpServletResponse The portal's response (nothing will be written to the response)
     */
    public void doReset(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
}
