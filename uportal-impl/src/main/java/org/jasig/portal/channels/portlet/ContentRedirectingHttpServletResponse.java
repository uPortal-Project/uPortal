/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.channels.portlet;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.Validate;

/**
 * A wrapper implementation that records output content to a buffer without actually
 * writing it to the underlying response.
 * 
 * @version $Revision: 11911 $
 */
public class ContentRedirectingHttpServletResponse extends HttpServletResponseWrapper {
    private final PrintWriter wrappedWriter;
    
    private PrintWriter writer;
    private boolean committed;

    /**
     * @param servlet response
     */
    public ContentRedirectingHttpServletResponse(HttpServletResponse res, PrintWriter printWriter) {
        super(res);
        
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
        this.writer.flush();
        
        this.committed = true;
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
            ContentRedirectingHttpServletResponse.this.setCommitted(true);
        }
    }
}
