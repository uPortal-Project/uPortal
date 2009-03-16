/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.registry;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Extended version of {@link IPortletWindowRegistry} that handles transient portlet windows that only exist in object
 * form for the duration of a request.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ITransientPortletWindowRegistry extends IPortletWindowRegistry {

    /**
     * Creates an IPortletWindowId for the specified string identifier
     * 
     * @param portletWindowId The string represenation of the portlet window ID.
     * @return The IPortletWindowId for the string
     * @throws IllegalArgumentException If portletWindowId is null
     */
    public IPortletWindowId createTransientPortletWindowId(HttpServletRequest request, IPortletWindowId sourcePortletWindowId);
}
