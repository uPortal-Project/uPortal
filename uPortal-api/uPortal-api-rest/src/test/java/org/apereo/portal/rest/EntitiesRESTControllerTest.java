/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apereo.portal.layout.dlm.remoting.IGroupListHelper;
import org.apereo.portal.layout.dlm.remoting.JsonEntityBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class EntitiesRESTControllerTest {
    public static final String ENTITY_TYPE = "entityType";
    public static final String ENTITY_ID_101 = "entityId-101";

    @InjectMocks private EntitiesRESTController entitiesRESTController;

    @Mock private IGroupListHelper groupListHelper;

    @Mock private HttpServletRequest req;

    @Mock private HttpServletResponse res;

    @Before
    public void setup() throws Exception {
        entitiesRESTController = new EntitiesRESTController();
        MockitoAnnotations.initMocks(this);
    }

    private JsonEntityBean buildJsonEntityBean() {
        JsonEntityBean bean = new JsonEntityBean();
        bean.setEntityType(ENTITY_TYPE);
        bean.setId(ENTITY_ID_101);
        bean.setName("test");
        bean.setDescription("Testing bean");

        return bean;
    }

    @Test
    public void testFindEntity() {
        Mockito.when(groupListHelper.getEntity(ENTITY_TYPE, ENTITY_ID_101, true))
                .thenReturn(new JsonEntityBean());
        JsonEntityBean entityBean =
                entitiesRESTController.findEntity(req, res, ENTITY_TYPE, ENTITY_ID_101);
        Assert.assertNotNull(entityBean);
    }

    @Test
    public void testFindEntityNull() {
        Mockito.when(groupListHelper.getEntity(null, null, true)).thenReturn(null);
        JsonEntityBean entityBean = entitiesRESTController.findEntity(req, res, null, null);
        Assert.assertNull(entityBean);
    }

    @Test
    public void testFindEntityEmpty() {
        Mockito.when(groupListHelper.getEntity("", "", true)).thenReturn(null);
        JsonEntityBean entityBean = entitiesRESTController.findEntity(req, res, "", "");
        Assert.assertNull(entityBean);
    }

    @Test
    public void testDoSearchNotFound() {
        Mockito.when(groupListHelper.search(ENTITY_TYPE, "test"))
                .thenReturn(Collections.emptySet());
        List<String> entitytypes = new ArrayList<String>();
        entitytypes.add(ENTITY_TYPE);
        Set<JsonEntityBean> beans = entitiesRESTController.doSearch(req, res, "test", entitytypes);
        Assert.assertTrue(beans.isEmpty());
    }

    @Test
    public void testDoSearchFound() {
        Set<JsonEntityBean> returnBeans = new HashSet<JsonEntityBean>();
        returnBeans.add(buildJsonEntityBean());
        Mockito.when(groupListHelper.search(ENTITY_TYPE, "test")).thenReturn(returnBeans);
        List<String> entitytypes = new ArrayList<String>();
        entitytypes.add(ENTITY_TYPE);
        Set<JsonEntityBean> beans = entitiesRESTController.doSearch(req, res, "test", entitytypes);
        Assert.assertEquals(1L, beans.size());
    }
}
