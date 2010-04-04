/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.url;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletURLProvider;
import org.apache.pluto.container.ResourceURLProvider;
import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Service interface for creating portlet URL provider objects 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletUrlCreator {
    
    public ResourceURLProvider createResourceUrlProvider(
            IPortletWindow portletWindow, HttpServletRequest containerRequest, HttpServletResponse containerResponse);
    
    public PortletURLProvider createRenderUrlProvider(
            IPortletWindow portletWindow, HttpServletRequest containerRequest, HttpServletResponse containerResponse);
    
    public PortletURLProvider createActionUrlProvider(
            IPortletWindow portletWindow, HttpServletRequest containerRequest, HttpServletResponse containerResponse);
    
    public PortletURLProvider createUrlProvider(TYPE type,
            IPortletWindow portletWindow, HttpServletRequest containerRequest, HttpServletResponse containerResponse);
}
