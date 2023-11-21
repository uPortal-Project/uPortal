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

import org.apereo.portal.layout.dlm.remoting.JsonEntityBean;
import org.apereo.portal.portlets.groupselector.EntityEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AssignmentTest {

    private Assignment assignment;

    private JsonEntityBean buildJsonGroupEntityBean() {
        JsonEntityBean groupBean = new JsonEntityBean();
        groupBean.setEntityType(EntityEnum.GROUP.toString());
        groupBean.setId("group101");
        groupBean.setName("test");
        groupBean.setDescription("Testing group bean");

        return groupBean;
    }

    @Before
    public void setup() {
        String principal = "principal";
        JsonEntityBean principalBean = buildJsonGroupEntityBean();
        assignment = new Assignment(principal, principalBean);
    }

    @Test
    public void testAddChild() {
        String principal = "principal1";
        JsonEntityBean principalBean = buildJsonGroupEntityBean();
        Assignment newAssignment = assignment.addChild(new Assignment(principal, principalBean));
        Assert.assertEquals(1L, newAssignment.getChildren().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddChildNull() {
        Assignment newAssignment = assignment.addChild(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindDescendantOrSelfIfExistsForNull() {
        Assignment assignment = this.assignment.findDescendantOrSelfIfExists(null);
    }

    @Test
    public void testFindDescendantOrSelfIfExists() {
        JsonEntityBean bean = buildJsonGroupEntityBean();
        Assignment newAssignment = this.assignment.findDescendantOrSelfIfExists(bean);
        Assert.assertNotNull(newAssignment);
    }
}
