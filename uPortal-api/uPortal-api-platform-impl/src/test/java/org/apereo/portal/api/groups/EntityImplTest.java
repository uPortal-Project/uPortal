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
package org.apereo.portal.api.groups;

import java.util.ArrayList;
import java.util.List;
import org.apereo.portal.api.PrincipalImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EntityImplTest {

    EntityImpl entity;

    @Before
    public void setup() {
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new EntityImpl());
        entity = new EntityImpl();
        entity.setCreatorId("creatorID");
        entity.setDescription("description");
        entity.setEntityType(Entity.ENTITY_PERSON);
        entity.setId("id");
        entity.setName("entity");
        entity.setPrincipal(new PrincipalImpl("john", "doe"));
        entity.setChildrenInitialized(true);
        entity.setChildren(entities);
    }

    @Test
    public void testGetEntityType() {
        Assert.assertEquals(Entity.ENTITY_PERSON, entity.getEntityType());
    }

    @Test
    public void testGetId() {
        Assert.assertEquals("id", entity.getId());
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("entity", entity.getName());
    }

    @Test
    public void testGetCreatorId() {
        Assert.assertEquals("creatorID", entity.getCreatorId());
    }

    @Test
    public void testGetDescription() {
        Assert.assertEquals("description", entity.getDescription());
    }

    @Test
    public void testGetPrincipal() {
        Assert.assertNotNull(entity.getPrincipal());
    }

    @Test
    public void testGetChildren() {
        Assert.assertFalse(entity.getChildren().isEmpty());
    }

    @Test
    public void testIsChildrenInitialized() {
        Assert.assertTrue(entity.isChildrenInitialized());
    }

    @Test
    public void testAddChild() {
        entity.addChild(new EntityImpl());
        Assert.assertEquals(2, entity.getChildren().size());
    }
}
