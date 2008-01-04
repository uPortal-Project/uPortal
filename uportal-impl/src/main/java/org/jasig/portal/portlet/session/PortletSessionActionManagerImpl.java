/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.portlet.session;

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.jasig.portal.portlet.container.services.AdministrativeRequestListenerController;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.springframework.beans.factory.annotation.Required;

/**
 * Uses the admin services API from the pluto PortletContainer to execute actions on
 * portlet sessions. The {@link PortletSessionAdministrativeRequestListener} is the companion
 * to this class that actually performs the actions.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletSessionActionManagerImpl implements IPortletSessionActionManager {
    private PortletContainer portletContainer;
    private String controllerAttributeName = AdministrativeRequestListenerController.DEFAULT_LISTENER_KEY_ATTRIBUTE;
    private String listenerName = "sessionActionListener";
    

    /**
     * @return the portletContainer
     */
    public PortletContainer getPortletContainer() {
        return this.portletContainer;
    }
    /**
     * @param portletContainer the portletContainer to set
     */
    @Required
    public void setPortletContainer(PortletContainer portletContainer) {
        this.portletContainer = portletContainer;
    }
    /**
     * @return the controllerAttributeName
     */
    public String getControllerAttributeName() {
        return this.controllerAttributeName;
    }
    /**
     * @param controllerAttributeName the controllerAttributeName to set
     */
    public void setControllerAttributeName(String controllerAttributeName) {
        this.controllerAttributeName = controllerAttributeName;
    }
    /**
     * @return the listenerName
     */
    public String getListenerName() {
        return this.listenerName;
    }
    /**
     * @param listenerName the listenerName to set
     */
    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }
    
    
    
    /**
     * @see org.jasig.portal.portlet.session.IPortletSessionActionManager#clear(org.jasig.portal.portlet.window.IPortletWindow, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void clear(IPortletWindow portletWindow, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws PortletException, IOException, PortletContainerException {
        this.clear(portletWindow, httpServletRequest, httpServletResponse, PortletSession.PORTLET_SCOPE);
    }

    /**
     * @see org.jasig.portal.portlet.session.IPortletSessionActionManager#clear(org.jasig.portal.portlet.window.IPortletWindow, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
     */
    public void clear(IPortletWindow portletWindow, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, int scope) throws PortletException, IOException, PortletContainerException {
        httpServletRequest.setAttribute(this.controllerAttributeName, this.listenerName);
        httpServletRequest.setAttribute(PortletSessionAdministrativeRequestListener.ACTION, PortletSessionAdministrativeRequestListener.SessionAction.CLEAR);
        httpServletRequest.setAttribute(PortletSessionAdministrativeRequestListener.SCOPE, scope);
        
        try {
            this.portletContainer.doAdmin(portletWindow, httpServletRequest, httpServletResponse);
        }
        finally {
            httpServletRequest.removeAttribute(PortletSessionAdministrativeRequestListener.ACTION);
            httpServletRequest.removeAttribute(PortletSessionAdministrativeRequestListener.SCOPE);
        }
    }

    /**
     * @see org.jasig.portal.portlet.session.IPortletSessionActionManager#setAttribute(org.jasig.portal.portlet.window.IPortletWindow, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, java.lang.Object)
     */
    public void setAttribute(IPortletWindow portletWindow, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String name, Object value) throws PortletException, IOException, PortletContainerException {
        this.setAttribute(portletWindow, httpServletRequest, httpServletResponse, name, value, PortletSession.PORTLET_SCOPE);
    }

    /**
     * @see org.jasig.portal.portlet.session.IPortletSessionActionManager#setAttribute(org.jasig.portal.portlet.window.IPortletWindow, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, java.lang.Object, int)
     */
    public void setAttribute(IPortletWindow portletWindow, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String name, Object value, int scope) throws PortletException, IOException, PortletContainerException {
        httpServletRequest.setAttribute(this.controllerAttributeName, this.listenerName);
        httpServletRequest.setAttribute(PortletSessionAdministrativeRequestListener.ACTION, PortletSessionAdministrativeRequestListener.SessionAction.SET_ATTRIBUTE);
        httpServletRequest.setAttribute(PortletSessionAdministrativeRequestListener.ARGUMENTS, new Object[] { name, value });
        httpServletRequest.setAttribute(PortletSessionAdministrativeRequestListener.SCOPE, scope);
        
        try {
            this.portletContainer.doAdmin(portletWindow, httpServletRequest, httpServletResponse);
        }
        finally {
            httpServletRequest.removeAttribute(PortletSessionAdministrativeRequestListener.ACTION);
            httpServletRequest.removeAttribute(PortletSessionAdministrativeRequestListener.ARGUMENTS);
            httpServletRequest.removeAttribute(PortletSessionAdministrativeRequestListener.SCOPE);
        }
    }
}
