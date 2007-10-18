/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.services;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.PortletSecurityException;
import javax.portlet.WindowState;

import org.apache.pluto.spi.PortletURLProvider;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletURLProviderImpl implements PortletURLProvider {
    private final IPortletWindow portletWindow;
    
    private final Map<String, String[]> parameters = new HashMap<String, String[]>();
    private PortletMode portletMode;
    private WindowState windowState;
    private boolean isAction = false;
    
    public PortletURLProviderImpl(IPortletWindow portletWindow) {
        this.portletWindow = portletWindow;
        
        this.portletMode = this.portletWindow.getPortletMode();
        this.windowState = this.portletWindow.getWindowState();
    }
    

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#clearParameters()
     */
    public void clearParameters() {
        this.parameters.clear();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#isSecureSupported()
     */
    public boolean isSecureSupported() {
        return false; //TODO determine how to tie back in to the uPortal secure URL APIs, if they exist.
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setAction(boolean)
     */
    public void setAction(boolean action) {
        this.isAction = action;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setParameters(java.util.Map)
     * @param parmeters is Map<String, String[]>
     */
    @SuppressWarnings("unchecked")
    public void setParameters(Map parameters) {
        this.parameters.clear();
        this.parameters.putAll(parameters);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setPortletMode(javax.portlet.PortletMode)
     */
    public void setPortletMode(PortletMode mode) {
        this.portletMode = mode;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setSecure()
     */
    public void setSecure() throws PortletSecurityException {
        throw new PortletSecurityException("Secure URLs are not supported at this time");
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setWindowState(javax.portlet.WindowState)
     */
    public void setWindowState(WindowState state) {
        this.windowState = state;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return null;
    }
}
