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

package org.jasig.portal.portlets.groupadmin;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.RuntimeAuthorizationException;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * GroupAdministrationHelper provides helper groups for the groups
 * administration webflows.  These methods include convenience 
 * methods for populating and editing form objects, as well
 * as saving information supplied to a group form.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Service
public class GroupAdministrationHelper {
	
	public static final String GROUPS_OWNER = "UP_GROUPS";
	public static final String CREATE_PERMISSION = "CREATE_GROUP";
	public static final String DELETE_PERMISSION = "DELETE_GROUP";
	public static final String EDIT_PERMISSION = "EDIT_GROUP";
	public static final String VIEW_PERMISSION = "VIEW_GROUP";
	
	protected final Log log = LogFactory.getLog(getClass()); 

	private IGroupListHelper groupListHelper;
	
	@Autowired(required = true)
	public void setGroupListHelper(IGroupListHelper groupListHelper) {
		this.groupListHelper = groupListHelper;
	}

	/**
	 * Construct a group form for the group with the specified
	 * key.
	 * 
	 * @param key
	 * @param entityEnum
	 * @return
	 */
	public GroupForm getGroupForm(String key) {
		
		log.debug("Initializing group form for group key " + key);

		// find the current version of this group entity
		IEntityGroup group = GroupService.findGroup(key);
		
		// update the group form with the existing group's main information
		GroupForm form = new GroupForm();
		form.setKey(key);
		form.setName(group.getName());
		form.setDescription(group.getDescription());
		form.setCreatorId(group.getCreatorID());
		form.setType(groupListHelper.getEntityType(group).toString());
		
		// add child groups to our group form bean
		@SuppressWarnings("unchecked")
		Iterator<IGroupMember> groupIter = (Iterator<IGroupMember>) group.getMembers();
		while (groupIter.hasNext()) {
			IGroupMember child = groupIter.next();
			JsonEntityBean childBean = groupListHelper.getEntity(child);
			form.addMember(childBean);
		}
		
		return form;
	}
	
	/**
	 * Delete a group from the group store
	 * 
	 * @param key key of the group to be deleted
	 * @param user performing the delete operation
	 */
	public void deleteGroup(String key, IPerson deleter) {

        if (!canDeleteGroup(deleter, key)) {
            throw new RuntimeAuthorizationException(deleter, DELETE_PERMISSION, key);
        }
		
		log.info("Deleting group with key " + key);

		// find the current version of this group entity
		IEntityGroup group = GroupService.findGroup(key);
		
		// remove this group from the membership list of any current parent
		// groups
		@SuppressWarnings("unchecked")
		Iterator<IEntityGroup> iter = (Iterator<IEntityGroup>) group.getContainingGroups();
		while (iter.hasNext()) {
			IEntityGroup parent = (IEntityGroup) iter.next();
			parent.removeMember(group);
			parent.updateMembers();
		}
		
		// delete the group
		group.delete();
		
	}
	
	/**
	 * Update the title and description of an existing group in the group store.
	 * 
	 * @param groupForm Form representing the new group configuration
	 * @param updater Updating user
	 */
	public void updateGroupDetails(GroupForm groupForm, IPerson updater) {

        if (!canEditGroup(updater, groupForm.getKey())) {
			throw new RuntimeAuthorizationException(updater, EDIT_PERMISSION, groupForm.getKey());
        }

		if (log.isDebugEnabled()) {
			log.debug("Updating group for group form [" + groupForm.toString() + "]");
		}
		
		// find the current version of this group entity
		IEntityGroup group = GroupService.findGroup(groupForm.getKey());
		group.setName(groupForm.getName());
		group.setDescription(groupForm.getDescription());
		
		// save the group, updating both its basic information and group
		// membership
		group.update();

	}

	/**
	 * Update the members of an existing group in the group store.
	 * 
	 * @param groupForm Form representing the new group configuration
	 * @param updater   Updating user
	 */
	public void updateGroupMembers(GroupForm groupForm, IPerson updater) {

        if (!canEditGroup(updater, groupForm.getKey())) {
            throw new RuntimeAuthorizationException(updater, EDIT_PERMISSION, groupForm.getKey());
        }

		if (log.isDebugEnabled()) {
			log.debug("Updating group members for group form [" + groupForm.toString() + "]");
		}

		// find the current version of this group entity
		IEntityGroup group = GroupService.findGroup(groupForm.getKey());
		
		// clear the current group membership list
		@SuppressWarnings("unchecked")
		Iterator<IGroupMember> groupIter = (Iterator<IGroupMember>) group.getMembers();
		while (groupIter.hasNext()) {
			IGroupMember child = groupIter.next();
			group.removeMember(child);
		}
		
		// add all the group membership information from the group form
		// to the group
		for (JsonEntityBean child : groupForm.getMembers()) {
			EntityEnum type = EntityEnum.getEntityEnum(child.getEntityTypeAsString());
			if (type.isGroup()) {
				IEntityGroup member = GroupService.findGroup(child.getId());
				group.addMember(member);
			} else {
				IGroupMember member = GroupService.getGroupMember(child.getId(), type.getClazz());
				group.addMember(member);
			}
		}
		
		// save the group, updating both its basic information and group
		// membership
		group.updateMembers();

	}
	
	/**
	 * Create a new group under the specified parent.  The new group will 
	 * automatically be added to the parent group.
	 * 
	 * @param groupForm		form object representing the new group
	 * @param parent		parent group for this new group
	 * @param creator		the uPortal user creating the new group
	 */
	public void createGroup(GroupForm groupForm, JsonEntityBean parent, IPerson creator) {
		
        if (!canCreateMemberGroup(creator, parent.getId())) {
            throw new RuntimeAuthorizationException(creator, CREATE_PERMISSION, groupForm.getKey());
        }

		if (log.isDebugEnabled()) {
			log.debug("Creating new group for group form ["
					+ groupForm.toString() + "] and parent ["
					+ parent.toString() + "]");
		}

		// get the entity type of the parent group
		EntityEnum type = EntityEnum.getEntityEnum(groupForm.getType());
		
		// create a new group with the parent's entity type
		IEntityGroup group = GroupService.newGroup(type.getClazz());
		
		// find the current version of this group entity
		group.setCreatorID(creator.getUserName());
		group.setName(groupForm.getName());
		group.setDescription(groupForm.getDescription());
		
		// add all the group membership information from the group form
		// to the group
		for (JsonEntityBean child : groupForm.getMembers()) {
			EntityEnum childType = EntityEnum.getEntityEnum(child.getEntityTypeAsString());
			if (childType.isGroup()) {
				IEntityGroup member = GroupService.findGroup(child.getId());
				group.addMember(member);
			} else {
				IGroupMember member = GroupService.getGroupMember(child.getId(), type.getClazz());
				group.addMember(member);
			}
		}
		
		// save the group, updating both its basic information and group
		// membership
		group.update();
		
		// add this group to the membership list for the specified
		// parent
		IEntityGroup parentGroup = GroupService.findGroup(parent.getId());
		parentGroup.addMember(group);
		parentGroup.updateMembers();

	}

    public boolean canEditGroup(IPerson currentUser, String target) {
        
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(GROUPS_OWNER, EDIT_PERMISSION, target));
    }
    
    public boolean canDeleteGroup(IPerson currentUser, String target) {
        
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(GROUPS_OWNER, DELETE_PERMISSION, target));
    }
    
    public boolean canCreateMemberGroup(IPerson currentUser, String target) {
        
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(GROUPS_OWNER, CREATE_PERMISSION, target));
    }
    
    public boolean canViewGroup(IPerson currentUser, String target) {
        
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(GROUPS_OWNER, VIEW_PERMISSION, target));
    }
    
    /**
     * Get the authoriztaion principal matching the supplied IPerson.
     * 
     * @param person
     * @return
     */
    protected IAuthorizationPrincipal getPrincipalForUser(final IPerson person) {
        final EntityIdentifier ei = person.getEntityIdentifier();
        return AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
    }

}
