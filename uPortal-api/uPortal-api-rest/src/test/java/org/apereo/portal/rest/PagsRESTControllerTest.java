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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import junit.framework.Assert;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.IGroupConstants;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.apereo.portal.groups.pags.dao.PagsService;
import org.apereo.portal.groups.pags.dao.jpa.PersonAttributesGroupDefinitionImpl;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.provider.PersonImpl;
import org.apereo.portal.services.GroupService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class PagsRESTControllerTest {
    public static final String USER_NAME = "jdoe";

    @InjectMocks private PagsRESTController pagsRESTController;

    @Mock private IPersonManager personManager;

    private PagsService pagsService;

    private GroupService groupService;

    private MockHttpServletRequest req;

    private MockHttpServletResponse res;

    @Mock private ObjectMapper mapper;

    @Before
    public void setup() throws Exception {
        pagsRESTController = new PagsRESTController();

        pagsService = new PagsService();

        pagsService = Mockito.mock(PagsService.class);
        groupService = Mockito.mock(GroupService.class);

        req = new MockHttpServletRequest();
        res = new MockHttpServletResponse();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindPagsGroup404() {
        String groupName = "groupname";
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(pagsService.getPagsDefinitionByName(person, groupName)).thenReturn(null);
        String response = pagsRESTController.findPagsGroup(req, res, "groupname");
        String expectedResponse = "{ 'error': 'Not Found' }";

        Assert.assertEquals(expectedResponse, response);
        org.junit.Assert.assertEquals(404, res.getStatus());
    }

    @Test
    public void testFindPagsGroup() throws JsonProcessingException {
        String groupName = "groupname";
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        IPersonAttributesGroupDefinition pagsGroup =
                new PersonAttributesGroupDefinitionImpl(groupName, "this is awesome");
        Mockito.when(pagsService.getPagsDefinitionByName(person, groupName)).thenReturn(pagsGroup);
        pagsRESTController.findPagsGroup(req, res, "groupname");

        Mockito.verify(mapper).writeValueAsString(pagsGroup);
    }

    // @Test
    public void testCreatePagsGroup() throws IOException {
        String groupName = "groupname";
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);
        person.setFullName("john doe");
        String json = null;
        PersonAttributesGroupDefinitionImpl personAttributesGroupDefinition =
                new PersonAttributesGroupDefinitionImpl();
        personAttributesGroupDefinition.setDescription("group description");
        personAttributesGroupDefinition.setName("group101");
        personAttributesGroupDefinition.setTestGroups(Collections.emptySet());
        EntityIdentifier[] eids = new EntityIdentifier[2];
        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(mapper.readValue(json, PersonAttributesGroupDefinitionImpl.class))
                .thenReturn(null);
        Mockito.when(
                        GroupService.searchForGroups(
                                Matchers.anyString(), IGroupConstants.IS, IPerson.class))
                .thenReturn(new EntityIdentifier[2]);

        pagsRESTController.createPagsGroup(req, res, "groupname", json);
        org.junit.Assert.assertEquals(404, res.getStatus());
    }

    @Test
    public void testUpdateGroup404() throws JsonProcessingException {
        String response = pagsRESTController.updatePagsGroup(req, res, "groupName", null);
        String expectedResponse = "{ 'error': 'Not found' }";

        Assert.assertEquals(expectedResponse, response);
        org.junit.Assert.assertEquals(404, res.getStatus());
    }
}
