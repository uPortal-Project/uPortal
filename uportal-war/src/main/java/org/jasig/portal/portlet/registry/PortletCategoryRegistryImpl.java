/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlet.registry;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
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
     * @see org.jasig.portal.portlet.registry.IPortletCategoryRegistry#getAllParentCategories(org.jasig.portal.portlet.om.PortletCategory)
     */
    @Override
    public Set<PortletCategory> getAllParentCategories(PortletCategory child) {
        Set<PortletCategory> rslt = new HashSet<PortletCategory>();
        for (PortletCategory parent : getParentCategories(child)) {
            // recurse
            rslt.add(parent);
            rslt.addAll(getAllParentCategories(parent));
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
        for (IGroupMember gm : parentGroup.getChildren()) {
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
        for (IGroupMember gm : parentGroup.getChildren()) {
            if (!gm.isGroup()) {
                IPortletDefinition portletDefinition = portletDefinitionRegistry.getPortletDefinition(gm.getKey());
                if(portletDefinition != null) {
                    portletDefs.add(portletDefinition);
                } else {
                     // This isn't supposed to happen.
                    log.warn("Failed to obtain a portletDefinition for groupMember '"
                                + gm.getUnderlyingEntityIdentifier()
                                + "';  this circumstance probably means a portlet was deleted "
                                + "in a way that didn't clean up details like categpry memberships "
                                + "and permissions;  all interfaces that delete portlets should go "
                                + "through IPortletPublishingService.removePortletDefinition();  "
                                + "memberships for this missing portlet will be removed.");

                    // Delete existing category memberships for this portlet
                    for (IEntityGroup group : gm.getParentGroups()) {
                        group.removeChild(gm);
                        group.update();
                    }
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

        for (IGroupMember gm : childGroup.getParentGroups()) {
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

        for (IGroupMember gm : childEntity.getParentGroups()) {
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

}
