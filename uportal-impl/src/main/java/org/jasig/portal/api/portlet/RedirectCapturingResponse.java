/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.api.portlet;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Captures a redirect instead of passing it up the chain.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class RedirectCapturingResponse extends HttpServletResponseWrapper {
    private String location = null;
    
    public RedirectCapturingResponse(HttpServletResponse response) {
        super(response);
    }
    
    public String getRedirectLocation() {
        return this.location;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        this.location = location;
    }
}
