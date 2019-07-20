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

    @Test
    public void tooEarly() {
        IPermission permission = mock(IPermission.class);
        final LocalDateTime localDateTime = LocalDateTime.now(tz).plusHours(1);
        final Date future = Date.from(localDateTime.atZone(tz).toInstant());
        when(permission.getEffective()).thenReturn(future);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertNull("", strategy.calcMaxInactive(person));
    }

    @Test
    public void tooLate() {
        IPermission permission = mock(IPermission.class);
        final LocalDateTime localDateTime = LocalDateTime.now(tz).minusHours(1);
        final Date past = Date.from(localDateTime.atZone(tz).toInstant());
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(past);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertNull("", strategy.calcMaxInactive(person));
    }

    @Test
    public void effectiveTime() {
        IPermission permission = mock(IPermission.class);
        final LocalDateTime localDateTime = LocalDateTime.now(tz).plusHours(1);
        final Date future = Date.from(localDateTime.atZone(tz).toInstant());
        final LocalDateTime localDateTime2 = LocalDateTime.now(tz).minusHours(1);
        final Date past = Date.from(localDateTime2.atZone(tz).toInstant());
        when(permission.getEffective()).thenReturn(past);
        when(permission.getExpires()).thenReturn(future);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission.getTarget()).thenReturn("5");

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(5), strategy.calcMaxInactive(person));
    }

    @Test
    public void nonNumberGrantTarget() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission.getTarget()).thenReturn("sdfsd");

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertNull("", strategy.calcMaxInactive(person));
    }

    @Test
    public void nonNumberDenyTarget() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission.getTarget()).thenReturn("sdfsd");

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertNull("", strategy.calcMaxInactive(person));
    }

    @Test
    public void validGrant() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission.getTarget()).thenReturn("10");

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(10), strategy.calcMaxInactive(person));
    }

    @Test
    public void validNegativeGrant() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission.getTarget()).thenReturn("-10");

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(-10), strategy.calcMaxInactive(person));
    }

    @Test
    public void validZeroGrant() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission.getTarget()).thenReturn("0");

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(0), strategy.calcMaxInactive(person));
    }

    @Test
    public void validDeny() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission.getTarget()).thenReturn("104");

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(0), strategy.calcMaxInactive(person));
    }

    @Test
    public void validNegativeDeny() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission.getTarget()).thenReturn("-10");

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        // negative deny permissions make no sense
        assertNull("", strategy.calcMaxInactive(person));
    }

    @Test
    public void validZeroDeny() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission.getTarget()).thenReturn("0");

        permissions = new IPermission[] {permission};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(0), strategy.calcMaxInactive(person));
    }

    @Test
    public void valid2Grants() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission.getTarget()).thenReturn("10");

        IPermission permission2 = mock(IPermission.class);
        when(permission2.getEffective()).thenReturn(null);
        when(permission2.getExpires()).thenReturn(null);
        when(permission2.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission2.getTarget()).thenReturn("100");

        permissions = new IPermission[] {permission, permission2};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(100), strategy.calcMaxInactive(person));
    }

    @Test
    public void valid2GrantsOneNegative() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission.getTarget()).thenReturn("10");

        IPermission permission2 = mock(IPermission.class);
        when(permission2.getEffective()).thenReturn(null);
        when(permission2.getExpires()).thenReturn(null);
        when(permission2.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission2.getTarget()).thenReturn("-100");

        permissions = new IPermission[] {permission, permission2};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        // Negative value means to never timeout
        assertEquals("", Integer.valueOf(-100), strategy.calcMaxInactive(person));
    }

    @Test
    public void valid2Denys() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission.getTarget()).thenReturn("10");

        IPermission permission2 = mock(IPermission.class);
        when(permission2.getEffective()).thenReturn(null);
        when(permission2.getExpires()).thenReturn(null);
        when(permission2.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission2.getTarget()).thenReturn("100");

        permissions = new IPermission[] {permission, permission2};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(0), strategy.calcMaxInactive(person));
    }

    @Test
    public void valid2DenysOneNegative() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission.getTarget()).thenReturn("10");

        IPermission permission2 = mock(IPermission.class);
        when(permission2.getEffective()).thenReturn(null);
        when(permission2.getExpires()).thenReturn(null);
        when(permission2.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission2.getTarget()).thenReturn("-100");

        permissions = new IPermission[] {permission, permission2};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertNull("", strategy.calcMaxInactive(person));
    }

    @Test
    public void validGrantGTDeny() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission.getTarget()).thenReturn("10");

        IPermission permission2 = mock(IPermission.class);
        when(permission2.getEffective()).thenReturn(null);
        when(permission2.getExpires()).thenReturn(null);
        when(permission2.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission2.getTarget()).thenReturn("3");

        permissions = new IPermission[] {permission, permission2};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(3), strategy.calcMaxInactive(person));
    }

    @Test
    public void validGrantLTDeny() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission.getTarget()).thenReturn("10");

        IPermission permission2 = mock(IPermission.class);
        when(permission2.getEffective()).thenReturn(null);
        when(permission2.getExpires()).thenReturn(null);
        when(permission2.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission2.getTarget()).thenReturn("34");

        permissions = new IPermission[] {permission, permission2};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(10), strategy.calcMaxInactive(person));
    }

    @Test
    public void validNegGrantPosDeny() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission.getTarget()).thenReturn("-11");

        IPermission permission2 = mock(IPermission.class);
        when(permission2.getEffective()).thenReturn(null);
        when(permission2.getExpires()).thenReturn(null);
        when(permission2.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission2.getTarget()).thenReturn("3");

        permissions = new IPermission[] {permission, permission2};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(3), strategy.calcMaxInactive(person));
    }

    @Test
    public void validPosGrantNegDeny() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission.getTarget()).thenReturn("12");

        IPermission permission2 = mock(IPermission.class);
        when(permission2.getEffective()).thenReturn(null);
        when(permission2.getExpires()).thenReturn(null);
        when(permission2.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission2.getTarget()).thenReturn("-34");

        permissions = new IPermission[] {permission, permission2};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(12), strategy.calcMaxInactive(person));
    }

    @Test
    public void validNegGrantMoreNegDeny() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission.getTarget()).thenReturn("-11");

        IPermission permission2 = mock(IPermission.class);
        when(permission2.getEffective()).thenReturn(null);
        when(permission2.getExpires()).thenReturn(null);
        when(permission2.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission2.getTarget()).thenReturn("-30");

        permissions = new IPermission[] {permission, permission2};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(-11), strategy.calcMaxInactive(person));
    }

    @Test
    public void validMoreNegGrantNegDeny() {
        IPermission permission = mock(IPermission.class);
        when(permission.getEffective()).thenReturn(null);
        when(permission.getExpires()).thenReturn(null);
        when(permission.getType()).thenReturn(IPermission.PERMISSION_TYPE_GRANT);
        when(permission.getTarget()).thenReturn("-112");

        IPermission permission2 = mock(IPermission.class);
        when(permission2.getEffective()).thenReturn(null);
        when(permission2.getExpires()).thenReturn(null);
        when(permission2.getType()).thenReturn(IPermission.PERMISSION_TYPE_DENY);
        when(permission2.getTarget()).thenReturn("-34");

        permissions = new IPermission[] {permission, permission2};
        when(authorizationService.getAllPermissionsForPrincipal(
                        principal, IPermission.PORTAL_SYSTEM, MAX_INACTIVE_ATTR, null))
                .thenReturn(permissions);

        assertEquals("", Integer.valueOf(-112), strategy.calcMaxInactive(person));
    }
}
