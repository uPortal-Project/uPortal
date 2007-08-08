/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.*;

public class StoredServletResponseImpl extends javax.servlet.http.HttpServletResponseWrapper {
    
    private PrintWriter writer;

    public StoredServletResponseImpl(HttpServletResponse response, PrintWriter _writer) {
        super(response);
        writer = _writer;
    }

    private javax.servlet.http.HttpServletResponse _getHttpServletResponse() {
        return (javax.servlet.http.HttpServletResponse)super.getResponse();
    }

    public void setResponse(HttpServletResponse response) {
        super.setResponse(response);
    }

    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    public void flushBuffer() throws IOException {
        writer.flush();
    }

}
