/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.version;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jasig.portal.shell.PortalShellBuildHelper;
import org.jasig.portal.version.dao.VersionDao;
import org.jasig.portal.version.om.Version;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContextException;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class VersionVerifierTest {
    @InjectMocks private final VersionVerifier versionVerifier = new VersionVerifier();
    @Mock private VersionDao versionDao;
    @Mock private PortalShellBuildHelper portalShellBuildHelper;
    
    @Test
    public void testSameVersions() throws Exception {
        //Code version 4.0.6
        versionVerifier.setRequiredProductVersions(ImmutableMap.of("uPortalDb", VersionUtils.parseVersion("4.0.6")));
        
        //DB version 4.0.6
        when(versionDao.getVersion("uPortalDb")).thenReturn(VersionUtils.parseVersion("4.0.6"));
        
        versionVerifier.afterPropertiesSet();
    }
    
    @Test
    public void testNewerDatabaseVersion() throws Exception {
        //Code version 4.0.5
        versionVerifier.setRequiredProductVersions(ImmutableMap.of("uPortalDb", VersionUtils.parseVersion("4.0.5")));
        
        //DB version 4.0.6
        when(versionDao.getVersion("uPortalDb")).thenReturn(VersionUtils.parseVersion("4.0.6"));
        
        versionVerifier.afterPropertiesSet();
    }
    
    @Test(expected=ApplicationContextException.class)
    public void testNewerCodePatchVersionNoAutoUpdate() throws Exception {
        //Code version 4.0.6
        versionVerifier.setRequiredProductVersions(ImmutableMap.of("uPortalDb", VersionUtils.parseVersion("4.0.6")));
        versionVerifier.setUpdatePolicy(null);
        
        //DB version 4.0.5
        when(versionDao.getVersion("uPortalDb")).thenReturn(VersionUtils.parseVersion("4.0.5"));
        
        versionVerifier.afterPropertiesSet();
    }
    
    @Test
    public void testNewerCodePatchVersionWithPatchAutoUpdate() throws Exception {
        //Code version 4.0.6
        versionVerifier.setRequiredProductVersions(ImmutableMap.of("uPortalDb", VersionUtils.parseVersion("4.0.6")));
        versionVerifier.setUpdatePolicy(Version.Field.PATCH);
        
        //DB version 4.0.5
        when(versionDao.getVersion("uPortalDb")).thenReturn(VersionUtils.parseVersion("4.0.5"));
        
        versionVerifier.afterPropertiesSet();
        
        verify(this.portalShellBuildHelper).hibernateUpdate("automated-hibernate-update", "uPortalDb", true, null);
    }
    
    @Test(expected=ApplicationContextException.class)
    public void testNewerCodePatchVersionWithLocalAutoUpdate() throws Exception {
        //Code version 4.0.6
        versionVerifier.setRequiredProductVersions(ImmutableMap.of("uPortalDb", VersionUtils.parseVersion("4.0.6")));
        versionVerifier.setUpdatePolicy(Version.Field.LOCAL);
        
        //DB version 4.0.5
        when(versionDao.getVersion("uPortalDb")).thenReturn(VersionUtils.parseVersion("4.0.5"));
        
        versionVerifier.afterPropertiesSet();
    }
    
    @Test(expected=ApplicationContextException.class)
    public void testNewerCodeLocalVersionNoAutoUpdate() throws Exception {
        //Code version 4.0.5.1
        versionVerifier.setRequiredProductVersions(ImmutableMap.of("uPortalDb", VersionUtils.parseVersion("4.0.5.1")));
        versionVerifier.setUpdatePolicy(null);
        
        //DB version 4.0.5
        when(versionDao.getVersion("uPortalDb")).thenReturn(VersionUtils.parseVersion("4.0.5"));
        
        versionVerifier.afterPropertiesSet();
    }
    
    @Test
    public void testNewerCodeLocalVersionWithPatchAutoUpdate() throws Exception {
        //Code version 4.0.5.1
        versionVerifier.setRequiredProductVersions(ImmutableMap.of("uPortalDb", VersionUtils.parseVersion("4.0.5.1")));
        versionVerifier.setUpdatePolicy(Version.Field.PATCH);
        
        //DB version 4.0.5
        when(versionDao.getVersion("uPortalDb")).thenReturn(VersionUtils.parseVersion("4.0.5"));
        
        versionVerifier.afterPropertiesSet();
        
        verify(this.portalShellBuildHelper).hibernateUpdate("automated-hibernate-update", "uPortalDb", true, null);
    }
    
    @Test
    public void testNewerCodeLocalVersionWithLocalAutoUpdate() throws Exception {
        //Code version 4.0.5.1
        versionVerifier.setRequiredProductVersions(ImmutableMap.of("uPortalDb", VersionUtils.parseVersion("4.0.5.1")));
        versionVerifier.setUpdatePolicy(Version.Field.LOCAL);
        
        //DB version 4.0.5
        when(versionDao.getVersion("uPortalDb")).thenReturn(VersionUtils.parseVersion("4.0.5"));
        
        versionVerifier.afterPropertiesSet();
        
        verify(this.portalShellBuildHelper).hibernateUpdate("automated-hibernate-update", "uPortalDb", true, null);
    }
}
