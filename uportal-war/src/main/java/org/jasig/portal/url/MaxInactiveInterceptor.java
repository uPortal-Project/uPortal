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

package org.jasig.portal.url;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.ISecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class MaxInactiveInterceptor extends HandlerInterceptorAdapter {
    protected final Log log = LogFactory.getLog(getClass());
    private IPersonManager personManager;
    private IAuthorizationService authorizationService;
    
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    
    @Autowired
    public void setAuthorizationService(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }
        
        // Now see if authentication was successful...
        final IPerson person = this.personManager.getPerson((HttpServletRequest) request);
        if (person == null) {
            return true;
        }
        
        final ISecurityContext securityContext = person.getSecurityContext();
        if (securityContext != null && securityContext.isAuthenticated()) {
            // We have an authenticated user... let's see if any MAX_INACTIVE settings apply...
            IAuthorizationPrincipal principal = authorizationService.newPrincipal((String) person.getAttribute(IPerson.USERNAME), IPerson.class);
            Integer rulingGrant = null;
            Integer rulingDeny = null;
            IPermission[] permissions = authorizationService.getAllPermissionsForPrincipal(principal, null, "MAX_INACTIVE", null);
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
                                        || grantEntry.intValue() < 0 /* Any negative number trumps all */
                                        || rulingGrant.intValue() < grantEntry.intValue()) {
                            rulingGrant = grantEntry;
                        }
                    } catch (NumberFormatException nfe) {
                        log.warn("Invalid MAX_INACTIVE permission grant '" 
                                        + p.getTarget() 
                                        + "';  target must be an integer value.");
                    }
                } else if (p.getType().equals(IPermission.PERMISSION_TYPE_DENY)) {
                    try {
                        Integer denyEntry = Integer.valueOf(p.getTarget());
                        if (rulingDeny == null || rulingDeny.intValue() > denyEntry.intValue()) {
                            rulingDeny = denyEntry;
                        }
                    } catch (NumberFormatException nfe) {
                        log.warn("Invalid MAX_INACTIVE permission deny '" 
                                        + p.getTarget() 
                                        + "';  target must be an integer value.");
                    }
                } else {
                    log.warn("Unknown permission type:  " + p.getType());
                }
            }

            if (rulingDeny != null && rulingDeny.intValue() < 0) {
                // Negative MaxInactiveInterval values mean the session never 
                // times out, so a negative DENY is somewhat nonsensical... just 
                // clear it.
                log.warn("A MAX_INACTIVE DENY entry improperly specified a negative target:  " 
                        + rulingDeny.intValue());
                rulingDeny = null;
            }
            if (rulingGrant != null || rulingDeny != null) {
                // We only want to intervene if there's some actual value 
                // specified... otherwise we'll just let the container settings 
                //govern.
                int maxInactive = rulingGrant != null 
                                    ? rulingGrant.intValue() 
                                    : 0;    // If rulingGrant is null, rulingDeny won't be...
                if (rulingDeny != null) {
                    // Applying DENY entries is tricky b/c GRANT entries may be negative...
                    int limit = rulingDeny.intValue();
                    if (maxInactive >= 0) {
                        maxInactive = limit < maxInactive ? limit : maxInactive;
                    } else {
                        // The best grant was negative (unlimited), so go with limit...
                        maxInactive = limit;
                    }
                }
                // Apply the specified setting...
                session.setMaxInactiveInterval(maxInactive);
                if (log.isInfoEnabled()) {
                    log.info("Setting maxInactive to '" + maxInactive 
                                + "' for user '" 
                                + person.getAttribute(IPerson.USERNAME) + "'");
                }
            }
            
        }
        
        return true;
    }
}
