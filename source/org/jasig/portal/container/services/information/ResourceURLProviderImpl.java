/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.services.information;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.ResourceURLProvider;
import org.jasig.portal.container.om.window.PortletWindowImpl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ResourceURLProviderImpl implements ResourceURLProvider {

    private PortletWindow portletWindow = null;
    private String stringUrl = "";
    private String base = "";

    public ResourceURLProviderImpl(DynamicInformationProviderImpl provider, PortletWindow portletWindow) {
        this.portletWindow = portletWindow;
        this.base = getBaseUrl(((PortletWindowImpl)portletWindow).getHttpServletRequest());
    }

    // ResourceURLProvider methods
    
    public void setAbsoluteURL(String path) {
        stringUrl = path;
    }

    public void setFullPath(String path) {
        stringUrl = base + path;
    }
    
    public String toString() {
        URL url = null;

        if (!stringUrl.equals("")) {
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException e) {
                throw new java.lang.IllegalArgumentException("A malformed URL has occured");
            }
        }

        return ((url == null) ? "" : url.toString());
    }
    
    // Additional methods
    
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        return scheme + "://" + serverName + ":" + serverPort;
    }
}
