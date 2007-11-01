/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url.support;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.url.IPortalUrlInfo;


/**
 * Provides information about the Portal URL elements for the current request. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalUrlInfoManager {
    /**
     * @param request The request to get the IPortletURLInfo for
     * @return The IPortalUrlInfo for the request.
     * @throws org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException If the IPortalUrlInfo is not yet associated with the request. 
     */
    public IPortalUrlInfo getPortalUrlInfo(HttpServletRequest request);
    
    /**
     * @param request The request to associate the IPortalUrlInfo with.
     * @param portalUrlInfo The IPortalUrlInfo to associate with the request.
     */
    public void setPortalUrlInfo(HttpServletRequest request, IPortalUrlInfo portalUrlInfo);
}
