/*
 Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information regarding copyright ownership. Apereo
 licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 this file except in compliance with the License. You may obtain a copy of the License at the
 following location:

 <p>http://www.apache.org/licenses/LICENSE-2.0

 <p>Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied. See the License for the specific language governing permissions and
 limitations under the License.
*/
package org.apereo.portal.url;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.ISecurityContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is a refactor to correct the application of the session timeout (aka MaxInactive time) to
 * make sure it is applied consistently. The {@code HandlerInterceptorAdapter} version was not
 * applying correctly to JSON and other corner cases.
 *
 * @since 5.7.1
 */
@Slf4j
public class MaxInactiveFilter implements Filter {

    private static final ZoneId tz = ZoneId.systemDefault();

    // IPerson attribute key to flag if this value has already been set
    public static final String SESSION_MAX_INACTIVE_SET_ATTR = "MAX_INACTIVE_SET";

    @Autowired private IPersonManager personManager;

    @Autowired private IMaxInactiveStrategy maxInactiveStrategy;

    public static final int REFRESH_MINUTES = 5;

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain)
            throws IOException, ServletException {
        log.debug("Entering MaxInactiveFilter");
        final HttpServletRequest request = (HttpServletRequest) req;
        checkMaxInactive(request);

        log.debug("Continuing on to other filters from MaxInactiveFilter");
        filterChain.doFilter(req, resp);
    }

    private void checkMaxInactive(HttpServletRequest request) {
        // Check for an existing session
        final HttpSession session = request.getSession(false);
        if (session == null) {
            log.debug("No session found");
            return;
        }

        // Now see if authentication was successful...
        final IPerson person = this.personManager.getPerson(request);
        if (person == null) {
            log.debug("Person not found in Person Manager");
            return;
        }

        final ISecurityContext securityContext = person.getSecurityContext();
        if (securityContext == null || !securityContext.isAuthenticated()) {
            log.debug("{} not authenticated", person.getAttribute(IPerson.USERNAME));
            return;
        }

        // Check if the session max inactive value has been set more recent than the refresh time
        LocalDateTime lastSetTime =
                (LocalDateTime) person.getAttribute(SESSION_MAX_INACTIVE_SET_ATTR);
        if (lastSetTime != null) {
            long sinceLastSetMin = Duration.between(lastSetTime, LocalDateTime.now(tz)).toMinutes();
            log.debug("Since max inactive last set in min: {}", sinceLastSetMin);
            if (REFRESH_MINUTES > sinceLastSetMin) {
                log.debug(
                        "Session.setMaxInactiveInterval() was set for user '{}' {} minutes ago",
                        person.getAttribute(IPerson.USERNAME),
                        sinceLastSetMin);
                return;
            } else {
                log.debug(
                        "Session.setMaxInactiveInterval() refresh time up for'{}' -- continuing ...",
                        person.getAttribute(IPerson.USERNAME));
            }
        } else {
            log.debug(
                    "Session.setMaxInactiveInterval() was not set for user '{}' -- continuing ...",
                    person.getAttribute(IPerson.USERNAME));
        }

        Integer maxInactive = maxInactiveStrategy.calcMaxInactive(person);
        if (maxInactive != null) {
            session.setMaxInactiveInterval(maxInactive);
            log.debug(
                    "Setting maxInactive to '{}' for user '{}'",
                    maxInactive,
                    person.getAttribute(IPerson.USERNAME));
        }
        person.setAttribute(SESSION_MAX_INACTIVE_SET_ATTR, LocalDateTime.now(tz));
    }
}
