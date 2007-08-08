/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.servlet;

import javax.servlet.http.*;

public class ServletResponseImpl extends javax.servlet.http.HttpServletResponseWrapper {

    public ServletResponseImpl(HttpServletResponse response) {
        super(response);
    }

    private javax.servlet.http.HttpServletResponse _getHttpServletResponse() {
        return (javax.servlet.http.HttpServletResponse)super.getResponse();
    }

    public void setResponse(HttpServletResponse response) {
        super.setResponse(response);
    }

    // HttpServletResponseWrapper overlay

}
