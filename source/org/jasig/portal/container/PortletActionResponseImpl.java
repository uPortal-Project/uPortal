/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container;

import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.core.InternalActionResponse;


/**
 * Simple java bean that contains the results of 
 * a process action call, such as changed window states,
 * changed portlet modes, the location that a portlet wants
 * to redirect to, and any render parameters.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletActionResponseImpl implements IPortletActionResponse {
    
    private final InternalActionResponse iar;
    
    /**
     * Constructor that copies data from Pluto's
     * <code>InternalActionResponse</code>.
     * @param iar Pluto's internal action response
     */
    public PortletActionResponseImpl(final InternalActionResponse iar) {
        this.iar = iar;
    }
    
    public PortletMode getChangedPortletMode() {
        return iar.getChangedPortletMode();
    }
    
    public WindowState getChangedWindowState() {
        return iar.getChangedWindowState();
    }
    
    public String getRedirectLocation() {
        return iar.getRedirectLocation();
    }
    
    public Map getRenderParameters() {
        return iar.getRenderParameters();
    }
}
