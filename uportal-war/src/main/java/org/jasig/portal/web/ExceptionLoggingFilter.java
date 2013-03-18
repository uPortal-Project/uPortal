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
package org.jasig.portal.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter that logs exceptions before re-throwing. Used to make sure that all portal related exceptions end up in the portal log
 * 
 * @author Eric Dalquist
 */
public class ExceptionLoggingFilter implements Filter {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        
        try {
            chain.doFilter(request, response);
        }
        catch (Throwable t) {
            this.logger.error(t.getMessage(), t);
            
            if (t instanceof Error) {
                throw (Error)t;
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            }
            if (t instanceof ServletException) {
                throw (ServletException)t;
            }
            if (t instanceof IOException) {
                throw (IOException)t;
            }
            throw new ServletException(t);
        }
    }
}
