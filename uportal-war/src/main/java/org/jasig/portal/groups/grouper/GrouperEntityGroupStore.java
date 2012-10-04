/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.groups.grouper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.EntityGroupImpl;
import org.jasig.portal.groups.EntityImpl;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntitySearcher;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.security.IPerson;

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

/**
 * GrouperEntityGroupStore provides an implementation of the group store 
 * interface capable of retrieving groups information from Grouper web services.
 * This implementation uses the standard Grouper client jar to search for group
 * information.  It does not currently support write access or group locking.
 * 
 * @author Bill Brown
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class GrouperEntityGroupStore implements IEntityGroupStore,
        IEntityStore, IEntitySearcher {

    private final static String STEM_PREFIX = "uportal.stem";
    
    /** Logger. */
    protected final static Log LOGGER = LogFactory
            .getLog(GrouperEntityGroupStoreFactory.class);

    /**
     * Package protected constructor used by the factory method.
     */
    GrouperEntityGroupStore() { /* Package protected. */
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(this + " created");
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.groups.IEntityGroupStore#contains(org.jasig.portal.groups.IEntityGroup, org.jasig.portal.groups.IGroupMember)
     */
    public boolean contains(IEntityGroup group, IGroupMember member)
            throws GroupsException {
    
        String groupContainerName = group.getLocalKey();
        String groupMemberName = member.getKey();

        if (!validKey(groupContainerName)
                || !validKey(groupMemberName)) {
            return false;
        }

        GcHasMember gcHasMember = new GcHasMember();
        gcHasMember.assignGroupName(groupContainerName);
        gcHasMember.addSubjectLookup(new WsSubjectLookup(null, "g:gsa",
                groupMemberName));
        WsHasMemberResults wsHasMemberResults = gcHasMember.execute();
        if (GrouperClientUtils.length(wsHasMemberResults.getResults()) == 1) {
            WsHasMemberResult wsHasMemberResult = wsHasMemberResults
                    .getResults()[0];
            return StringUtils.equals("IS_MEMBER", wsHasMemberResult
                    .getResultMetadata().getResultCode());
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.groups.IEntityGroupStore#find(java.lang.String)
     */
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
                LOGGER.debug("Retrieved group from the Grouper server matching key "
                        + key + ": " + group.toString());
            }
            
            // return the group
            return group;
            

        } catch (Exception e) {
            LOGGER.warn("Exception while attempting to retrieve " 
                    + "group with key " + key + " from Grouper web services: " + e.getMessage());
            return null;
        }

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.groups.IEntityGroupStore#findContainingGroups(org.jasig.portal.groups.IGroupMember)
     */
    @SuppressWarnings("unchecked")
    public Iterator findContainingGroups(IGroupMember gm)
            throws GroupsException {
        
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
        getGroups.addSubjectLookup(new WsSubjectLookup(null, subjectSourceId,
                key));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Searching Grouper for parent groups of the entity with key: "
                        + key);
            }

            try {
                
                WsGetGroupsResults results = getGroups.execute();


                if (results == null || results.getResults() == null || results.getResults().length != 1) {
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
                    LOGGER.debug("Retrieved " + parents.size() + " parent groups of entity with key " + key);
                }

            } catch (Exception e) {
                LOGGER.warn("Exception while attempting to retrieve "
                        + "parents for entity with key " + key
                        + " from Grouper web services: " + e.getMessage());
                return Collections.<IEntityGroup>emptyList().iterator();
            }

        return parents.iterator();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.groups.IEntityGroupStore#findEntitiesForGroup(org.jasig.portal.groups.IEntityGroup)
     */
    @SuppressWarnings("unchecked")
    public Iterator findEntitiesForGroup(IEntityGroup group)
            throws GroupsException {
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Searching Grouper for members of the group with key: " 
                    + group.getKey());
        }

        try {
            
            // execute a search for members of the specified group
            GcGetMembers getGroupsMembers = new GcGetMembers();
            getGroupsMembers.addGroupName(group.getLocalKey());
            getGroupsMembers.assignIncludeSubjectDetail(true);
            WsGetMembersResults results = getGroupsMembers.execute();
            
            if (results == null || results.getResults() == null
                    || results.getResults().length == 0
                    || results.getResults()[0].getWsSubjects() == null) {
                LOGGER.debug("No members found for Grouper group with key "
                        + group.getLocalKey());
                return Collections.<IGroupMember>emptyList().iterator();
            }

            WsSubject[] gInfos = results.getResults()[0].getWsSubjects();
            final List<IGroupMember> members = new ArrayList<IGroupMember>(
                    gInfos.length);
            
            // add each result to the member list
            for (WsSubject gInfo : gInfos) {
                
                // if the member is not a group (aka person)
            	if (!StringUtils.equals(gInfo.getSourceId(), "g:gsa")) {

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("creating leaf member:" + gInfo.getId()
								+ " and name: " + gInfo.getName()
								+ " from group: " + group.getLocalKey());
					}
					//use the name instead of id as it shows better in the display
					IGroupMember member = new EntityImpl(gInfo.getName(),
							IPerson.class);
					members.add(member);
				}

            }

            // return an iterator for the assembled group
            return members.iterator();
            
        } catch (Exception e) {
            LOGGER.warn("Exception while attempting to retrieve "
                    + "member entities of group with key " + group.getKey() 
                    + " from Grouper web services: " + e.getMessage());
            return Collections.<IGroupMember>emptyList().iterator();
        }

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.groups.IEntityGroupStore#findMemberGroupKeys(org.jasig.portal.groups.IEntityGroup)
     */
    @SuppressWarnings("unchecked")
    public String[] findMemberGroupKeys(IEntityGroup group)
            throws GroupsException {
        
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

    @SuppressWarnings("unchecked")
    public Iterator findMemberGroups(IEntityGroup group) throws GroupsException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Searching for group-type members of group with key: " 
                    + group.getKey());
        }

        try {
            
            if (!validKey(group.getLocalKey())) {
                return Collections.<IEntityGroup> emptyList().iterator();
            }

            GcGetMembers gcGetMembers = new GcGetMembers();
            gcGetMembers.addGroupName(group.getLocalKey());
            gcGetMembers.assignIncludeSubjectDetail(true);
            gcGetMembers.addSourceId("g:gsa");

            WsGetMembersResults results = gcGetMembers.execute();

            if (results == null || results.getResults() == null
					|| results.getResults().length == 0
					|| results.getResults()[0].getWsSubjects() == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER
                            .debug("No group-type members found for group with key "
                                    + group.getKey());
                }
                return Collections.<IEntityGroup> emptyList().iterator();
            }

            final List<IEntityGroup> members = new ArrayList<IEntityGroup>();
            WsSubject[] subjects = results.getResults()[0].getWsSubjects();

			for (WsSubject wsSubject : subjects) {
                if (validKey(wsSubject.getName())) {
                    WsGroup wsGroup = findGroupFromKey(wsSubject
                            .getName());
                    if (wsGroup != null) {
                        IEntityGroup member = createUportalGroupFromGrouperGroup(wsGroup);
                        members.add(member);
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("found IEntityGroup member: "
                                    + member);
                        }
                    }
                }
            }

            
            return members.iterator();
            
        } catch (Exception e) {
            LOGGER.warn("Exception while attempting to retrieve "
                    + "member groups of group with key " + group.getKey() 
                    + " from Grouper web services: " + e.getMessage());
            return Collections.<IGroupMember>emptyList().iterator();
        }

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.groups.IEntityGroupStore#searchForGroups(java.lang.String, int, java.lang.Class)
     */
    public EntityIdentifier[] searchForGroups(final String query,
            final int method,
            @SuppressWarnings("unchecked") final Class leaftype) {
    	
    	//only search for groups
        if ( leaftype != IPerson.class )
          { return new EntityIdentifier[] {}; }

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
            //is this an exact search or fuzzy
            if(method == IGroupConstants.IS){
            	filter.setQueryFilterType("FIND_BY_GROUP_NAME_EXACT");
            }else{
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
                    groups.add(new EntityIdentifier(g.getName(),
                            IEntityGroup.class));
                }
            }
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Returning " + groups.size()
                        + " results for query " + query);
            }

            return (EntityIdentifier[]) groups
                    .toArray(new EntityIdentifier[] {});

        } catch (Exception e) {
            LOGGER.warn("Exception while attempting to retrieve "
                    + "search results for query " + query + " and entity type " 
                    + leaftype.getCanonicalName() + " : " + e.getMessage());
            return new EntityIdentifier[] {};
        }

    }
    
    /**
     * @see org.jasig.portal.groups.IEntitySearcher#searchForEntities(java.lang.String,
     *      int, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public EntityIdentifier[] searchForEntities(String query, int method,
            Class type) throws GroupsException {
    	
    	// only search for groups
		if (type != IPerson.class) {
			return new EntityIdentifier[] {};
		}

		List<EntityIdentifier> entityIdentifiers = new ArrayList<EntityIdentifier>();

		try {

			GcGetSubjects subjects = new GcGetSubjects();
			subjects.assignIncludeSubjectDetail(true);
			WsGetSubjectsResults results = subjects.assignSearchString(query)
					.execute();

			if (results != null && results.getWsSubjects() != null) {

				for (WsSubject wsSubject : results.getWsSubjects()) {
					entityIdentifiers.add(new EntityIdentifier(wsSubject
							.getName(), EntityTypes.LEAF_ENTITY_TYPE));
				}
			}
			return ((EntityIdentifier[]) entityIdentifiers.toArray());

		} catch (Exception e) {
			LOGGER.warn("Exception while attempting to retrieve "
					+ "search results for query " + query + " and entity type "
					+ type.getCanonicalName() + " : " + e.getMessage());
			return new EntityIdentifier[] {};
		}
    
    }
    
    /**
     * @see org.jasig.portal.groups.IEntityStore#newInstance(java.lang.String)
     */
    public IEntity newInstance(String key) throws GroupsException {
        return new EntityImpl(key, null);
    }

    /**
     * @see org.jasig.portal.groups.IEntityStore#newInstance(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public IEntity newInstance(String key, Class type) throws GroupsException {
        if (org.jasig.portal.EntityTypes.getEntityTypeID(type) == null) {
            throw new GroupsException("Invalid group type: " + type);
        }
        return new EntityImpl(key, type);
    }

    /**
     * Test a Grouper {WsGroup} against a query string according to the specified
     * method and determine if it matches the query.
     * 
     * @param group  WsGroup to be tested
     * @param query  Query string
     * @param method int-based method matching one of the standard search methods
     *               defined in {IGroupConstants}
     * @return       <code>true</code> if the group matches, <code>false</code>
     *               otherwise
     */
    protected boolean groupMatches(WsGroup group, String query, int method) {
        
        // Ensure that this group has a name defined before performing 
        // comparisons.
        if (group == null || group.getName() == null) {
            return false;
        }
        
        switch (method) {
            case IGroupConstants.IS:
                return group.getName().equals(query);
            case IGroupConstants.STARTS_WITH:
                return group.getName().startsWith(query);
            case IGroupConstants.ENDS_WITH:
                return group.getName().endsWith(query);
            case IGroupConstants.CONTAINS:
                return group.getName().contains(query);
            default:
                return false;
        }
    }
    
    /**
     * Construct an IEntityGroup from a Grouper WsGroup.
     * 
     * @param wsGroup
     * @return the group
     */
    protected IEntityGroup createUportalGroupFromGrouperGroup(WsGroup wsGroup) {
        IEntityGroup iEntityGroup = new EntityGroupImpl(wsGroup.getName(),
                IPerson.class);

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
			if (results != null && results.getGroupResults() != null
					&& results.getGroupResults().length > 0) {

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("found group from key " + key + ": "
							+ results.getGroupResults()[0]);
				}

				wsGroup = results.getGroupResults()[0];
			}
		}

		return wsGroup;
    } 

    /**
     * Get the prefix for the stem containing uPortal groups.  If this value 
     * is non-empty, all groups will be required to be prefixed with the 
     * specified stem name.
     * 
     * @return the uportal stem in the registry, without trailing colon
     */
    protected static String getStemPrefix() {
        
        String uportalStem = GrouperClientUtils.propertiesValue(STEM_PREFIX,
                false);

        // make sure it ends in colon
        if (!StringUtils.isBlank(uportalStem)) {
            if (uportalStem.endsWith(":")) {
                uportalStem = uportalStem
                        .substring(0, uportalStem.length() - 1);
            }
        }

        return uportalStem;
    }

    /**
     * 
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

    /**
     * @see org.jasig.portal.groups.IEntityGroupStore#update(org.jasig.portal.groups.IEntityGroup)
     */
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

    /**
     * @see org.jasig.portal.groups.IEntityGroupStore#updateMembers(org.jasig.portal.groups.IEntityGroup)
     */
    public void updateMembers(IEntityGroup group) throws GroupsException {

        // assume key is fully qualified group name
        String groupName = group.getLocalKey();

        GcAddMember gcAddMember = new GcAddMember().assignGroupName(groupName);

        Iterator<IGroupMember> membersIterator = group.getMembers();

        while (membersIterator != null && membersIterator.hasNext()) {
            IGroupMember iGroupMember = membersIterator.next();
            EntityIdentifier entityIdentifier = iGroupMember
                    .getEntityIdentifier();

            String identifier = entityIdentifier.getKey();

            gcAddMember.addSubjectIdentifier(identifier);
        }
        gcAddMember.execute();

    }

    /**
     * @see org.jasig.portal.groups.IEntityGroupStore#delete(org.jasig.portal.groups.IEntityGroup)
     */
    public void delete(IEntityGroup group) throws GroupsException {

        String groupName = group.getLocalKey();
        new GcGroupDelete().addGroupLookup(new WsGroupLookup(groupName, null))
                .execute();
    }

    /**
     * @see org.jasig.portal.groups.IEntityGroupStore#findLockable(java.lang.String)
     */
    public ILockableEntityGroup findLockable(String key) throws GroupsException {
        throw new UnsupportedOperationException(
            "Group locking is not supported by the Grouper groups service");
    }

    /*
     * @see
     * org.jasig.portal.groups.IEntityGroupStore#newInstance(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public IEntityGroup newInstance(Class entityType) throws GroupsException {
        throw new UnsupportedOperationException(
            "Group updates are not supported by the Grouper groups service");
    }

}
