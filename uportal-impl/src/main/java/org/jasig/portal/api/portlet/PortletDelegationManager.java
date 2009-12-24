/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.api.portlet;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.url.PortletUrl;

/**
 * Provides some utility methods for dealing with delegate rendering of portlets
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface PortletDelegationManager {
    public static final String DELEGATE_ACTION_REDIRECT_TOKEN = "DELEGATE_ACTION_REDIRECT";
    
    /**
     * Get the URL data to use for the delegation parent window
     * 
     * @param parentPortletWindowId The ID of the portlet window to get the PortletUrl for
     * @return The PortletUrl for the parent window, null if no base URL data is provided
     */
    public PortletUrl getParentPortletUrl(HttpServletRequest request, IPortletWindowId parentPortletWindowId);
    
    /**
     * Pass the url generated after a delegates processAction 
     * 
     * @param request The portlet adaptor request for the delegate
     * @param portletUrl The URL to pass to the delegation dispatcher
     */
    public void setDelegatePortletActionRedirectUrl(HttpServletRequest request, PortletUrl portletUrl);
    
    /**
     * @param request The portlet adapter request for the delgate parent
     * @return The URL generated after the delegates processAction completes
     */
    public PortletUrl getDelegatePortletActionRedirectUrl(HttpServletRequest request);
}
