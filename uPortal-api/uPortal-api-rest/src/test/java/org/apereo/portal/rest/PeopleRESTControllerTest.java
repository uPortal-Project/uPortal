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
package org.apereo.portal.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apereo.portal.portlets.lookup.PersonLookupHelperImpl;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.provider.PersonImpl;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.NamedPersonImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;

public class PeopleRESTControllerTest {

    public static final String USER_NAME = "jdoe";

    @InjectMocks private PeopleRESTController peopleRESTController;

    @Mock private IPersonManager personManager;

    @Mock private PersonLookupHelperImpl lookupHelper;

    @Mock private HttpServletRequest req;

    @Mock private HttpServletResponse res;

    @Before
    public void setup() throws Exception {
        peopleRESTController = new PeopleRESTController();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetPeopleNull() {
        List<String> searchTerms = new ArrayList<>();
        searchTerms.add("john");
        searchTerms.add("doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(null);
        ModelAndView modelAndView = peopleRESTController.getPeople(searchTerms, req, res);

        Assert.assertEquals(null, modelAndView);
    }

    @Test
    public void testGetPeopleEmpty() {
        String query = " ";
        List<String> searchTerms = new ArrayList<>();
        searchTerms.add(query);
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(lookupHelper.searchForPeople(Mockito.any(), Mockito.anyMap()))
                .thenReturn(Collections.emptyList());
        ModelAndView modelAndView = peopleRESTController.getPeople(searchTerms, req, res);

        Assert.assertEquals("json", modelAndView.getViewName());
        List<IPersonAttributes> persons =
                (List<IPersonAttributes>) modelAndView.getModel().get("people");

        Assert.assertTrue(persons.isEmpty());
    }

    @Test
    public void testGetPeople() {

        List<String> searchTerms = new ArrayList<>();
        searchTerms.add("q1");
        searchTerms.add("q2");
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);
        person.setFullName("john doe");

        List<IPersonAttributes> personAttributes = getIPersonAttributes();

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(lookupHelper.searchForPeople(Mockito.any(), Mockito.anyMap()))
                .thenReturn(personAttributes);
        ModelAndView modelAndView = peopleRESTController.getPeople(searchTerms, req, res);

        Assert.assertEquals("json", modelAndView.getViewName());
        List<IPersonAttributes> persons =
                (List<IPersonAttributes>) modelAndView.getModel().get("people");

        Assert.assertEquals(1L, persons.size());
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
        ModelAndView modelAndView = peopleRESTController.getPerson(USER_NAME, req, res);

        Assert.assertEquals(null, modelAndView);
    }

    @Test
    public void testGetPerson() {
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(lookupHelper.findPerson(person, USER_NAME)).thenReturn(getNamedPerson());
        ModelAndView modelAndView = peopleRESTController.getPerson(USER_NAME, req, res);
        IPersonAttributes returnperson = (IPersonAttributes) modelAndView.getModel().get("person");

        Assert.assertEquals("json", modelAndView.getViewName());
        Assert.assertNotNull(returnperson);
        Assert.assertEquals(2L, returnperson.getAttributes().get("school").size());
    }
}
