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
package org.jasig.portal.portlets.account;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.services.persondir.IPersonAttributes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

public class UserAccountHelperTest {
    
    UserAccountHelper helper;
    @Mock MessageSource messageSource;
    @Mock HttpServletRequest request;
    Locale locale;

    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        helper = spy(new UserAccountHelper());
        helper.setMessageSource(messageSource);

        locale = Locale.US;
        doReturn(locale).when(helper).getCurrentUserLocale(request);
        
        when(messageSource.getMessage("attribute.displayName.username", new Object[]{}, "username", locale)).thenReturn("Net ID");
        when(messageSource.getMessage("attribute.displayName.user.login.id", new Object[]{}, "user.login.id", locale)).thenReturn("Net ID");
        when(messageSource.getMessage("attribute.displayName.mail", new Object[]{}, "mail", locale)).thenReturn("Email");
        when(messageSource.getMessage("attribute.displayName.displayName", new Object[]{}, "displayName", locale)).thenReturn("Full Name");

    }
    
    @Test
    public void testMatchingDisplayAttributes() {
        final IPersonAttributes person = mock(IPersonAttributes.class);
        
        final Map<String, List<Object>> attributes = new HashMap<String, List<Object>>();
        attributes.put("username", Collections.<Object>singletonList("user1"));
        attributes.put("user.login.id", Collections.<Object>singletonList("user1"));
        when(person.getAttributes()).thenReturn(attributes);
        
        final List<GroupedPersonAttribute> displayed = helper.groupPersonAttributes(person, request);
        assertEquals(1, displayed.size());
        assertEquals(2, displayed.get(0).getAttributeNames().size());
    }

    @Test
    public void testDifferentValueDisplayAttributes() {
        final IPersonAttributes person = mock(IPersonAttributes.class);
        
        final Map<String, List<Object>> attributes = new HashMap<String, List<Object>>();
        attributes.put("username", Collections.<Object>singletonList("user1"));
        attributes.put("user.login.id", Collections.<Object>singletonList("user2"));
        when(person.getAttributes()).thenReturn(attributes);
        
        final List<GroupedPersonAttribute> displayed = helper.groupPersonAttributes(person, request);
        assertEquals(2, displayed.size());
    }

    @Test
    public void testDifferentNameDisplayAttributes() {
        final IPersonAttributes person = mock(IPersonAttributes.class);
        
        final Map<String, List<Object>> attributes = new HashMap<String, List<Object>>();
        attributes.put("username", Collections.<Object>singletonList("user1"));
        attributes.put("mail", Collections.<Object>singletonList("mail@somewhere.com"));
        when(person.getAttributes()).thenReturn(attributes);
        
        final List<GroupedPersonAttribute> displayed = helper.groupPersonAttributes(person, request);
        assertEquals(2, displayed.size());
    }

}
