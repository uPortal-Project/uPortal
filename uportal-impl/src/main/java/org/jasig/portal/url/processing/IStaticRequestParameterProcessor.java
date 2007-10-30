/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.url.processing;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides APIs a class can implement if it wishes to be part of the request parameter processing chain. Implementations
 * can read request parameters before any other part of the framework deals with the request.
 *
 * @author Eric Dalquist
 * @version $Revision: 11911 $
 */
public interface IStaticRequestParameterProcessor {

    /**
     * Analyze current request, process necessary URL parameters, delivering information to the appropriate components.
     * 
     * @param req - incoming request
     * @param res - outgoing response
     * @throws IllegalArgumentException If req or res are null.
     */
    public void processParameters(HttpServletRequest req, HttpServletResponse res);
}
