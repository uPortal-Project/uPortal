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
package org.apereo.portal.layout.dlm.remoting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IEntityNameFinder;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlets.groupselector.EntityEnum;
import org.apereo.portal.security.AuthorizationPrincipalHelper;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.AuthorizationServiceFacade;
import org.apereo.portal.services.EntityNameFinderService;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.spring.locator.EntityTypesLocator;

public class GroupListHelperImpl implements IGroupListHelper {

    private static final String PRINCIPAL_SEPARATOR = "\\.";

    private static final Log log = LogFactory.getLog(GroupListHelperImpl.class);

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.layout.dlm.remoting.IGroupListHelper#search(java.lang.String, java.lang.String)
     *
     * External search, thus case insensitive.
     *
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<JsonEntityBean> search(String entityType, String searchTerm) {

        Set<JsonEntityBean> results = new HashSet<>();

        EntityEnum entityEnum = EntityEnum.getEntityEnum(entityType);
        if (entityEnum == null) {
            throw new IllegalArgumentException(
                    String.format("Parameter entityType has an unknown value of [%s]", entityType));
        }

        EntityIdentifier[] identifiers;

        Class identifierType;

        // if the entity type is a group, use the group service's findGroup method
        // to locate it
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

        for (int i = 0; i < identifiers.length; i++) {
            if (identifiers[i].getType().equals(identifierType)) {
                IGroupMember entity = GroupService.getGroupMember(identifiers[i]);
                if (entity != null) {
                    JsonEntityBean jsonBean = getEntity(entity);
                    results.add(jsonBean);
                } else {
                    log.warn("Grouper member entity of " + identifiers[i].getKey() + " is null.");
                }
            }
        }

        return results;
    }

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.layout.dlm.remoting.IGroupListHelper#getRootEntity(java.lang.String)
     */
    @Override
    public JsonEntityBean getRootEntity(String groupType) {

        EntityEnum type = EntityEnum.getEntityEnum(groupType);

        String rootKey;
        if (EntityEnum.GROUP.equals(type)) {
            rootKey = "local.0";
        } else if (EntityEnum.CATEGORY.equals(type)) {
            IEntityGroup categoryGroup =
                    GroupService.getDistinguishedGroup(IPortletDefinition.DISTINGUISHED_GROUP);
            return new JsonEntityBean(categoryGroup, EntityEnum.CATEGORY);
        } else {
            throw new IllegalArgumentException(
                    "Unable to determine a root entity for group type '" + groupType + "'");
        }

        JsonEntityBean bean = getEntity(groupType, rootKey, false);

        return bean;
    }

    @Override
    public JsonEntityBean getIndividualBestRootEntity(
            final IPerson person,
            final String groupType,
            final String permissionOwner,
            final String permissionActivity) {
        return getIndividualBestRootEntity(
                person, groupType, permissionOwner, new String[] {permissionActivity});
    }

    @Override
    public JsonEntityBean getIndividualBestRootEntity(
            final IPerson person,
            final String groupType,
            final String permissionOwner,
            final String[] permissionActivities) {

        if (log.isDebugEnabled()) {
            String username = (person == null) ? "null" : person.getUserName();

            log.debug(
                    "Choosing best root group for user='"
                            + username
                            + "', groupType='"
                            + groupType
                            + "', permissionOwner='"
                            + permissionOwner
                            + "', permissionActivities='"
                            + Arrays.toString(permissionActivities)
                            + "'");
        }

        final IAuthorizationPrincipal principal =
                AuthorizationPrincipalHelper.principalFromUser(person);
        final JsonEntityBean canonicalRootGroup = getRootEntity(groupType);

        if (log.isDebugEnabled()) {
            log.debug(
                    "Found for groupType='"
                            + groupType
                            + "' the following canonicalRootGroup:  "
                            + canonicalRootGroup);
        }

        /*
         *  First check the canonical root group for the applicable activities
         *  (NOTE: the uPortal permissions infrastructure handles checking of
         *  special, collective targets like "ALL_GROUPS" and "All_categories").
         */
        for (String activity : permissionActivities) {
            if (principal.hasPermission(permissionOwner, activity, canonicalRootGroup.getId())) {
                return canonicalRootGroup;
            }
        }

        // So much for the easy path -- see if the user has any records at all for this specific
        // owner/activity
        JsonEntityBean rslt = null; // Default
        final List<IPermission> permissionsOfRelevantActivity = new ArrayList<IPermission>();
        for (String activity : permissionActivities) {
            permissionsOfRelevantActivity.addAll(
                    Arrays.asList(principal.getAllPermissions(permissionOwner, activity, null)));
        }
        if (log.isDebugEnabled()) {
            log.debug(
                    "For user='"
                            + person.getUserName()
                            + "', groupType='"
                            + groupType
                            + "', permissionOwner='"
                            + permissionOwner
                            + "', permissionActivities='"
                            + Arrays.toString(permissionActivities)
                            + "' permissionsOfRelevantTypes.size()="
                            + permissionsOfRelevantActivity.size());
        }
        switch (permissionsOfRelevantActivity.size()) {
            case 0:
                // No problem -- user doesn't have any of this sort of permission (leave it null)
                break;
            default:
                // We need to make some sort of determination as to the best
                // root group to send back.  With luck there aren't many matches.
                for (IPermission p : permissionsOfRelevantActivity) {
                    IEntityGroup groupMember = GroupService.findGroup(p.getTarget());
                    final JsonEntityBean candidate = getEntity(groupMember);
                    // Pass on any matches of the wrong groupType...
                    if (!candidate.getEntityTypeAsString().equalsIgnoreCase(groupType)) {
                        continue;
                    }
                    if (rslt == null) {
                        // First allowable selection;  run with this one
                        // unless/until we're forced to make a choice.
                        rslt = candidate;
                    } else {
                        // For the present we'll assume the match with the most
                        // children is the best;  this approach should work
                        // decently unless folks start putting redundant
                        // permissions records in the DB for multiple levels of
                        // the same rich hierarchy.
                        if (candidate.getChildren().size() > rslt.getChildren().size()) {
                            rslt = candidate;
                        }
                    }
                }
                break;
        }

        if (log.isDebugEnabled()) {
            log.debug(
                    "Selected for user='"
                            + person.getUserName()
                            + "', groupType='"
                            + groupType
                            + "', permissionOwner='"
                            + permissionOwner
                            + "', permissionActivities='"
                            + Arrays.toString(permissionActivities)
                            + "' the following best root group:  "
                            + rslt);
        }

        return rslt;
    }

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.layout.dlm.remoting.IGroupListHelper#getEntityTypesForGroupType(java.lang.String)
     */
    @Override
    public Set<String> getEntityTypesForGroupType(String groupType) {

        // add the group type itself to the allowed list
        Set<String> set = new HashSet<String>();
        set.add(groupType);

        /*
         * If the supplied type is a person group, add the person entity type.
         * If the supplied type is a category, add the channel type.  Otherwise,
         * throw an exception.
         *
         * This method will require an update if more entity types are added
         * in the future.
         */

        EntityEnum type = EntityEnum.getEntityEnum(groupType);
        if (EntityEnum.GROUP.equals(type)) {
            set.add(EntityEnum.PERSON.toString());
        } else if (EntityEnum.CATEGORY.equals(type)) {
            set.add(EntityEnum.PORTLET.toString());
        } else {
            throw new IllegalArgumentException(
                    "Unable to determine a root entity for group type '" + groupType + "'");
        }

        return set;
    }

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.layout.dlm.remoting.IGroupListHelper#getEntity(java.lang.String, java.lang.String, boolean)
     */
    @Override
    public JsonEntityBean getEntity(String entityType, String entityId, boolean populateChildren) {

        // get the EntityEnum for the specified entity type
        EntityEnum entityEnum = EntityEnum.getEntityEnum(entityType);
        if (entityEnum == null) {
            throw new IllegalArgumentException(
                    String.format("Parameter entityType has an unknown value of [%s]", entityType));
        }
        // if the entity type is a group, use the group service's findGroup method
        // to locate it
        if (entityEnum.isGroup()) {
            // attempt to find the entity
            IEntityGroup entity = GroupService.findGroup(entityId);
            if (entity == null) {
                return null;
            } else {
                JsonEntityBean jsonBean = new JsonEntityBean(entity, entityEnum);
                if (populateChildren) {
                    Iterator<IGroupMember> members = entity.getChildren().iterator();
                    jsonBean = populateChildren(jsonBean, members);
                }
                if (jsonBean.getEntityType().isGroup()
                        || EntityEnum.PERSON.equals(jsonBean.getEntityType())) {
                    IAuthorizationPrincipal principal = getPrincipalForEntity(jsonBean);
                    jsonBean.setPrincipalString(principal.getPrincipalString());
                }
                return jsonBean;
            }
        }

        // otherwise use the getGroupMember method
        else {
            IGroupMember entity = GroupService.getGroupMember(entityId, entityEnum.getClazz());
            if (entity == null || entity instanceof IEntityGroup) {
                return null;
            }
            JsonEntityBean jsonBean = new JsonEntityBean(entity, entityEnum);

            // the group member interface doesn't include the entity name, so
            // we'll need to look that up manually
            jsonBean.setName(lookupEntityName(jsonBean));
            if (EntityEnum.GROUP.equals(jsonBean.getEntityType())
                    || EntityEnum.PERSON.equals(jsonBean.getEntityType())) {
                IAuthorizationPrincipal principal = getPrincipalForEntity(jsonBean);
                jsonBean.setPrincipalString(principal.getPrincipalString());
            }
            return jsonBean;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.layout.dlm.remoting.IGroupListHelper#getEntity(org.apereo.portal.groups.IGroupMember)
     */
    @Override
    public JsonEntityBean getEntity(IGroupMember member) {

        // get the type of this member entity
        EntityEnum entityEnum = getEntityType(member);

        // construct a new entity bean for this entity
        JsonEntityBean entity;
        if (entityEnum.isGroup()) {
            entity = new JsonEntityBean((IEntityGroup) member, entityEnum);
        } else {
            entity = new JsonEntityBean(member, entityEnum);
        }

        // if the name hasn't been set yet, look up the entity name
        if (entity.getName() == null) {
            entity.setName(lookupEntityName(entity));
        }

        if (EntityEnum.GROUP.equals(entity.getEntityType())
                || EntityEnum.PERSON.equals(entity.getEntityType())) {
            IAuthorizationPrincipal principal = getPrincipalForEntity(entity);
            entity.setPrincipalString(principal.getPrincipalString());
        }
        return entity;
    }

    @Override
    public JsonEntityBean getEntityForPrincipal(String principalString) {
        if (principalString == null) {
            throw new IllegalArgumentException("Parameter cannot be null.");
        }
        // split the principal string into its type and key components
        String[] parts = principalString.split(PRINCIPAL_SEPARATOR, 2);
        String key = parts[1];
        int typeId = Integer.parseInt(parts[0]);

        // get the EntityEnum type for the entity id number
        @SuppressWarnings("unchecked")
        Class type = EntityTypesLocator.getEntityTypes().getEntityTypeFromID(typeId);
        String entityType = "person";
        if (IEntityGroup.class.isAssignableFrom(type)) {
            entityType = "group";
        }

        // get the JsonEntityBean for this type and key
        JsonEntityBean bean = getEntity(entityType, key, false);
        return bean;
    }

    @Override
    public IAuthorizationPrincipal getPrincipalForEntity(JsonEntityBean entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Parameter cannot be null.");
        }

        // attempt to determine the entity type class for this principal
        Class entityType;
        EntityEnum jsonType = entity.getEntityType();
        if (jsonType == null) {
            throw new IllegalArgumentException("Parameter's entityType cannot be null.");
        }
        if (jsonType.isGroup()) {
            entityType = IEntityGroup.class;
        } else {
            entityType = jsonType.getClazz();
        }

        // construct an authorization principal for this JsonEntityBean
        AuthorizationServiceFacade authService = AuthorizationServiceFacade.instance();
        IAuthorizationPrincipal p = authService.newPrincipal(entity.getId(), entityType);
        return p;
    }

    /**
     * Populates the children of the JsonEntityBean. Creates new JsonEntityBeans for the known types
     * (person, group, or category), and adds them as children to the current bean.
     *
     * @param jsonBean Entity bean to which the children are added
     * @param children An Iterator containing IGroupMember elements. Usually obtained from
     *     entity.getMembers().
     * @return jsonBean with the children populated
     */
    private JsonEntityBean populateChildren(
            JsonEntityBean jsonBean, Iterator<IGroupMember> children) {

        while (children.hasNext()) {

            IGroupMember member = children.next();

            // add the entity bean to the list of children
            JsonEntityBean jsonChild = getEntity(member);
            jsonBean.addChild(jsonChild);
        }

        // mark this entity bean as having had it's child list initialized
        jsonBean.setChildrenInitialized(true);

        return jsonBean;
    }

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.layout.dlm.remoting.IGroupListHelper#getEntityType(org.apereo.portal.groups.IGroupMember)
     */
    @Override
    public EntityEnum getEntityType(IGroupMember entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }

        if (IEntityGroup.class.isAssignableFrom(entity.getClass())) {
            return EntityEnum.getEntityEnum(entity.getLeafType(), true);
        } else {
            return EntityEnum.getEntityEnum(entity.getLeafType(), false);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.portlets.groupselector.GroupsSelectorHelper#getEntityBeans(java.util.List)
     */
    @Override
    public List<JsonEntityBean> getEntityBeans(List<String> params) {
        // if no parameters have been supplied, just return an empty list
        if (params == null || params.isEmpty()) {
            return Collections.<JsonEntityBean>emptyList();
        }

        List<JsonEntityBean> beans = new ArrayList<JsonEntityBean>();
        for (String param : params) {
            String[] parts = param.split(":", 2);
            JsonEntityBean member = getEntity(parts[0], parts[1], false);
            beans.add(member);
        }
        return beans;
    }

    /**
     * Convenience method that looks up the name of the given group member. Used for person types.
     *
     * @param groupMember Entity to look up
     * @return groupMember's name or null if there's an error
     */
    @Override
    public String lookupEntityName(JsonEntityBean entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Parameter cannot be null.");
        }
        EntityEnum entityEnum = entity.getEntityType();
        if (entityEnum == null) {
            throw new IllegalArgumentException(
                    String.format(
                            "Parameter's entityType has an unknown value of [%s]",
                            entity.getEntityType()));
        }
        IEntityNameFinder finder;
        if (entityEnum.isGroup()) {
            finder = EntityNameFinderService.instance().getNameFinder(IEntityGroup.class);
        } else {
            finder = EntityNameFinderService.instance().getNameFinder(entityEnum.getClazz());
        }

        try {
            return finder.getName(entity.getId());
        } catch (Exception e) {
            /* An exception here isn't the end of the world.  Just log it
            and return null. */
            log.warn("Couldn't find name for entity " + entity.getId(), e);
            return null;
        }
    }
}
