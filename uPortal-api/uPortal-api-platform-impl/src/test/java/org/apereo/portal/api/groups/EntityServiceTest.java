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

import java.util.Set;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EntityServiceTest {
    EntityService entityService;

    @Before
    public void setup() {
        entityService = EntityService.instance();
    }

    @Test
    public void testLookupEntityName() {
        String returnString = entityService.lookupEntityName(null);
        Assert.assertNull(returnString);
    }

    @Test
    public void testLookupEntityNameByEntityIdNull() {
        String returnString = entityService.lookupEntityName(null, null);
        Assert.assertNull(returnString);
    }

    @Test
    public void testGetPrincipalForEntity() {
        IAuthorizationPrincipal returnString = entityService.getPrincipalForEntity(null);
        Assert.assertNull(returnString);
    }

    @Test
    public void testGetEntityNull() {
        Assert.assertNull(entityService.getEntity(null));
    }

    @Test
    public void testGetEntity() {
        Entity entity = entityService.getEntity(null, null, true);
        Assert.assertNull(entity);
    }

    @Test
    public void testSearch() {
        Set<Entity> entities = entityService.search(null, null);
        Assert.assertNull(entities);
    }
}
