/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.api.portlet;

import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface PortletDelegationLocator {
    public PortletDelegationDispatcher createRequestDispatcher(String fName);
    
    public PortletDelegationDispatcher createRequestDispatcher(IPortletDefinitionId portletDefinitionId);
    
    public PortletDelegationDispatcher getRequestDispatcher(IPortletWindowId iPortletWindowId);
}
