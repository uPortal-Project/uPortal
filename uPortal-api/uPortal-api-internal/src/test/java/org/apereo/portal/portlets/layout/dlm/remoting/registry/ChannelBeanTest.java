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

import org.apereo.portal.layout.dlm.remoting.registry.ChannelBean;
import org.junit.Test;

public class ChannelBeanTest {

    private ChannelBean buildChannelBean(String id, String name) {
        ChannelBean cb = new ChannelBean();
        cb.setId(id);
        cb.setName(name);
        return cb;
    }

    @Test
    public void testHashCode() {
        ChannelBean cb1 = buildChannelBean("id_test", "name_test");
        ChannelBean cb2 = buildChannelBean("id_test", "name_test");

        assertEquals(cb1.hashCode(), cb2.hashCode());
    }

    @Test
    public void testCompareToDifferent() {
        String id1 = "id1";
        String id2 = "id2";
        ChannelBean cb1 = buildChannelBean(id1, "name_test");
        ChannelBean cb2 = buildChannelBean(id2, "name_test");

        assertEquals(id1.compareTo(id2), cb1.compareTo(cb2));
    }

    @Test
    public void testCompareToSimilar() {
        String id1 = "id1";
        String id2 = "id1";
        ChannelBean cb1 = buildChannelBean(id1, "name_test");
        ChannelBean cb2 = buildChannelBean(id2, "name_test");

        assertEquals(id1.compareTo(id2), cb1.compareTo(cb2));
    }

    @Test
    public void testEqualsSameID() {
        ChannelBean cb1 = buildChannelBean("id1", "name_test");
        ChannelBean cb2 = buildChannelBean("id1", "name_test");

        assertTrue(cb1.equals(cb2));
    }

    @Test
    public void testEqualsDifferentID() {
        ChannelBean cb1 = buildChannelBean("id1", "name_test");
        ChannelBean cb2 = buildChannelBean("id2", "name_test");

        assertFalse(cb1.equals(cb2));
    }

    @Test
    public void testEqualsSelf() {
        ChannelBean cb1 = buildChannelBean("id1", "name_test");
        assertTrue(cb1.equals(cb1));
    }

    @Test
    public void testEqualsOtherObject() {
        ChannelBean cb1 = buildChannelBean("id1", "name_test");
        assertFalse(cb1.equals("id1"));
    }

    @Test
    public void testEqualsNull() {
        ChannelBean cb1 = buildChannelBean("id1", "name_test");
        assertFalse(cb1.equals(null));
    }

    @Test
    public void testToStringAllNulls() {
        ChannelBean cb = new ChannelBean();
        assertEquals(
                "ID: null title: null Description: null fname: null name: null state: null typeID: 0 iconUrl: null",
                cb.toString());
    }

    @Test
    public void testToStringFilled() {
        ChannelBean cb = new ChannelBean();
        cb.setId("myId");
        cb.setTitle("myTitle");
        cb.setFname("myFname");
        cb.setName("myName");
        cb.setState("myState");
        cb.setTypeId(324);
        cb.setIconUrl("myIconUrl");
        assertEquals(
                "ID: myId title: myTitle Description: null fname: myFname name: myName state: myState typeID: 324 iconUrl: myIconUrl",
                cb.toString());
    }
}
