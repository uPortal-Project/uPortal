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

import org.apache.commons.lang3.Validate;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.services.AuthorizationService;

/**
 * Static convenience methods for working with IAuthorizationPricipal.
 *
 * <p>Currently offers one method which encapsulates the magic for converting a uPortal IPerson to
 * an IAuthorizationPrincipal.
 *
 * @since 4.1
 */
public final class AuthorizationPrincipalHelper {

    /**
     * Convenience method for converting an IPerson to an IAuthorizationPrincipal.
     *
     * @param user a non-null valid IPerson
     * @return an IAuthorizationPrincipal representing that user
     * @throws IllegalArgumentException if the user object is null or defective.
     * @since 4.1
     */
    public static IAuthorizationPrincipal principalFromUser(final IPerson user) {

        Validate.notNull(user, "Cannot determine an authorization principal for null user.");

        final EntityIdentifier userEntityIdentifier = user.getEntityIdentifier();
        Validate.notNull(user, "The user object is defective: lacks entity identifier.");

        final String userEntityKey = userEntityIdentifier.getKey();
        Validate.notNull(userEntityKey, "The user object is defective: lacks entity key.");
        final Class userEntityType = userEntityIdentifier.getType();
        Validate.notNull(userEntityType, "The user object is defective: lacks entity type.");

        final IAuthorizationPrincipal principal =
                AuthorizationService.instance().newPrincipal(userEntityKey, userEntityType);

        return principal;
    }
}
