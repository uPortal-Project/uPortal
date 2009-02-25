/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.portlet;

import org.jasig.portal.PortalException;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Indicates that there was a problem loading or reading part of the portlet's object model
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class InconsistentPortletModelException extends PortalException {
    private static final long serialVersionUID = 1L;
    
    private final IPortletWindowId portletWindowId; 

    public InconsistentPortletModelException(String msg, IPortletWindowId portletWindowId) {
        super(msg, false, true);
        this.portletWindowId = portletWindowId;
    }
    
    public InconsistentPortletModelException(String msg, IPortletWindowId portletWindowId, Throwable cause) {
        super(msg, cause, false, true);
        this.portletWindowId = portletWindowId;
    }

    /**
     * @return the portletWindowId
     */
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindowId;
    }
}
