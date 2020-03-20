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

import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("maxInactiveStrategy")
public class ClassicMaxInactiveStrategy implements IMaxInactiveStrategy {

    public static final String MAX_INACTIVE_ATTR = "MAX_INACTIVE";

    @Autowired private IAuthorizationService authorizationService;

    @Override
    public Integer calcMaxInactive(IPerson person) {
        assert person != null;

        IAuthorizationPrincipal principal =
                authorizationService.newPrincipal(
                        (String) person.getAttribute(IPerson.USERNAME), IPerson.class);
        IPermission[] permissions =
                authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null);

        assert permissions != null;
        if (permissions.length == 0) {
            // No max inactive permission set for this user
            log.info(
                    "No {} permissions apply to user '{}'",
                    MAX_INACTIVE_ATTR,
                    person.getAttribute(IPerson.USERNAME));
            return null;
        }

        Integer rulingGrant = null;
        Integer rulingDeny = null;
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
            return maxInactive;
        }
        return null;
    }
}
