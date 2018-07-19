/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlet.registry;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.PortalException;
import org.apereo.portal.groups.IEntity;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupConstants;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.PortletCategory;
import org.apereo.portal.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("portletCategoryRegistry")
public class PortletCategoryRegistryImpl implements IPortletCategoryRegistry {

    private IPortletDefinitionRegistry portletDefinitionRegistry;

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Override
    public Set<PortletCategory> getAllChildCategories(PortletCategory parent) {
        Set<PortletCategory> rslt = new HashSet<>();

        for (PortletCategory child : getChildCategories(parent)) {
            // recurse
            rslt.add(child);
            rslt.addAll(getAllChildCategories(child));
        }

        return rslt;
    }

    @Override
    public Set<PortletCategory> getAllParentCategories(PortletCategory child) {
        Set<PortletCategory> rslt = new HashSet<>();
        for (PortletCategory parent : getParentCategories(child)) {
            // recurse
            rslt.add(parent);
            rslt.addAll(getAllParentCategories(parent));
        }
        return rslt;
    }

    @Override
    public Set<IPortletDefinition> getAllChildPortlets(PortletCategory parent) {

        Set<IPortletDefinition> rslt;

        try {
            rslt = new HashSet<>(getChildPortlets(parent));
            for (PortletCategory category : getAllChildCategories(parent)) {
                // Append portlets to list for each child category in the tree
                rslt.addAll(getChildPortlets(category));
            }

        } catch (Exception e) {
            String msg =
                    "Failed to obtain child portlets for the specified parent '"
                            + parent.getName()
                            + "', id="
                            + parent.getId();
            throw new PortalException(msg, e);
        }

        return rslt;
    }

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

    @Override
    public PortletCategory getPortletCategoryByName(String portletCategoryName) {
        final EntityIdentifier[] eids =
                GroupService.searchForGroups(
                        portletCategoryName,
                        IGroupConstants.SearchMethod.DISCRETE_CI,
                        IPortletDefinition.class);
        if (eids == null || eids.length == 0) {
            return null;
        } else if (eids.length > 1) {
            throw new IllegalStateException(
                    "More than one group found with name '"
                            + portletCategoryName
                            + "' (case insensitive)");
        }
        final IEntityGroup categoryGroup = GroupService.findGroup(eids[0].getKey());
        final PortletCategory category = new PortletCategory(eids[0].getKey());
        category.setName(categoryGroup.getName());
        category.setDescription(categoryGroup.getDescription());
        category.setCreatorId(categoryGroup.getCreatorID());
        return category;
    }

    @Override
    public Set<PortletCategory> getChildCategories(PortletCategory parent) {
        String parentKey = String.valueOf(parent.getId());
        IEntityGroup parentGroup = GroupService.findGroup(parentKey);
        Set<PortletCategory> categories = new HashSet<>();
        for (IGroupMember gm : parentGroup.getChildren()) {
            if (gm.isGroup()) {
                String categoryId = gm.getKey();
                categories.add(getPortletCategory(categoryId));
            }
        }
        return categories;
    }

    @Override
    public Set<IPortletDefinition> getChildPortlets(PortletCategory parent) {
        String parentKey = String.valueOf(parent.getId());
        IEntityGroup parentGroup = GroupService.findGroup(parentKey);
        Set<IPortletDefinition> portletDefs = new HashSet<>();
        for (IGroupMember gm : parentGroup.getChildren()) {
            if (!gm.isGroup()) {
                IPortletDefinition portletDefinition =
                        portletDefinitionRegistry.getPortletDefinition(gm.getKey());
                if (portletDefinition != null) {
                    portletDefs.add(portletDefinition);
                } else {
                    // This isn't supposed to happen.
                    log.warn(
                            "Failed to obtain a portletDefinition for groupMember '"
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

    @Override
    public Set<PortletCategory> getParentCategories(PortletCategory child) {
        String childKey = String.valueOf(child.getId());
        IEntityGroup childGroup = GroupService.findGroup(childKey);
        Set<PortletCategory> parents = new HashSet<>();

        for (IGroupMember gm : childGroup.getParentGroups()) {
            if (gm.isGroup()) {
                String categoryId = gm.getKey();
                parents.add(getPortletCategory(categoryId));
            }
        }
        return parents;
    }

    @Override
    public Set<PortletCategory> getParentCategories(IPortletDefinition child) {
        String childKey = child.getPortletDefinitionId().getStringId();
        IEntity childEntity = GroupService.getEntity(childKey, IPortletDefinition.class);
        Set<PortletCategory> parents = new HashSet<>();

        for (IGroupMember gm : childEntity.getParentGroups()) {
            if (gm.isGroup()) {
                String categoryId = gm.getKey();
                parents.add(getPortletCategory(categoryId));
            }
        }
        return parents;
    }

    @Override
    public PortletCategory getTopLevelPortletCategory() {
        IEntityGroup categoryGroup =
                GroupService.getDistinguishedGroup(IPortletDefinition.DISTINGUISHED_GROUP);
        return getPortletCategory(categoryGroup.getKey());
    }
}
