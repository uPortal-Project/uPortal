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

package org.jasig.portal.layout.dlm.remoting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.ChannelCategory;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;

/**
 * <p>Entity bean for JSON output.  Used for categories, groups, and people.</p>
 * 
 * @author Drew Mazurek
 */
@SuppressWarnings("unchecked")
public class JsonEntityBean implements Serializable {

	public static final String ENTITY_CATEGORY = "category";
	public static final String ENTITY_CHANNEL = "channel";
	public static final String ENTITY_GROUP = "group";
	public static final String ENTITY_PERSON = "person";
	
	private String entityType;
	private String id;
	private String name;
	private String creatorId;
	private String description;
	private List children = new ArrayList();
	private boolean childrenInitialized = false;
		
	public JsonEntityBean() { }
	
	public JsonEntityBean(ChannelCategory category) {
		
		this.entityType = ENTITY_CATEGORY;
		this.id = category.getId();
		this.name = category.getName();
		this.creatorId = category.getCreatorId();
		this.description = category.getDescription();
	}
	
	public JsonEntityBean(IGroupMember groupMember, String entityType) {
		
		this.entityType = entityType;
		this.id = groupMember.getKey();
	}

	public JsonEntityBean(IEntityGroup entityGroup, String entityType) {
		
		this.entityType = entityType;
		this.id = entityGroup.getKey();
		this.name = entityGroup.getName();
		this.creatorId = entityGroup.getCreatorID();
		this.description = entityGroup.getDescription();
	}
	
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List getChildren() {
		return children;
	}
	public void setChildren(List children) {
		this.children = children;
	}
	
	/**
	 * <p>Convenience method to add a child to this object's list of
	 * children.</p>
	 * @param child Object to add
	 */
	public void addChild(Object child) {
		children.add(child);
	}
	
	public boolean isChildrenInitialized() {
		return childrenInitialized;
	}
	public void setChildrenInitialized(boolean childrenInitialized) {
		this.childrenInitialized = childrenInitialized;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (childrenInitialized ? 1231 : 1237);
		result = prime * result
				+ ((creatorId == null) ? 0 : creatorId.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((entityType == null) ? 0 : entityType.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JsonEntityBean other = (JsonEntityBean) obj;
		if (childrenInitialized != other.childrenInitialized)
			return false;
		if (creatorId == null) {
			if (other.creatorId != null)
				return false;
		} else if (!creatorId.equals(other.creatorId))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (entityType == null) {
			if (other.entityType != null)
				return false;
		} else if (!entityType.equals(other.entityType))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
