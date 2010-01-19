/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.url;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Wrapper for all portal responses
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private final Object urlEncodingMutex = new Object();

    public PortalHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    /*
     * encoding URLs is not thread-safe in Tomcat, sync around url encoding
     */

    @Override
    public String encodeRedirectUrl(String url) {
        synchronized (this.urlEncodingMutex) {
            return super.encodeRedirectUrl(url);
        }
    }

    @Override
    public String encodeRedirectURL(String url) {
        synchronized (this.urlEncodingMutex) {
            return super.encodeRedirectURL(url);
        }
    }

    @Override
    public String encodeUrl(String url) {
        synchronized (this.urlEncodingMutex) {
            return super.encodeUrl(url);
        }
    }

    @Override
    public String encodeURL(String url) {
        synchronized (this.urlEncodingMutex) {
            return super.encodeURL(url);
        }
    }
}
