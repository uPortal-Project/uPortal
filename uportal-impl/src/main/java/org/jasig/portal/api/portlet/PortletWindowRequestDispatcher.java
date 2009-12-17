/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.api.portlet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface PortletWindowRequestDispatcher {
    public IPortletWindowId getPortletWindowId();
    
    public PortletMode getPortletMode();
    
    public WindowState getWindowState();
    
    /*
     * TODO
     *  does the delegate see parent's parameters?
     *  how does the parent change its parameters for URLs rendered by the child?
     *  can the parent modify the state/mode/parameters of the child?
     */
    
    
    public void doAction(ActionRequest actionRequest, ActionResponse actionResponse);
    
    public void doRender(RenderRequest renderRequest, RenderResponse renderResponse);
}
