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
import java.io.PrintWriter;
import java.util.concurrent.Callable;

import javax.portlet.CacheControl;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Response wrapper that lazily retrieves the PrintWriter and ServletOutputStream objects waiting
 * until a method is actually called on one or the other.
 * 
 * Also ignores the close call on either the PrintWriter or ServletOutputStream if {@link CacheControl#useCachedContent()}
 * is true
 * 
 * This is needed as servlet spec says that after a {@link RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
 * the {@link PrintWriter}/{@link ServletOutputStream} must be closed. This is a problem for resource responses that want to use cached content
 * as after the forward is complete the portal needs to replay the cached content.
 * 
 * @author Eric Dalquist
 */
public class ResourceHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private final CacheControl cacheControl;
    private PrintWriter printWriter;
    private ServletOutputStream servletOutputStream;
    
    public ResourceHttpServletResponseWrapper(HttpServletResponse response, CacheControl cacheControl) {
        super(response);
        this.cacheControl = cacheControl;
    }
    
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (servletOutputStream == null) {
            servletOutputStream = new LazyServletOutputStream(new Callable<ServletOutputStream>() {
                @Override
                public ServletOutputStream call() throws Exception {
                    return ResourceHttpServletResponseWrapper.super.getOutputStream();
                }
            }) {
                
                @Override
                public void close() throws IOException {
                    //Don't close the ServletOutputStream if useCachedContent is true!
                    if (!cacheControl.useCachedContent()) {
                        super.close();
                    }
                }
            };
        }
        
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (printWriter == null) {
            printWriter = new LazyPrintWriter(new Callable<PrintWriter>() {
                @Override
                public PrintWriter call() throws Exception {
                    return ResourceHttpServletResponseWrapper.super.getWriter();
                }
            }) {
                
                @Override
                public void close() {
                    //Don't close the PrintWriter if useCachedContent is true!
                    if (!cacheControl.useCachedContent()) {
                        super.close();
                    }
                }
            };
        }
        
        return printWriter;
    }
}
