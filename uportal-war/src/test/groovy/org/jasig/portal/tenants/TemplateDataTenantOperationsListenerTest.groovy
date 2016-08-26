package org.jasig.portal.tenants

import static org.junit.Assert.*

import org.junit.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource

class TemplateDataTenantOperationsListenerTest {
    static final String simplePath="classpath:/org/jasig/portal/tenants/sampledata";
    static final String simplePath2="classpath:/org/jasig/portal/tenants/sampledata/";
    static final String wildcardPath="classpath:/org/jasig/portal/tenants/sampledata/**/*.xml";
    static final String adminFullPath="classpath:/org/jasig/portal/tenants/data/group_membership/Administrators.group-membership.xml";
    static final String membersRelPath="pags-group/Members.pags-group.xml";
    static final String membersFullPath="classpath:/org/jasig/portal/tenants/sampledata/pags-group/Members.pags-group.xml";

    @Test
    void testDetermineImportOnUpdatePaths() {
        final Set<String> configResources = new HashSet<String>();
        configResources.add(adminFullPath);
        configResources.add(membersRelPath);
        final Set<String> fullpathResources = new HashSet<String>();
        fullpathResources.add(adminFullPath);
        fullpathResources.add(membersFullPath);

        Set<String> resources = TemplateDataTenantOperationsListener.determineImportOnUpdatePaths(simplePath, configResources);
        assertEquals("Simple path replacement incorrect", fullpathResources, resources);

        resources = TemplateDataTenantOperationsListener.determineImportOnUpdatePaths(simplePath2, configResources);
        assertEquals("Simple path ending in '/' replacement incorrect", fullpathResources, resources);

        resources = TemplateDataTenantOperationsListener.determineImportOnUpdatePaths(wildcardPath, configResources);
        assertEquals("Wildcard path replacement incorrect", fullpathResources, resources);
    }
}