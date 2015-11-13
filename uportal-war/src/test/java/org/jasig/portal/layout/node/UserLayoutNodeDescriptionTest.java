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

import org.jgroups.util.Util;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests for UserLayoutNodeDescription.
 * Currently tests copy constructor to document with test the uPortal 4.1 change to copy DLM attributes in copy
 * constructor.
 */
public class UserLayoutNodeDescriptionTest {

    /**
     *  Test that the copy constructor copies over the attributes of the source node description.
     */
    @Test
    public void copyConstructorCopiesAttributes() {

        UserLayoutNodeDescription copyFrom = new UserLayoutNodeDescription() {
            @Override
            public LayoutNodeType getType() {
                return null;
            }

            @Override
            public Element getXML(Document root) {
                return null;
            }
        };



        // set up copyFrom

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

        // note that this invokes copy constructor
        UserLayoutNodeDescription copyTo = new UserLayoutNodeDescription(copyFrom) {
            @Override
            public LayoutNodeType getType() {
                return null;
            }

            @Override
            public Element getXML(Document root) {
                return null;
            }
        };

        // assert that copyTo now has the characteristics of copyFrom
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


        // now set up copyFrom with different values to ensure tests didn't succeed because of defaulting
        copyFrom.setAddChildAllowed(false);
        copyFrom.setDeleteAllowed(false);
        copyFrom.setEditAllowed(false);
        copyFrom.setHidden(false);
        copyFrom.setId("bucky");
        copyFrom.setImmutable(false);
        copyFrom.setMoveAllowed(false);
        copyFrom.setName("badger");
        copyFrom.setPrecedence(41.6);
        copyFrom.setUnremovable(false);

        // again invoke copy constructor
        copyTo = new UserLayoutNodeDescription(copyFrom) {
            @Override
            public LayoutNodeType getType() {
                return null;
            }

            @Override
            public Element getXML(Document root) {
                return null;
            }
        };

        // assert that copyTo again has the characteristics of copyFrom
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

    }

}
