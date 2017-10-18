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
package org.apereo.portal.portlets.layout.dlm.remoting.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apereo.portal.layout.dlm.remoting.registry.ChannelCategoryBean;
import org.apereo.portal.portlet.om.PortletCategory;
import org.junit.Test;

public class ChannelCategoryBeanTest {

    private ChannelCategoryBean buildChannelCategoryBean(String id, String name) {
        ChannelCategoryBean ccb = new ChannelCategoryBean();
        ccb.setId(id);
        ccb.setName(name);
        return ccb;
    }

    @Test
    public void testConstructorViaPortletCategory() {
        String id = "pcID";
        String name = "pcName";
        String desc = "pcDesc";
        PortletCategory pCategory = new PortletCategory(id);
        pCategory.setName(name);
        pCategory.setDescription(desc);
        ChannelCategoryBean ccb = new ChannelCategoryBean(pCategory);

        assertEquals(id, ccb.getId());
        assertEquals(name, ccb.getName());
        assertEquals(desc, ccb.getDescription());
    }

    @Test
    public void testHashCode() {
        ChannelCategoryBean ccb1 = buildChannelCategoryBean("id_test", "name_test");
        ChannelCategoryBean ccb2 = buildChannelCategoryBean("id_test", "name_test");

        assertEquals(ccb1.hashCode(), ccb2.hashCode());
    }

    @Test
    public void testCompareToDifferent() {
        String id1 = "id1";
        String id2 = "id2";
        ChannelCategoryBean ccb1 = buildChannelCategoryBean(id1, "name_test");
        ChannelCategoryBean ccb2 = buildChannelCategoryBean(id2, "name_test");

        assertEquals(id1.compareTo(id2), ccb1.compareTo(ccb2));
    }

    @Test
    public void testCompareToSimilar() {
        String id1 = "id1";
        String id2 = "id1";
        ChannelCategoryBean ccb1 = buildChannelCategoryBean(id1, "name_test");
        ChannelCategoryBean ccb2 = buildChannelCategoryBean(id2, "name_test");

        assertEquals(id1.compareTo(id2), ccb1.compareTo(ccb2));
    }

    @Test
    public void testEqualsSameID() {
        ChannelCategoryBean ccb1 = buildChannelCategoryBean("id1", "name_test");
        ChannelCategoryBean ccb2 = buildChannelCategoryBean("id1", "name_test");

        assertTrue(ccb1.equals(ccb2));
    }

    @Test
    public void testEqualsDifferentID() {
        ChannelCategoryBean ccb1 = buildChannelCategoryBean("id1", "name_test");
        ChannelCategoryBean ccb2 = buildChannelCategoryBean("id2", "name_test");

        assertFalse(ccb1.equals(ccb2));
    }

    @Test
    public void testEqualsSelf() {
        ChannelCategoryBean ccb1 = buildChannelCategoryBean("id1", "name_test");
        assertTrue(ccb1.equals(ccb1));
    }

    @Test
    public void testEqualsOtherObject() {
        ChannelCategoryBean ccb1 = buildChannelCategoryBean("id1", "name_test");
        assertFalse(ccb1.equals("id1"));
    }

    @Test
    public void testEqualsNull() {
        ChannelCategoryBean ccb1 = buildChannelCategoryBean("id1", "name_test");
        assertFalse(ccb1.equals(null));
    }
}
