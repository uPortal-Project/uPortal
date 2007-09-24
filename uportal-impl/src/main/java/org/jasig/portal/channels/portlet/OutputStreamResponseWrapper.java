/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.portlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * The portlet needs to be able to use a {@link javax.servlet.http.HttpServletResponse}
 * that just uses the {@link javax.servlet.ServletOutputStream} as the backing
 * for the {@link java.io.PrintWriter}.
 * <br>
 * When this class is constructed a {@link java.io.PrintWriter} is created
 * and stored in a member variable and returned via {@link #getWriter()}
 * calls. {@link #flushBuffer()} flushes the {@link java.io.PrintWriter}
 * then calls the super {@link #flushBuffer()}.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 */
public class OutputStreamResponseWrapper extends HttpServletResponseWrapper {
    final PrintWriter pw;
    
    /**
     * Create a new wrapper for a {@link HttpServletResponse}, over-ridding
     * the {@link PrintWriter}.
     * 
     * @param response The {@link HttpServletResponse} to wrap.
     * @throws IOException Possible from calling {@link HttpServletResponse#getOutputStream()}
     */
    public OutputStreamResponseWrapper(final HttpServletResponse response) throws IOException {
        super(response);
        this.pw = new PrintWriter(this.getOutputStream());
    }
    
    /**
     * Flushes the local {@link java.io.PrintStream}, then calls the same
     * method on the wrapped {@link HttpServletResponse}.
     * 
     * @see javax.servlet.ServletResponse#flushBuffer()
     */
    public void flushBuffer() throws IOException {
        this.pw.flush();
        super.flushBuffer();
    }
    
    /**
     * Gets the {@link PrintWriter} that wraps the {@link javax.servlet.ServletOutputStream}
     * for the wrapped {@link HttpServletResponse}.
     * 
     * @see javax.servlet.ServletResponse#getWriter()
     */
    public PrintWriter getWriter() throws IOException {
        return this.pw;
    }
}
