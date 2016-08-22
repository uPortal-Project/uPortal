package org.jasig.portal.tenants

import static org.junit.Assert.*

import org.junit.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource

class TemplateDataTenantOperationsListenerTest {
    static final String simplePath="classpath:/org/jasig/portal/tenants/sampledata";
    static final String simplePath2="classpath:/org/jasig/portal/tenants/sampledata/";
    static final String wildcardPath="classpath:/org/jasig/portal/tenants/sampledata/**/*.xml";

    @Test
    void testDetermineImportOnUpdatePaths() {
        final Set<Resource> configResources = new HashSet<Resource>();
        final ClassPathResource fullpathResource =
                new ClassPathResource("classpath:/org/jasig/portal/tenants/data/group_membership/Administrators.group-membership.xml");
        final ClassPathResource relpathResource = new ClassPathResource("pags-group/Members.pags-group.xml");
        configResources.add(fullpathResource);
        configResources.add(relpathResource);
        final Set<Resource> fullpathResources = new HashSet<Resource>();
        fullpathResources.add(new ClassPathResource("classpath:/org/jasig/portal/tenants/data/group_membership/Administrators.group-membership.xml"));
        fullpathResources.add(new ClassPathResource("classpath:/org/jasig/portal/tenants/sampledata/pags-group/Members.pags-group.xml"));

        println(fullpathResource.getFilename());
        println(fullpathResource.getPath());
        println(relpathResource.getPath());

        Set<Resource> resources = TemplateDataTenantOperationsListener.determineImportOnUpdatePaths(simplePath, configResources);
        assertEquals("Simple path replacement incorrect", fullpathResources, resources);

        resources = TemplateDataTenantOperationsListener.determineImportOnUpdatePaths(simplePath2, configResources);
        assertEquals("Simple path ending in '/' replacement incorrect", fullpathResources, resources);

        resources = TemplateDataTenantOperationsListener.determineImportOnUpdatePaths(wildcardPath, configResources);
        assertEquals("Wildcard path replacement incorrect", fullpathResources, resources);
    }
}