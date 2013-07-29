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

package org.jasig.portal.security.sso;


import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.beans.factory.annotation.Autowired;

public final class RemoteUserFilterBean implements Filter {

    public static final String TICKET_PARAMETER = "ticket";

    @Autowired
    private ISsoTicketDao ticketDao;

    @Override
    public void init(FilterConfig arg0) { /* Nothing to do... */ }

    @Override
    public void destroy() { /* Nothing to do... */ }

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
        final String ticket = req.getParameter(TICKET_PARAMETER);
        final String remoteUser = evaluateTemporarySsoTicket(ticket);
        final ServletRequest wrapper = new HttpServletRequestWrapperImpl(
                (HttpServletRequest) req,
                remoteUser);
        chain.doFilter(wrapper, res);
    }

    /*
     * Implementation
     */

    private String evaluateTemporarySsoTicket(final String uuid) {
        ISsoTicket ticket = this.ticketDao.getTicket(uuid);
        return ticket != null
                ? ticket.getUsername()
                : null;
    }

    /*
     * Nested Types
     */

    private static final class HttpServletRequestWrapperImpl extends HttpServletRequestWrapper {

        private final String remoteUser;

        public HttpServletRequestWrapperImpl(final HttpServletRequest req, final String remoteUser) {
            super(req);
            this.remoteUser = remoteUser;
        }

        @Override
        public String getRemoteUser() {
            return remoteUser;
        }

    }

}
