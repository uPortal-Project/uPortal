/*
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
package org.apereo.portal.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.portlets.lookup.PersonLookupHelperImpl;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.provider.PersonImpl;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.NamedPersonImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class PeopleRESTControllerV50Test {

    public static final String USER_NAME = "jdoe";

    @InjectMocks private PeopleRESTControllerV50 peopleRESTControllerV50;

    @Mock private IPersonManager personManager;

    @Mock private PersonLookupHelperImpl lookupHelper;

    @Mock private HttpServletRequest req;

    private MockHttpServletResponse res;

    @Mock private ObjectMapper jsonMapper;

    @Before
    public void setup() throws Exception {
        res = new MockHttpServletResponse();
        peopleRESTControllerV50 = new PeopleRESTControllerV50();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSearchPeopleUnauthorized() throws IOException {
        Map<String, Object> query = getQuery();

        Mockito.when(personManager.getPerson(req)).thenReturn(null);
        peopleRESTControllerV50.searchPeople(query, req, res);
        Assert.assertEquals(401, res.getStatus());
    }

    private Map<String, Object> getQuery() {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("school", "ASU");
        query.put("firstname", "john");
        return query;
    }

    @Test
    public void testSearchPeopleNtFound() throws IOException {
        Map<String, Object> query = getQuery();
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(lookupHelper.searchForPeople(Mockito.any(), Mockito.anyMap()))
                .thenReturn(null);
        peopleRESTControllerV50.searchPeople(query, req, res);

        Assert.assertEquals(404, res.getStatus());
    }

    @Test
    public void testSearchPeople() throws IOException {
        Map<String, Object> query = getQuery();
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(lookupHelper.searchForPeople(person, query))
                .thenReturn(getIPersonAttributes());
        peopleRESTControllerV50.searchPeople(query, req, res);

        Assert.assertEquals(200, res.getStatus());
        Assert.assertNotNull(res.getOutputStream());
    }

    private List<IPersonAttributes> getIPersonAttributes() {
        List<IPersonAttributes> personAttributes = new ArrayList();
        personAttributes.add(getNamedPerson());
        return personAttributes;
    }

    private NamedPersonImpl getNamedPerson() {
        final Map<String, List<Object>> mappedAttributes =
                new LinkedHashMap<String, List<Object>>();
        mappedAttributes.put("school", Arrays.asList("ASU", "Standford"));
        return new NamedPersonImpl(USER_NAME, mappedAttributes);
    }

    @Test
    public void testGetPersonNull() {
        Mockito.when(personManager.getPerson(req)).thenReturn(null);
        ModelAndView modelAndView = peopleRESTControllerV50.getPerson(USER_NAME, req, res);

        Assert.assertEquals(401, res.getStatus());
        Assert.assertEquals(null, modelAndView);
    }

    @Test
    public void testGetPerson() {
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(lookupHelper.findPerson(person, USER_NAME)).thenReturn(getNamedPerson());
        ModelAndView modelAndView = peopleRESTControllerV50.getPerson(USER_NAME, req, res);

        Assert.assertEquals("json", modelAndView.getViewName());
        Assert.assertNotNull(modelAndView.getModel());
    }

    @Test
    public void testMe() {
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(lookupHelper.findPerson(person, USER_NAME)).thenReturn(getNamedPerson());
        ModelAndView modelAndView = peopleRESTControllerV50.getMe(req, res);

        Assert.assertEquals("json", modelAndView.getViewName());
        Assert.assertNotNull(modelAndView.getModel());
    }

    @Test
    public void testGetMeUnauthorized() {
        Mockito.when(personManager.getPerson(req)).thenReturn(null);
        ModelAndView modelAndView = peopleRESTControllerV50.getMe(req, res);

        Assert.assertEquals(401, res.getStatus());
        Assert.assertEquals(null, modelAndView);
    }
}
