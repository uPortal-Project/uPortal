package org.jasig.portal.version;

import static org.mockito.Mockito.when;

import org.jasig.portal.version.dao.VersionDao;
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
    public void testNewerCodeVersion() throws Exception {
        //Code version 4.0.6
        versionVerifier.setRequiredProductVersions(ImmutableMap.of("uPortalDb", VersionUtils.parseVersion("4.0.6")));
        
        //DB version 4.0.5
        when(versionDao.getVersion("uPortalDb")).thenReturn(VersionUtils.parseVersion("4.0.5"));
        
        versionVerifier.afterPropertiesSet();
    }
}
