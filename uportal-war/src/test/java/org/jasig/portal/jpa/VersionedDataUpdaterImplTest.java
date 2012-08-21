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
package org.jasig.portal.jpa;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.jasig.portal.version.VersionUtils;
import org.jasig.portal.version.dao.VersionDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class VersionedDataUpdaterImplTest {
    @InjectMocks private VersionedDataUpdaterImpl versionedDataUpdaterImpl = new VersionedDataUpdaterImpl();
    @Mock private VersionDao versionDao;
    @Mock private IVersionedDatabaseUpdateHelper updateHelperPortalDb402;
    @Mock private IVersionedDatabaseUpdateHelper updateHelperRawEventsDb402;
    @Mock private IVersionedDatabaseUpdateHelper updateHelperRawEventsDb403;
    
    @Before
    public void setup() {
        when(updateHelperPortalDb402.getVersion()).thenReturn(VersionUtils.parseVersion("4.0.2"));
        when(updateHelperPortalDb402.getDatabaseName()).thenReturn("PortalDb");
        when(updateHelperRawEventsDb402.getVersion()).thenReturn(VersionUtils.parseVersion("4.0.2"));
        when(updateHelperRawEventsDb402.getDatabaseName()).thenReturn("RawEventsDb");
        when(updateHelperRawEventsDb403.getVersion()).thenReturn(VersionUtils.parseVersion("4.0.3"));
        when(updateHelperRawEventsDb403.getDatabaseName()).thenReturn("RawEventsDb");
        
        
        versionedDataUpdaterImpl.setRequiredProductVersions(ImmutableMap.of(
                "PortalDb", VersionUtils.parseVersion("4.0.6"),
                "RawEventsDb", VersionUtils.parseVersion("4.0.6"),
                "AggrEventsDb", VersionUtils.parseVersion("4.0.6")));
        
        versionedDataUpdaterImpl.setVersionedDatabaseUpdateHelpers(ImmutableSet.of(updateHelperPortalDb402, updateHelperRawEventsDb402, updateHelperRawEventsDb403));
    }
    
    @Test
    public void testPostInit() {
        versionedDataUpdaterImpl.postInitDatabase("PortalDb");
        versionedDataUpdaterImpl.postInitDatabase("RawEventsDb");
        versionedDataUpdaterImpl.postInitDatabase("AggrEventsDb");
        
        verify(versionDao).setVersion("PortalDb", VersionUtils.parseVersion("4.0.6"));
        verify(versionDao).setVersion("RawEventsDb", VersionUtils.parseVersion("4.0.6"));
        verify(versionDao).setVersion("AggrEventsDb", VersionUtils.parseVersion("4.0.6"));
        
        verify(updateHelperPortalDb402).getDatabaseName();
        verify(updateHelperRawEventsDb402).getDatabaseName();
        verify(updateHelperRawEventsDb402).getVersion();
        verify(updateHelperRawEventsDb403).getDatabaseName();
        verify(updateHelperRawEventsDb403).getVersion();
        
        verifyNoMoreInteractions(versionDao, updateHelperPortalDb402, updateHelperRawEventsDb402, updateHelperRawEventsDb403);
    }
    
    @Test
    public void testPreUpdateReturnNull() {
        versionedDataUpdaterImpl.preUpdateDatabase("PortalDb");
        versionedDataUpdaterImpl.preUpdateDatabase("RawEventsDb");
        versionedDataUpdaterImpl.preUpdateDatabase("AggrEventsDb");
        
        verify(versionDao).getVersion("PortalDb");
        verify(versionDao).getVersion("RawEventsDb");
        verify(versionDao).getVersion("AggrEventsDb");
        
        verify(updateHelperPortalDb402).getDatabaseName();
        verify(updateHelperPortalDb402).getVersion();
        verify(updateHelperRawEventsDb402).getDatabaseName();
        verify(updateHelperRawEventsDb402, times(2)).getVersion();
        verify(updateHelperRawEventsDb403).getDatabaseName();
        verify(updateHelperRawEventsDb403, times(2)).getVersion();
        
        verify(updateHelperPortalDb402).preUpdate();
        verify(updateHelperRawEventsDb402).preUpdate();
        verify(updateHelperRawEventsDb403).preUpdate();
        
        verifyNoMoreInteractions(versionDao, updateHelperPortalDb402, updateHelperRawEventsDb402, updateHelperRawEventsDb403);
    }
    
    @Test
    public void testPreUpdate402() {
        when(versionDao.getVersion("PortalDb")).thenReturn(VersionUtils.parseVersion("4.0.2"));
        when(versionDao.getVersion("RawEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.2"));
        when(versionDao.getVersion("AggrEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.2"));

        versionedDataUpdaterImpl.preUpdateDatabase("PortalDb");
        versionedDataUpdaterImpl.preUpdateDatabase("RawEventsDb");
        versionedDataUpdaterImpl.preUpdateDatabase("AggrEventsDb");
        
        verify(versionDao).getVersion("PortalDb");
        verify(versionDao).getVersion("RawEventsDb");
        verify(versionDao).getVersion("AggrEventsDb");
        
        verify(updateHelperPortalDb402).getDatabaseName();
        verify(updateHelperPortalDb402).getVersion();
        verify(updateHelperRawEventsDb402).getDatabaseName();
        verify(updateHelperRawEventsDb402, times(2)).getVersion();
        verify(updateHelperRawEventsDb403).getDatabaseName();
        verify(updateHelperRawEventsDb403, times(2)).getVersion();
        
        verify(updateHelperPortalDb402).preUpdate();
        verify(updateHelperRawEventsDb402).preUpdate();
        verify(updateHelperRawEventsDb403).preUpdate();
        
        verifyNoMoreInteractions(versionDao, updateHelperPortalDb402, updateHelperRawEventsDb402, updateHelperRawEventsDb403);
    }
    
    @Test
    public void testPreUpdate403() {
        when(versionDao.getVersion("PortalDb")).thenReturn(VersionUtils.parseVersion("4.0.3"));
        when(versionDao.getVersion("RawEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.3"));
        when(versionDao.getVersion("AggrEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.3"));

        versionedDataUpdaterImpl.preUpdateDatabase("PortalDb");
        versionedDataUpdaterImpl.preUpdateDatabase("RawEventsDb");
        versionedDataUpdaterImpl.preUpdateDatabase("AggrEventsDb");
        
        verify(versionDao).getVersion("PortalDb");
        verify(versionDao).getVersion("RawEventsDb");
        verify(versionDao).getVersion("AggrEventsDb");
        
        verify(updateHelperPortalDb402).getDatabaseName();
        verify(updateHelperPortalDb402).getVersion();
        verify(updateHelperRawEventsDb402).getDatabaseName();
        verify(updateHelperRawEventsDb402, times(2)).getVersion();
        verify(updateHelperRawEventsDb403).getDatabaseName();
        verify(updateHelperRawEventsDb403, times(2)).getVersion();
        
        verify(updateHelperRawEventsDb403).preUpdate();
        
        verifyNoMoreInteractions(versionDao, updateHelperPortalDb402, updateHelperRawEventsDb402, updateHelperRawEventsDb403);
    }
    
    @Test
    public void testPreUpdate404() {
        when(versionDao.getVersion("PortalDb")).thenReturn(VersionUtils.parseVersion("4.0.4"));
        when(versionDao.getVersion("RawEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.4"));
        when(versionDao.getVersion("AggrEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.4"));

        versionedDataUpdaterImpl.preUpdateDatabase("PortalDb");
        versionedDataUpdaterImpl.preUpdateDatabase("RawEventsDb");
        versionedDataUpdaterImpl.preUpdateDatabase("AggrEventsDb");
        
        verify(versionDao).getVersion("PortalDb");
        verify(versionDao).getVersion("RawEventsDb");
        verify(versionDao).getVersion("AggrEventsDb");
        
        verify(updateHelperPortalDb402).getDatabaseName();
        verify(updateHelperPortalDb402).getVersion();
        verify(updateHelperRawEventsDb402).getDatabaseName();
        verify(updateHelperRawEventsDb402, times(2)).getVersion();
        verify(updateHelperRawEventsDb403).getDatabaseName();
        verify(updateHelperRawEventsDb403, times(2)).getVersion();
        
        verifyNoMoreInteractions(versionDao, updateHelperPortalDb402, updateHelperRawEventsDb402, updateHelperRawEventsDb403);
    }
    
    @Test
    public void testPreUpdate406() {
        when(versionDao.getVersion("PortalDb")).thenReturn(VersionUtils.parseVersion("4.0.6"));
        when(versionDao.getVersion("RawEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.6"));
        when(versionDao.getVersion("AggrEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.6"));

        versionedDataUpdaterImpl.preUpdateDatabase("PortalDb");
        versionedDataUpdaterImpl.preUpdateDatabase("RawEventsDb");
        versionedDataUpdaterImpl.preUpdateDatabase("AggrEventsDb");
        
        verify(versionDao).getVersion("PortalDb");
        verify(versionDao).getVersion("RawEventsDb");
        verify(versionDao).getVersion("AggrEventsDb");
        
        verify(updateHelperPortalDb402).getDatabaseName();
        verify(updateHelperPortalDb402).getVersion();
        verify(updateHelperRawEventsDb402).getDatabaseName();
        verify(updateHelperRawEventsDb402, times(2)).getVersion();
        verify(updateHelperRawEventsDb403).getDatabaseName();
        verify(updateHelperRawEventsDb403, times(2)).getVersion();
        
        verifyNoMoreInteractions(versionDao, updateHelperPortalDb402, updateHelperRawEventsDb402, updateHelperRawEventsDb403);
    }
    
    @Test
    public void testPostUpdateReturnNull() {
        versionedDataUpdaterImpl.postUpdateDatabase("PortalDb");
        versionedDataUpdaterImpl.postUpdateDatabase("RawEventsDb");
        versionedDataUpdaterImpl.postUpdateDatabase("AggrEventsDb");
        
        verify(versionDao).getVersion("PortalDb");
        verify(versionDao).getVersion("RawEventsDb");
        verify(versionDao).getVersion("AggrEventsDb");
        
        verify(updateHelperPortalDb402).getDatabaseName();
        verify(updateHelperPortalDb402).getVersion();
        verify(updateHelperRawEventsDb402).getDatabaseName();
        verify(updateHelperRawEventsDb402, times(2)).getVersion();
        verify(updateHelperRawEventsDb403).getDatabaseName();
        verify(updateHelperRawEventsDb403, times(2)).getVersion();
        
        verify(updateHelperPortalDb402).postUpdate();
        verify(updateHelperRawEventsDb402).postUpdate();
        verify(updateHelperRawEventsDb403).postUpdate();
        
        verify(versionDao).setVersion("PortalDb", VersionUtils.parseVersion("4.0.6"));
        verify(versionDao).setVersion("RawEventsDb", VersionUtils.parseVersion("4.0.6"));
        verify(versionDao).setVersion("AggrEventsDb", VersionUtils.parseVersion("4.0.6"));
        
        verifyNoMoreInteractions(versionDao, updateHelperPortalDb402, updateHelperRawEventsDb402, updateHelperRawEventsDb403);
    }
    
    @Test
    public void testPostUpdate402() {
        when(versionDao.getVersion("PortalDb")).thenReturn(VersionUtils.parseVersion("4.0.2"));
        when(versionDao.getVersion("RawEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.2"));
        when(versionDao.getVersion("AggrEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.2"));

        versionedDataUpdaterImpl.postUpdateDatabase("PortalDb");
        versionedDataUpdaterImpl.postUpdateDatabase("RawEventsDb");
        versionedDataUpdaterImpl.postUpdateDatabase("AggrEventsDb");
        
        verify(versionDao).getVersion("PortalDb");
        verify(versionDao).getVersion("RawEventsDb");
        verify(versionDao).getVersion("AggrEventsDb");
        
        verify(updateHelperPortalDb402).getDatabaseName();
        verify(updateHelperPortalDb402).getVersion();
        verify(updateHelperRawEventsDb402).getDatabaseName();
        verify(updateHelperRawEventsDb402, times(2)).getVersion();
        verify(updateHelperRawEventsDb403).getDatabaseName();
        verify(updateHelperRawEventsDb403, times(2)).getVersion();
        
        verify(updateHelperPortalDb402).postUpdate();
        verify(updateHelperRawEventsDb402).postUpdate();
        verify(updateHelperRawEventsDb403).postUpdate();
        
        verify(versionDao).setVersion("PortalDb", VersionUtils.parseVersion("4.0.6"));
        verify(versionDao).setVersion("RawEventsDb", VersionUtils.parseVersion("4.0.6"));
        verify(versionDao).setVersion("AggrEventsDb", VersionUtils.parseVersion("4.0.6"));
        
        verifyNoMoreInteractions(versionDao, updateHelperPortalDb402, updateHelperRawEventsDb402, updateHelperRawEventsDb403);
    }
    
    @Test
    public void testPostUpdate403() {
        when(versionDao.getVersion("PortalDb")).thenReturn(VersionUtils.parseVersion("4.0.3"));
        when(versionDao.getVersion("RawEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.3"));
        when(versionDao.getVersion("AggrEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.3"));

        versionedDataUpdaterImpl.postUpdateDatabase("PortalDb");
        versionedDataUpdaterImpl.postUpdateDatabase("RawEventsDb");
        versionedDataUpdaterImpl.postUpdateDatabase("AggrEventsDb");
        
        verify(versionDao).getVersion("PortalDb");
        verify(versionDao).getVersion("RawEventsDb");
        verify(versionDao).getVersion("AggrEventsDb");
        
        verify(updateHelperPortalDb402).getDatabaseName();
        verify(updateHelperPortalDb402).getVersion();
        verify(updateHelperRawEventsDb402).getDatabaseName();
        verify(updateHelperRawEventsDb402, times(2)).getVersion();
        verify(updateHelperRawEventsDb403).getDatabaseName();
        verify(updateHelperRawEventsDb403, times(2)).getVersion();
        
        verify(updateHelperRawEventsDb403).postUpdate();
        
        verify(versionDao).setVersion("PortalDb", VersionUtils.parseVersion("4.0.6"));
        verify(versionDao).setVersion("RawEventsDb", VersionUtils.parseVersion("4.0.6"));
        verify(versionDao).setVersion("AggrEventsDb", VersionUtils.parseVersion("4.0.6"));
        
        verifyNoMoreInteractions(versionDao, updateHelperPortalDb402, updateHelperRawEventsDb402, updateHelperRawEventsDb403);
    }
    
    @Test
    public void testPostUpdate404() {
        when(versionDao.getVersion("PortalDb")).thenReturn(VersionUtils.parseVersion("4.0.4"));
        when(versionDao.getVersion("RawEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.4"));
        when(versionDao.getVersion("AggrEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.4"));

        versionedDataUpdaterImpl.postUpdateDatabase("PortalDb");
        versionedDataUpdaterImpl.postUpdateDatabase("RawEventsDb");
        versionedDataUpdaterImpl.postUpdateDatabase("AggrEventsDb");
        
        verify(versionDao).getVersion("PortalDb");
        verify(versionDao).getVersion("RawEventsDb");
        verify(versionDao).getVersion("AggrEventsDb");
        
        verify(updateHelperPortalDb402).getDatabaseName();
        verify(updateHelperPortalDb402).getVersion();
        verify(updateHelperRawEventsDb402).getDatabaseName();
        verify(updateHelperRawEventsDb402, times(2)).getVersion();
        verify(updateHelperRawEventsDb403).getDatabaseName();
        verify(updateHelperRawEventsDb403, times(2)).getVersion();
        
        verify(versionDao).setVersion("PortalDb", VersionUtils.parseVersion("4.0.6"));
        verify(versionDao).setVersion("RawEventsDb", VersionUtils.parseVersion("4.0.6"));
        verify(versionDao).setVersion("AggrEventsDb", VersionUtils.parseVersion("4.0.6"));
        
        verifyNoMoreInteractions(versionDao, updateHelperPortalDb402, updateHelperRawEventsDb402, updateHelperRawEventsDb403);
    }
    
    @Test
    public void testPostUpdate406() {
        when(versionDao.getVersion("PortalDb")).thenReturn(VersionUtils.parseVersion("4.0.6"));
        when(versionDao.getVersion("RawEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.6"));
        when(versionDao.getVersion("AggrEventsDb")).thenReturn(VersionUtils.parseVersion("4.0.6"));

        versionedDataUpdaterImpl.postUpdateDatabase("PortalDb");
        versionedDataUpdaterImpl.postUpdateDatabase("RawEventsDb");
        versionedDataUpdaterImpl.postUpdateDatabase("AggrEventsDb");
        
        verify(versionDao).getVersion("PortalDb");
        verify(versionDao).getVersion("RawEventsDb");
        verify(versionDao).getVersion("AggrEventsDb");
        
        verify(updateHelperPortalDb402).getDatabaseName();
        verify(updateHelperPortalDb402).getVersion();
        verify(updateHelperRawEventsDb402).getDatabaseName();
        verify(updateHelperRawEventsDb402, times(2)).getVersion();
        verify(updateHelperRawEventsDb403).getDatabaseName();
        verify(updateHelperRawEventsDb403, times(2)).getVersion();
        
        verify(versionDao).setVersion("PortalDb", VersionUtils.parseVersion("4.0.6"));
        verify(versionDao).setVersion("RawEventsDb", VersionUtils.parseVersion("4.0.6"));
        verify(versionDao).setVersion("AggrEventsDb", VersionUtils.parseVersion("4.0.6"));
        
        verifyNoMoreInteractions(versionDao, updateHelperPortalDb402, updateHelperRawEventsDb402, updateHelperRawEventsDb403);
    }
}
