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

package org.apereo.portal.tenants

import static org.junit.Assert.*

import org.junit.Test

class TemplateDataTenantOperationsListenerTest {
    static final String simplePath = 'classpath:/org/apereo/portal/tenants/sampledata'
    static final String simplePath2 = 'classpath:/org/apereo/portal/tenants/sampledata/'
    static final String wildcardPath = 'classpath:/org/apereo/portal/tenants/sampledata/**/*.xml"
    static final String adminFullPath = "classpath:/org/apereo/portal/tenants/data/group_membership/Administrators.group-membership.xml'
    static final String membersRelPath = 'pags-group/Members.pags-group.xml'
    static final String membersFullPath = 'classpath:/org/apereo/portal/tenants/sampledata/pags-group/Members.pags-group.xml'

    @Test
    void testDetermineImportOnUpdatePaths() {
        final Set<String> configResources = new HashSet<String>()
        configResources.add(adminFullPath)
        configResources.add(membersRelPath)
        final Set<String> fullpathResources = new HashSet<String>()
        fullpathResources.add(adminFullPath)
        fullpathResources.add(membersFullPath)

        Set<String> resources = TemplateDataTenantOperationsListener.determineImportOnUpdatePaths(simplePath, configResources)
        assertEquals('Simple path replacement incorrect', fullpathResources, resources)

        resources = TemplateDataTenantOperationsListener.determineImportOnUpdatePaths(simplePath2, configResources)
        assertEquals('Simple path ending in '/' replacement incorrect', fullpathResources, resources)

        resources = TemplateDataTenantOperationsListener.determineImportOnUpdatePaths(wildcardPath, configResources)
        assertEquals('Wildcard path replacement incorrect', fullpathResources, resources)
    }

}
