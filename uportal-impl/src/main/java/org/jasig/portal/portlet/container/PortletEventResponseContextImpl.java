/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.container;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletEventResponseContext;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.IPortalUrlProvider;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletEventResponseContextImpl extends PortletStateAwareResponseContextImpl implements PortletEventResponseContext {

    public PortletEventResponseContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortalUrlProvider portalUrlProvider) {

        super(portletContainer, portletWindow, containerRequest, containerResponse, requestPropertiesManager, portalUrlProvider);
        
    }
}
