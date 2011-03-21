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

package org.jasig.portal.layout;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.IUserProfile;
import org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.jasig.portal.layout.om.IOutputPropertyDescriptor;
import org.jasig.portal.layout.om.IStylesheetData;
import org.jasig.portal.layout.om.IStylesheetData.Scope;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.security.IPerson;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CompositeStylesheetUserPreferencesTest {
    @Test
    public void testOutputProperties() throws Exception {
        final HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        final IStylesheetDescriptor stylesheetDescriptor = mock(IStylesheetDescriptor.class);
        final IPerson person = mock(IPerson.class);
        final IUserProfile userProfile = mock(IUserProfile.class);
        final IStylesheetUserPreferencesDao stylesheetUserPreferencesDao = mock(IStylesheetUserPreferencesDao.class);
        final IOutputPropertyDescriptor reqOutputPropertyDescriptor = mock(IOutputPropertyDescriptor.class);
        final IOutputPropertyDescriptor sessOutputPropertyDescriptor = mock(IOutputPropertyDescriptor.class);
        final IOutputPropertyDescriptor dbOutputPropertyDescriptor = mock(IOutputPropertyDescriptor.class);
        final IStylesheetUserPreferences dbStylesheetUserPreferences = new StylesheetUserPreferencesImpl(1);
        
        httpServletRequest.getSession(); //init the session

        when(stylesheetDescriptor.getOutputPropertyDescriptor("req")).thenReturn(reqOutputPropertyDescriptor);
        when(reqOutputPropertyDescriptor.getName()).thenReturn("req");
        when(reqOutputPropertyDescriptor.getScope()).thenReturn(Scope.REQUEST);

        when(stylesheetDescriptor.getOutputPropertyDescriptor("sess")).thenReturn(sessOutputPropertyDescriptor);
        when(sessOutputPropertyDescriptor.getName()).thenReturn("sess");
        when(sessOutputPropertyDescriptor.getScope()).thenReturn(Scope.SESSION);
        when(sessOutputPropertyDescriptor.getDefaultValue()).thenReturn("true");
        
        when(stylesheetDescriptor.getOutputPropertyDescriptor("db")).thenReturn(dbOutputPropertyDescriptor);
        when(dbOutputPropertyDescriptor.getName()).thenReturn("db");
        when(dbOutputPropertyDescriptor.getScope()).thenReturn(Scope.PERSISTENT);
        when(dbOutputPropertyDescriptor.getDefaultValue()).thenReturn("blue");
        
        when(stylesheetUserPreferencesDao.getStylesheetUserPreferences(stylesheetDescriptor, person, userProfile)).thenReturn(dbStylesheetUserPreferences);
        
        final Map<Scope, IStylesheetUserPreferences> componentPreferences = new LinkedHashMap<IStylesheetData.Scope, IStylesheetUserPreferences>();
        componentPreferences.put(Scope.PERSISTENT, dbStylesheetUserPreferences);
        
        final CompositeStylesheetUserPreferences stylesheetUserPreferences = new CompositeStylesheetUserPreferences(stylesheetDescriptor, componentPreferences, null, false);
        
        String value;
        Properties outputProperties, expectedProperties;
        
        outputProperties = stylesheetUserPreferences.getOutputProperties();
        expectedProperties = new Properties();
        assertEquals(expectedProperties, outputProperties);
        
        value = stylesheetUserPreferences.getOutputProperty("req");
        assertNull(value);
        
        value = stylesheetUserPreferences.getOutputProperty("sess");
        assertNull(value);
        
        value = stylesheetUserPreferences.setOutputProperty("req", "bar");
        assertNull(value);
        
        value = stylesheetUserPreferences.setOutputProperty("req", "hi");
        assertEquals("bar", value);
        
        outputProperties = stylesheetUserPreferences.getOutputProperties();
        expectedProperties = new Properties();
        expectedProperties.put("req", "hi");
        assertEquals(expectedProperties, outputProperties);
        
        value = stylesheetUserPreferences.setOutputProperty("sess", "tab1");
        assertNull(value);
        
        outputProperties = stylesheetUserPreferences.getOutputProperties();
        expectedProperties = new Properties();
        expectedProperties.put("req", "hi");
        expectedProperties.put("sess", "tab1");
        assertEquals(expectedProperties, outputProperties);
        
        value = stylesheetUserPreferences.removeOutputProperty("req");
        assertEquals("hi", value);
        
        outputProperties = stylesheetUserPreferences.getOutputProperties();
        expectedProperties = new Properties();
        expectedProperties.put("sess", "tab1");
        assertEquals(expectedProperties, outputProperties);
        
        value = stylesheetUserPreferences.setOutputProperty("db", "tab8");
        assertNull(value);
        
        outputProperties = stylesheetUserPreferences.getOutputProperties();
        expectedProperties = new Properties();
        expectedProperties.put("sess", "tab1");
        expectedProperties.put("db", "tab8");
        assertEquals(expectedProperties, outputProperties);
    }
}
