/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.flow;

import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import org.springframework.webflow.context.ExternalContext;

/**
 * Helper methods to allow setting window states and portlet modes during flow exections
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletUrlFlowHelper {
    public void setWindowState(ExternalContext externalContext, String windowState) {
        final ActionResponse actionResponse;
        try {
            actionResponse = (ActionResponse)externalContext.getNativeResponse();
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException("setWindowState can only be called during an action request", e);
        }
        
        try {
            actionResponse.setWindowState(new WindowState(windowState));
        }
        catch (WindowStateException e) {
            throw new IllegalArgumentException("The specified WindowState '" + windowState + "' is not valid", e);
        }
    }

    public void setPortletMode(ExternalContext externalContext, String portletMode) {
        final ActionResponse actionResponse;
        try {
            actionResponse = (ActionResponse)externalContext.getNativeResponse();
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException("setPortletMode can only be called during an action request", e);
        }
        
        try {
            actionResponse.setPortletMode(new PortletMode(portletMode));
        }
        catch (PortletModeException e) {
            throw new IllegalArgumentException("The specified PortletMode '" + portletMode + "' is not valid", e);
        }
    }
}
