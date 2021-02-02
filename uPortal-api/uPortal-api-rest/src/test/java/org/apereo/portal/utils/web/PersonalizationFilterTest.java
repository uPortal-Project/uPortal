/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.utils.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.utils.personalize.IPersonalizer;
import org.apereo.portal.utils.personalize.PersonalizerImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

public class PersonalizationFilterTest {

    PersonalizationFilter personalizationFilter;
    private MockHttpServletRequest req;
    private MockHttpServletResponse res;

    @Before
    public void setup() {
        res = new MockHttpServletResponse();
        req = new MockHttpServletRequest();
        personalizationFilter = new PersonalizationFilter();
        IPerson person = mockPerson("user1");
        IPersonManager pMgr = mockPersonManager(req, person);
        personalizationFilter.setPersonManager(pMgr);
    }

    @Test
    public void testDoFilter() throws IOException, ServletException {
        req.setMethod("POST");
        ReflectionTestUtils.setField(personalizationFilter, "enableFilter", true);
        FilterChain chain = new MockFilterChain();
        personalizationFilter.doFilter(req, res, chain);
        Assert.assertEquals(0, res.getContentLength());
    }

    @Test
    public void testDoFilterDisabled() throws IOException, ServletException {
        req.setMethod("GET");
        ReflectionTestUtils.setField(personalizationFilter, "enableFilter", false);
        FilterChain chain = new MockFilterChain();
        personalizationFilter.doFilter(req, res, chain);
        Assert.assertEquals(0, res.getContentLength());
    }

    // Utility classes.  Was not configuring correctly to bring in from uportal-utils test files

    public static IPerson mockPerson(String username) {
        IPerson person = Mockito.mock(IPerson.class);
        Map<String, List<Object>> atts = new HashMap<>();
        atts.put("username", Arrays.asList(username));
        Mockito.when(person.getAttributeMap()).thenReturn(atts);
        Mockito.when(person.getAttribute("username")).thenReturn(username);
        return person;
    }

    public static IPersonalizer mockPersonalizer() {
        final IPersonalizer p = new PersonalizerImpl();
        ReflectionTestUtils.setField(p, "prefix", "apereo.");
        ReflectionTestUtils.setField(p, "patternStr", "\\{\\{(.*?)\\}\\}");
        ReflectionTestUtils.invokeMethod(p, "postConstruct", null);
        return p;
    }

    public static IPersonManager mockPersonManager(HttpServletRequest req, IPerson person) {
        IPersonManager pMgr = Mockito.mock(IPersonManager.class);
        Mockito.when(pMgr.getPerson(req)).thenReturn(person);
        return pMgr;
    }
}
