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
package org.jasig.portal.portlet.marketplace;

import com.google.common.collect.ImmutableSet;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.rest.layout.MarketplaceEntry;
import org.jasig.portal.security.IPerson;

import java.util.Set;

/**
 * Marketplace service layer responsible for gathering and applying policy about what Marketplace entries
 * and categories ought to be available to a given user.
 *
 * @since uPortal 4.1
 */
public interface IMarketplaceService {


    /**
     * Return the Marketplace entries visible to the user.
     * Marketplace entries are visible to the user when the user enjoys permission for the
     * UP_PORTLET_SUBSCRIBE.BROWSE or UP_PORTLET_PUBLISH.MANAGE activity on the portlet entity.
     * @throws IllegalArgumentException when passed in user is null
     * @since uPortal 4.2
     */
    ImmutableSet<MarketplaceEntry> marketplaceEntriesBrowseableBy(IPerson user);

    /**
     * Return the Marketplace portlet definitions browseable by the given user.
     * Marketplace portlet definitions are visible to the user when the user enjoys permission for
     * UP_PORTLET_SUBSCRIBE.BROWSE or UP_PORTLET_PUBLISH.MANAGE activity on the portlet entity.
     *
     * @param user non-null person for whom the definitions are browseable
     * @return non-null potentially empty Set of definitions browseable by the user.
     * @throws RuntimeException when cannot determine result
     * @since uPortal 4.2
     */
    public ImmutableSet<MarketplacePortletDefinition>
        marketplacePortletDefinitionsBrowseableBy(IPerson user);

    /**
     * Return the Marketplace portlet definitions related to the given MarketplacePortletDefinition,
     * regardless of whether those Definitions are BROWSEable by the principal served in the current
     * execution context.
     *
     * Currently, Marketplace portlet definition A is considered related to portlet definition
     * B when A is a member of at least one category or (extended) sub-category of a category of
     * which B is a member.
     *
     * @param definition a non-null Definition to which the returned Defintions are related.
     * @return a potentially empty non-null Set of related Definitions
     * @throws RuntimeException when cannot determine related definitions.
     * @since uPortal 4.2
     */
    public ImmutableSet<MarketplacePortletDefinition>
        marketplacePortletDefinitionsRelatedTo(MarketplacePortletDefinition definition);

    /**
     * Return the potentially empty Set of portlet categories such that
     * 1. the user has BROWSE (or MANAGE implying BROWSE) permission on the **category**, and
     * 2. the user has BROWSE (or MANAGE implying BROWSE) permission on at least one portlet in that category.
     * (That is, the category is not "empty" from the perspective of this user browsing).
     *
     * @param user non-null user
     * @return potentially empty non-null Set of browseable categories
     * @since uPortal 4.2
     */
    Set<PortletCategory> nonEmptyPortletCategoriesBrowseableBy(IPerson user);

    /**
     * Answers whether the given user may browse the portlet marketplace entry for the given portlet definition.
     * @param user a non-null IPerson who might be permitted to browse the entry
     * @param portletDefinition a non-null portlet definition the Marketplace entry of which the user might browse
     * @return true if permitted, false otherwise
     * @throws IllegalArgumentException if user is null
     * @throws IllegalArgumentException if portletDefinition is null
     * @since uPortal 4.1
     */
    boolean mayBrowsePortlet(IPerson user, IPortletDefinition portletDefinition);
    
    /**
     * Answers whether the given user may add the portlet to their layout
     * @param user a non-null IPerson who might be permitted to add
     * @param portletDefinition a non-null portlet definition
     * @return true if permitted, false otherwise
     * @throws IllegalArgumentException if user is null
     * @throws IllegalArgumentException if portletDefinition is null
     * @since uPortal 4.2
     */
    boolean mayAddPortlet(IPerson user, IPortletDefinition portletDefinition);

    /**
     * Provides the potentially empty non-null Set of featured portlets for this user.
     * "For this user" is subject to implementation-specific considerations (as in, is it the same featured portlets
     * for everyone?  Does it depend on user role?  Do you especially feature GTD portlets for users who seem to be
     * absent-minded?  Do you feature meal-related portlets around lunch time?  These decisions are entirely up to the
     * implementation.
     * However, the user MUST have BROWSE permission on all members of the Set.
     * @param user non-null user for whom featured portlets are desired
     * @return non-null potentially empty Set of featured portlet MarketplacePortletDefinitions
     * @throws java.lang.IllegalArgumentException if user is null or otherwise observed to be broken
     */
    Set<MarketplacePortletDefinition> featuredPortletsForUser(IPerson user);

    /**
     * Provides a {@link MarketplacePortletDefinition} object that corresponds to the specified
     * portlet definition tailored for the given user.
     * Implementations of IMarketplaceService may cache these objects to-taste.
     * @param portletDefinition A valid {@link IPortletDefinition}
     * @param user for whom the definition is to be tailored; may be null.
     * @return A {@link MarketplacePortletDefinition} wrapping the specified portlet definition.
     *
     * @since uPortal 4.2
     */
    MarketplacePortletDefinition marketplacePortletDefinitionFor(
        IPortletDefinition portletDefinition, IPerson user);
    
    /**
     * Provides a {@link MarketplacePortletDefinition} object that corresponds to the specified
     * portlet definition, optionally as viewed by a given user.
     *
     * Enforces BROWSE permission iff a user is specified.
     *
     * @param fname a non-null String that might be the fname of a portlet (if not, returns null)
     * @param user may be null, user for whom definition is being requested.
     *
     * @return A {@link MarketplacePortletDefinition} wrapping the specified portlet definition,
     * or null if no portlet definition found by that fname.
     *
     * @throws RuntimeException if the user is not null and is not authorized to BROWSE the portlet.
     *
     * @since uPortal 4.2
     */
    MarketplacePortletDefinition marketplacePortletDefinitionByFname(String fname, IPerson user);

}
