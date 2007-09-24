/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.portlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.portlet.PortletMode;

import org.apache.pluto.om.portlet.ContentType;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ContentTypeImpl implements ContentType, Serializable {
    
    private String contentType = null;
    private Collection portletModes = null;

    public ContentTypeImpl() {
        portletModes = new ArrayList();
        // All portlets must support the VIEW mode
        // The spec doesn't require a portlet's deployment
        // descriptor to declare that is supports VIEW - see PLT.8.6
        portletModes.add(PortletMode.VIEW);
    }

    public String getContentType() {
        return contentType;
    }

    public Iterator getPortletModes() {
        return portletModes.iterator();
    }

    public boolean supportsPortletMode(PortletMode portletMode) {
        return portletModes.contains(portletMode);
    }
    
    // Additional methods
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public void addPortletMode(PortletMode portletMode) {
        portletModes.add(portletMode);
    }

}
