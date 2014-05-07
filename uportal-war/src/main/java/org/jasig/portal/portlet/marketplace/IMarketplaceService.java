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

import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.security.IPerson;

import java.util.Set;

/**
 * Marketplace service layer responsible for gathering and applying policy about what Marketplace entries
 * and categories ought to be available to a given user.
 * @since uPortal 4.1
 */
public interface IMarketplaceService {

    /**
     * Return the Marketplace entries visible to the user.
     * Marketplace entries are visible to the user when the user enjoys permission for the
     * UP_PORTLET_SUBSCRIBE.BROWSE or UP_PORTLET_PUBLISH.MANAGE activity on the portlet entity.
     * @throws IllegalArgumentException when passed in user is null
     * @since uPortal 4.1
     */
    Set<MarketplacePortletDefinition> browseableMarketplaceEntriesFor(IPerson user);

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
    Set<PortletCategory> browseableNonEmptyPortletCategoriesFor(IPerson user);

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
}
