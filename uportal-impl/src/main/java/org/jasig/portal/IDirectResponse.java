/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import javax.servlet.http.HttpServletResponse;

/**
 * An interface that a channel that wants to
 * have direct access to the HttpServletResponse object must implement.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 */
public interface IDirectResponse {

    /**
     * Sets the HttpServletResponse for the channel to use.
     * 
     * @param response The HttpServletResponse for the channel to use.
     */
    public void setResponse (HttpServletResponse response) throws PortalException;    
}
