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
package org.apereo.portal.portlet.marketplace;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.PortletCategory;
import org.apereo.portal.rest.layout.MarketplaceEntry;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPerson;

/**
 * Marketplace service layer responsible for gathering and applying policy about what Marketplace
 * entries and categories ought to be available to a given user.
 *
 * @since 4.1
 */
public interface IMarketplaceService {

    /**
     * Return the Marketplace entries visible to the user. Marketplace entries are visible to the
     * user when the user enjoys permission for the UP_PORTLET_SUBSCRIBE.BROWSE or
     * UP_PORTLET_PUBLISH.MANAGE activity on the portlet entity.
     *
     * @param user The non-null user
     * @param categories Restricts the output to entries within the specified categories if
     *     non-empty
     * @throws IllegalArgumentException when passed in user is null
     * @since 4.2
     */
    ImmutableSet<MarketplaceEntry> browseableMarketplaceEntriesFor(
            IPerson user, final Set<PortletCategory> categories);

    /**
     * Return the potentially empty Set of portlet categories such that 1. the user has BROWSE (or
     * MANAGE implying BROWSE) permission on the **category**, and 2. the user has BROWSE (or MANAGE
     * implying BROWSE) permission on at least one portlet in that category. (That is, the category
     * is not "empty" from the perspective of this user browsing).
     *
     * @param user non-null user
     * @param categories Restricts the output to entries within the specified categories if
     *     non-empty
     * @return potentially empty non-null Set of browseable categories
     * @since 4.1
     */
    Set<PortletCategory> browseableNonEmptyPortletCategoriesFor(
            IPerson user, final Set<PortletCategory> categories);

    /**
     * Answers whether the given user may browse the portlet marketplace entry for the given portlet
     * definition.
     *
     * @param principal A non-null IPerson who might be permitted to browse the entry
     * @param portletDefinition A non-null portlet definition the Marketplace entry of which the
     *     user might browse
     * @return true if permitted, false otherwise
     * @throws IllegalArgumentException If user is null
     * @throws IllegalArgumentException If portletDefinition is null
     * @since 4.1
     */
    boolean mayBrowsePortlet(
            IAuthorizationPrincipal principal, IPortletDefinition portletDefinition);

    /**
     * Provides the potentially empty non-null Set of featured Marketplace entries for the user.
     *
     * <p>The user MUST have BROWSE permission on all members of the Set.
     *
     * @param user the non-null user for whom featured portlets are desired.
     * @param categories Restricts the output to entries within the specified categories if
     *     non-empty
     * @return non-null potentially empty Set of featured portlet MarketplaceEntries.
     * @since 4.2
     */
    Set<MarketplaceEntry> featuredEntriesForUser(
            IPerson user, final Set<PortletCategory> categories);

    /**
     * Provides a {@link MarketplacePortletDefinition} object that corresponds to the specified
     * portlet definition. Implementations of IMarketplaceService may cache these objects to-taste.
     *
     * @param portletDefinition A valid {@link IPortletDefinition}
     * @return A {@link MarketplacePortletDefinition} wrapping the specified portlet definition.
     */
    MarketplacePortletDefinition getOrCreateMarketplacePortletDefinition(
            IPortletDefinition portletDefinition);

    /**
     * Provides a {@link MarketplacePortletDefinition} object that corresponds to the specified
     * portlet definition. Implementations of IMarketplaceService may cache these objects to-taste.
     *
     * @param fname a valid fname of a portlet
     * @return A {@link MarketplacePortletDefinition} wrapping the specified portlet definition.
     */
    MarketplacePortletDefinition getOrCreateMarketplacePortletDefinitionIfTheFnameExists(
            String fname);

    /**
     * Answers whether the given user may add the portlet to their layout
     *
     * @param user a non-null IPerson who might be permitted to add
     * @param portletDefinition a non-null portlet definition
     * @return true if permitted, false otherwise
     * @throws IllegalArgumentException if user is null
     * @throws IllegalArgumentException if portletDefinition is null
     * @since 4.2
     */
    boolean mayAddPortlet(final IPerson user, final IPortletDefinition portletDefinition);
}
