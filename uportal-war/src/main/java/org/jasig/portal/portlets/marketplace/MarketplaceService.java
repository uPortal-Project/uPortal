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

package org.jasig.portal.portlets.marketplace;

import org.apache.commons.lang3.Validate;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.AuthorizationPrincipalHelper;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service layer for Marketplace.
 */
@Service
public class MarketplaceService {
    // TODO Caching

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Autowired.
     */
    private IPortletDefinitionRegistry portletDefinitionRegistry;

    /**
     * Autowired.
     */
    private IPortletCategoryRegistry portletCategoryRegistry;

    /**
     * Return the Marketplace entries visible to the user.
     * Marketplace entries are visible to the user when the user enjoys permission for the
     * UP_PORTLET_SUBSCRIBE.BROWSE or UP_PORTLET_PUBLISH.MANAGE activity on the portlet entity.
     * @throws IllegalArgumentException when passed in user is null
     * @since uPortal 4.1
     */
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



    /**
     * Return the potentially empty Set of portlet categories such that
     * 1. the user has BROWSE (or MANAGE implying BROWSE) permission on the **category**, and
     * 2. the user has BROWSE (or MANAGE implying BROWSE) permission on at least one portlet in that category.
     * (That is, the category is not "empty" from the perspective of this user browsing).
     *
     * @param user non-null user
     * @return potentially empty non-null Set of browseable categories
     * @since uPortal 4.1
     */
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

    /**
     * Answers whether the given user may browse the portlet marketplace entry for the given portlet definition.
     * @param user a non-null IPerson who might be permitted to browse the entry
     * @param portletDefinition a non-null portlet definition the Marketplace entry of which the user might browse
     * @return true if permitted, false otherwise
     * @throws IllegalArgumentException if user is null
     * @throws IllegalArgumentException if portletDefinition is null
     * @since uPortal 4.1
     */
    public boolean mayBrowsePortlet(final IPerson user, final IPortletDefinition portletDefinition) {
        Validate.notNull(user, "Cannot determine if null users can browse portlets.");
        Validate.notNull(portletDefinition, "Cannot determine whether a user can browse a null portlet definition.");

        final IAuthorizationPrincipal principal = AuthorizationPrincipalHelper.principalFromUser(user);

        final String portletPermissionEntityId = permissionTargetIdForPortletDefinition(portletDefinition);

        return mayBrowse(principal, portletPermissionEntityId);

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
                IPermission.PORTLET_SUBSCRIBER_BROWSE_ACTIVITY, targetId)
                || principal.hasPermission(IPermission.PORTAL_PUBLISH,
                IPermission.PORTLET_MANAGER_ACTIVITY, targetId));

    }

    /**
     * Static utility method computing the permission target ID for a portlet definition.
     * @param portletDefinition a portlet definition
     * @return String permission target ID for the portlet definition.
     * @throws IllegalArgumentException if portletDefinition is null
     */
    private static String permissionTargetIdForPortletDefinition(IPortletDefinition portletDefinition) {
        // TODO: Put this method somewhere better.

        Validate.notNull(portletDefinition, "Cannot compute permission target ID for a null portlet definition.");

        final String portletPublicationId = portletDefinition.getPortletDefinitionId().getStringId();

        return IPermission.PORTLET_PREFIX.concat(portletPublicationId);
    }

    // Getters and setters below here.

    public IPortletDefinitionRegistry getPortletDefinitionRegistry() {
        return portletDefinitionRegistry;
    }

    @Autowired
    public void setPortletDefinitionRegistry(final IPortletDefinitionRegistry portletDefinitionRegistry) {
        Validate.notNull(portletDefinitionRegistry, "Pportlet definition registry must not be null.");
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    public IPortletCategoryRegistry getPortletCategoryRegistry() {
        return portletCategoryRegistry;
    }

    @Autowired
    public void setPortletCategoryRegistry(final IPortletCategoryRegistry portletCategoryRegistry) {
        Validate.notNull(portletCategoryRegistry);
        this.portletCategoryRegistry = portletCategoryRegistry;
    }

}
