/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlet.delegation.jsp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.jsp.JspWriter;

import org.jasig.portal.portlet.rendering.PortletOutputHandler;

/**
 * Output handler that delegates to a {@link JspWriter}
 * 
 * @author Eric Dalquist
 */
public class JspWriterPortletOutputHandler implements PortletOutputHandler {
    private final JspWriter jspWriter;
    private final PrintWriter printWriter;
    
    public JspWriterPortletOutputHandler(JspWriter jspWriter) {
        this.jspWriter = jspWriter;
        this.printWriter = new PrintWriter(this.jspWriter);
    }

    @Override
    public PrintWriter getPrintWriter() throws IOException {
        return this.printWriter;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IllegalStateException("getWriter has already been called");
    }

    @Override
    public void flushBuffer() throws IOException {
        this.printWriter.flush();
    }

    @Override
    public int getBufferSize() {
        return this.jspWriter.getBufferSize();
    }

    @Override
    public boolean isCommitted() {
        return true;
    }

    @Override
    public void reset() {
        throw new IllegalStateException("Response has already been committed");
    }

    @Override
    public void resetBuffer() {
        try {
            this.jspWriter.clear();
        }
        catch (IOException e) {
            throw new IllegalStateException("Response has already been committed", e);
        }
    }

    @Override
    public void setBufferSize(int size) {
    }

    @Override
    public void setContentType(String contentType) {
    }
}
