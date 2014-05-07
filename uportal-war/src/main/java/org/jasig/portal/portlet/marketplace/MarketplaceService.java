/*
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

package org.jasig.portal.portlet.marketplace;

import org.apache.commons.lang3.Validate;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service layer implementation for Marketplace.
 * @since uPortal 4.1
 */
@Service
public class MarketplaceService implements IMarketplaceService {

    public static String FEATURED_CATEGORY_NAME="Featured";
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IPortletDefinitionRegistry portletDefinitionRegistry;

    private IPortletCategoryRegistry portletCategoryRegistry;

    @Override
    public Set<MarketplacePortletDefinition> browseableMarketplaceEntriesFor(final IPerson user) {

        final List<IPortletDefinition> allPortletDefinitions =
                this.portletDefinitionRegistry.getAllPortletDefinitions();

        final Set<MarketplacePortletDefinition> visiblePortletDefinitions = new HashSet<>();

        for (final IPortletDefinition portletDefinition : allPortletDefinitions) {

            if ( mayBrowsePortlet(user, portletDefinition) ) {
                final MarketplacePortletDefinition marketplacePortletDefinition =
                        new MarketplacePortletDefinition(portletDefinition, this.portletCategoryRegistry);
                visiblePortletDefinitions.add(marketplacePortletDefinition);
            }
        }

        logger.trace("These portlet definitions {} are browseable by {}.", visiblePortletDefinitions, user);

        return visiblePortletDefinitions;

    }



    @Override
    public Set browseableNonEmptyPortletCategoriesFor(final IPerson user) {

        final IAuthorizationPrincipal principal = AuthorizationPrincipalHelper.principalFromUser(user);

        final Set<MarketplacePortletDefinition> browseablePortlets = browseableMarketplaceEntriesFor(user);

        final Set<PortletCategory> browseableCategories = new HashSet<PortletCategory>();

        // by considering only the parents of portlets browseable by this user,
        // categories containing zero browseable portlets are excluded.
        for (final IPortletDefinition portletDefinition : browseablePortlets) {

            for (final PortletCategory category : this.portletCategoryRegistry.getParentCategories(portletDefinition)) {

                final String categoryId = category.getId();

                if ( mayBrowse(principal, categoryId) ) {
                    browseableCategories.add(category);
                } else {
                    logger.trace("Portlet {} is browseable by {} but it is in category {} " +
                            "which is not browseable by that user.  " +
                            "This may be as intended, " +
                            "or it may be that that portlet category ought to be more widely browseable.",
                            portletDefinition, user, category);
                }
            }
        }

        logger.trace("These categories {} are browseable by {}.", browseableCategories, user);

        return browseableCategories;

    }

    @Override
    public boolean mayBrowsePortlet(final IPerson user, final IPortletDefinition portletDefinition) {
        Validate.notNull(user, "Cannot determine if null users can browse portlets.");
        Validate.notNull(portletDefinition, "Cannot determine whether a user can browse a null portlet definition.");

        final IAuthorizationPrincipal principal = AuthorizationPrincipalHelper.principalFromUser(user);

        final String portletPermissionEntityId = PermissionHelper.permissionTargetIdForPortletDefinition(portletDefinition);

        return mayBrowse(principal, portletPermissionEntityId);

    }

    @Override
    public Set<MarketplacePortletDefinition> featuredPortletsForUser(IPerson user) {
        Validate.notNull(user, "Cannot determine relevant featured portlets for null user.");

        final Set<MarketplacePortletDefinition> browseablePortlets = browseableMarketplaceEntriesFor(user);
        final Set<MarketplacePortletDefinition> featuredPortlets = new HashSet<>();

        for (final IPortletDefinition portletDefinition : browseablePortlets) {

            for (final PortletCategory category : this.portletCategoryRegistry.getParentCategories(portletDefinition)) {

                if ( FEATURED_CATEGORY_NAME.equalsIgnoreCase(category.getName())){
                    featuredPortlets.add(
                            new MarketplacePortletDefinition(portletDefinition, this.portletCategoryRegistry));
                }

            }
        }

        return featuredPortlets;
    }


    // Private stateless static utility methods below here

    /**
     * True if the principal has UP_PORTLET_SUBSCRIBE.BROWSE or UP_PORTLET_PUBLISH.MANAGE on the target id.
     * The target ID must be fully resolved.  This method will not e.g. prepend the portlet prefix to target ids
     * that seem like they might be portlet IDs.
     * Implementation note: technically this method is not stateless since asking an AuthorizationPrincipal about
     * its permissions has caching side effects in the permissions system, but it's stateless as far as this Service
     * is concerned.
     * @param principal non-null IAuthorizationPrincipal who might have permission
     * @param targetId non-null identifier of permission target
     * @return true if has BROWSE or MANAGE permissions, false otherwise.
     */
    private static boolean mayBrowse(final IAuthorizationPrincipal principal, final String targetId) {
        Validate.notNull(principal, "Cannot determine permissions for a null user.");
        Validate.notNull(targetId, "Cannot determine permissions on a null target.");

        return (principal.hasPermission(IPermission.PORTAL_SUBSCRIBE,
                IPermission.PORTLET_BROWSE_ACTIVITY, targetId)
                || principal.hasPermission(IPermission.PORTAL_PUBLISH,
                IPermission.PORTLET_MANAGER_ACTIVITY, targetId));

    }

    // JavaBean property setters below here.
    // getters omitted because no use cases for reading the properties

    @Autowired
    public void setPortletDefinitionRegistry(final IPortletDefinitionRegistry portletDefinitionRegistry) {
        Validate.notNull(portletDefinitionRegistry, "Portlet definition registry must not be null.");
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    public void setPortletCategoryRegistry(final IPortletCategoryRegistry portletCategoryRegistry) {
        Validate.notNull(portletCategoryRegistry);
        this.portletCategoryRegistry = portletCategoryRegistry;
    }

}
