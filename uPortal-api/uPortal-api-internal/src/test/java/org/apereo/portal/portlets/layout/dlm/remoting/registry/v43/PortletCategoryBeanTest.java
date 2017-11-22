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
package org.apereo.portal.portlets.layout.dlm.remoting.registry.v43;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import org.apereo.portal.layout.dlm.remoting.registry.v43.PortletCategoryBean;
import org.apereo.portal.portlet.om.PortletCategory;
import org.junit.Test;

public class PortletCategoryBeanTest {

    @Test
    public void testfromPortletCategoryNullSubCatsAndPortlets() {
        String id = "id_test";
        String name = "id_test";
        String desc = "desc_test";
        PortletCategory pc = new PortletCategory(id);
        pc.setName(name);
        pc.setDescription(desc);
        PortletCategoryBean pcb = PortletCategoryBean.fromPortletCategory(pc, null, null);
        assertEquals(id, pcb.getId());
        assertEquals(name, pcb.getName());
        assertEquals(desc, pcb.getDescription());
        assertEquals(Collections.emptySet(), pcb.getPortlets());
        assertEquals(Collections.emptySet(), pcb.getSubcategories());
    }

    @Test
    public void testFromPortletCategoryInflatedSubCatsAndPortlets() {
        String id = "id_test";
        String name = "name_test";
        String desc = "desc_test";
        String cat1id = "cat1_id_test";
        String cat1name = "cat1_name_test";
        String cat1desc = "cat1_desc_test";
        PortletCategoryBean pcb =
                buildTestPortletCategoryBean(id, name, desc, cat1id, cat1name, cat1desc);
        assertEquals(id, pcb.getId());
        assertEquals(name, pcb.getName());
        assertEquals(desc, pcb.getDescription());

        // Check the portlets set
        assertEquals(Collections.emptySet(), pcb.getPortlets());

        // Check the subCat set
        assertEquals(1, pcb.getSubcategories().size());
        assertEquals(cat1id, pcb.getSubcategories().first().getId());
        assertEquals(cat1name, pcb.getSubcategories().first().getName());
        assertEquals(cat1desc, pcb.getSubcategories().first().getDescription());
    }

    @Test
    public void testHashCode() {
        String id = "id_test";
        String name = "name_test";
        String desc = "desc_test";
        PortletCategoryBean pcb1 = buildTestPortletCategoryBean(id, name, desc);
        PortletCategoryBean pcb2 = buildTestPortletCategoryBean(id, name, desc);
        assertEquals(pcb1.hashCode(), pcb2.hashCode());
    }

    @Test
    public void testCompareToDifferent() {
        String name1 = "name1";
        String name2 = "name2";
        PortletCategoryBean pcb1 = buildTestPortletCategoryBean("id1", name1, "desc");
        PortletCategoryBean pcb2 = buildTestPortletCategoryBean("id1", name2, "desc");
        assertEquals(name1.compareTo(name2), pcb1.compareTo(pcb2));
    }

    @Test
    public void testCompareToSimilar() {
        String name1 = "name1";
        String name2 = "name1";
        PortletCategoryBean pcb1 = buildTestPortletCategoryBean("id1", name1, "desc");
        PortletCategoryBean pcb2 = buildTestPortletCategoryBean("id1", name2, "desc");
        assertEquals(name1.compareTo(name2), pcb1.compareTo(pcb2));
    }

    @Test
    public void testEqualsSameID() {
        String id1 = "id1";
        PortletCategoryBean pcb1 = buildTestPortletCategoryBean(id1, "name1", "desc");
        PortletCategoryBean pcb2 = buildTestPortletCategoryBean(id1, "name2", "desc");
        assertTrue(pcb1.equals(pcb2));
    }

    @Test
    public void testEqualsDifferentID() {
        PortletCategoryBean pcb1 = buildTestPortletCategoryBean("id1", "name1", "desc");
        PortletCategoryBean pcb2 = buildTestPortletCategoryBean("id2", "name2", "desc");
        assertFalse(pcb1.equals(pcb2));
    }

    @Test
    public void testEqualsSelf() {
        PortletCategoryBean pcb1 = buildTestPortletCategoryBean("id1", "name1", "desc");
        assertTrue(pcb1.equals(pcb1));
    }

    @Test
    public void testEqualsOtherObject() {
        PortletCategoryBean pcb1 = buildTestPortletCategoryBean("id1", "name1", "desc");
        assertFalse(pcb1.equals("id1"));
    }

    @Test
    public void testEqualsNull() {
        PortletCategoryBean pcb1 = buildTestPortletCategoryBean("id1", "name1", "desc");
        assertFalse(pcb1.equals(null));
    }

    private PortletCategoryBean buildTestPortletCategoryBean(
            String id, String name, String desc, String cat1id, String cat1name, String cat1desc) {
        PortletCategory pc = new PortletCategory(id);
        pc.setName(name);
        pc.setDescription(desc);
        return PortletCategoryBean.fromPortletCategory(
                pc, buildTestCategorySet(cat1id, cat1name, cat1desc), Collections.emptySet());
    }

    private PortletCategoryBean buildTestPortletCategoryBean(String id, String name, String desc) {
        PortletCategory pc = new PortletCategory(id);
        pc.setName(name);
        pc.setDescription(desc);
        return PortletCategoryBean.fromPortletCategory(pc, null, null);
    }

    private Set<PortletCategoryBean> buildTestCategorySet(String id, String name, String desc) {
        PortletCategory pc = new PortletCategory(id);
        pc.setName(name);
        pc.setDescription(desc);
        PortletCategoryBean pcb = PortletCategoryBean.fromPortletCategory(pc, null, null);
        Set<PortletCategoryBean> testCategorySet = new TreeSet<>();
        testCategorySet.add(pcb);
        return testCategorySet;
    }
}
