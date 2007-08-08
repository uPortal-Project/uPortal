/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.servlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

public class ServletResponseFactoryImpl implements ServletResponseFactory {

    private javax.servlet.ServletConfig servletConfig;

    // ServletResponseFactory implementation.

    public javax.servlet.http.HttpServletResponse getServletResponse(HttpServletResponse response) {
        HttpServletResponse servletResponse = new ServletResponseImpl(response);
        return servletResponse;
    }

    public javax.servlet.http.HttpServletResponse getStoredServletResponse(HttpServletResponse response, PrintWriter writer) {
        HttpServletResponse servletResponse = new StoredServletResponseImpl(response, writer);
        return servletResponse;
    }

    // additional methods.

    public void init(javax.servlet.ServletConfig config, java.util.Map properties) throws Exception {
        servletConfig = config;
    }

    public void destroy() throws Exception {
    }
}
