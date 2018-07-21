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
package org.apereo.portal.groups.grouper;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.api.GcGroupDelete;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.EntityGroupImpl;
import org.apereo.portal.groups.EntityImpl;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.ICompositeGroupService;
import org.apereo.portal.groups.IEntity;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IEntityGroupStore;
import org.apereo.portal.groups.IEntitySearcher;
import org.apereo.portal.groups.IEntityStore;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.groups.ILockableEntityGroup;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.spring.locator.EntityTypesLocator;

/**
 * GrouperEntityGroupStore provides an implementation of the group store interface capable of
 * retrieving groups information from Grouper web services. This implementation uses the standard
 * Grouper client jar to search for group information. It does not currently support write access or
 * group locking.
 */
public class GrouperEntityGroupStore implements IEntityGroupStore, IEntityStore, IEntitySearcher {

    private static final String STEM_PREFIX = "uportal.stem";

    /** Logger. */
    protected static final Log LOGGER = LogFactory.getLog(GrouperEntityGroupStoreFactory.class);

    /** Package protected constructor used by the factory method. */
    GrouperEntityGroupStore() {
        /* Package protected. */
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(this + " created");
        }
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.groups.IEntityGroupStore#contains(org.apereo.portal.groups.IEntityGroup, org.apereo.portal.groups.IGroupMember)
     */
    @Override
    public boolean contains(IEntityGroup group, IGroupMember member) throws GroupsException {

        String groupContainerName = group.getLocalKey();
        String groupMemberName = member.getKey();

        if (!validKey(groupContainerName) || !validKey(groupMemberName)) {
            return false;
        }

        GcHasMember gcHasMember = new GcHasMember();
        gcHasMember.assignGroupName(groupContainerName);
        gcHasMember.addSubjectLookup(new WsSubjectLookup(null, "g:gsa", groupMemberName));
        WsHasMemberResults wsHasMemberResults = gcHasMember.execute();
        if (GrouperClientUtils.length(wsHasMemberResults.getResults()) == 1) {
            WsHasMemberResult wsHasMemberResult = wsHasMemberResults.getResults()[0];
            return StringUtils.equals(
                    "IS_MEMBER", wsHasMemberResult.getResultMetadata().getResultCode());
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.groups.IEntityGroupStore#find(java.lang.String)
     */
    @Override
    public IEntityGroup find(String key) throws GroupsException {

        try {

            // Search the Grouper server for groups with the specified local
            // key
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Searching Grouper for a direct match for key: " + key);
            }
            WsGroup wsGroup = findGroupFromKey(key);
            if (wsGroup == null) {
                return null;
            }
            IEntityGroup group = createUportalGroupFromGrouperGroup(wsGroup);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "Retrieved group from the Grouper server matching key "
                                + key
                                + ": "
                                + group.toString());
            }

            // return the group
            return group;

        } catch (Exception e) {
            LOGGER.warn(
                    "Exception while attempting to retrieve "
                            + "group with key "
                            + key
                            + " from Grouper web services: "
                            + e.getMessage());
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.groups.IEntityGroupStore#findParentGroups(org.apereo.portal.groups.IGroupMember)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator findParentGroups(IGroupMember gm) throws GroupsException {

        final List<IEntityGroup> parents = new LinkedList<IEntityGroup>();

        GcGetGroups getGroups = new GcGetGroups();

        String uportalStem = getStemPrefix();

        // if only searching in a specific stem
        if (!StringUtils.isBlank(uportalStem)) {
            getGroups.assignStemScope(StemScope.ALL_IN_SUBTREE);
            getGroups.assignWsStemLookup(new WsStemLookup(uportalStem, null));
        }

        String key = null;
        String subjectSourceId = null;
        if (gm.isGroup()) {

            key = ((IEntityGroup) gm).getLocalKey();

            if (!validKey(key)) {
                return parents.iterator();
            }
            subjectSourceId = "g:gsa";
        } else {

            // Determine the key to use for this entity. If the entity is a
            // group, we should use the group's local key (excluding the
            // "grouper." portion of the full key. If the entity is not a
            // group type, just use the key.
            key = gm.getKey();
        }
        getGroups.addSubjectLookup(new WsSubjectLookup(null, subjectSourceId, key));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Searching Grouper for parent groups of the entity with key: " + key);
        }

        try {

            WsGetGroupsResults results = getGroups.execute();

            if (results == null
                    || results.getResults() == null
                    || results.getResults().length != 1) {
                LOGGER.debug("Grouper service returned no matches for key " + key);
                return parents.iterator();
            }
            WsGetGroupsResult wsg = results.getResults()[0];
            if (wsg.getWsGroups() != null) {
                for (WsGroup g : wsg.getWsGroups()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.trace("Retrieved group: " + g.getName());
                    }
                    IEntityGroup parent = createUportalGroupFromGrouperGroup(g);
                    parents.add(parent);
                }
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "Retrieved " + parents.size() + " parent groups of entity with key " + key);
            }

        } catch (Exception e) {
            LOGGER.warn(
                    "Exception while attempting to retrieve "
                            + "parents for entity with key "
                            + key
                            + " from Grouper web services: "
                            + e.getMessage());
            return Collections.<IEntityGroup>emptyList().iterator();
        }

        return parents.iterator();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.groups.IEntityGroupStore#findEntitiesForGroup(org.apereo.portal.groups.IEntityGroup)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator findEntitiesForGroup(IEntityGroup group) throws GroupsException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Searching Grouper for members of the group with key: " + group.getKey());
        }

        try {

            // execute a search for members of the specified group
            GcGetMembers getGroupsMembers = new GcGetMembers();
            getGroupsMembers.addGroupName(group.getLocalKey());
            getGroupsMembers.assignIncludeSubjectDetail(true);
            WsGetMembersResults results = getGroupsMembers.execute();

            if (results == null
                    || results.getResults() == null
                    || results.getResults().length == 0
                    || results.getResults()[0].getWsSubjects() == null) {
                LOGGER.debug("No members found for Grouper group with key " + group.getLocalKey());
                return Collections.<IGroupMember>emptyList().iterator();
            }

            WsSubject[] gInfos = results.getResults()[0].getWsSubjects();
            final List<IGroupMember> members = new ArrayList<IGroupMember>(gInfos.length);

            // add each result to the member list
            for (WsSubject gInfo : gInfos) {

                // if the member is not a group (aka person)
                if (!StringUtils.equals(gInfo.getSourceId(), "g:gsa")) {

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(
                                "creating leaf member:"
                                        + gInfo.getId()
                                        + " and name: "
                                        + gInfo.getName()
                                        + " from group: "
                                        + group.getLocalKey());
                    }
                    // use the name instead of id as it shows better in the display
                    IGroupMember member = new EntityImpl(gInfo.getName(), IPerson.class);
                    members.add(member);
                }
            }

            // return an iterator for the assembled group
            return members.iterator();

        } catch (Exception e) {
            LOGGER.warn(
                    "Exception while attempting to retrieve "
                            + "member entities of group with key "
                            + group.getKey()
                            + " from Grouper web services: "
                            + e.getMessage());
            return Collections.<IGroupMember>emptyList().iterator();
        }
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.groups.IEntityGroupStore#findMemberGroupKeys(org.apereo.portal.groups.IEntityGroup)
     */
    @Override
    @SuppressWarnings("unchecked")
    public String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException {

        // first the get an iterator for the member groups
        final Iterator<IEntityGroup> it = findMemberGroups(group);

        // construct a list of group keys from this iterator
        List<String> keys = new ArrayList<String>();
        while (it.hasNext()) {
            IEntityGroup eg = it.next();
            keys.add(eg.getKey());
        }

        // return an iterator over the assembled list
        return keys.toArray(new String[keys.size()]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator findMemberGroups(IEntityGroup group) throws GroupsException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Searching for group-type members of group with key: " + group.getKey());
        }

        try {

            if (!validKey(group.getLocalKey())) {
                return Collections.<IEntityGroup>emptyList().iterator();
            }

            GcGetMembers gcGetMembers = new GcGetMembers();
            gcGetMembers.addGroupName(group.getLocalKey());
            gcGetMembers.assignIncludeSubjectDetail(true);
            gcGetMembers.addSourceId("g:gsa");

            WsGetMembersResults results = gcGetMembers.execute();

            if (results == null
                    || results.getResults() == null
                    || results.getResults().length == 0
                    || results.getResults()[0].getWsSubjects() == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                            "No group-type members found for group with key " + group.getKey());
                }
                return Collections.<IEntityGroup>emptyList().iterator();
            }

            final List<IEntityGroup> members = new ArrayList<IEntityGroup>();
            WsSubject[] subjects = results.getResults()[0].getWsSubjects();

            for (WsSubject wsSubject : subjects) {
                if (validKey(wsSubject.getName())) {
                    WsGroup wsGroup = findGroupFromKey(wsSubject.getName());
                    if (wsGroup != null) {
                        IEntityGroup member = createUportalGroupFromGrouperGroup(wsGroup);
                        members.add(member);
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("found IEntityGroup member: " + member);
                        }
                    }
                }
            }

            return members.iterator();

        } catch (Exception e) {
            LOGGER.warn(
                    "Exception while attempting to retrieve "
                            + "member groups of group with key "
                            + group.getKey()
                            + " from Grouper web services: "
                            + e.getMessage());
            return Collections.<IGroupMember>emptyList().iterator();
        }
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.groups.IEntityGroupStore#searchForGroups(java.lang.String, int, java.lang.Class)
     */
    @Override
    public EntityIdentifier[] searchForGroups(
            final String query,
            final SearchMethod method,
            @SuppressWarnings("unchecked") final Class leaftype) {

        // only search for groups
        if (leaftype != IPerson.class) {
            return new EntityIdentifier[] {};
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Searching Grouper for groups matching query: " + query);
        }

        // result groups.
        List<EntityIdentifier> groups = new ArrayList<EntityIdentifier>();

        try {

            // TODO: searches need to be performed against the group display
            // name rather than the group key

            GcFindGroups groupSearch = new GcFindGroups();
            WsQueryFilter filter = new WsQueryFilter();
            // is this an exact search or fuzzy
            if ((method == SearchMethod.DISCRETE_CI) || (method == SearchMethod.DISCRETE)) {
                filter.setQueryFilterType("FIND_BY_GROUP_NAME_EXACT");
            } else {
                filter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
            }
            filter.setGroupName(query);
            groupSearch.assignQueryFilter(filter);
            WsFindGroupsResults results = groupSearch.execute();

            if (results != null && results.getGroupResults() != null) {
                for (WsGroup g : results.getGroupResults()) {
                    if (validKey(g.getName())) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Retrieved group: " + g.getName());
                        }
                        groups.add(new EntityIdentifier(g.getName(), IEntityGroup.class));
                    }
                }
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Returning " + groups.size() + " results for query " + query);
            }

            return groups.toArray(new EntityIdentifier[groups.size()]);

        } catch (Exception e) {
            LOGGER.warn(
                    "Exception while attempting to retrieve "
                            + "search results for query "
                            + query
                            + " and entity type "
                            + leaftype.getCanonicalName()
                            + " : "
                            + e.getMessage());
            return new EntityIdentifier[] {};
        }
    }

    /**
     * @see IEntitySearcher#searchForEntities(java.lang.String,
     *     org.apereo.portal.groups.IGroupConstants.SearchMethod, java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public EntityIdentifier[] searchForEntities(String query, SearchMethod method, Class type)
            throws GroupsException {

        // only search for groups
        if (type != IPerson.class) {
            return new EntityIdentifier[] {};
        }

        List<EntityIdentifier> entityIdentifiers = new ArrayList<EntityIdentifier>();

        try {

            GcGetSubjects subjects = new GcGetSubjects();
            subjects.assignIncludeSubjectDetail(true);
            WsGetSubjectsResults results = subjects.assignSearchString(query).execute();

            if (results != null && results.getWsSubjects() != null) {

                for (WsSubject wsSubject : results.getWsSubjects()) {
                    entityIdentifiers.add(
                            new EntityIdentifier(
                                    wsSubject.getName(), ICompositeGroupService.LEAF_ENTITY_TYPE));
                }
            }
            return entityIdentifiers.toArray(new EntityIdentifier[entityIdentifiers.size()]);

        } catch (Exception e) {
            LOGGER.warn(
                    "Exception while attempting to retrieve "
                            + "search results for query "
                            + query
                            + " and entity type "
                            + type.getCanonicalName()
                            + " : "
                            + e.getMessage());
            return new EntityIdentifier[] {};
        }
    }

    /** @see IEntityStore#newInstance(java.lang.String, java.lang.Class) */
    @Override
    @SuppressWarnings("unchecked")
    public IEntity newInstance(String key, Class type) throws GroupsException {
        if (EntityTypesLocator.getEntityTypes().getEntityIDFromType(type) == null) {
            throw new GroupsException("Invalid group type: " + type);
        }
        return new EntityImpl(key, type);
    }

    /**
     * Construct an IEntityGroup from a Grouper WsGroup.
     *
     * @param wsGroup
     * @return the group
     */
    protected IEntityGroup createUportalGroupFromGrouperGroup(WsGroup wsGroup) {
        IEntityGroup iEntityGroup = new EntityGroupImpl(wsGroup.getName(), IPerson.class);

        // need to set the group name and description to the actual
        // display name and description
        iEntityGroup.setName(wsGroup.getDisplayName());
        iEntityGroup.setDescription(wsGroup.getDescription());
        return iEntityGroup;
    }

    /**
     * Find the Grouper group matching the specified key.
     *
     * @param key
     * @return the group or null
     */
    protected WsGroup findGroupFromKey(String key) {
        WsGroup wsGroup = null;

        if (key != null) {

            GcFindGroups gcFindGroups = new GcFindGroups();
            gcFindGroups.addGroupName(key);
            WsFindGroupsResults results = gcFindGroups.execute();

            // if no results were returned, return null
            if (results != null
                    && results.getGroupResults() != null
                    && results.getGroupResults().length > 0) {

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                            "found group from key " + key + ": " + results.getGroupResults()[0]);
                }

                wsGroup = results.getGroupResults()[0];
            }
        }

        return wsGroup;
    }

    /**
     * Get the prefix for the stem containing uPortal groups. If this value is non-empty, all groups
     * will be required to be prefixed with the specified stem name.
     *
     * @return the uportal stem in the registry, without trailing colon
     */
    protected static String getStemPrefix() {

        String uportalStem = GrouperClientUtils.propertiesValue(STEM_PREFIX, false);

        // make sure it ends in colon
        if (!StringUtils.isBlank(uportalStem)) {
            if (uportalStem.endsWith(":")) {
                uportalStem = uportalStem.substring(0, uportalStem.length() - 1);
            }
        }

        return uportalStem;
    }

    /**
     * @param key
     * @return true if ok key (group name) false if not
     */
    protected static boolean validKey(String key) {
        String uportalStem = getStemPrefix();

        if (!StringUtils.isBlank(uportalStem)
                && (StringUtils.isBlank(key) || !key.startsWith(uportalStem.concat(":")))) {
            // if the uPortal stem prefix is specified and the key is blank
            // or does not contain the stem prefix, return false
            return false;
        } else {
            // otherwise, indicate that the key is valid
            return true;
        }
    }

    /** @see IEntityGroupStore#update(IEntityGroup) */
    @Override
    public void update(IEntityGroup group) throws GroupsException {

        // assume key is fully qualified group name
        String groupName = group.getLocalKey();

        String description = group.getDescription();

        // the name is the displayExtension
        String displayExtension = group.getName();

        WsGroupToSave wsGroupToSave = new WsGroupToSave();
        wsGroupToSave.setCreateParentStemsIfNotExist("T");
        wsGroupToSave.setWsGroupLookup(new WsGroupLookup(groupName, null));
        WsGroup wsGroup = new WsGroup();
        wsGroup.setName(groupName);
        wsGroup.setDisplayExtension(displayExtension);
        wsGroup.setDescription(description);
        wsGroupToSave.setWsGroup(wsGroup);

        new GcGroupSave().addGroupToSave(wsGroupToSave).execute();

        updateMembers(group);
    }

    /** @see IEntityGroupStore#updateMembers(IEntityGroup) */
    @Override
    public void updateMembers(IEntityGroup group) throws GroupsException {

        // assume key is fully qualified group name
        String groupName = group.getLocalKey();

        GcAddMember gcAddMember = new GcAddMember().assignGroupName(groupName);

        for (IGroupMember iGroupMember : group.getChildren()) {
            EntityIdentifier entityIdentifier = iGroupMember.getEntityIdentifier();
            String identifier = entityIdentifier.getKey();
            gcAddMember.addSubjectIdentifier(identifier);
        }
        gcAddMember.execute();
    }

    /** @see IEntityGroupStore#delete(IEntityGroup) */
    @Override
    public void delete(IEntityGroup group) throws GroupsException {

        String groupName = group.getLocalKey();
        new GcGroupDelete().addGroupLookup(new WsGroupLookup(groupName, null)).execute();
    }

    /** @see IEntityGroupStore#findLockable(java.lang.String) */
    @Override
    public ILockableEntityGroup findLockable(String key) throws GroupsException {
        throw new UnsupportedOperationException(
                "Group locking is not supported by the Grouper groups service");
    }

    /*
     * @see
     * org.apereo.portal.groups.IEntityGroupStore#newInstance(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public IEntityGroup newInstance(Class entityType) throws GroupsException {
        throw new UnsupportedOperationException(
                "Group updates are not supported by the Grouper groups service");
    }
}
