/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.container.services.information;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Collection;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletConfig;

import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.StaticInformationProvider;
import org.apache.pluto.services.information.PortletActionProvider;
import org.apache.pluto.services.information.PortletURLProvider;
import org.apache.pluto.services.information.ResourceURLProvider;

/**
 * Implementation of Apache Pluto DynamicInformationProvider.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class DynamicInformationProviderImpl implements DynamicInformationProvider {
	
    private static StaticInformationProvider staticInfoProvider = null;
    private static final String staticInformationProviderName = "org.apache.pluto.services.information.StaticInformationProvider";
    private HttpServletRequest request = null;
    private String responseContentType = null;
    private static final int KNOWN_MIME_TYPES = 15;
    private PortalControlParameter controlParameter = null;
    
    public DynamicInformationProviderImpl(HttpServletRequest request, ServletConfig servletConfig ) {
        this.request = request;
        controlParameter = new PortalControlParameter(getRequestedPortalURL());
        if ( servletConfig != null && staticInfoProvider == null )
		 staticInfoProvider = (StaticInformationProvider) servletConfig.getServletContext().getAttribute(staticInformationProviderName);
        responseContentType = "text/html";
    }

    // DynamicInformationProvider methods
    
    public String getResponseContentType() {
        return responseContentType;
    }

    public Iterator getResponseContentTypes() {
        Set responseMimeTypes = new HashSet(KNOWN_MIME_TYPES); // number of known mime types
        responseMimeTypes.add(responseContentType);
        return responseMimeTypes.iterator();
    }

    public PortletURLProvider getPortletURLProvider(PortletWindow portletWindow) {
        return new PortletURLProviderImpl(this, portletWindow);
    }

    public ResourceURLProvider getResourceURLProvider(PortletWindow portletWindow) {
        return new ResourceURLProviderImpl(this, portletWindow);
    }

    public PortletActionProvider getPortletActionProvider(PortletWindow portletWindow) {
        return new PortletActionProviderImpl(request, portletWindow);
    }

    public PortletMode getPortletMode(PortletWindow portletWindow) {
        return controlParameter.getMode(portletWindow);
    }

    public PortletMode getPreviousPortletMode(PortletWindow portletWindow) {
        return controlParameter.getPrevMode(portletWindow);
    }

    public WindowState getWindowState(PortletWindow portletWindow) {
        return controlParameter.getState(portletWindow);
    }

    public WindowState getPreviousWindowState(PortletWindow portletWindow) {
        return controlParameter.getPrevState(portletWindow);
    }

    public boolean isPortletModeAllowed(PortletMode mode) {
    	Collection supportedModes = staticInfoProvider.getPortalContextProvider().getSupportedPortletModes();
		return supportedModes.contains(mode);
    }

    public boolean isWindowStateAllowed(WindowState state) {
		Collection supportedStates = staticInfoProvider.getPortalContextProvider().getSupportedWindowStates();
		return supportedStates.contains(state);
    }
    
	private String getRequestedPortalURL() {
	      // TO GET requested portal URL from HttpServletRequest !!!!!
	      return null;
	}

}
