/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apereo.portal.security.provider

import java.util.Date

import org.apereo.portal.security.IPermission

import static org.junit.Assert.*
import org.junit.Test

class AnyUnblockedGrantPermissionPolicyTest extends AnyUnblockedGrantPermissionPolicy {

    private static final long TWENTY_FOUR_HOURS_IN_MILLIS = 24L * 60L * 60L * 1000L

    private IPermission activeGrantPermission = {
        IPermission rslt = new PermissionImpl('UP_SYSTEM')
        rslt.setPrincipal('local.0')
        rslt.setActivity('CUSTOMIZE')
        rslt.setType('GRANT')
        return rslt
    }()
    private IPermission activeDenyPermission = {
        IPermission rslt = new PermissionImpl('UP_SYSTEM')
        rslt.setPrincipal('local.0')
        rslt.setActivity('CUSTOMIZE')
        rslt.setType('DENY')
        return rslt
    }()
    private IPermission expiredGrantPermission = {
        IPermission rslt = new PermissionImpl('UP_SYSTEM')
        rslt.setPrincipal('local.0')
        rslt.setActivity('CUSTOMIZE')
        rslt.setType('GRANT')
        rslt.setExpires(new Date(System.currentTimeMillis() - TWENTY_FOUR_HOURS_IN_MILLIS))
        return rslt
    }()
    private IPermission futureGrantPermission = {
        IPermission rslt = new PermissionImpl('UP_SYSTEM')
        rslt.setPrincipal('local.0')
        rslt.setActivity('CUSTOMIZE')
        rslt.setType('GRANT')
        rslt.setEffective(new Date(System.currentTimeMillis() + TWENTY_FOUR_HOURS_IN_MILLIS))
        return rslt
    }()

    @Test
    void testContainsType() {
        Set<IPermission> onlyGrant = new HashSet([
            activeGrantPermission
        ])
        Set<IPermission> onlyDeny = new HashSet([
            activeDenyPermission
        ])
        Set<IPermission> bothGrantAndDeny = new HashSet([
            activeGrantPermission,
            activeDenyPermission
        ])

        assertEquals('Incorrect output from containsType() -- ',
            super.containsType(onlyGrant, 'GRANT'), true)
        assertEquals('Incorrect output from containsType() -- ',
            super.containsType(onlyDeny, 'GRANT'), false)
        assertEquals('Incorrect output from containsType() -- ',
            super.containsType(bothGrantAndDeny, 'GRANT'), true)

        assertEquals('Incorrect output from containsType() -- ',
            super.containsType(onlyGrant, 'DENY'), false)
        assertEquals('Incorrect output from containsType() -- ',
            super.containsType(onlyDeny, 'DENY'), true)
        assertEquals('Incorrect output from containsType() -- ',
            super.containsType(bothGrantAndDeny, 'DENY'), true)
    }

    @Test
    void testRemoveInactivePermissions() {
        IPermission[] activePermissions = [
            activeGrantPermission
        ]
        IPermission[] inactivePermissions = [
            expiredGrantPermission,
            futureGrantPermission
        ]

        IPermission[] inpt = activePermissions + inactivePermissions
        Set<IPermission> filteredPermissions = super.removeInactivePermissions(inpt)
        assertEquals('Incorrect output from removeInactivePermissions() -- ',
            activePermissions, filteredPermissions.toArray(new IPermission[0]))
    }

}
