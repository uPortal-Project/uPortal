/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Appends the username of the current user to the name of the current thread
 *
 */
@Service("threadNamingRequestFilter")
public class ThreadNamingRequestFilter extends OncePerRequestFilter {
    private final ThreadLocal<String> originalThreadNameLocal = new ThreadLocal<String>();
    private IPersonManager personManager;

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    /**
     * Update the thread name to use the specified username. Useful for authentication requests
     * where the username changes mid-request
     */
    public void updateCurrentUsername(String newUsername) {
        final String originalThreadName = originalThreadNameLocal.get();
        if (originalThreadName != null && newUsername != null) {
            final Thread currentThread = Thread.currentThread();
            final String threadName = getThreadName(originalThreadName, newUsername);
            currentThread.setName(threadName);
        }
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String username = getUserName(request);
        if (username == null) {
            //No user, skip thread naming
            filterChain.doFilter(request, response);
        } else {
            final Thread currentThread = Thread.currentThread();
            final String originalThreadName = currentThread.getName();
            try {
                originalThreadNameLocal.set(originalThreadName);
                final String threadName = getThreadName(originalThreadName, username);
                currentThread.setName(threadName);
                filterChain.doFilter(request, response);
            } finally {
                currentThread.setName(originalThreadName);
                originalThreadNameLocal.remove();
            }
        }
    }

    protected String getThreadName(String originalThreadName, String newUsername) {
        return originalThreadName + "-" + newUsername;
    }

    protected String getUserName(HttpServletRequest request) {
        final IPerson person = this.personManager.getPerson(request);
        if (person == null) {
            return null;
        }

        return person.getUserName();
    }
}
