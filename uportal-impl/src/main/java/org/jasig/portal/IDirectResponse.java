/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

import javax.servlet.http.HttpServletResponse;

/**
 * An interface that a channel that wants to
 * have direct access to the HttpServletResponse object must implement.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface IDirectResponse {

    /**
     * Sets the HttpServletResponse for the channel to use.
     * 
     * @param response The HttpServletResponse for the channel to use.
     */
    public void setResponse (HttpServletResponse response) throws PortalException;    
}
