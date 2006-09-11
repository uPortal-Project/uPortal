/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import javax.servlet.http.HttpServletResponse;

/**
 * An interface that a multithreaded channel that wants to
 * have direct access to the HttpServletResponse object must implement.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 * 
 * @deprecated Use the IChannel* interfaces instead or write a portlet. For more information see: 
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IMultithreaded+Interfaces

 */
public interface IMultithreadedDirectResponse {
    
    /**
     * Sets the HttpServletResponse for the channel to use.
     * 
     * @param uid The unique ID of the channel
     * @param response The HttpServletResponse for the channel to use.
     */
    public void setResponse (String uid, HttpServletResponse response);    
}
