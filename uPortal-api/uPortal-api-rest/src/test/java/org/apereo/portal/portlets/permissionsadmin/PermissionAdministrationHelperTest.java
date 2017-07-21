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
package org.apereo.portal.portlets.permissionsadmin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apereo.portal.layout.dlm.remoting.IGroupListHelper;
import org.apereo.portal.layout.dlm.remoting.JsonEntityBean;
import org.apereo.portal.portlets.groupselector.EntityEnum;
import org.apereo.portal.security.IPermissionStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PermissionAdministrationHelperTest {
    @InjectMocks PermissionAdministrationHelper permissionAdministrationHelper;
    @Mock private IGroupListHelper groupListHelper;
    @Mock private IPermissionStore permissionStore;

    @Before
    public void setup() {
        permissionAdministrationHelper = new PermissionAdministrationHelper();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetEntitiesForPrincipals() {
        String[] principals = new String[] {"principal1", "principal2", "principal3"};
        Set<JsonEntityBean> beans =
                permissionAdministrationHelper.getEntitiesForPrincipals(Arrays.asList(principals));

        Assert.assertEquals(1L, beans.size());
    }

    @Test
    public void testGetPrincipalsForEntitiesEmpty() {
        Set<JsonEntityBean> beans =
                permissionAdministrationHelper.getEntitiesForPrincipals(Collections.emptyList());
        Assert.assertTrue(beans.isEmpty());
    }

    private JsonEntityBean buildJsonGroupEntityBean() {
        JsonEntityBean groupBean = new JsonEntityBean();
        groupBean.setEntityType(EntityEnum.GROUP.toString());
        groupBean.setId("group101");
        groupBean.setName("test");
        groupBean.setDescription("Testing group bean");

        return groupBean;
    }

    @Test(expected = NullPointerException.class)
    public void testGetPrincipalsForEntities() {
        JsonEntityBean bean = buildJsonGroupEntityBean();
        List<JsonEntityBean> beans = new ArrayList<JsonEntityBean>();
        beans.add(bean);
        Set<JsonEntityBean> returnBeans =
                permissionAdministrationHelper.getEntitiesForPrincipals(null);
    }

    @Test(expected = NullPointerException.class)
    public void testGetCurrentPrincipals() {
        Set<String> strs = permissionAdministrationHelper.getCurrentPrincipals(null, null, null);
    }
}
