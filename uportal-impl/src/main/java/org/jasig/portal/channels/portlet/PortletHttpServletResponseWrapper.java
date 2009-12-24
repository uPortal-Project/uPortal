/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.channels.portlet;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A wrapper implementation that records output content to a buffer without actually
 * writing it to the underlying response.
 * 
 * @version $Revision: 11911 $
 */
public class PortletHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private final Object urlEncodingMutex;
    private final PrintWriter wrappedWriter;
    
    private PrintWriter writer;
    private boolean committed;

    /**
     * @param servlet response
     */
    public PortletHttpServletResponseWrapper(HttpServletResponse res, PrintWriter printWriter) {
        super(res);
        
        this.urlEncodingMutex = this.getRootResponse();
        
        Validate.notNull(printWriter, "printWriter cannot be null");
        this.wrappedWriter = printWriter;
    }

    /**
     * @param committed Marks the response committed
     */
    private void setCommitted(boolean committed) {
        this.committed = committed;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getOutputStream()
     */
    @Override
    public ServletOutputStream getOutputStream() {
        throw new UnsupportedOperationException("As of Pluto 1.1.4 Portlet rendering should always only use getWriter() on the underlying HttpServletResponse");
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getWriter()
     */
    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException {
        if (this.writer == null) {
            this.writer = new PrintWriterWrapper(this.wrappedWriter);
        }

        return writer;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#setBufferSize(int)
     */
    @Override
    public void setBufferSize(int bufferSize) {
        //Ignored
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getBufferSize()
     */
    @Override
    public int getBufferSize() {
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#flushBuffer()
     */
    @Override
    public void flushBuffer() {
        this.committed = true;
        
        if (this.writer != null) {
            this.writer.flush();
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#resetBuffer()
     */
    @Override
    public void resetBuffer() {
        if (this.committed) {
            throw new IllegalStateException("Cannot reset buffer - response is already committed");
        }

        //no buffering for now
        //TODO implement some sort of buffering
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PortletHttpServletResponseWrapper)) {
            return false;
        }
        
        final PortletHttpServletResponseWrapper rhs = (PortletHttpServletResponseWrapper)obj;
        
        return new EqualsBuilder()
            .append(this.getResponse(), rhs.getResponse())
            .append(this.committed, rhs.committed)
            .append(this.wrappedWriter, rhs.wrappedWriter)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(977252351, 179084321)
            .append(this.getResponse())
            .append(this.committed)
            .append(this.wrappedWriter)
            .toHashCode();
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

    protected HttpServletResponse getRootResponse() {
        HttpServletResponse response = this;
        
        final Set<HttpServletResponse> loopDetector = new HashSet<HttpServletResponse>();
        
        while (response instanceof HttpServletResponseWrapper && loopDetector.add(response)) {
            response = (HttpServletResponse)super.getResponse();
        }
        
        return response;
    }


    /**
     * Wrapper to watch for {@link #flush()} calls and to mark the response committed when that happens
     */
    private class PrintWriterWrapper extends PrintWriter {
        public PrintWriterWrapper(PrintWriter pw) {
            super(pw);
        }

        /* (non-Javadoc)
         * @see java.io.PrintWriter#flush()
         */
        @Override
        public void flush() {
            super.flush();
            PortletHttpServletResponseWrapper.this.setCommitted(true);
        }
    }
}
