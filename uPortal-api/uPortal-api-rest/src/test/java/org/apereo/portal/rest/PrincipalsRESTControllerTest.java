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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apereo.portal.layout.dlm.remoting.IGroupListHelper;
import org.apereo.portal.layout.dlm.remoting.JsonEntityBean;
import org.apereo.portal.portlets.groupselector.EntityEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;

public class PrincipalsRESTControllerTest {

    @InjectMocks private PrincipalsRESTController principalsRESTController;

    @Mock private HttpServletRequest req;

    @Mock private HttpServletResponse res;

    @Mock private IGroupListHelper groupListHelper;

    @Before
    public void setup() throws Exception {
        principalsRESTController = new PrincipalsRESTController();
        MockitoAnnotations.initMocks(this);
    }

    private JsonEntityBean buildJsonGroupEntityBean() {
        JsonEntityBean groupBean = new JsonEntityBean();
        groupBean.setEntityType(EntityEnum.GROUP.toString());
        groupBean.setId("group101");
        groupBean.setName("test");
        groupBean.setDescription("Testing group bean");

        return groupBean;
    }

    private JsonEntityBean buildJsonPersonEntityBean() {
        JsonEntityBean personBean = new JsonEntityBean();
        personBean.setEntityType(EntityEnum.GROUP.toString());
        personBean.setId("person101");
        personBean.setName("test");
        personBean.setDescription("Testing person bean");

        return personBean;
    }

    @Test
    public void testGetPrincipals() throws Exception {
        String query = "test";
        Mockito.when(groupListHelper.search(EntityEnum.GROUP.toString(), "test"))
                .thenReturn(Collections.emptySet());
        Mockito.when(groupListHelper.search(EntityEnum.PERSON.toString(), "test"))
                .thenReturn(Collections.emptySet());

        ModelAndView modelAndView = principalsRESTController.getPrincipals(query, req, res);
        List<JsonEntityBean> returnPersonEntities =
                (List<JsonEntityBean>) modelAndView.getModel().get("people");
        List<JsonEntityBean> returnGroupEntities =
                (List<JsonEntityBean>) modelAndView.getModel().get("groups");

        Assert.assertTrue(returnPersonEntities.isEmpty());
        Assert.assertTrue(returnGroupEntities.isEmpty());
    }

    @Test
    public void testGetPrincipalsPerson() throws Exception {
        String query = "test";
        Set<JsonEntityBean> beans = new HashSet<JsonEntityBean>();
        beans.add(buildJsonPersonEntityBean());

        Mockito.when(groupListHelper.search(EntityEnum.GROUP.toString(), "test"))
                .thenReturn(Collections.emptySet());
        Mockito.when(groupListHelper.search(EntityEnum.PERSON.toString(), "test"))
                .thenReturn(beans);

        ModelAndView modelAndView = principalsRESTController.getPrincipals(query, req, res);
        List<JsonEntityBean> returnPersonEntities =
                (List<JsonEntityBean>) modelAndView.getModel().get("people");
        List<JsonEntityBean> returnGroupEntities =
                (List<JsonEntityBean>) modelAndView.getModel().get("groups");

        Assert.assertFalse(returnPersonEntities.isEmpty());
        Assert.assertTrue(returnGroupEntities.isEmpty());
    }

    @Test
    public void testGetPrincipalsGroup() throws Exception {
        String query = "test";
        Set<JsonEntityBean> beans = new HashSet<JsonEntityBean>();
        beans.add(buildJsonGroupEntityBean());

        Mockito.when(groupListHelper.search(EntityEnum.GROUP.toString(), "test")).thenReturn(beans);
        Mockito.when(groupListHelper.search(EntityEnum.PERSON.toString(), "test"))
                .thenReturn(Collections.emptySet());

        ModelAndView modelAndView = principalsRESTController.getPrincipals(query, req, res);
        List<JsonEntityBean> returnPersonEntities =
                (List<JsonEntityBean>) modelAndView.getModel().get("people");
        List<JsonEntityBean> returnGroupEntities =
                (List<JsonEntityBean>) modelAndView.getModel().get("groups");

        Assert.assertTrue(returnPersonEntities.isEmpty());
        Assert.assertFalse(returnGroupEntities.isEmpty());
    }
}
