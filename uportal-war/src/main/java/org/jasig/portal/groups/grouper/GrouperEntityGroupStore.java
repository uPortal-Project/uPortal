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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
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

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

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

	/** Logger. */
	protected final Log LOGGER = LogFactory
			.getLog(GrouperEntityGroupStoreFactory.class);

	/**
	 * Package protected constructor used by the factory method.
	 */
	GrouperEntityGroupStore() { /* Package protected. */
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " created");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.groups.IEntityGroupStore#contains(org.jasig.portal.groups.IEntityGroup, org.jasig.portal.groups.IGroupMember)
	 */
	public boolean contains(IEntityGroup group, IGroupMember member)
			throws GroupsException {
	    // TODO: Original implementation simply returned the existence of 
	    // the member group in the Grouper service.  We need to instead
	    // determine if the parent group contains the member.
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.groups.IEntityGroupStore#find(java.lang.String)
	 */
	public IEntityGroup find(String key) throws GroupsException {


        try {

            // Search the Grouper server for groups with the specified local
            // key
            LOGGER.debug("Searching Grouper for a direct match for key: " + key);
            GcGetGroups getGroups = new GcGetGroups();
            getGroups.addSubjectIdentifier(key);
            WsGetGroupsResults results = getGroups.execute();
            
            // if no results were returned, return null
            if (results == null || results.getResults() == null
                    || results.getResults().length == 0) {
                LOGGER.debug("Grouper service returned no matches for key " + key);
                return null;
            }
            
            // construct a uPortal group representation of the first returned
            // result
            WsSubject subject = results.getResults()[0].getWsSubject();
            IEntityGroup group = new EntityGroupImpl(subject.getName(), IPerson.class);
            
            // TODO: need to set the group name and description to the actual
            // display name and description
            group.setName(subject.getName());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Retrieved group from the Grouper server matching key "
                        + key + ": " + group.toString());
            }
            
            // return the group
            return group;
            

        } catch (Exception e) {
            LOGGER.warn("Exception while attempting to retrieve " 
                    + "group with key " + key + " from Grouper web services", e);
            return null;
        }

	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.groups.IEntityGroupStore#findContainingGroups(org.jasig.portal.groups.IGroupMember)
	 */
	@SuppressWarnings("unchecked")
	public Iterator findContainingGroups(IGroupMember gm)
			throws GroupsException {
	    
		if (gm.isGroup()) {

		    // TODO: need to add support for retrieving the parents of 
		    // groups
			return new LinkedList<IEntityGroup>().iterator();

		} else {

		    GcGetGroups getGroups = new GcGetGroups();

		    // Determine the key to use for this entity.  If the entity is a
		    // group, we should use the group's local key (excluding the 
		    // "grouper." portion of the full key.  If the entity is not a 
		    // group type, just use the key.
	        String key = null;
	        if (gm instanceof IEntityGroup) {
	            key = ((IEntityGroup) gm).getLocalKey();
	        } else {
	            key = gm.getKey();
	        }
            getGroups.addSubjectIdentifier(key);

	        if (LOGGER.isDebugEnabled()) {
	            LOGGER.debug("Searching Grouper for parent groups of the entity with key: "
	                    + key);
	        }

	        try {
	            
	            WsGetGroupsResults results = getGroups.execute();

	            final List<IEntityGroup> parents = new LinkedList<IEntityGroup>();

	            if (results == null || results.getResults() == null || results.getResults().length == 0) {
	                LOGGER.debug("Grouper service returned no matches for key " + key);
	                return parents.iterator();
	            }
	            
	            // add each returned group to the parents list
                for (WsGetGroupsResult wsg : results.getResults()) {
                    if (wsg.getWsGroups() != null) {
                        for (WsGroup g : wsg.getWsGroups()) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.trace("Retrieved group: " + g.getName());
                            }
                            IEntityGroup parent = new EntityGroupImpl(g.getName(), IPerson.class);
                            // TODO: set display name and description
                            parent.setName(g.getName());
                        }
                    }
                }

	            if (LOGGER.isDebugEnabled()) {
	                LOGGER.debug("Retrieved " + parents.size() + " parent groups of entity with key " + key);
	            }

	            return parents.iterator();
	            
	        } catch (Exception e) {
                LOGGER.warn("Exception while attempting to retrieve "
                        + "parents for entity with key " + key
                        + " from Grouper web services", e);
	            return Collections.<IEntityGroup>emptyList().iterator();
	        }

		}
	}

	/*
	 * (non-Javadoc)
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
                
                // TODO: Is there more reliable logic for determining the entity
                // type of a Grouper result?

                // if the member is a person group
                if (gInfo.getName() != null && gInfo.getName().contains(":")) {
                    LOGGER.trace("creating group member: " + gInfo.getName());
                    IEntityGroup member = new EntityGroupImpl(gInfo.getAttributeValue(4), IPerson.class);
                    // TODO: set display name and description
                    member.setName(gInfo.getAttributeValue(4));
                    members.add(member);
                }
                
                // otherwise assume the member is an individual person 
                else {
                    LOGGER.trace("creating leaf member: " + gInfo.getId());
                    IGroupMember member = new EntityImpl(gInfo.getId(), IPerson.class);
                    members.add(member);
                }

            }

            // return an iterator for the assembled group
            return members.iterator();
            
        } catch (Exception e) {
            LOGGER.warn("Exception while attempting to retrieve "
                    + "member entities of group with key " + group.getKey() 
                    + " from Grouper web services", e);
            return Collections.<IGroupMember>emptyList().iterator();
        }

	}

	/*
	 * (non-Javadoc)
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
            
            GcGetGroups getGroups = new GcGetGroups();
            getGroups.addSubjectIdentifier(group.getLocalKey());
            WsGetGroupsResults results = getGroups.execute();
            
            if (results == null || results.getResults() == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("No group-type members found for group with key "
                            + group.getKey());
                }
                return Collections.<IEntityGroup>emptyList().iterator();                
            }
            
            final List<IEntityGroup> members = new ArrayList<IEntityGroup>();
            for (WsGetGroupsResult wsg : results.getResults()) {
                if (wsg.getWsGroups() != null) {
                    for (WsGroup g : wsg.getWsGroups()) {
                        IEntityGroup member = new EntityGroupImpl(g.getName(), IPerson.class);
                        // TODO: set display name and description
                        member.setName(g.getName());
                        members.add(member);
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("found IEntityGroup member: " + member);
                        }
                    }
                }
            }
            
            return members.iterator();
            
        } catch (Exception e) {
            LOGGER.warn("Exception while attempting to retrieve "
                    + "member groups of group with key " + group.getKey() 
                    + " from Grouper web services", e);
            return Collections.<IGroupMember>emptyList().iterator();
        }

	}


	public EntityIdentifier[] searchForGroups(final String query,
			final int method,
			@SuppressWarnings("unchecked") final Class leaftype) {

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
            filter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
            filter.setGroupName(query);
            groupSearch.assignQueryFilter(filter);
            WsFindGroupsResults results = groupSearch.execute();
            
            if (results != null && results.getGroupResults() != null) {
                for (WsGroup g : results.getGroupResults()) {
                    LOGGER.trace("Retrieved group: " + g.getName());
                    groups.add(new EntityIdentifier(g.getName(),
                            IEntityGroup.class));
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
                    + leaftype.getCanonicalName(), e);
            return new EntityIdentifier[] {};
        }

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
	
	
	/*
	 * UNSUPPORTED WRITE OPERATIONS
	 * 
	 * The Grouper group service does not currently support operations that 
	 * require write access or locking.  This implementation may be updated
	 * in the future to include those features.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.groups.IEntityGroupStore#update(org.jasig.portal.groups.IEntityGroup)
	 */
	public void update(IEntityGroup group) throws GroupsException {
	    throw new UnsupportedOperationException(
            "Group updates are not supported by the Grouper groups service");
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.groups.IEntityGroupStore#updateMembers(org.jasig.portal.groups.IEntityGroup)
	 */
	public void updateMembers(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException(
            "Group updates are not supported by the Grouper groups service");
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.groups.IEntityGroupStore#delete(org.jasig.portal.groups.IEntityGroup)
	 */
    public void delete(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException(
                "Group deletion is not supported by the Grouper groups service");
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.groups.IEntityGroupStore#findLockable(java.lang.String)
     */
    public ILockableEntityGroup findLockable(String key) throws GroupsException {
        throw new UnsupportedOperationException(
            "Group locking is not supported by the Grouper groups service");
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.groups.IEntityGroupStore#newInstance(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public IEntityGroup newInstance(Class entityType) throws GroupsException {
        throw new UnsupportedOperationException(
            "Group updates are not supported by the Grouper groups service");
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.groups.IEntityStore#newInstance(java.lang.String)
     */
	public IEntity newInstance(String key) throws GroupsException {
        throw new UnsupportedOperationException(
            "Group updates are not supported by the Grouper groups service");
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.groups.IEntityStore#newInstance(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public IEntity newInstance(String key, Class type) throws GroupsException {
        throw new UnsupportedOperationException(
            "Group updates are not supported by the Grouper groups service");
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.groups.IEntitySearcher#searchForEntities(java.lang.String, int, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public EntityIdentifier[] searchForEntities(String query, int method,
			Class type) throws GroupsException {
        throw new UnsupportedOperationException(
            "Entity search is not supported by the Grouper groups service");
	}
	
}
