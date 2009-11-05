/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.dlm.remoting;

import java.util.List;
import java.util.Set;

import org.jasig.portal.groups.IGroupMember;

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
	 * Return the string representation of the type of a specified entity object.
	 * 
	 * @param entity	Entity whose type needs to be determined
	 * @return			One of the possible EntityEnum string representations
	 */
	public String getEntityType(IGroupMember entity); 
	
	/**
	 * Find the name of a specified entity.
	 * 
	 * @param entityBean	JsonEntityBean representation of an entity
	 * @return				Entity name, or <code>null</code> if none is found
	 */
	public String lookupEntityName(JsonEntityBean entityBean);
	
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

}
