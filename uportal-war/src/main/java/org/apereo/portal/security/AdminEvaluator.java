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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.services.AuthorizationService;
import org.apereo.portal.services.GroupService;

/**
 * Provides single location for housing knowledge of the various ways to determine if a user is an
 * administrator or is in any administrative sub-group.
 *
 * @deprecated The few things this class controls should be converted to proper permissions.
 */
@Deprecated
public class AdminEvaluator {

    public static final String PORTAL_ADMINISTRATORS_DISTINGUISHED_GROUP = IPerson.DISTINGUISHED_GROUP + ".PortalAdministrators";


    private static final Log cLog = LogFactory.getLog(AdminEvaluator.class);

    /**
     * Determines if the passed-in IPerson represents a user that is a member of the administrator
     * group or any of its sub groups.
     *
     * @param p
     * @return
     */
    public static boolean isAdmin(IPerson p) {
        IAuthorizationPrincipal iap =
                AuthorizationService.instance()
                        .newPrincipal(
                                p.getEntityIdentifier().getKey(),
                                p.getEntityIdentifier().getType());

        return isAdmin(iap);
    }

    /**
     * Determines if the passed-in authorization principal represents a user that is a member of the
     * administrator group or any of its sub groups.
     */
    public static boolean isAdmin(IAuthorizationPrincipal ap) {
        IGroupMember member = AuthorizationService.instance().getGroupMember(ap);
        return isAdmin(member);
    }

    /**
     * Determines if the passed-in group member represents a user that is a member of the
     * administrator group or any of its sub groups.
     */
    public static boolean isAdmin(IGroupMember member) {
        IEntityGroup adminGroup = null;

        try {
            adminGroup = GroupService.getDistinguishedGroup(PORTAL_ADMINISTRATORS_DISTINGUISHED_GROUP);
        } catch (GroupsException ge) {
            // cannot determine whether or not the user is an admin.
            cLog.error(
                    "Administrative group not found, cannot determine "
                            + "user's admininstrative membership.",
                    ge);
        }

        return (null != adminGroup && adminGroup.deepContains(member));
    }
}
