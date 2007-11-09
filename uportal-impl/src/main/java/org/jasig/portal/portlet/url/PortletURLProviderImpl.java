/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.PortletSecurityException;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.spi.PortletURLProvider;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Tracks configuration for a portlet URL then generates one when {@link #toString()} is called.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletURLProviderImpl implements PortletURLProvider {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final IPortletWindow portletWindow;
    private final HttpServletRequest httpServletRequest;
    private final IPortletUrlSyntaxProvider portletUrlSyntaxProvider;
    
    private final PortletUrl portletUrl = new PortletUrl();
    
    public PortletURLProviderImpl(IPortletWindow portletWindow, HttpServletRequest httpServletRequest, IPortletUrlSyntaxProvider portletUrlSyntaxProvider) {
        Validate.notNull(portletWindow, "portletWindow can not be null");
        Validate.notNull(httpServletRequest, "httpServletRequest can not be null");
        Validate.notNull(portletUrlSyntaxProvider, "portletUrlSyntaxProvider can not be null");
        
        this.portletWindow = portletWindow;
        this.httpServletRequest = httpServletRequest;
        this.portletUrlSyntaxProvider = portletUrlSyntaxProvider;
        
        //Init the portlet URL to have the same default assumptions as the PortletURLProvider interface
        this.portletUrl.setParameters(new HashMap<String, String[]>());
        this.portletUrl.setRequestType(RequestType.RENDER);
    }
    

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#clearParameters()
     */
    public void clearParameters() {
        this.portletUrl.getParameters().clear();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#isSecureSupported()
     */
    public boolean isSecureSupported() {
        return false; //TODO determine how to tie back in to the uPortal secure URL APIs, if they exist.
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setSecure()
     */
    public void setSecure() throws PortletSecurityException {
        throw new PortletSecurityException("Secure URLs are not supported at this time");
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setAction(boolean)
     */
    public void setAction(boolean action) {
        if (action) {
            this.portletUrl.setRequestType(RequestType.ACTION);
        }
        else {
            this.portletUrl.setRequestType(RequestType.RENDER);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setParameters(java.util.Map)
     * @param parmeters is Map<String, String[]>
     */
    @SuppressWarnings("unchecked")
    public void setParameters(Map parameters) {
        this.portletUrl.setParameters(parameters);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setPortletMode(javax.portlet.PortletMode)
     */
    public void setPortletMode(PortletMode mode) {
        if (!this.portletWindow.getPortletMode().equals(mode)) {
            this.portletUrl.setPortletMode(mode);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setWindowState(javax.portlet.WindowState)
     */
    public void setWindowState(WindowState state) {
        if (!this.portletWindow.getWindowState().equals(state)) {
            this.portletUrl.setWindowState(state);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.portletUrlSyntaxProvider.generatePortletUrl(this.httpServletRequest, this.portletWindow, this.portletUrl);
    }
}
