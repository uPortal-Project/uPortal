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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.services.information.PortalContextProvider;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortalContextProviderImpl implements PortalContextProvider {
    
    private Map properties = null;
    private PortletMode[] supportedPortletModes = null;
    private WindowState[] supportedWindowStates = null;
    private String portalInfo = null;
    
    public PortalContextProviderImpl() {
        properties = new HashMap();
        // Should read these from a properties file
        supportedPortletModes = new PortletMode[] {new PortletMode("view"), new PortletMode("edit"), new PortletMode("help")};
        supportedWindowStates = new WindowState[] {new WindowState("normal"), new WindowState("maximized"), new WindowState("minimized")};
        portalInfo = "uPortal/2.4";
    }

    // PortalContextProvider methods
    
    public String getProperty(String name) {
        return (String)properties.get(name);
    }

    public Collection getPropertyNames() {
        return properties.keySet();
    }

    public Collection getSupportedPortletModes() {
        return Arrays.asList(supportedPortletModes);
    }

    public Collection getSupportedWindowStates() {
        return Arrays.asList(supportedWindowStates);
    }

    public String getPortalInfo() {
        return portalInfo;
    }

}
