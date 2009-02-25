/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.portlet;

import org.jasig.portal.PortalException;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Indicates that there was an exception while dispatching to a portlet.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletDispatchException extends PortalException {
    private static final long serialVersionUID = 1L;
    
    private final IPortletWindow portletWindow; 

    public PortletDispatchException(String msg, IPortletWindow portletWindow, Throwable cause) {
        super(msg, cause, true, true);
        this.portletWindow = portletWindow;
    }

    /**
     * @return the portletWindow
     */
    public IPortletWindow getPortletWindow() {
        return portletWindow;
    }
}
