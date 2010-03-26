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
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.portlets.groupselector.EntityEnum;
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
		form.setType(groupListHelper.getEntityType(group));
		
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
	 * @param key
	 */
	public void deleteGroup(String key) {

		log.debug("Deleting group with key " + key);

		// find the current version of this group entity
		IEntityGroup group = GroupService.findGroup(key);
		
		// delete the group
		group.delete();
		
	}
	
	/**
	 * Update an existing group in the group store, or if the key is
	 * null, create a new group.
	 * 
	 * @param groupForm
	 */
	public void updateGroup(GroupForm groupForm) {

		if (log.isDebugEnabled()) {
			log.debug("Updating group for group form [" + groupForm.toString() + "]");
		}

		// find the current version of this group entity
		IEntityGroup group = GroupService.findGroup(groupForm.getKey());
		group.setName(groupForm.getName());
		group.setDescription(groupForm.getDescription());
		
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
			EntityEnum type = EntityEnum.getEntityEnum(child.getEntityType());
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
		group.update();

	}
	
	/**
	 * Create a new group under the specified parent.
	 * 
	 * @param groupForm
	 * @param parent
	 */
	public void createGroup(GroupForm groupForm, JsonEntityBean parent, String creatorId) {
		
		if (log.isDebugEnabled()) {
			log.debug("Creating new group for group form ["
					+ groupForm.toString() + "] and parent ["
					+ parent.toString() + "]");
		}

		// get the entity type of the parent group
		EntityEnum type = EntityEnum.getEntityEnum(parent.getEntityType());
		
		// create a new group with the parent's entity type
		IEntityGroup group = GroupService.newGroup(type.getClazz());
		
		// find the current version of this group entity
		group.setCreatorID(creatorId);
		group.setName(groupForm.getName());
		group.setDescription(groupForm.getDescription());
		
		// add all the group membership information from the group form
		// to the group
		for (JsonEntityBean child : groupForm.getMembers()) {
			IGroupMember member = GroupService.findGroup(child.getId());
			group.addMember(member);
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
	
}
