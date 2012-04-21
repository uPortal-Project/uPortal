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
package org.jasig.portal.portlet.rendering;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.portlet.MimeResponse;
import javax.servlet.ServletResponse;

/**
 * Used to handle output from portlet render and resource requests.
 * 
 * @author Eric Dalquist
 */
public interface PortletOutputHandler {
    /**
     * @see MimeResponse#getWriter()
     * @see ServletResponse#getWriter() 
     */
    PrintWriter getPrintWriter() throws IOException;
    
    /**
     * @see MimeResponse#getPortletOutputStream()
     * @see ServletResponse#getOutputStream() 
     */
    OutputStream getOutputStream() throws IOException ;
    
    /**
     * @see MimeResponse#flushBuffer()
     * @see ServletResponse#flushBuffer()
     */
    void flushBuffer() throws IOException;
    
    /**
     * @see MimeResponse#getBufferSize()
     * @see ServletResponse#getBufferSize()
     */
    int getBufferSize();
    
    /**
     * @see MimeResponse#isCommitted()
     * @see ServletResponse#isCommitted()
     */
    boolean isCommitted();
    
    /**
     * @see MimeResponse#reset()
     * @see ServletResponse#reset()
     */
    void reset();
    
    /**
     * @see MimeResponse#resetBuffer()
     * @see ServletResponse#resetBuffer()
     */
    void resetBuffer();
    
    /**
     * @see MimeResponse#setBufferSize(int)
     * @see ServletResponse#setBufferSize(int)
     */
    void setBufferSize(int size);
    
    /**
     * @see MimeResponse#setContentType(String)
     * @see ServletResponse#setContentType(String)
     */
    void setContentType(String contentType);
}
