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

package org.jasig.portal.portlet.registry;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("portletCategoryRegistry")
public class PortletCategoryRegistryImpl implements IPortletCategoryRegistry {
	
	private IPortletDefinitionRegistry portletDefinitionRegistry;
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired(required = true)
	public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
		this.portletDefinitionRegistry = portletDefinitionRegistry;
	}

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#addCategoryToCategory(org.jasig.portal.portlet.om.PortletCategory, org.jasig.portal.portlet.om.PortletCategory)
	 */
	@Override
    public void addCategoryToCategory(PortletCategory child, PortletCategory parent) {
        String childKey = String.valueOf(child.getId());
        IEntityGroup childGroup = GroupService.findGroup(childKey);
        String parentKey = String.valueOf(parent.getId());
        IEntityGroup parentGroup = GroupService.findGroup(parentKey);
        parentGroup.addMember(childGroup);
        parentGroup.updateMembers();
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#deletePortletCategory(org.jasig.portal.portlet.om.PortletCategory)
	 */
	@Override
    public void deletePortletCategory(PortletCategory category) {
        String key = String.valueOf(category.getId());
        ILockableEntityGroup categoryGroup = GroupService.findLockableGroup(key,"UP_PORTLET_PUBLISH");
        categoryGroup.delete();
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#getAllChildCategories(org.jasig.portal.portlet.om.PortletCategory)
	 */
	@Override
    public Set<PortletCategory> getAllChildCategories(PortletCategory parent) {
        Set<PortletCategory> rslt = new HashSet<PortletCategory>();

        for (PortletCategory child : getChildCategories(parent)) {
            // recurse
            rslt.add(child);
            rslt.addAll(getAllChildCategories(child));
        }

        return rslt;
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#getAllChildChannels(org.jasig.portal.portlet.om.PortletCategory)
	 */
	@Override
    public Set<IPortletDefinition> getAllChildPortlets(PortletCategory parent) {
        
        Set<IPortletDefinition> rslt = new HashSet<IPortletDefinition>();
        
        try {
            for (IPortletDefinition portlet : getChildPortlets(parent)) {
                rslt.add(portlet);
            }
            for (PortletCategory category : getAllChildCategories(parent)) {
                // append portlets to list for each child category in the tree
                for (IPortletDefinition portlet : getChildPortlets(category)) {
                    rslt.add(portlet);
                }
            }
            
        } catch (Exception e) {
            String msg = "Failed to obtain child portlets for the specified parent '" 
                                    + parent.getName() + "', id=" +parent.getId();
            throw new PortalException(msg, e);
        }

        return rslt;

    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#getPortletCategory(java.lang.String)
	 */
	@Override
    public PortletCategory getPortletCategory(String portletCategoryId) {
        IEntityGroup categoryGroup = GroupService.findGroup(portletCategoryId);
        if (categoryGroup == null) {
            return null;
        }
        PortletCategory category = new PortletCategory(portletCategoryId);
        category.setName(categoryGroup.getName());
        category.setDescription(categoryGroup.getDescription());
        category.setCreatorId(categoryGroup.getCreatorID());
        return category;
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#getChildCategories(org.jasig.portal.portlet.om.PortletCategory)
	 */
    @Override
    public Set<PortletCategory> getChildCategories(PortletCategory parent) {
        String parentKey = String.valueOf(parent.getId());
        IEntityGroup parentGroup = GroupService.findGroup(parentKey);
        Set<PortletCategory> categories = new HashSet<PortletCategory>();
    	@SuppressWarnings("unchecked")
        Iterator<IGroupMember> iter = parentGroup.getMembers();
        while (iter.hasNext()) {
            IGroupMember gm = iter.next();
            if (gm.isGroup()) {
                String categoryId = gm.getKey();
                categories.add(getPortletCategory(categoryId));
            }
        }
        return categories;
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#getChildChannels(org.jasig.portal.portlet.om.PortletCategory)
	 */
    @Override
    public Set<IPortletDefinition> getChildPortlets(PortletCategory parent) {
        String parentKey = String.valueOf(parent.getId());
        IEntityGroup parentGroup = GroupService.findGroup(parentKey);
        Set<IPortletDefinition> portletDefs = new HashSet<IPortletDefinition>();
    	@SuppressWarnings("unchecked")
        Iterator<IGroupMember> iter = parentGroup.getMembers();
        while (iter.hasNext()) {
            IGroupMember gm = iter.next();
            if (gm.isEntity()) {
            	IPortletDefinition portletDefinition = portletDefinitionRegistry.getPortletDefinition(gm.getKey());
            	if(portletDefinition != null) {
            		portletDefs.add(portletDefinition);
            	} else {
            		log.warn("portletDefinition was null for groupMember: " + gm );
            	}   
            }
        }
        return portletDefs;
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#getParentCategories(org.jasig.portal.portlet.om.PortletCategory)
	 */
	@Override
    public Set<PortletCategory> getParentCategories(PortletCategory child) {
        String childKey = String.valueOf(child.getId());
        IEntityGroup childGroup = GroupService.findGroup(childKey);
        Set<PortletCategory> parents = new HashSet<PortletCategory>();

        @SuppressWarnings("unchecked")
        Iterator<IGroupMember> iter = childGroup.getContainingGroups();
        while (iter.hasNext()) {
            IGroupMember gm = iter.next();
            if (gm.isGroup()) {
                String categoryId = gm.getKey();
                parents.add(getPortletCategory(categoryId));
            }
        }
        return parents;
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#getParentCategories(org.jasig.portal.portlet.om.IPortletDefinition)
	 */
	@Override
    public Set<PortletCategory> getParentCategories(IPortletDefinition child) {
        String childKey = child.getPortletDefinitionId().getStringId();
        IEntity childEntity = GroupService.getEntity(childKey, IPortletDefinition.class);
        Set<PortletCategory> parents = new HashSet<PortletCategory>();

        @SuppressWarnings("unchecked")
        Iterator<IGroupMember> iter = childEntity.getContainingGroups();
        while (iter.hasNext()) {
            IGroupMember gm = iter.next();
            if (gm.isGroup()) {
                String categoryId = gm.getKey();
                parents.add(getPortletCategory(categoryId));
            }
        }
        return parents;
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#getTopLevelPortletCategory()
	 */
	@Override
    public PortletCategory getTopLevelPortletCategory() {
        IEntityGroup categoryGroup = GroupService.getDistinguishedGroup(IGroupConstants.PORTLET_CATEGORIES);
        return getPortletCategory(categoryGroup.getKey());
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#createPortletCategory(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
    public PortletCategory createPortletCategory( String name,
            String description, String creatorId ) {
        IEntityGroup categoryGroup = GroupService.newGroup(IPortletDefinition.class);
        categoryGroup.setName( name ); // name cannot be null
        categoryGroup.setCreatorID( creatorId ); // creatorId cannot be null
        categoryGroup.setDescription( description );
        categoryGroup.update();
        String id = categoryGroup.getKey();
        PortletCategory cat = new PortletCategory(id);
        cat.setName( name );
        cat.setDescription( description );
        cat.setCreatorId( creatorId );
        return cat;
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#removeCategoryFromCategory(org.jasig.portal.portlet.om.PortletCategory, org.jasig.portal.portlet.om.PortletCategory)
	 */
	@Override
    public void removeCategoryFromCategory(PortletCategory child, PortletCategory parent) {
        String childKey = String.valueOf(child.getId());
        IEntityGroup childGroup = GroupService.findGroup(childKey);
        String parentKey = String.valueOf(parent.getId());
        IEntityGroup parentGroup = GroupService.findGroup(parentKey);
        parentGroup.removeMember(childGroup);
        parentGroup.updateMembers();
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#updatePortletCategory(org.jasig.portal.portlet.om.PortletCategory)
	 */
	@Override
    public void updatePortletCategory(PortletCategory category) {
        IEntityGroup categoryGroup = GroupService.findGroup(category.getId());
        categoryGroup.setName(category.getName());
        categoryGroup.setDescription(category.getDescription());
        categoryGroup.setCreatorID(category.getCreatorId());
        categoryGroup.update();
    }

}
