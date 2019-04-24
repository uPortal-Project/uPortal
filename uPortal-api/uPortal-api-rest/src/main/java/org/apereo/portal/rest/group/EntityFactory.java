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
package org.apereo.portal.rest.group;

import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.portlets.groupselector.EntityEnum;
import org.apereo.portal.security.IAuthorizationPrincipal;

/* package-private */ class EntityFactory {
    private EntityFactory() {}

    /* package-private */ static Entity createEntity(IGroupMember member, EntityEnum entityType) {
        if (member == null) {
            return null;
        }
        Entity entity = new EntityImpl();
        entity.setEntityType(entityType.toString());
        entity.setId(member.getKey());
        setPrincipal(entity);
        return entity;
    }

    /* package-private */ static Entity createEntity(IEntityGroup group, EntityEnum entityType) {
        if (group == null) {
            return null;
        }
        Entity entity = new EntityImpl();
        entity.setEntityType(entityType.toString());
        entity.setId(group.getKey());
        entity.setName(group.getName());
        entity.setCreatorId(group.getCreatorID());
        entity.setDescription(group.getDescription());
        setPrincipal(entity);
        return entity;
    }

    private static void setPrincipal(Entity entity) {
        IAuthorizationPrincipal authP = EntityService.instance().getPrincipalForEntity(entity);
        Principal principal = new PrincipalImpl(authP.getKey(), authP.getPrincipalString());
        entity.setPrincipal(principal);
    }
}
