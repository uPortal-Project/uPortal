/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.session;

import javax.portlet.PortletContext;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.internal.impl.PortletSessionImpl;
import org.jasig.portal.portlet.om.IPortletEntityId;

/**
 * Custom portlet session impl, uses the entityId instead of the windowId for the session namespace
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ScopingPortletSessionImpl extends PortletSessionImpl {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private final IPortletEntityId portletEntityId;

    public ScopingPortletSessionImpl(IPortletEntityId portletEntityId, PortletContext portletContext, InternalPortletWindow internalPortletWindow, HttpSession httpSession) {
        super(portletContext, internalPortletWindow, httpSession);
        this.portletEntityId = portletEntityId;
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.internal.impl.PortletSessionImpl#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String name) {
        Object attribute = super.getAttribute(name);
        if (attribute != null) {
            return attribute;
        }
        
        attribute = super.getAttribute(name, PortletSession.APPLICATION_SCOPE);
        if (attribute != null) {
            final PortletContext portletContext = this.getPortletContext();
            this.logger.warn("Retrieved portlet session attribute '" + name + "' from APPLICATION_SCOPE after failing to find it in PORTLET_SCOPE for PortletContext: " + (portletContext != null ? portletContext.getPortletContextName() : null));
        }
        
        return attribute;
    }

    @Override
    protected String createPortletScopedId(String name) {
        return PORTLET_SCOPE_NAMESPACE + portletEntityId.getStringId() + ID_NAME_SEPARATOR + name;
    }

    @Override
    protected boolean isInCurrentPortletScope(String name) {
        // Portlet-scoped attribute names MUST start with "javax.portlet.p.",
        //   and contain the ID-name separator '?'.
        if (name.startsWith(PORTLET_SCOPE_NAMESPACE) && name.indexOf(ID_NAME_SEPARATOR) > -1) {
            final String id = name.substring(PORTLET_SCOPE_NAMESPACE.length(), name.indexOf(ID_NAME_SEPARATOR));
            return (id.equals(portletEntityId.getStringId()));
        }

        // Application-scoped attribute names are not in portlet scope.
        return false;
    }
}
