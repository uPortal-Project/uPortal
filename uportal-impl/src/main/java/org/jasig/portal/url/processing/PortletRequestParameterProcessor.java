/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url.processing;

import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.url.IPortletRequestParameterManager;
import org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider;
import org.jasig.portal.portlet.url.PortletUrl;
import org.jasig.portal.url.IWritableHttpServletRequest;
import org.springframework.beans.factory.annotation.Required;

/**
 * Uses the {@link IPortletUrlSyntaxProvider} to parse the portlet parameters from the request into {@link PortletUrl}s.
 * The WindowState, PortletMode and parameter Map is set directly on the {@link IPortletWindow}. The {@link org.jasig.portal.portlet.url.RequestType}
 * is tracked in the {@link IPortletRequestParameterManager}.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRequestParameterProcessor implements IRequestParameterProcessor {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private IPortletUrlSyntaxProvider portletUrlSyntaxProvider;
    private IPortletRequestParameterManager portletRequestParameterManager;
    private IPortletWindowRegistry portletWindowRegistry;
    

    /**
     * @return the portletUrlSyntaxProvider
     */
    public IPortletUrlSyntaxProvider getPortletUrlSyntaxProvider() {
        return portletUrlSyntaxProvider;
    }
    /**
     * @param portletUrlSyntaxProvider the portletUrlSyntaxProvider to set
     */
    @Required
    public void setPortletUrlSyntaxProvider(IPortletUrlSyntaxProvider portletUrlSyntaxProvider) {
        Validate.notNull(portletUrlSyntaxProvider, "portletUrlSyntaxProvider can not be null");
        this.portletUrlSyntaxProvider = portletUrlSyntaxProvider;
    }

    /**
     * @return the portletRequestParameterManager
     */
    public IPortletRequestParameterManager getPortletRequestParameterManager() {
        return portletRequestParameterManager;
    }
    /**
     * @param portletRequestParameterManager the portletRequestParameterManager to set
     */
    @Required
    public void setPortletRequestParameterManager(IPortletRequestParameterManager portletRequestParameterManager) {
        Validate.notNull(portletRequestParameterManager, "portletRequestParameterManager can not be null");
        this.portletRequestParameterManager = portletRequestParameterManager;
    }

    /**
     * @return the portletWindowRegistry
     */
    public IPortletWindowRegistry getPortletWindowRegistry() {
        return portletWindowRegistry;
    }
    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    @Required
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        Validate.notNull(portletWindowRegistry, "portletWindowRegistry can not be null");
        this.portletWindowRegistry = portletWindowRegistry;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.url.processing.IRequestParameterProcessor#processParameters(org.jasig.portal.url.IWritableHttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public boolean processParameters(IWritableHttpServletRequest request, HttpServletResponse response) {
        final Map<IPortletWindowId, PortletUrl> portletUrls = this.portletUrlSyntaxProvider.parsePortletParameters(request);

        //If no PortletUrls then no targeted portlets
        if (portletUrls.isEmpty()) {
            this.portletRequestParameterManager.setNoPortletRequest(request);
            return true;
        }

        //Iterate over each PortletUrl, updating the IPortletWindow it is for and notifying the IPorltetRequestParameterManager
        //that a request for that portlet has been made
        for (final Map.Entry<IPortletWindowId, PortletUrl> portletUrlEntry : portletUrls.entrySet()) {
            final IPortletWindowId portletWindowId = portletUrlEntry.getKey();
            final PortletUrl portletUrl = portletUrlEntry.getValue();

            final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
            if (portletWindow == null) {
                this.logger.warn("No IPortletWindow exists for IPortletWindowId='" + portletWindowId
                        + "'. Request parameters for this IPortletWindowId will be ignored. Ignored parameters: "
                        + portletUrl);
            }

            final PortletMode portletMode = portletUrl.getPortletMode();
            if (portletMode != null) {
                portletWindow.setPortletMode(portletMode);
            }

            final WindowState windowState = portletUrl.getWindowState();
            if (windowState != null) {
                portletWindow.setWindowState(windowState);
            }

            final Map<String, String[]> parameters = portletUrl.getParameters();
            if (parameters != null) {
                portletWindow.setRequestParameters(parameters);
            }

            this.portletRequestParameterManager.setRequestType(request, portletWindowId, portletUrl.getRequestType());
        }

        return true;
    }
}
