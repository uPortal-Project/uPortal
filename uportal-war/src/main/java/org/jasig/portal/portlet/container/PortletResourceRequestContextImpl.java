/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
