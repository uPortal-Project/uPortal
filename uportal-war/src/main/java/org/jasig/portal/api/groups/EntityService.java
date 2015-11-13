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
package org.jasig.portal.api.groups;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.api.Principal;
import org.jasig.portal.api.PrincipalImpl;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityNameFinder;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.EntityNameFinderService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;

public final class EntityService {
    private static final Log log = LogFactory.getLog(EntityService.class);

    private static final SingletonDoubleCheckedCreator<EntityService> instance = new SingletonDoubleCheckedCreator<EntityService>() {
        @Override
        protected EntityService createSingleton(Object... args) {
            return new EntityService();
        }
    };

    public static EntityService instance() {
        return instance.get();
    }

    public Set<Entity> search(String entityType, String searchTerm) {

        Set<Entity> results = new HashSet<Entity>();
        EntityEnum entityEnum = EntityEnum.getEntityEnum(entityType);
        EntityIdentifier[] identifiers;
        Class identifierType;

        // if the entity type is a group, use the group service's findGroup method
        // to locate it
        if (entityEnum.isGroup()) {
            identifiers = GroupService.searchForGroups(searchTerm, GroupService.CONTAINS,entityEnum.getClazz());
            identifierType = IEntityGroup.class;
        }
        // otherwise use the getGroupMember method
        else {
            identifiers = GroupService.searchForEntities(searchTerm, GroupService.CONTAINS,entityEnum.getClazz());
            identifierType = entityEnum.getClazz();
        }

        for(EntityIdentifier entityIdentifier : identifiers) {
            if(entityIdentifier.getType().equals(identifierType)) {
                IGroupMember groupMember = GroupService.getGroupMember(entityIdentifier);
                Entity entity = getEntity(groupMember);
                results.add(entity);
            }
        }

        return results;
    }

    public Entity getEntity(String entityType, String entityId, boolean populateChildren) {

        // get the EntityEnum for the specified entity type
        EntityEnum entityEnum = EntityEnum.getEntityEnum(entityType);

        // if the entity type is a group, use the group service's findGroup method
        // to locate it
        if(entityEnum.isGroup()) {
            // attempt to find the entity
            IEntityGroup entityGroup = GroupService.findGroup(entityId);
            if(entityGroup == null) {
                return null;
            } else {
                Entity entity = EntityFactory.createEntity(entityGroup,entityEnum);
                if (populateChildren) {
                    @SuppressWarnings("unchecked")
                    Iterator<IGroupMember> members = (Iterator<IGroupMember>) entityGroup.getMembers();
                    entity = populateChildren(entity,members);
                }
                IAuthorizationPrincipal authP = getPrincipalForEntity(entity);
                Principal principal = new PrincipalImpl(authP.getKey(), authP.getPrincipalString());

                entity.setPrincipal(principal);
                return entity;
            }
        }

        // otherwise use the getGroupMember method
        else {
            IGroupMember groupMember = GroupService.getGroupMember(entityId, entityEnum.getClazz());
            if(groupMember == null || groupMember instanceof IEntityGroup) {
                return null;
            }
            Entity entity = EntityFactory.createEntity(groupMember,entityEnum);

            // the group member interface doesn't include the entity name, so
            // we'll need to look that up manually
            entity.setName(lookupEntityName(entity));
            if (EntityEnum.GROUP.toString().equals(entity.getEntityType()) || EntityEnum.PERSON.toString().equals(entity.getEntityType())) {
                IAuthorizationPrincipal authP = getPrincipalForEntity(entity);
                Principal principal = new PrincipalImpl(authP.getKey(), authP.getPrincipalString());
                entity.setPrincipal(principal);
            }
            return entity;
        }

    }

    public Entity getEntity(IGroupMember member) {

        // get the type of this member entity
        EntityEnum entityEnum = getEntityType(member);

        // construct a new entity bean for this entity
        Entity entity;
        if (entityEnum.isGroup()) {
            entity = EntityFactory.createEntity((IEntityGroup) member, entityEnum);
        } else {
            entity = EntityFactory.createEntity(member, entityEnum);
        }

        // if the name hasn't been set yet, look up the entity name
        if (entity.getName() == null) {
            entity.setName(lookupEntityName(entity));
        }

        if (EntityEnum.GROUP.toString().equals(entity.getEntityType()) || EntityEnum.PERSON.toString().equals(entity.getEntityType())) {
            IAuthorizationPrincipal authP = getPrincipalForEntity(entity);
            Principal principal = new PrincipalImpl(authP.getKey(), authP.getPrincipalString());
            entity.setPrincipal(principal);
        }
        return entity;
    }

    public EntityEnum getEntityType(IGroupMember entity) {

        if (IEntityGroup.class.isAssignableFrom(entity.getClass())) {
            return EntityEnum.getEntityEnum(entity.getEntityType(), true);
        } else {
            return EntityEnum.getEntityEnum(entity.getEntityType(), false);
        }

    }

    public IAuthorizationPrincipal getPrincipalForEntity(Entity entity) {

        // attempt to determine the entity type class for this principal
        Class entityType;
        if(entity.getEntityType().equals(EntityEnum.GROUP.toString())) {
            entityType = IEntityGroup.class;
        } else {
            entityType = EntityEnum.getEntityEnum(entity.getEntityType()).getClazz();
        }

        // construct an authorization principal for this JsonEntityBean
        AuthorizationService authService = AuthorizationService.instance();
        IAuthorizationPrincipal p = authService.newPrincipal(entity.getId(), entityType);
        return p;
    }

    /**
     * <p>Convenience method that looks up the name of the given group member.
     * Used for person types.</p>
     * @param entity Entity to look up
     * @return groupMember's name or null if there's an error
     */
    public String lookupEntityName(Entity entity) {
        EntityEnum entityEnum = EntityEnum.getEntityEnum(entity.getEntityType());
        return lookupEntityName(entityEnum,entity.getId());
    }

    public String lookupEntityName(EntityEnum entityType, String entityId) {
        IEntityNameFinder finder;
        if (entityType.isGroup()) {
            finder = EntityNameFinderService.instance()
                    .getNameFinder(IEntityGroup.class);
        } else {
            finder = EntityNameFinderService.instance()
                    .getNameFinder(entityType.getClazz());
        }

        try {
            return finder.getName(entityId);
        } catch (Exception e) {
			/* An exception here isn't the end of the world.  Just log it
			   and return null. */
            log.warn("Couldn't find name for entity " + entityId, e);
            return null;
        }
    }

    private Entity populateChildren(Entity entity, Iterator<IGroupMember> children) {

        while(children.hasNext()) {

            IGroupMember member = children.next();

            // add the entity bean to the list of children
            Entity entityChild = getEntity(member);
            entity.addChild(entityChild);
        }

        // mark this entity bean as having had it's child list initialized
        entity.setChildrenInitialized(true);

        return entity;
    }
}
