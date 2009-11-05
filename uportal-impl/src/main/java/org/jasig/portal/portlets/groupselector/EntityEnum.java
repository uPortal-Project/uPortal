package org.jasig.portal.portlets.groupselector;

import org.jasig.portal.channel.IChannelDefinition;
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
	
	CHANNEL(IChannelDefinition.class, "channel", false), 	// uPortal channel
	CATEGORY(IChannelDefinition.class, "category", true), 	// uPortal channel category
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
		if (CHANNEL.toString().equals(type)) {
			return CHANNEL;
		} else if (CATEGORY.toString().equals(type)) {
			return CATEGORY;
		} else if (PERSON.toString().equals(type)) {
			return PERSON;
		} else if (GROUP.toString().equals(type)) {
			return GROUP;
		}
		return null;
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
