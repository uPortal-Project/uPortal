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
package org.jasig.portal.layout.profile;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.*;

import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.security.IPerson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $RevisionR
 */
public class ChainingProfileMapperImplTest {
    
    @InjectMocks
    ChainingProfileMapperImpl mapper = new ChainingProfileMapperImpl();
    
    @Mock IPerson person;
    @Mock HttpServletRequest request;
    @Mock
    IProfileMapper subMapper1;
    @Mock IProfileMapper subMapper2;
    
    @Mock IUserLayoutStore layoutStore;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        mapper.setDefaultProfileName("profile");
        
        List<IProfileMapper> subMappers = new ArrayList<IProfileMapper>();
        subMappers.add(subMapper1);
        subMappers.add(subMapper2);
        mapper.setSubMappers(subMappers);
    }
    
    @Test
    public void testDefaultProfile() {
        String fname = mapper.getProfileFname(person, request);
        assertEquals("profile", fname);
    }
    
    @Test
    public void testFirstProfile() {
        when(subMapper1.getProfileFname(person, request)).thenReturn("profile1");
        when(subMapper2.getProfileFname(person, request)).thenReturn("profile2");
        String fname = mapper.getProfileFname(person, request);
        assertEquals("profile1", fname);
    }

    @Test
    public void testSecondProfile() {
        when(subMapper2.getProfileFname(person, request)).thenReturn("profile2");
        String fname = mapper.getProfileFname(person, request);
        assertEquals("profile2", fname);
    }

    /**
     * Fails gracefully when one mapper fails, proceeding to the next mapper.
     */
    @Test
    public void testFailsGracefullyProceedingToNextMapper() {
        when(subMapper1.getProfileFname(person, request)).thenThrow(RuntimeException.class);
        when(subMapper2.getProfileFname(person, request)).thenReturn("profile2");
        String fname = mapper.getProfileFname(person, request);
        assertEquals("profile2", fname);
    }

    /**
     * Fails gracefully when the last mapper throws, falling back on the default.
     */
    @Test
    public void testFailsGracefullyToDefaultWhenLastMapperThrows() {
        when(subMapper2.getProfileFname(person, request)).thenThrow(RuntimeException.class);
        String fname = mapper.getProfileFname(person, request);
        // "profile" is the default profile name configured in setUp().
        assertEquals("profile", fname);
    }

}
