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

import static org.apereo.portal.url.ClassicMaxInactiveStrategy.MAX_INACTIVE_ATTR;
import static org.apereo.portal.url.MaxInactiveFilter.SESSION_MAX_INACTIVE_SET_ATTR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPerson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class ClassicMaxInactiveStrategyTest {

    private static final ZoneId tz = ZoneId.systemDefault();
    private final IPerson person = mock(IPerson.class);
    private final IAuthorizationPrincipal principal = mock(IAuthorizationPrincipal.class);
    private IPermission[] permissions = new IPermission[] {};
    private final IAuthorizationService authorizationService = mock(IAuthorizationService.class);
    private final ClassicMaxInactiveStrategy strategy = new ClassicMaxInactiveStrategy();

    @Before
    public void setUp() {
        when(person.getAttribute(IPerson.USERNAME)).thenReturn("jsmith");
        when(authorizationService.newPrincipal("jsmith", IPerson.class)).thenReturn(principal);

        ReflectionTestUtils.setField(strategy, "authorizationService", authorizationService);
    }

    @After
    public void postChecks() {
        verify(person, never())
                .setAttribute(eq(SESSION_MAX_INACTIVE_SET_ATTR), any(LocalDateTime.class));
        verify(authorizationService, times(1))
                .getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null);
    }

    @Test
    public void noPermissionsWorkflow() {
        permissions = new IPermission[] {};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertNull("", strategy.calcMaxInactive(person));
    }

    @Test
    public void unknownPermissionType() {
        IPermission unknownType = mock(IPermission.class);
        when(unknownType.getEffective()).thenReturn(null);
        when(unknownType.getExpires()).thenReturn(null);
        when(unknownType.getType()).thenReturn("some invalid type string");

        permissions = new IPermission[] {unknownType};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertNull("", strategy.calcMaxInactive(person));
    }

    private IPermission createMockPermission(
            Date future, Date past, String permissionTypeGrant, String s) {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(past);
        when(permission.getExpires()).thenReturn(future);
        when(permission.getType()).thenReturn(permissionTypeGrant);
        when(permission.getTarget()).thenReturn(s);
        return permission;
    }

    @Test
    public void tooEarly() {
        final LocalDateTime localDateTime = LocalDateTime.now(tz).plusHours(1);
        final Date future = Date.from(localDateTime.atZone(tz).toInstant());
        IPermission permission =
                createMockPermission(future, null, IPermission.PERMISSION_TYPE_GRANT, null);

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertNull("", strategy.calcMaxInactive(person));
    }

    @Test
    public void tooLate() {
        final LocalDateTime localDateTime = LocalDateTime.now(tz).minusHours(1);
        final Date past = Date.from(localDateTime.atZone(tz).toInstant());
        IPermission permission =
                createMockPermission(null, past, IPermission.PERMISSION_TYPE_GRANT, null);

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertNull("", strategy.calcMaxInactive(person));
    }

    @Test
    public void effectiveTime() {
        final LocalDateTime localDateTime = LocalDateTime.now(tz).plusHours(1);
        final Date future = Date.from(localDateTime.atZone(tz).toInstant());
        final LocalDateTime localDateTime2 = LocalDateTime.now(tz).minusHours(1);
        final Date past = Date.from(localDateTime2.atZone(tz).toInstant());
        IPermission permission =
                createMockPermission(future, past, IPermission.PERMISSION_TYPE_GRANT, "5");

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(5), strategy.calcMaxInactive(person));
    }

    private void testSinglePermission(String type, String returnValue, Integer calcValue) {
        IPermission permission = createMockPermission(null, null, type, returnValue);

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", calcValue, strategy.calcMaxInactive(person));
    }

    @Test
    public void nonNumberGrantTarget() {
        testSinglePermission(IPermission.PERMISSION_TYPE_GRANT, "sdfsd", null);
    }

    @Test
    public void nonNumberDenyTarget() {
        testSinglePermission(IPermission.PERMISSION_TYPE_DENY, "sdfsd", null);
    }

    @Test
    public void validGrant() {
        testSinglePermission(IPermission.PERMISSION_TYPE_GRANT, "10", Integer.valueOf(10));
    }

    @Test
    public void validNegativeGrant() {
        testSinglePermission(IPermission.PERMISSION_TYPE_GRANT, "-10", Integer.valueOf(-10));
    }

    @Test
    public void validZeroGrant() {
        testSinglePermission(IPermission.PERMISSION_TYPE_GRANT, "0", Integer.valueOf(0));
    }

    @Test
    public void validDeny() {
        testSinglePermission(IPermission.PERMISSION_TYPE_DENY, "104", Integer.valueOf(0));
    }

    @Test
    public void validNegativeDeny() {
        testSinglePermission(IPermission.PERMISSION_TYPE_DENY, "-10", null);
    }

    @Test
    public void validZeroDeny() {
        testSinglePermission(IPermission.PERMISSION_TYPE_DENY, "0", Integer.valueOf(0));
    }

    private void testTwoPermissions(
            String type1,
            String returnValue1,
            String type2,
            String returnValue2,
            Integer calcValue) {
        IPermission permission = createMockPermission(null, null, type1, returnValue1);

        IPermission permission2 = createMockPermission(null, null, type2, returnValue2);

        permissions = new IPermission[] {permission, permission2};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", calcValue, strategy.calcMaxInactive(person));
    }

    @Test
    public void valid2Grants() {
        testTwoPermissions(
                IPermission.PERMISSION_TYPE_GRANT,
                "10",
                IPermission.PERMISSION_TYPE_GRANT,
                "100",
                Integer.valueOf(100));
    }

    @Test
    public void valid2GrantsOneNegative() {
        testTwoPermissions(
                IPermission.PERMISSION_TYPE_GRANT,
                "10",
                IPermission.PERMISSION_TYPE_GRANT,
                "-100",
                Integer.valueOf(-100));
    }

    @Test
    public void valid2Denys() {
        testTwoPermissions(
                IPermission.PERMISSION_TYPE_DENY,
                "10",
                IPermission.PERMISSION_TYPE_DENY,
                "100",
                Integer.valueOf(0));
    }

    @Test
    public void valid2DenysOneNegative() {
        testTwoPermissions(
                IPermission.PERMISSION_TYPE_DENY,
                "10",
                IPermission.PERMISSION_TYPE_DENY,
                "-100",
                null);
    }

    @Test
    public void validGrantGTDeny() {
        testTwoPermissions(
                IPermission.PERMISSION_TYPE_GRANT,
                "10",
                IPermission.PERMISSION_TYPE_DENY,
                "3",
                Integer.valueOf(3));
    }

    @Test
    public void validGrantLTDeny() {
        testTwoPermissions(
                IPermission.PERMISSION_TYPE_GRANT,
                "10",
                IPermission.PERMISSION_TYPE_DENY,
                "34",
                Integer.valueOf(10));
    }

    @Test
    public void validNegGrantPosDeny() {
        testTwoPermissions(
                IPermission.PERMISSION_TYPE_GRANT,
                "-11",
                IPermission.PERMISSION_TYPE_DENY,
                "3",
                Integer.valueOf(3));
    }

    @Test
    public void validPosGrantNegDeny() {
        testTwoPermissions(
                IPermission.PERMISSION_TYPE_GRANT,
                "12",
                IPermission.PERMISSION_TYPE_DENY,
                "-34",
                Integer.valueOf(12));
    }

    @Test
    public void validNegGrantMoreNegDeny() {
        testTwoPermissions(
                IPermission.PERMISSION_TYPE_GRANT,
                "-11",
                IPermission.PERMISSION_TYPE_DENY,
                "-30",
                Integer.valueOf(-11));
    }

    @Test
    public void validMoreNegGrantNegDeny() {
        testTwoPermissions(
                IPermission.PERMISSION_TYPE_GRANT,
                "-112",
                IPermission.PERMISSION_TYPE_DENY,
                "-34",
                Integer.valueOf(-112));
    }
}
