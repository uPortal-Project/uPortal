/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.container;

import java.util.Collection;

import javax.portlet.PortletMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletRenderResponseContext;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
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
            IRequestPropertiesManager requestPropertiesManager, IPortalUrlProvider portalUrlProvider) {

        super(portletContainer, portletWindow, containerRequest, containerResponse, requestPropertiesManager, portalUrlProvider);

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
        if (!this.isClosed()) {
            this.servletRequest.setAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_TITLE, title);
        }
    }

}
