/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.portlet;

import org.jasig.portal.PortalException;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Indicates that the portlet container failed to load the specified portlet.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletLoadFailureException extends PortalException {
    private static final long serialVersionUID = 1L;
    
    private final IPortletWindow portletWindow; 

    public PortletLoadFailureException(String msg, IPortletWindow portletWindow, Throwable cause) {
        super(msg, cause, false, true);
        this.portletWindow = portletWindow;
    }
    
    public PortletLoadFailureException(String msg, IPortletWindow portletWindow) {
        super(msg, false, true);
        this.portletWindow = portletWindow;
    }

    /**
     * @return the portletWindow
     */
    public IPortletWindow getPortletWindow() {
        return portletWindow;
    }
}
