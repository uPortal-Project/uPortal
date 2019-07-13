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
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPermission;
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

    // IPerson attribute key to flag if this value has already been set
    private static final String SESSION_MAX_INACTIVE_SET_ATTR = "MAX_INACTIVE_SET";

    @Autowired private IPersonManager personManager;

    @Autowired private IAuthorizationService authorizationService;

    private int refreshMinutes = 5;

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain)
            throws IOException, ServletException {
        log.info("Entering MaxInactiveFilter");
        final HttpServletRequest request = (HttpServletRequest) req;
        checkMaxInactive(request);

        log.info("Continuing on to other filters from MaxInactiveFilter");
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

        // Check if the session max inactive value has been set more recent than the refresh time
        LocalDateTime lastSetTime =
                (LocalDateTime) person.getAttribute(SESSION_MAX_INACTIVE_SET_ATTR);
        if (lastSetTime != null) {
            long sinceLastSetMin = Duration.between(lastSetTime, LocalDateTime.now()).toMinutes();
            log.debug("Since max inactive last set in min: {}", sinceLastSetMin);
            if (refreshMinutes > sinceLastSetMin) {
                log.debug(
                        "Session.setMaxInactiveInterval() was set for user '{}' {} minutes ago",
                        person.getAttribute(IPerson.USERNAME),
                        sinceLastSetMin);
                return;
            }
        }

        setMaxInactive(session, person);
    }

    private void setMaxInactive(HttpSession session, IPerson person) {
        final ISecurityContext securityContext = person.getSecurityContext();
        if (securityContext != null && securityContext.isAuthenticated()) {
            // We have an authenticated user... let's see if any MAX_INACTIVE settings apply...
            IAuthorizationPrincipal principal =
                    authorizationService.newPrincipal(
                            (String) person.getAttribute(IPerson.USERNAME), IPerson.class);
            Integer rulingGrant = null;
            Integer rulingDeny = null;
            IPermission[] permissions =
                    authorizationService.getAllPermissionsForPrincipal(
                            principal, IPermission.PORTAL_SYSTEM, "MAX_INACTIVE", null);
            assert (permissions != null);
            if (permissions.length == 0) {
                // No max inactive permission set for this user
                if (log.isInfoEnabled()) {
                    log.info(
                            "No MAX_INACTIVE permissions apply to user '"
                                    + person.getAttribute(IPerson.USERNAME)
                                    + "'");
                }
                person.setAttribute(SESSION_MAX_INACTIVE_SET_ATTR, LocalDateTime.now());
                return;
            }
            for (IPermission p : permissions) {
                // First be sure the record applies currently...
                long now = System.currentTimeMillis();
                if (p.getEffective() != null && p.getEffective().getTime() > now) {
                    // It's *TOO EARLY* for this record... move on.
                    continue;
                }
                if (p.getExpires() != null && p.getExpires().getTime() < now) {
                    // It's *TOO LATE* for this record... move on.
                    continue;
                }
                if (p.getType().equals(IPermission.PERMISSION_TYPE_GRANT)) {
                    try {
                        Integer grantEntry = Integer.valueOf(p.getTarget());
                        if (rulingGrant == null
                                || grantEntry < 0 /* Any negative number trumps all */
                                || rulingGrant < grantEntry) {
                            rulingGrant = grantEntry;
                        }
                    } catch (NumberFormatException nfe) {
                        log.warn(
                                "Invalid MAX_INACTIVE permission grant '"
                                        + p.getTarget()
                                        + "';  target must be an integer value.");
                    }
                } else if (p.getType().equals(IPermission.PERMISSION_TYPE_DENY)) {
                    try {
                        Integer denyEntry = Integer.valueOf(p.getTarget());
                        if (rulingDeny == null || rulingDeny > denyEntry) {
                            rulingDeny = denyEntry;
                        }
                    } catch (NumberFormatException nfe) {
                        log.warn(
                                "Invalid MAX_INACTIVE permission deny '"
                                        + p.getTarget()
                                        + "';  target must be an integer value.");
                    }
                } else {
                    log.warn("Unknown permission type:  " + p.getType());
                }
            }

            if (rulingDeny != null && rulingDeny < 0) {
                // Negative MaxInactiveInterval values mean the session never
                // times out, so a negative DENY is somewhat nonsensical... just
                // clear it.
                log.warn(
                        "A MAX_INACTIVE DENY entry improperly specified a negative target:  "
                                + rulingDeny);
                rulingDeny = null;
            }
            if (rulingGrant != null || rulingDeny != null) {
                // We only want to intervene if there's some actual value
                // specified... otherwise we'll just let the container settings
                // govern.
                int maxInactive =
                        rulingGrant != null
                                ? rulingGrant
                                : 0; // If rulingGrant is null, rulingDeny won't be...
                if (rulingDeny != null) {
                    // Applying DENY entries is tricky b/c GRANT entries may be negative...
                    int limit = rulingDeny;
                    if (maxInactive >= 0) {
                        maxInactive = limit < maxInactive ? limit : maxInactive;
                    } else {
                        // The best grant was negative (unlimited), so go with limit...
                        maxInactive = limit;
                    }
                }
                // Apply the specified setting...
                session.setMaxInactiveInterval(maxInactive);
                person.setAttribute(SESSION_MAX_INACTIVE_SET_ATTR, LocalDateTime.now());
                if (log.isInfoEnabled()) {
                    log.info(
                            "Setting maxInactive to '"
                                    + maxInactive
                                    + "' for user '"
                                    + person.getAttribute(IPerson.USERNAME)
                                    + "'");
                }
            }
        }
    }
}
