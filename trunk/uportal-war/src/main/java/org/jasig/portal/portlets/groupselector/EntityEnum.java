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

package org.jasig.portal.portlets.groupselector;

import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.security.IPerson;

/**
 * EntityEnum represents an enumeration of known uPortal entity types.  This 
 * class was designed specifically for use with the JsonEntityBean entity
 * wrapper and for simplified channel and group browsing.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public enum EntityEnum {
	
	PORTLET(IPortletDefinition.class, "portlet", false), 	// uPortal portlet
	CATEGORY(IPortletDefinition.class, "category", true), 	// uPortal portlet category
	PERSON(IPerson.class, "person", false), 				// uPortal person
	GROUP(IPerson.class, "group", true);					// uPortal person group
	
	private Class<?> clazz;
	private String stringValue;
	private boolean isGroup;
	
	private EntityEnum(Class<?> clazz, String stringValue, boolean isGroup) {
		this.clazz = clazz;
		this.stringValue = stringValue;
		this.isGroup = isGroup;
	}
	
	/**
	 * Get an EntityEnum for a String type representation.
	 * 
	 * @param type
	 * @return
	 */
	public static EntityEnum getEntityEnum(String type) {
		if (PORTLET.toString().equalsIgnoreCase(type)) {
			return PORTLET;
		} else if (CATEGORY.toString().equalsIgnoreCase(type)) {
			return CATEGORY;
		} else if (PERSON.toString().equalsIgnoreCase(type)) {
			return PERSON;
		} else if (GROUP.toString().equalsIgnoreCase(type)) {
			return GROUP;
		}
		return null;
	}
	
	public static EntityEnum getEntityEnum(Class<?> entityTypeClazz, boolean isGroup) {
	    EntityEnum rslt = null;
	    for (EntityEnum val : EntityEnum.values()) {
	        if (val.getClazz().equals(entityTypeClazz) && val.isGroup() == isGroup) {
	            rslt = val;
	            break;
	        }
	    }
	    return rslt;
	}
	
	/**
	 * Get the Class associated with this entity type.
	 * 
	 * @return
	 */
	public Class<?> getClazz() {
		return this.clazz;
	}
	
	/**
	 * Determine if this entity type is a group type.  It should be noted that 
	 * method returns the general "group"-iness of the entity, not whether this
	 * entity is specifically a person group.
	 * 
	 * @return
	 */
	public boolean isGroup() {
		return this.isGroup;
	}
	
	
	@Override
	public String toString() {
		return this.stringValue;
	}

}
