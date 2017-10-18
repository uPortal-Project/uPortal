/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.portal.spring.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/** Decorator to enable/disable a filter based on a configuration property. */
public class OptionalFilterDecorator implements Filter {

    private boolean enable = false;
    private Filter wrappedFilter;

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setWrappedFilter(Filter wrappedFilter) {
        this.wrappedFilter = wrappedFilter;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (wrappedFilter == null) {
            throw new ServletException("No wrappedFilter configured");
        }

        if (this.enable) {
            this.wrappedFilter.init(filterConfig);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (wrappedFilter == null) {
            throw new ServletException("No wrappedFilter configured");
        }

        if (this.enable) {
            this.wrappedFilter.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        if (this.enable && this.wrappedFilter != null) {
            this.wrappedFilter.destroy();
        }
    }
}
