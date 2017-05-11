/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apereo.portal.layout.dlm;

import org.apereo.portal.IUserProfile;
import org.apereo.portal.UserProfile;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.provider.PersonImpl;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DLM.
 */
public class DistributedLayoutManagerTest {


    /**
     * One cannot add a null node to a layout.
     */
    @Test
    public void cannotAddNullNode() {

        final IPerson person = new PersonImpl();
        final IUserProfile profile = new UserProfile();

        final DistributedLayoutManager dlm = new DistributedLayoutManager(person, profile);

        final IUserLayoutNodeDescription nullNode = null;
        final IUserLayoutNodeDescription parent =  mock(IUserLayoutNodeDescription.class);
        final String noNextSiblingId = "";

        assertFalse(dlm.canAddNode(nullNode, parent, noNextSiblingId));
    }

    /**
     * Adding a node to a null parent makes no sense and is disallowed.
     */
    @Test
    public void cannotAddNodeToNullParent() {

        final IPerson person = new PersonImpl();
        final IUserProfile profile = new UserProfile();

        final DistributedLayoutManager dlm = new DistributedLayoutManager(person, profile);

        final IUserLayoutNodeDescription nodeToAdd = mock(IUserLayoutNodeDescription.class);
        final IUserLayoutNodeDescription nullParent = null;
        final String noNextSiblingId = "";

        assertFalse(dlm.canAddNode(nodeToAdd, nullParent, noNextSiblingId));
    }

    /**
     * If you can't move a node, you can't add that node.
     */
    @Test
    public void cannotAddAnImmovableNode() {

        final IPerson person = new PersonImpl();
        final IUserProfile profile = new UserProfile();

        final DistributedLayoutManager dlm = new DistributedLayoutManager(person, profile);

        final IUserLayoutNodeDescription nodeToAdd = mock(IUserLayoutNodeDescription.class);

        final IUserLayoutNodeDescription parent = mock(IUserLayoutNodeDescription.class);
        when(parent.isMoveAllowed()).thenReturn(Boolean.FALSE);

        final String noNextSiblingId = "";

        assertFalse(dlm.canAddNode(nodeToAdd, parent, noNextSiblingId));

    }

}
