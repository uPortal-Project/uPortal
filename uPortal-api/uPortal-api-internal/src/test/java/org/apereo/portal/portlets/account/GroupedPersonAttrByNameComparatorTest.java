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
package org.apereo.portal.portlets.account;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class GroupedPersonAttrByNameComparatorTest {

    private static final String TEST_DISPLAY_NAME_0 = "john_doe";

    private static final String TEST_DISPLAY_NAME_1 = "jane_doe";

    private List<GroupedPersonAttribute> groupedPersonAttributeList;

    @Before
    public void setup() throws Exception {
        groupedPersonAttributeList = new ArrayList<>();
        GroupedPersonAttribute attr0 = new GroupedPersonAttribute(TEST_DISPLAY_NAME_0, null);
        GroupedPersonAttribute attr1 = new GroupedPersonAttribute(TEST_DISPLAY_NAME_1, null);
        groupedPersonAttributeList.add(attr0);
        groupedPersonAttributeList.add(attr1);
    }

    @Test
    public void testGroupedPersonAttributeByNameComparator() {
        groupedPersonAttributeList.sort(new GroupedPersonAttributeByNameComparator());
        GroupedPersonAttribute attr0 = groupedPersonAttributeList.get(0);
        assertEquals(attr0.getDisplayName(), TEST_DISPLAY_NAME_1);
        GroupedPersonAttribute attr1 = groupedPersonAttributeList.get(1);
        assertEquals(attr1.getDisplayName(), TEST_DISPLAY_NAME_0);
    }
}
