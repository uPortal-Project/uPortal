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

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;

import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.ICompositeGroupService;
import org.apereo.portal.io.xml.IPortalDataHandlerService;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.provider.AuthorizationPrincipalImpl;
import org.apereo.portal.security.provider.PersonImpl;
import org.apereo.portal.services.AuthorizationService;
import org.apereo.portal.xml.XmlUtilities;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

public class ImportExportControllerTest {

    public static final String ENTITY_TYPE = "user";
    public static final String ENTITY_ID = "123";
    public static final String USER_NAME = "jdoe";

    @InjectMocks
    private ImportExportController importExportController;

    @Mock
    private IPersonManager personManager;

    @Mock
    private IPortalDataHandlerService portalDataHandlerService;

    @Mock
    private XmlUtilities xmlUtilities;

    MockHttpServletResponse res;

    @Mock
    private HttpServletRequest req;

    private MockMultipartFile firstFile;

    @Before
    public void setup() {
        firstFile = new MockMultipartFile("data", "filename.txt", "text/plain", "some xml".getBytes());
        importExportController = new ImportExportController();
        MockitoAnnotations.initMocks(this);
        res = new MockHttpServletResponse();

    }

    @Test//(expected = java.lang.ExceptionInInitializerError.class)
    public void testDeleteEntityUnauthorized() throws IOException {
        EntityIdentifier id = new EntityIdentifier(ENTITY_ID, ICompositeGroupService.LEAF_ENTITY_TYPE);
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);
        Mockito.when( personManager.getPerson(req)).thenReturn(person);
        AuthorizationService service = Mockito.mock(AuthorizationService.class);
        IAuthorizationPrincipal principal = Mockito.mock(AuthorizationPrincipalImpl.class);
        //Mockito.when(AuthorizationService.class.getResourceAsStream("properties/security.properties")).thenReturn("properties/security.properties");
         Mockito.when(AuthorizationService.instance().newPrincipal(USER_NAME,IPerson.class)).thenReturn(principal);
        importExportController.deleteEntity(ENTITY_TYPE, ENTITY_ID, req, res);
    }

    /*@Test(expected = java.lang.ExceptionInInitializerError.class)
    public void testDeleteEntityFound() throws IOException {
        Properties mockProperties = Mockito.mock(Properties.class);
        AuthorizationService service = Mockito.mock(AuthorizationService.class);
       // IAuthorizationPrincipal principal = Mockito.mock(AuthorizationPrincipalImpl.class);
        //Mockito.when(AuthorizationService.class.getResourceAsStream("properties/security.properties")).thenReturn("properties/security.properties");
       // Mockito.when(AuthorizationService.instance().newPrincipal(USER_NAME,IPerson.class)).thenReturn(principal);
        importExportController.deleteEntity(ENTITY_TYPE, "123", req, res);
    }

    /*@Test(expected = java.lang.ExceptionInInitializerError.class)
    public void testImportEnity200() throws IOException,XMLStreamException {
       // Mockito.when( personManager.getPerson(req).getEntityIdentifier()).thenReturn(null);
        importExportController.importEntity(firstFile,req,res);
    }

    @Test(expected = java.lang.ExceptionInInitializerError.class)
    public void testImportEnity401() throws IOException,XMLStreamException {
        Mockito.when( personManager.getPerson(req).getEntityIdentifier()).thenReturn(null);
        importExportController.importEntity(firstFile,req,res);
    }

    @Test(expected = java.lang.ExceptionInInitializerError.class)
    public void testExportEntity401() throws IOException, JSONException {
        importExportController.exportEntity(ENTITY_ID,ENTITY_TYPE,true,"XML", req, res);

    }

    @Test(expected = java.lang.ExceptionInInitializerError.class)
    public void testExportEntityBadRequest() throws IOException, JSONException {
        Mockito.when( personManager.getPerson(req).getEntityIdentifier()).thenReturn(null);
        importExportController.exportEntity(ENTITY_ID,ENTITY_TYPE,true,"XML", req, res);
    }*/

}
