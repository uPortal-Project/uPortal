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

import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.PortletURLProvider;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.container.om.window.PortletWindowImpl;

/**
 * Implementation of Apache Pluto PortletURLProvider.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletURLProviderImpl implements PortletURLProvider {

    private DynamicInformationProviderImpl provider;
    private PortletWindow portletWindow;
    private PortletMode portletMode;
    private WindowState windowState;
    private boolean action;
    private boolean secure;
    private boolean clearParameters;
    private Map parameters;

    public PortletURLProviderImpl(DynamicInformationProviderImpl provider, PortletWindow portletWindow) {
        this.provider = provider;
        this.portletWindow = portletWindow;
    }
    
    // PortletURLProvider methods

    public void setPortletMode(PortletMode mode) {
        this.portletMode = mode;
    }

    public void setWindowState(WindowState state) {
        this.windowState = state;
    }

    public void setAction() {
        action = true;
    }

    public void setSecure() {
        secure = true;
    }

    public void clearParameters() {
        clearParameters = true;
    }

    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }

    public String toString() {
        ChannelRuntimeData runtimeData = ((PortletWindowImpl)portletWindow).getChannelRuntimeData();
        String baseActionURL = runtimeData.getBaseActionURL();
		StringBuffer url = new StringBuffer(baseActionURL);
        if ( parameters != null && !parameters.isEmpty() ) {
         url.append("?");
         Iterator names = parameters.keySet().iterator();
         while (names.hasNext()) {
            String name = (String)names.next();
            Object value = parameters.get(name);
            url.append(name).append("=").append(value.toString());
            url.append("&"); // Need to add logic to add this the last time though only
         }
        } 
        return url.toString();
    }

}
