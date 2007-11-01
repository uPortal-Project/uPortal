/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.url.processing;

import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.url.IWritableHttpServletRequest;

/**
 * Provides APIs a class can implement if it wishes to be part of the request parameter processing chain. Implementations
 * can read and write request parameters before any other part of the framework deals with the request.
 *
 * @author Eric Dalquist
 * @version $Revision: 11911 $
 */
public interface IRequestParameterProcessor {
	
    /**
     * Analyze current request, process necessary URL parameters, delivering information to the appropriate components.
     * This method can also add, modify and remove parameters on the request. If the request is not yet in a state where
     * it can be completely processed this method may return <code>false</code> so that it can be called again after other
     * processors have been allowed to execute. Even if <code>false</code> is returned as much processing as possible
     * should happen for each pass.
     * 
     * @param req - incoming request
     * @param res - outgoing response
     * @returns <code>true</code> if processing is complete, <code>false</code> if processing is not complete and this
     * processor should be called again after all other processors have been executed. 
     * @throws IllegalArgumentException If req or res are null.
     */
    public boolean processParameters(IWritableHttpServletRequest request, HttpServletResponse response);
}
