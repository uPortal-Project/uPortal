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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;

/**
 * GroupForm represents a uPortal group instance during group editing.  
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class GroupForm implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String key;
	private String name;
	private String description;
	private String creatorId;
	private String entityType;
	
	private List<JsonEntityBean> members = new ArrayList<JsonEntityBean>();
	
	public GroupForm() { }
	
	public String getKey() {
		return this.key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getType() {
		return this.entityType;
	}

	public void setType(String entityType) {
		this.entityType = entityType;
	}

	public List<JsonEntityBean> getMembers() {
		return this.members;
	}
	
	public void setMembers(List<JsonEntityBean> members) {
		this.members = members;
	}
	
	public void addMember(JsonEntityBean member) {
		this.members.add(member);
	}

	public String getCreatorId() {
		return this.creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}
	
}
