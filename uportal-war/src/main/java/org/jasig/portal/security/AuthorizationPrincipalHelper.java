package org.jasig.portal.security;

import org.apache.commons.lang3.Validate;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.services.AuthorizationService;

/**
 * Static convenience methods for working with IAuthorizationPricipal.
 *
 * Currently offers one method which encapsulates the magic for
 * converting a uPortal IPerson to an IAuthorizationPrincipal.
 *
 * @since uPortal 4.1
 */
public final class AuthorizationPrincipalHelper {

    /**
     * Convenience method for converting an IPerson to an IAuthorizationPrincipal.
     * @param user a non-null valid IPerson
     * @return an IAuthorizationPrincipal representing that user
     * @throws IllegalArgumentException if the user object is null or defective.
     * @since uPortal 4.1
     */
    public static IAuthorizationPrincipal principalFromUser(final IPerson user) {

        Validate.notNull(user, "Cannot determine marketplace entries for null user.");

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
