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

package org.jasig.portal.container.services.information.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.PortletActionProvider;
import org.apache.pluto.services.information.PortletURLProvider;
import org.apache.pluto.services.information.ResourceURLProvider;

/**
 * Implementation of Apache Pluto DynamicInformationProvider.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class DynamicInformationProviderImpl implements DynamicInformationProvider {

    private HttpServletRequest request = null;
    private String responseContentType = null;
    
    public DynamicInformationProviderImpl(HttpServletRequest request) {
        this.request = request;
        responseContentType = "text/html";
    }

    // DynamicInformationProvider methods
    
    public String getResponseContentType() {
        return responseContentType;
    }

    public Iterator getResponseContentTypes() {
        Set responseMimeTypes = new HashSet(15); // 15 = number of known mime types
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
        return PortletMode.VIEW;
    }

    public PortletMode getPreviousPortletMode(PortletWindow portletWindow) {
        return PortletMode.VIEW;
    }

    public WindowState getWindowState(PortletWindow portletWindow) {
        return WindowState.NORMAL;
    }

    public WindowState getPreviousWindowState(PortletWindow portletWindow) {
        return WindowState.NORMAL;
    }

    public boolean isPortletModeAllowed(PortletMode mode) {
        return false;
    }

    public boolean isWindowStateAllowed(WindowState state) {
        return false;
    }

}
