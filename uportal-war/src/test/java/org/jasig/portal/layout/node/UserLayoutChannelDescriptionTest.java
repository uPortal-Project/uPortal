/**
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
package org.jasig.portal.layout.node;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for UserLayoutChannelDescription.
 */
public class UserLayoutChannelDescriptionTest {

    /**
     * Test that the copy constructor copies attributes.
     */
    @Test
    public void copyConstructorCopiesAttributes() {

        UserLayoutChannelDescription copyFrom = new UserLayoutChannelDescription();

        // set up copyFrom : Node properties
        copyFrom.setAddChildAllowed(true);
        copyFrom.setDeleteAllowed(true);
        copyFrom.setEditAllowed(true);
        copyFrom.setHidden(true);
        copyFrom.setId("identifier");
        copyFrom.setImmutable(true);
        copyFrom.setMoveAllowed(true);
        copyFrom.setName("xkcd");
        copyFrom.setPrecedence(80.0);
        copyFrom.setUnremovable(true);

        // set up copyFrom : Channel-specific properties
        copyFrom.setChannelPublishId("penguin");
        copyFrom.setChannelSubscribeId("rodale");
        copyFrom.setChannelTypeId("mono");
        copyFrom.setClassName("org.apereo.Awesomeness");
        copyFrom.setDescription("Nasty, brutish, and short.");
        copyFrom.setEditable(true);
        copyFrom.setFunctionalName("awesome");

        // invoke copy constructor
        UserLayoutChannelDescription copyTo = new UserLayoutChannelDescription(copyFrom);

        // verify Node properties carried over
        assertTrue(copyTo.isAddChildAllowed());
        assertTrue(copyTo.isDeleteAllowed());
        assertTrue(copyTo.isEditAllowed());
        assertTrue(copyTo.isHidden());
        assertEquals(copyFrom.getId(), copyTo.getId());
        assertTrue(copyTo.isImmutable());
        assertTrue(copyTo.isMoveAllowed());
        assertEquals(copyFrom.getName(), copyTo.getName());
        assertEquals(copyFrom.getPrecedence(), copyTo.getPrecedence(), 0.00001d);
        assertTrue(copyFrom.isUnremovable());

        // verify Channel properties carried over
        assertEquals(copyFrom.getChannelPublishId(), copyTo.getChannelPublishId());
        assertEquals(copyFrom.getChannelSubscribeId(), copyTo.getChannelSubscribeId());
        assertEquals(copyFrom.getChannelTypeId(), copyTo.getChannelTypeId());
        assertEquals(copyFrom.getClassName(), copyTo.getClassName());
        assertEquals(copyFrom.getDescription(), copyTo.getDescription());
        assertTrue(copyTo.isEditable());
        assertEquals(copyFrom.getFunctionalName(), copyTo.getFunctionalName());

        // now set up copyFrom with different values to ensure tests didn't succeed because of defaulting
        // set up copyFrom : Node properties
        copyFrom.setAddChildAllowed(false);
        copyFrom.setDeleteAllowed(false);
        copyFrom.setEditAllowed(false);
        copyFrom.setHidden(false);
        copyFrom.setId("yo");
        copyFrom.setImmutable(false);
        copyFrom.setMoveAllowed(false);
        copyFrom.setName("oatmeal");
        copyFrom.setPrecedence(90.0);
        copyFrom.setUnremovable(false);

        // set up copyFrom : Channel-specific properties
        copyFrom.setChannelPublishId("manning");
        copyFrom.setChannelSubscribeId("csa");
        copyFrom.setChannelTypeId("type1");
        copyFrom.setClassName("org.apereo.Community");
        copyFrom.setDescription("Verbose.");
        copyFrom.setEditable(false);
        copyFrom.setFunctionalName("dys");

        // invoke copy constructor again
        copyTo = new UserLayoutChannelDescription(copyFrom);

        // verify Node properties carried over
        assertFalse(copyTo.isAddChildAllowed());
        assertFalse(copyTo.isDeleteAllowed());
        assertFalse(copyTo.isEditAllowed());
        assertFalse(copyTo.isHidden());
        assertEquals(copyFrom.getId(), copyTo.getId());
        assertFalse(copyTo.isImmutable());
        assertFalse(copyTo.isMoveAllowed());
        assertEquals(copyFrom.getName(), copyTo.getName());
        assertEquals(copyFrom.getPrecedence(), copyTo.getPrecedence(), 0.00001d);
        assertFalse(copyFrom.isUnremovable());

        // verify Channel properties carried over
        assertEquals(copyFrom.getChannelPublishId(), copyTo.getChannelPublishId());
        assertEquals(copyFrom.getChannelSubscribeId(), copyTo.getChannelSubscribeId());
        assertEquals(copyFrom.getChannelTypeId(), copyTo.getChannelTypeId());
        assertEquals(copyFrom.getClassName(), copyTo.getClassName());
        assertEquals(copyFrom.getDescription(), copyTo.getDescription());
        assertFalse(copyTo.isEditable());
        assertEquals(copyFrom.getFunctionalName(), copyTo.getFunctionalName());

    }

}
