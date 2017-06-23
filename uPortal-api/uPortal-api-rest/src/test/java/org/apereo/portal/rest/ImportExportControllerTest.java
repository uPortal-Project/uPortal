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

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;

import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.io.xml.IPortalDataHandlerService;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.xml.XmlUtilities;
import org.json.JSONException;
import org.junit.Assert;
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
    public static final String ENTITY_ID = "demo";

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

    @Test
    public void testDeleteEntityUnauthorized() throws IOException {
        //EntityIdentifier id = new EntityIdentifier(ENTITY_ID);
        Mockito.when( personManager.getPerson(req).getEntityIdentifier()).thenReturn(null);
        importExportController.deleteEntity(ENTITY_TYPE, ENTITY_ID, req, res);
        Assert.assertEquals(401,res.getStatus());
    }

    @Test
    public void testDeleteEntityFound() throws IOException {
        //EntityIdentifier id = new EntityIdentifier(ENTITY_ID);
        Mockito.when( personManager.getPerson(req).getEntityIdentifier()).thenReturn(null);
        importExportController.deleteEntity(ENTITY_TYPE, ENTITY_ID, req, res);
        Assert.assertEquals(204,res.getStatus());
    }

    @Test
    public void testImportEnity200() throws IOException,XMLStreamException {

        //EntityIdentifier id = new EntityIdentifier(ENTITY_ID);
        Mockito.when( personManager.getPerson(req).getEntityIdentifier()).thenReturn(null);
        importExportController.importEntity(firstFile,req,res);
        //Assert.assertEquals(401,res.getStatus());
    }

    @Test
    public void testImportEnity401() throws IOException,XMLStreamException {
        //EntityIdentifier id = new EntityIdentifier(ENTITY_ID);
        Mockito.when( personManager.getPerson(req).getEntityIdentifier()).thenReturn(null);
        importExportController.importEntity(firstFile,req,res);
        Assert.assertEquals(401,res.getStatus());
    }

    @Test
    public void testExportEntity401() throws IOException, JSONException {

        importExportController.exportEntity(ENTITY_ID,ENTITY_TYPE,true,"XML", req, res);
        Assert.assertEquals(401,res.getStatus());
    }

    @Test
    public void testExportEntityBadRequest() throws IOException, JSONException {
        //EntityIdentifier id = new EntityIdentifier(ENTITY_ID);
        Mockito.when( personManager.getPerson(req).getEntityIdentifier()).thenReturn(null);
        importExportController.exportEntity(ENTITY_ID,ENTITY_TYPE,true,"XML", req, res);
        Assert.assertEquals(401,res.getStatus());
    }

}
