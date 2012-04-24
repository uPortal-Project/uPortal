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
package org.jasig.portal.spring.security;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Iterators;

public class RemoteUserSettingFilter implements Filter {
    private File remoteUserFile;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        final String remoteUserFileName = filterConfig.getInitParameter("remoteUserFile");
        this.remoteUserFile = new File(remoteUserFileName);
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final String remoteUser = StringUtils.trimToNull(FileUtils.readFileToString(this.remoteUserFile));
        
        if (remoteUser != null) {
            request = new HttpServletRequestWrapper((HttpServletRequest) request) {
                /* (non-Javadoc)
                 * @see javax.servlet.http.HttpServletRequestWrapper#getRemoteUser()
                 */
                @Override
                public String getRemoteUser() {
                    return remoteUser;
                }

                /* (non-Javadoc)
                 * @see javax.servlet.http.HttpServletRequestWrapper#getHeader(java.lang.String)
                 */
                @Override
                public String getHeader(String name) {
                    if ("REMOTE_USER".equals(name)) {
                        return remoteUser;
                    }
                    return super.getHeader(name);
                }

                /* (non-Javadoc)
                 * @see javax.servlet.http.HttpServletRequestWrapper#getHeaders(java.lang.String)
                 */
                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("REMOTE_USER".equals(name)) {
                        return Iterators.asEnumeration(Collections.singleton(remoteUser).iterator());
                    }
                    return super.getHeaders(name);
                }

                /* (non-Javadoc)
                 * @see javax.servlet.http.HttpServletRequestWrapper#getHeaderNames()
                 */
                @Override
                public Enumeration<String> getHeaderNames() {
                    final LinkedHashSet<String> headers = new LinkedHashSet<String>();
                    for (final Enumeration<String> headersEnum = super.getHeaderNames(); headersEnum.hasMoreElements();) {
                        headers.add(headersEnum.nextElement());
                    }
                    headers.add("REMOTE_USER");
                    
                    return Iterators.asEnumeration(headers.iterator());
                }

                /* (non-Javadoc)
                 * @see javax.servlet.http.HttpServletRequestWrapper#getIntHeader(java.lang.String)
                 */
                @Override
                public int getIntHeader(String name) {
                    if ("REMOTE_USER".equals(name)) {
                        return Integer.valueOf(remoteUser);
                    }
                    return super.getIntHeader(name);
                }
            };
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
