/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.services.information;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.services.information.PortalContextProvider;
import org.jasig.portal.Version;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortalContextProviderImpl implements PortalContextProvider {
    
    protected static final WindowState EXCLUSIVE = new WindowState("exclusive");
    
    private Map properties = null;
    private PortletMode[] supportedPortletModes = null;
    private WindowState[] supportedWindowStates = null;
    private String portalInfo = null;
    
    public PortalContextProviderImpl() {
        properties = new HashMap();
        // Should read these from a properties file
        supportedPortletModes = new PortletMode[] {PortletMode.VIEW, PortletMode.EDIT, PortletMode.HELP};
        supportedWindowStates = new WindowState[] {WindowState.NORMAL, WindowState.MAXIMIZED, WindowState.MINIMIZED, EXCLUSIVE};
        portalInfo = Version.getProduct() + "/" + Version.getVersion();
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
