/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container;

import java.util.Enumeration;

import javax.portlet.PortalContext;

import org.jasig.portal.security.IPermission;
import org.jasig.portal.tools.versioning.Version;
import org.jasig.portal.tools.versioning.VersionsManager;

/**
 * Provides basic information about uPortal and features it supports.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalContextImpl implements PortalContext {
    

    /* (non-Javadoc)
     * @see javax.portlet.PortalContext#getPortalInfo()
     */
    public String getPortalInfo() {
        //TODO Refactor this code once VersionsManager is reviewed
        final VersionsManager versionManager = VersionsManager.getInstance();
        final Version version = versionManager.getVersion(IPermission.PORTAL_FRAMEWORK);
        return "uPortal/" + version.dottedTriple();
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortalContext#getProperty(java.lang.String)
     */
    public String getProperty(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortalContext#getPropertyNames()
     */
    public Enumeration getPropertyNames() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortalContext#getSupportedPortletModes()
     */
    public Enumeration getSupportedPortletModes() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortalContext#getSupportedWindowStates()
     */
    public Enumeration getSupportedWindowStates() {
        // TODO Auto-generated method stub
        return null;
    }

}
