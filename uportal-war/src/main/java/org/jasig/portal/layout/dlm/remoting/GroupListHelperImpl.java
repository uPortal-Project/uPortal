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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityNameFinder;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.EntityNameFinderService;
import org.jasig.portal.services.GroupService;

public class GroupListHelperImpl implements IGroupListHelper {

    private static final String PRINCIPAL_SEPARATOR = "\\.";

	private static final Log log = LogFactory.getLog(GroupListHelperImpl.class);
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.layout.dlm.remoting.IGroupListHelper#search(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Set<JsonEntityBean> search(String entityType, String searchTerm) {
		
		Set<JsonEntityBean> results = new HashSet<JsonEntityBean>();

		EntityEnum entityEnum = EntityEnum.getEntityEnum(entityType);

		EntityIdentifier[] identifiers;
		
		Class identifierType;
		
		// if the entity type is a group, use the group service's findGroup method
		// to locate it
		if (entityEnum.isGroup()) {
			identifiers = GroupService.searchForGroups(searchTerm, GroupService.CONTAINS, 
					entityEnum.getClazz());
			identifierType = IEntityGroup.class;
		} 
		
		// otherwise use the getGroupMember method
		else {
			identifiers = GroupService.searchForEntities(searchTerm, GroupService.CONTAINS,
					entityEnum.getClazz());
			identifierType = entityEnum.getClazz();
		}
		
		for(int i=0;i<identifiers.length;i++) {
			if(identifiers[i].getType().equals(identifierType)) {
				IGroupMember entity = GroupService.getGroupMember(identifiers[i]);
                JsonEntityBean jsonBean = getEntity(entity);
                results.add(jsonBean);
			}
		}
		
		return results;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.layout.dlm.remoting.IGroupListHelper#getRootEntity(java.lang.String)
	 */
	public JsonEntityBean getRootEntity(String groupType) {
		
	    EntityEnum type = EntityEnum.getEntityEnum(groupType);
	    
		String rootKey;
		if (EntityEnum.GROUP.equals(type)) {
			rootKey = "local.0";
		} else if (EntityEnum.CATEGORY.equals(type)) {
	        IEntityGroup categoryGroup = GroupService.getDistinguishedGroup(IGroupConstants.PORTLET_CATEGORIES);
	        return new JsonEntityBean(categoryGroup, EntityEnum.CATEGORY);
		} else {
			throw new IllegalArgumentException("Unable to determine a root entity for group type '" + groupType + "'");
		}
		
		JsonEntityBean bean = getEntity(groupType, rootKey, false);
		
		return bean;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.layout.dlm.remoting.IGroupListHelper#getEntityTypesForGroupType(java.lang.String)
	 */
	public Set<String> getEntityTypesForGroupType(String groupType) {

		// add the group type itself to the allowed list
		Set<String> set = new HashSet<String>();
		set.add(groupType);
		
		/*
		 * If the supplied type is a person group, add the person entity type.
		 * If the supplied type is a category, add the channel type.  Otherwise,
		 * throw an exception.
		 * 
		 * This method will require an update if more entity types are added
		 * in the future.
		 */
		
		EntityEnum type = EntityEnum.getEntityEnum(groupType);
		if (EntityEnum.GROUP.equals(type)) {
			set.add(EntityEnum.PERSON.toString());
		} else if (EntityEnum.CATEGORY.equals(type)) {
			set.add(EntityEnum.PORTLET.toString());
		} else {
			throw new IllegalArgumentException("Unable to determine a root entity for group type '" + groupType + "'");
		}
		
		return set;

	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.layout.dlm.remoting.IGroupListHelper#getEntity(java.lang.String, java.lang.String, boolean)
	 */
	public JsonEntityBean getEntity(String entityType, String entityId, boolean populateChildren) {

		// get the EntityEnum for the specified entity type
		EntityEnum entityEnum = EntityEnum.getEntityEnum(entityType);
		
		// if the entity type is a group, use the group service's findGroup method
		// to locate it
		if(entityEnum.isGroup()) {
			// attempt to find the entity
			IEntityGroup entity = GroupService.findGroup(entityId);
			if(entity == null) {
				return null;
			} else {
				JsonEntityBean jsonBean = new JsonEntityBean(entity, entityEnum);
				if (populateChildren) {
					@SuppressWarnings("unchecked")
					Iterator<IGroupMember> members = (Iterator<IGroupMember>) entity.getMembers();
					jsonBean = populateChildren(jsonBean, members);
				}
                if (jsonBean.getEntityType().isGroup() || EntityEnum.PERSON.equals(jsonBean.getEntityType())) {
                    IAuthorizationPrincipal principal = getPrincipalForEntity(jsonBean);
                    jsonBean.setPrincipalString(principal.getPrincipalString());
                }
				return jsonBean;
			}
		} 
		
		// otherwise use the getGroupMember method
		else {
			IGroupMember entity = GroupService.getGroupMember(entityId, entityEnum.getClazz());
			if(entity == null || entity instanceof IEntityGroup) {
				return null;
			}
			JsonEntityBean jsonBean = new JsonEntityBean(entity, entityEnum);
			
			// the group member interface doesn't include the entity name, so
			// we'll need to look that up manually
			jsonBean.setName(lookupEntityName(jsonBean));
            if (EntityEnum.GROUP.equals(jsonBean.getEntityType()) || EntityEnum.PERSON.equals(jsonBean.getEntityType())) {
                IAuthorizationPrincipal principal = getPrincipalForEntity(jsonBean);
                jsonBean.setPrincipalString(principal.getPrincipalString());
            }
			return jsonBean;
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.layout.dlm.remoting.IGroupListHelper#getEntity(org.jasig.portal.groups.IGroupMember)
	 */
	public JsonEntityBean getEntity(IGroupMember member) {

		// get the type of this member entity
		EntityEnum entityEnum = getEntityType(member);
		
		// construct a new entity bean for this entity
		JsonEntityBean entity;
		if (entityEnum.isGroup()) {
			entity = new JsonEntityBean((IEntityGroup) member, entityEnum);
		} else {
			entity = new JsonEntityBean(member, entityEnum);
		}
		
		// if the name hasn't been set yet, look up the entity name
		if (entity.getName() == null) {
			entity.setName(lookupEntityName(entity));
		}
        
        if (EntityEnum.GROUP.equals(entity.getEntityType()) || EntityEnum.PERSON.equals(entity.getEntityType())) {
            IAuthorizationPrincipal principal = getPrincipalForEntity(entity);
            entity.setPrincipalString(principal.getPrincipalString());
        }
		return entity;
	}
	
    public JsonEntityBean getEntityForPrincipal(String principalString) {
        
        // split the principal string into its type and key components
        String[] parts = principalString.split(PRINCIPAL_SEPARATOR, 2);        
        String key = parts[1];
        int typeId = Integer.parseInt(parts[0]);
        
        // get the EntityEnum type for the entity id number
        @SuppressWarnings("unchecked")
        Class type = EntityTypes.getEntityType(typeId);
        String entityType = "person";
        if (IEntityGroup.class.isAssignableFrom(type)) {
            entityType = "group";
        }

        // get the JsonEntityBean for this type and key
        JsonEntityBean bean = getEntity(entityType, key, false);
        return bean;
    }
    
    public IAuthorizationPrincipal getPrincipalForEntity(JsonEntityBean entity) {
        
        // attempt to determine the entity type class for this principal
        Class entityType;
        EntityEnum jsonType = entity.getEntityType(); 
        if (jsonType.isGroup()) {
            entityType = IEntityGroup.class;
        } else {
            entityType = jsonType.getClazz();
        }
        
        // construct an authorization principal for this JsonEntityBean
        AuthorizationService authService = AuthorizationService.instance();
        IAuthorizationPrincipal p = authService.newPrincipal(entity.getId(), entityType);
        return p;
    }

	
	/**
	 * <p>Populates the children of the JsonEntityBean.  Creates new
	 * JsonEntityBeans for the known types (person, group, or category), and
	 * adds them as children to the current bean.</p> 
	 * @param jsonBean Entity bean to which the children are added
	 * @param children An Iterator containing IGroupMember elements.  Usually
	 * obtained from entity.getMembers().
	 * @return jsonBean with the children populated
	 */
	private JsonEntityBean populateChildren(JsonEntityBean jsonBean, Iterator<IGroupMember> children) {
		
		while(children.hasNext()) {
			
			IGroupMember member = children.next();
			
			// add the entity bean to the list of children
			JsonEntityBean jsonChild = getEntity(member);
			jsonBean.addChild(jsonChild);
		}
		
		// mark this entity bean as having had it's child list initialized
		jsonBean.setChildrenInitialized(true);

		return jsonBean;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.layout.dlm.remoting.IGroupListHelper#getEntityType(org.jasig.portal.groups.IGroupMember)
	 */
	public EntityEnum getEntityType(IGroupMember entity) {
		
	    if (IEntityGroup.class.isAssignableFrom(entity.getClass())) {
	        return EntityEnum.getEntityEnum(entity.getEntityType(), true);
	    } 
	    
	    else {
            return EntityEnum.getEntityEnum(entity.getEntityType(), false);
	    }
	    
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlets.groupselector.GroupsSelectorHelper#getEntityBeans(java.util.List)
	 */
	public List<JsonEntityBean> getEntityBeans(List<String> params) {
	    // if no parameters have been supplied, just return an empty list
	    if (params == null || params.isEmpty()) {
	        return Collections.<JsonEntityBean>emptyList();
	    }
	    
		List<JsonEntityBean> beans = new ArrayList<JsonEntityBean>();
		for (String param : params) {
			String[] parts = param.split(":", 2);
			JsonEntityBean member = getEntity(parts[0], parts[1], false);
			beans.add(member);
		}
		return beans;
	}

	/**
	 * <p>Convenience method that looks up the name of the given group member.
	 * Used for person types.</p>
	 * @param groupMember Entity to look up
	 * @return groupMember's name or null if there's an error
	 */
	public String lookupEntityName(JsonEntityBean entity) {
		
		EntityEnum entityEnum = entity.getEntityType();
		IEntityNameFinder finder;
		if (entityEnum.isGroup()) {
			finder = EntityNameFinderService.instance()
				.getNameFinder(IEntityGroup.class);
		} else {
			finder = EntityNameFinderService.instance()
				.getNameFinder(entityEnum.getClazz());
		}
		
		try {
			return finder.getName(entity.getId());
		} catch (Exception e) {
			/* An exception here isn't the end of the world.  Just log it
			   and return null. */
			log.warn("Couldn't find name for entity " + entity.getId(), e);
			return null;
		}
	}
	
}
