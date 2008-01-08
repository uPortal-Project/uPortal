/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.url.processing;

import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.url.IWritableHttpServletRequest;

/**
 * IRequestParameterController presents an interface that is capable of processing an incoming
 * request and creating URL-generating objects, all according to the internal syntax.
 *
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision: 11911 $
 */
public interface IRequestParameterProcessorController {
    /**
     * Analyze current request, process necessary URL parameters,
     * and deliver information to the appropriate components.
     * 
     * @param req the incoming request
     * @param res the outgoing response
     * @throws IllegalArgumentException if req or res are null.
     */
    public void processParameters(IWritableHttpServletRequest req, HttpServletResponse res);
}
