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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IEntityNameFinder;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.portlets.groupselector.EntityEnum;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.services.AuthorizationServiceFacade;
import org.apereo.portal.services.EntityNameFinderService;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.utils.threading.SingletonDoubleCheckedCreator;

public final class EntityService {
    private static final Log log = LogFactory.getLog(EntityService.class);

    private static final SingletonDoubleCheckedCreator<EntityService> instance =
            new SingletonDoubleCheckedCreator<EntityService>() {
                @Override
                protected EntityService createSingleton(Object... args) {
                    return new EntityService();
                }
            };

    public static EntityService instance() {
        return instance.get();
    }

    // External search, thus case insensitive.
    public Set<Entity> search(String entityType, String searchTerm) {
        if (StringUtils.isBlank(entityType) && StringUtils.isBlank(searchTerm)) {
            return null;
        }
        final Set<Entity> results = new HashSet<>();

        final EntityEnum entityEnum = EntityEnum.getEntityEnum(entityType);
        if (entityEnum == null) {
            throw new RuntimeException(
                    "EntityEnum instance not found for specified type:  " + entityType);
        }

        // if the entity type is a group, use the group service's findGroup method
        // to locate it
        EntityIdentifier[] identifiers;
        Class<?> identifierType;
        if (entityEnum.isGroup()) {
            identifiers =
                    GroupService.searchForGroups(
                            searchTerm,
                            GroupService.SearchMethod.CONTAINS_CI,
                            entityEnum.getClazz());
            identifierType = IEntityGroup.class;
        }
        // otherwise use the getGroupMember method
        else {
            identifiers =
                    GroupService.searchForEntities(
                            searchTerm,
                            GroupService.SearchMethod.CONTAINS_CI,
                            entityEnum.getClazz());
            identifierType = entityEnum.getClazz();
        }

        for (EntityIdentifier entityIdentifier : identifiers) {
            if (entityIdentifier.getType().equals(identifierType)) {
                IGroupMember groupMember = GroupService.getGroupMember(entityIdentifier);
                Entity entity = getEntity(groupMember);
                results.add(entity);
            }
        }

        return results;
    }

    public Entity getEntity(String entityType, String entityId, boolean populateChildren) {

        // get the EntityEnum for the specified entity type
        if (StringUtils.isBlank(entityType) && StringUtils.isBlank(entityId)) {
            return null;
        }

        final EntityEnum entityEnum = EntityEnum.getEntityEnum(entityType);
        if (entityEnum == null) {
            throw new RuntimeException(
                    "EntityEnum instance not found for specified type:  " + entityType);
        }

        // if the entity type is a group, use the group service's findGroup method
        // to locate it
        if (entityEnum.isGroup()) {
            // attempt to find the entity
            IEntityGroup entityGroup = GroupService.findGroup(entityId);
            if (entityGroup == null) {
                return null;
            } else {
                Entity entity = EntityFactory.createEntity(entityGroup, entityEnum);
                if (populateChildren) {
                    Iterator<IGroupMember> members = entityGroup.getChildren().iterator();
                    entity = populateChildren(entity, members);
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
            if (groupMember == null || groupMember instanceof IEntityGroup) {
                return null;
            }
            Entity entity = EntityFactory.createEntity(groupMember, entityEnum);

            // the group member interface doesn't include the entity name, so
            // we'll need to look that up manually
            entity.setName(lookupEntityName(entity));
            if (EntityEnum.GROUP.toString().equals(entity.getEntityType())
                    || EntityEnum.PERSON.toString().equals(entity.getEntityType())) {
                IAuthorizationPrincipal authP = getPrincipalForEntity(entity);
                Principal principal = new PrincipalImpl(authP.getKey(), authP.getPrincipalString());
                entity.setPrincipal(principal);
            }
            return entity;
        }
    }

    public Entity getEntity(IGroupMember member) {
        if (member == null) {
            return null;
        }
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

        if (EntityEnum.GROUP.toString().equals(entity.getEntityType())
                || EntityEnum.PERSON.toString().equals(entity.getEntityType())) {
            IAuthorizationPrincipal authP = getPrincipalForEntity(entity);
            Principal principal = new PrincipalImpl(authP.getKey(), authP.getPrincipalString());
            entity.setPrincipal(principal);
        }
        return entity;
    }

    private EntityEnum getEntityType(IGroupMember entity) {

        if (IEntityGroup.class.isAssignableFrom(entity.getClass())) {
            return EntityEnum.getEntityEnum(entity.getLeafType(), true);
        } else {
            return EntityEnum.getEntityEnum(entity.getLeafType(), false);
        }
    }

    /* package-private */ IAuthorizationPrincipal getPrincipalForEntity(Entity entity) {

        // attempt to determine the entity type class for this principal
        if (entity == null) {
            return null;
        }
        Class entityType;
        if (entity.getEntityType().equals(EntityEnum.GROUP.toString())) {
            entityType = IEntityGroup.class;
        } else {
            entityType = EntityEnum.getEntityEnum(entity.getEntityType()).getClazz();
        }

        // construct an authorization principal for this JsonEntityBean
        AuthorizationServiceFacade authService = AuthorizationServiceFacade.instance();
        return authService.newPrincipal(entity.getId(), entityType);
    }

    /**
     * Convenience method that looks up the name of the given group member. Used for person types.
     *
     * @param entity Entity to look up
     * @return groupMember's name or null if there's an error
     */
    private String lookupEntityName(Entity entity) {
        if (entity == null) {
            return null;
        }
        EntityEnum entityEnum = EntityEnum.getEntityEnum(entity.getEntityType());
        return lookupEntityName(entityEnum, entity.getId());
    }

    private String lookupEntityName(EntityEnum entityType, String entityId) {
        if (entityType == null && (StringUtils.isBlank(entityId))) {
            return null;
        }
        IEntityNameFinder finder;
        if (entityType.isGroup()) {
            finder = EntityNameFinderService.instance().getNameFinder(IEntityGroup.class);
        } else {
            finder = EntityNameFinderService.instance().getNameFinder(entityType.getClazz());
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

        while (children.hasNext()) {

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
