package org.apereo.portal.portlets.lookup;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPermission;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link PersonLookupHelperImpl}.
 *
 * @since 5.0
 */
public class PersonLookupHelperImplTest extends PersonLookupHelperImpl {

    private static final String GENERALLY_PERMITTED_ATTRIBUTE = "generally.permitted.attribute";
    private static final String PERMITTED_OWN_ATTRIBUTE = "permitted.own.attribute";
    private static final String NEVER_PERMITTED_ATTRIBUTE = "never.permitted.attribute";

    private static final Set<String> ALL_ATTRIBUTES =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            Arrays.asList(
                                    new String[] {
                                        GENERALLY_PERMITTED_ATTRIBUTE,
                                        PERMITTED_OWN_ATTRIBUTE,
                                        NEVER_PERMITTED_ATTRIBUTE
                                    })));

    private IAuthorizationPrincipal principal;

    @Before
    public void init() {

        // Must make the superclass use our collection of attribute names
        IPersonAttributeDao personAttributeDao = mock(IPersonAttributeDao.class);
        when(personAttributeDao.getPossibleUserAttributeNames(null)).thenReturn(ALL_ATTRIBUTES);
        setPersonAttributeDao(personAttributeDao);

        principal = mock(IAuthorizationPrincipal.class);

        // Generally permitted
        when(principal.hasPermission(
                        IPermission.PORTAL_USERS,
                        IPermission.VIEW_USER_ATTRIBUTE_ACTIVITY,
                        GENERALLY_PERMITTED_ATTRIBUTE))
                .thenReturn(true);
        when(principal.hasPermission(
                        IPermission.PORTAL_USERS,
                        IPermission.VIEW_USER_ATTRIBUTE_ACTIVITY,
                        PERMITTED_OWN_ATTRIBUTE))
                .thenReturn(false);
        when(principal.hasPermission(
                        IPermission.PORTAL_USERS,
                        IPermission.VIEW_USER_ATTRIBUTE_ACTIVITY,
                        NEVER_PERMITTED_ATTRIBUTE))
                .thenReturn(false);

        // Own attributes
        when(principal.hasPermission(
                        IPermission.PORTAL_USERS,
                        IPermission.VIEW_OWN_USER_ATTRIBUTE_ACTIVITY,
                        GENERALLY_PERMITTED_ATTRIBUTE))
                .thenReturn(true);
        when(principal.hasPermission(
                        IPermission.PORTAL_USERS,
                        IPermission.VIEW_OWN_USER_ATTRIBUTE_ACTIVITY,
                        PERMITTED_OWN_ATTRIBUTE))
                .thenReturn(true);
        when(principal.hasPermission(
                        IPermission.PORTAL_USERS,
                        IPermission.VIEW_OWN_USER_ATTRIBUTE_ACTIVITY,
                        NEVER_PERMITTED_ATTRIBUTE))
                .thenReturn(false);
    }

    @Test
    public void testGetPermittedAttributes() {

        final Set<String> permittedAttributes = getPermittedAttributes(principal, ALL_ATTRIBUTES);

        assertTrue(permittedAttributes.contains(GENERALLY_PERMITTED_ATTRIBUTE));
        assertFalse(permittedAttributes.contains(PERMITTED_OWN_ATTRIBUTE));
        assertFalse(permittedAttributes.contains(NEVER_PERMITTED_ATTRIBUTE));
    }

    @Test
    public void testGetPermittedOwnAttributes() {

        final Set<String> permittedAttributes = getPermittedAttributes(principal, ALL_ATTRIBUTES);
        final Set<String> permittedOwnAttributes =
                getPermittedOwnAttributes(principal, permittedAttributes);

        assertTrue(permittedOwnAttributes.contains(GENERALLY_PERMITTED_ATTRIBUTE));
        assertTrue(permittedOwnAttributes.contains(PERMITTED_OWN_ATTRIBUTE));
        assertFalse(permittedOwnAttributes.contains(NEVER_PERMITTED_ATTRIBUTE));
    }
}
