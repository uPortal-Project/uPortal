/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url.support;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletRequestSupport {
    /**
     * If this request targets a portlet.
     * 
     * @param request
     * @return True if a portlet is targeted by this request 
     * @throws RequestParameterProcessingIncompleteException If {@link org.jasig.portal.url.processing.PortletParameterProcessor} has not completed on this request.
     */
    public boolean isPortletTargeted(HttpServletRequest request);
}
