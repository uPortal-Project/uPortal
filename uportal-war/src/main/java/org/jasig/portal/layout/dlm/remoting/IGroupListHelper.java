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

import java.util.List;
import java.util.Set;

import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.security.IAuthorizationPrincipal;

/**
 * Helper methods for retrieving portal entities.
 * 
 * @author Drew Mazurek
 * @author Jen Bourey
 * @revision $Revision$
 */
public interface IGroupListHelper {

	/**
	 * Search for entities of a specified entity type using a search string.
	 * 
	 * @param entityType	entity type to search for
	 * @param searchTerm	search string
	 * @return				set of matching JsonEntityBeans
	 */
	public Set<JsonEntityBean> search(String entityType, String searchTerm);

	/**
	 * Get the root entity for a particular type of group entity.
	 * 
	 * @param groupType
	 * @return
	 */
	public JsonEntityBean getRootEntity(String groupType);
	
	/**
	 * Get the set of entity types allowed as children of the specified group
	 * type.
	 * 
	 * @param groupType
	 * @return
	 */
	public Set<String> getEntityTypesForGroupType(String groupType);

	/**
	 * Return the string representation of the type of a specified entity object.
	 * 
	 * @param entity	Entity whose type needs to be determined
	 * @return			One of the possible EntityEnum string representations
	 */
	public EntityEnum getEntityType(IGroupMember entity); 
	
	/**
	 * Find the name of a specified entity.
	 * 
	 * @param entityBean	JsonEntityBean representation of an entity
	 * @return				Entity name, or <code>null</code> if none is found
	 */
	public String lookupEntityName(JsonEntityBean entityBean);

	/**
	 * Return a JsonEntityBean for the supplied IGroupMember instance.
	 * 
	 * @param member
	 * @return
	 */
	public JsonEntityBean getEntity(IGroupMember member);

	/**
	 * Retrieve an individual entity matching the specified type and id.  If
	 * populateChildren is set to <code>true</code> populate the entity bean
	 * with the entity's children before returning it.  This argument will be
	 * ignored entirely if the entity is not of a group type, since non-group
	 * entities may not have children.
	 * 
	 * @param entityType		type of the entity to be returned
	 * @param entityId			ID of the entity to be returned
	 * @param populateChildren	<code>true</code> to populate the bean with children
	 * @return					JsonEntityBean representation or <code>null</code>
	 */
	public JsonEntityBean getEntity(String entityType, String entityId, boolean populateChildren);
	
	/**
	 * Get a list of JsonEntityBeans for a supplied list of string identifiers, 
	 * where an identifier consists of a colon-separated pairing of entity type
	 * and entity ID.  For example, a Person Group entity with they key local.0
	 * would be indicated by the identifier string "group:local.0".
	 * 
	 * @param params	List of string identifiers
	 * @return			List of matching JsonEntityBeans 
	 */
	public List<JsonEntityBean> getEntityBeans(List<String> params);
	
    public JsonEntityBean getEntityForPrincipal(String principalString);

    public IAuthorizationPrincipal getPrincipalForEntity(JsonEntityBean entity);

}
