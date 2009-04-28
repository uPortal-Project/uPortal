package org.jasig.portal.layout.dlm.remoting;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityNameFinder;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.EntityNameFinderService;
import org.jasig.portal.services.GroupService;

public class GroupListHelperImpl implements IGroupListHelper {

	private static final Log log = LogFactory.getLog(GroupListController.class);

	// Inherit Javadoc
	@SuppressWarnings("unchecked")
	public Set<JsonEntityBean> search(String entityType, String entityId,
			String searchTerm) {
		
		Set<JsonEntityBean> results = new HashSet<JsonEntityBean>();

		Class clazz;

		if(JsonEntityBean.ENTITY_CATEGORY.equals(entityType)) {
			clazz = ChannelDefinition.class;
		} else if(JsonEntityBean.ENTITY_PERSON.equals(entityType)) {
			clazz = IPerson.class;
		} else if(JsonEntityBean.ENTITY_GROUP.equals(entityType)) {
			clazz = IPerson.class;
		} else {
			throw new IllegalArgumentException("Unknown entity type " + entityType);
		}

		/* No entity ID or search term.  Return root plus one level
		   beneath. */
		if(entityId == null && searchTerm == null) {

			IEntityGroup rootGroup = GroupService.getRootGroup(clazz);
			JsonEntityBean jsonBean = new JsonEntityBean(rootGroup, getEntityType(rootGroup));
			
			/* We can't differentiate between channels and categories, so if
			   the user wants categories, just return the categories.  For
			   "person" or "group" searches, only return groups if the user
			   wants groups.  If the user wants persons, return both groups
			   and persons. */
			if(JsonEntityBean.ENTITY_CATEGORY.equals(entityType) 
					|| JsonEntityBean.ENTITY_GROUP.equals(entityType)) {
				jsonBean = populateChildren(jsonBean, rootGroup.getMembers());
			} else {
				jsonBean = populateChildren(jsonBean, rootGroup.getAllMembers());
			}

			results.add(jsonBean);
			
		} else if(entityId != null) {
			
			if(JsonEntityBean.ENTITY_PERSON.equals(entityType)) {
				IGroupMember person = GroupService.getGroupMember(entityId, clazz);
				if(person == null || person instanceof IEntityGroup) {
					return results;
				}
				JsonEntityBean jsonBean = new JsonEntityBean(person, getEntityType(person));
				jsonBean.setName(lookupEntityName(person));
				results.add(jsonBean);
			} else {
				IEntityGroup entity = GroupService.findGroup(entityId);
				if(entity == null) {
					return results;
				} else {
					JsonEntityBean jsonBean = new JsonEntityBean(entity, getEntityType(entity));
					/* See comment above. */
					if(JsonEntityBean.ENTITY_CATEGORY.equals(entityType) 
							|| JsonEntityBean.ENTITY_GROUP.equals(entityType)) {
						jsonBean = populateChildren(jsonBean, entity.getMembers());
					} else {
						jsonBean = populateChildren(jsonBean, entity.getAllMembers());
					}
					results.add(jsonBean);
				}
			}

		} else if(searchTerm != null) {

			EntityIdentifier[] identifiers;
			
			Class identifierType;
			if(JsonEntityBean.ENTITY_PERSON.equals(entityType)) {
				identifiers = GroupService.searchForEntities(searchTerm, GroupService.CONTAINS,
						clazz);
				identifierType = IPerson.class;
			} else {
				identifiers = GroupService.searchForGroups(searchTerm, GroupService.CONTAINS, 
						clazz);
				identifierType = IEntityGroup.class;
			}
			
			for(int i=0;i<identifiers.length;i++) {
				if(identifiers[i].getType().equals(identifierType)) {
					IGroupMember entity = GroupService.getGroupMember(identifiers[i]);
					if(entity instanceof IEntityGroup && !JsonEntityBean.ENTITY_PERSON.equals(entityType)) {
						/* Don't look up the children for a search. */
						JsonEntityBean jsonBean = new JsonEntityBean((IEntityGroup)entity, getEntityType(entity));
						results.add(jsonBean);
					} else if (JsonEntityBean.ENTITY_PERSON.equals(entityType)){
						JsonEntityBean jsonBean = new JsonEntityBean(entity, getEntityType(entity));
						jsonBean.setName(lookupEntityName(entity));
						results.add(jsonBean);
					}
				}
			}
		}
		
		return results;
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
	@SuppressWarnings("unchecked")
	private JsonEntityBean populateChildren(JsonEntityBean jsonBean, Iterator children) {
		
		/* Populate the children only if we're a group or a category. */
		if(!JsonEntityBean.ENTITY_GROUP.equals(jsonBean.getEntityType()) 
				&& !JsonEntityBean.ENTITY_CATEGORY.equals(jsonBean.getEntityType())) {
			return jsonBean;
		}

		while(children.hasNext()) {
			IGroupMember member = (IGroupMember) children.next();		
			if(member instanceof IEntityGroup) {
				/* It's either a group or a category. */
				if(member.getEntityType().equals(IPerson.class)) {
					jsonBean.addChild(new JsonEntityBean(
							(IEntityGroup) member,
							JsonEntityBean.ENTITY_GROUP));
				} else if(member.getEntityType().equals(ChannelDefinition.class)) {
					jsonBean.addChild(new JsonEntityBean(
							(IEntityGroup) member,
							JsonEntityBean.ENTITY_CATEGORY));
				} else {
					/* Don't know what it is... skip it. */
					continue;
				}
			} else if(member.getEntityType().equals(IPerson.class)) {
				JsonEntityBean child = new JsonEntityBean(member,
						JsonEntityBean.ENTITY_PERSON);
				child.setName(lookupEntityName(member));
				jsonBean.addChild(child);
			}
		}
		
		jsonBean.setChildrenInitialized(true);

		return jsonBean;
	}
	
	/**
	 * <p>Tries to determine the JsonEntityBean entity type based on the
	 * entity passed in.</p>
	 * @param entity Entity whose type needs to be determined
	 * @return One of JsonEntityBean.ENTITY_GROUP, ENTITY_PERSON, or
	 * ENTITY_CATEGORY.
	 */
	private String getEntityType(IGroupMember entity) {
		
		if(entity.getEntityType().equals(IPerson.class)) {
			if(entity instanceof IEntityGroup) {
				return JsonEntityBean.ENTITY_GROUP;
			} else {
				return JsonEntityBean.ENTITY_PERSON;
			}
		} else if(entity.getEntityType().equals(ChannelDefinition.class)) {
			return JsonEntityBean.ENTITY_CATEGORY;
		} else {
			/* Don't know what it is. */
			return null;
		}
	}
	
	/**
	 * <p>Convenience method that looks up the name of the given group member.
	 * Used for person types.</p>
	 * @param groupMember Entity to look up
	 * @return groupMember's name or null if there's an error
	 */
	private String lookupEntityName(IGroupMember groupMember) {
		
		try {
			IEntityNameFinder finder = EntityNameFinderService.instance()
				.getNameFinder(groupMember.getEntityType());
			return(finder.getName(groupMember.getKey()));
		} catch(Exception ex) {
			/* An exception here isn't the end of the world.  Just log it
			   and return null. */
			log.warn("Couldn't find name for entity " + groupMember.getKey(),ex);
			return null;
		}
	}
}
