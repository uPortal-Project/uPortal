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

package org.jasig.portal.portlet.marketplace;

import com.google.common.collect.ImmutableSet;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang3.Validate;
import org.jasig.portal.concurrency.caching.RequestCache;
import org.jasig.portal.events.LoginEvent;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.rest.layout.MarketplaceEntry;
import org.jasig.portal.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Service layer implementation for Marketplace.
 * @since uPortal 4.1
 */
@Service
public class MarketplaceService implements IMarketplaceService, ApplicationListener<LoginEvent> {

    public static String FEATURED_CATEGORY_NAME="Featured";
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IPortletDefinitionRegistry portletDefinitionRegistry;

    private IPortletCategoryRegistry portletCategoryRegistry;
    
    private IAuthorizationService authorizationService;
    private boolean enableMarketplacePreloading = false;

    @Autowired
    public void setAuthorizationService(IAuthorizationService service) {
        this.authorizationService = service;
    }

    /**
     * Used to store individual MarketplacePortletDefinition instances.
     */
    @Autowired
    @Qualifier(value = "org.jasig.portal.portlet.marketplace.MarketplaceService.marketplacePortletDefinitionCache")
    private Cache marketplacePortletDefinitionCache;

    /**
     * Cache of Username -> Future<Set<MarketplaceEntry>
     */
    @Autowired
    @Qualifier(value = "org.jasig.portal.portlet.marketplace.MarketplaceService.marketplaceUserPortletDefinitionCache")
    private Cache marketplaceUserPortletDefinitionCache;

    /**
     * Caches objects related to the ability to limit the portlets displayed
     * in a single publication of the Marketplace.
     */
    @Autowired
    @Qualifier(value = "org.jasig.portal.portlet.marketplace.MarketplaceService.marketplaceCategoryCache")
    private Cache marketplaceCategoryCache;

    @Value("${org.jasig.portal.portlets.marketplacePortlet.loadMarketplaceOnLogin:false}")
    public void setLoadMarketplaceOnLogin(final boolean enableMarketplacePreloading) {
        this.enableMarketplacePreloading = enableMarketplacePreloading;
    }


    /**
     * Handle the portal LoginEvent.   If marketplace caching is enabled, will preload
     * marketplace entries for the currently logged in user.
     *
     * @param loginEvent the login event.
     */
    @Override
    public void onApplicationEvent(LoginEvent loginEvent) {
        if (enableMarketplacePreloading) {
            final IPerson person = loginEvent.getPerson();
            /*
             * Passing an empty collection pre-loads an unfiltered collection;
             * instances of PortletMarketplace that specify filtering will
             * trigger a new collection to be loaded.
             */
            final Set<PortletCategory> empty = Collections.emptySet();
            loadMarketplaceEntriesFor(person, empty);
        }
    }

    /**
     * Load the list of marketplace entries for a user.  Will load entries async.
     * This method is primarily intended for seeding data.  Most impls should call
     * browseableMarketplaceEntriesFor() instead.
     *
     * Note:  Set is immutable since it is potentially shared between threads.  If
     * the set needs mutability, be sure to consider the thread safety implications.
     * No protections have been provided against modifying the MarketplaceEntry itself,
     * so be careful when modifying the entities contained in the list.
     *
     * @param user The non-null user
     * @param categories Restricts the output to entries within the specified categories if non-empty
     * @return a Future that will resolve to a set of MarketplaceEntry objects
     *      the requested user has browse access to.
     * @throws java.lang.IllegalArgumentException if user is null
     * @since 4.2
     */
    @Async
    public Future<ImmutableSet<MarketplaceEntry>> loadMarketplaceEntriesFor(final IPerson user, final Set<PortletCategory> categories) {

        final IAuthorizationPrincipal principal = AuthorizationPrincipalHelper.principalFromUser(user);

        List<IPortletDefinition> allDisplayablePortletDefinitions =
                this.portletDefinitionRegistry.getAllPortletDefinitions();

        if (!categories.isEmpty()) {
            // Indicates we plan to restrict portlets displayed in the Portlet
            // Marketplace to those that belong to one or more specified groups.
            Element portletDefinitionsElement = marketplaceCategoryCache.get(categories);
            if (portletDefinitionsElement == null) {

                /*
                 * Collection not in cache -- need to recreate it
                 */

                // Gather the complete collection of allowable categories (specified categories & their descendants)
                final Set<PortletCategory> allSpecifiedAndDecendantCategories = new HashSet<>();
                for (PortletCategory pc : categories) {
                    collectSpecifiedAndDescendantCategories(pc, allSpecifiedAndDecendantCategories);
                }

                // Filter portlets that match the criteria
                Set<IPortletDefinition> filteredPortletDefinitions = new HashSet<>();
                for (final IPortletDefinition portletDefinition : allDisplayablePortletDefinitions) {
                    final Set<PortletCategory> parents = portletCategoryRegistry.getParentCategories(portletDefinition);
                    for (final PortletCategory parent : parents) {
                        if (allSpecifiedAndDecendantCategories.contains(parent)) {
                            filteredPortletDefinitions.add(portletDefinition);
                            break;
                        }
                    }
                }

                portletDefinitionsElement = new Element(categories, new ArrayList<>(filteredPortletDefinitions));
                marketplaceCategoryCache.put(portletDefinitionsElement);
            }
            allDisplayablePortletDefinitions = (List<IPortletDefinition>) portletDefinitionsElement.getObjectValue();
        }

        final Set<MarketplaceEntry> visiblePortletDefinitions = new HashSet<>();

        for (final IPortletDefinition portletDefinition : allDisplayablePortletDefinitions) {

            if (mayBrowsePortlet(principal, portletDefinition)) {
                final MarketplacePortletDefinition marketplacePortletDefinition = getOrCreateMarketplacePortletDefinition(portletDefinition);
                final MarketplaceEntry entry =
                    new MarketplaceEntry(marketplacePortletDefinition, user);

                // flag whether this use can add the portlet...
                boolean canAdd = mayAddPortlet(user, portletDefinition);
                entry.setCanAdd(canAdd);

                visiblePortletDefinitions.add(entry);
            }
        }

        logger.trace("These portlet definitions {} are browseable by {}.", visiblePortletDefinitions, user);

        Future<ImmutableSet<MarketplaceEntry>> result = new AsyncResult<>(ImmutableSet.copyOf(visiblePortletDefinitions));
        Element cacheElement = new Element(user.getUserName(), result);
        marketplaceUserPortletDefinitionCache.put(cacheElement);

        return result;
    }

    @Override
    public ImmutableSet<MarketplaceEntry> browseableMarketplaceEntriesFor(final IPerson user, final Set<PortletCategory> categories) {
        Element cacheElement = marketplaceUserPortletDefinitionCache.get(user.getUserName());
        Future<ImmutableSet<MarketplaceEntry>> future = null;
        if (cacheElement == null) {
            // not in cache, load it and cache the results...
            future = loadMarketplaceEntriesFor(user, categories);
        } else {
            future = (Future<ImmutableSet<MarketplaceEntry>>) cacheElement.getObjectValue();
        }

        try {
            return future.get();

        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            return ImmutableSet.of();
        }
    }

    @Override
    public Set<PortletCategory> browseableNonEmptyPortletCategoriesFor(final IPerson user, final Set<PortletCategory> categories) {
        final IAuthorizationPrincipal principal = AuthorizationPrincipalHelper.principalFromUser(user);

        final Set<MarketplaceEntry> browseablePortlets = browseableMarketplaceEntriesFor(user, categories);

        final Set<PortletCategory> browseableCategories = new HashSet<PortletCategory>();

        // by considering only the parents of portlets browseable by this user,
        // categories containing zero browseable portlets are excluded.
        for (final MarketplaceEntry entry : browseablePortlets) {
            IPortletDefinition portletDefinition = entry.getMarketplacePortletDefinition();
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
    public boolean mayBrowsePortlet(final IAuthorizationPrincipal principal, final IPortletDefinition portletDefinition) {
        Validate.notNull(principal, "Cannot determine if null principals can browse portlets.");
        Validate.notNull(portletDefinition, "Cannot determine whether a user can browse a null portlet definition.");

        final String portletPermissionEntityId = PermissionHelper.permissionTargetIdForPortletDefinition(portletDefinition);
        return mayBrowse(principal, portletPermissionEntityId);
    }

    @Override
    public Set<MarketplaceEntry> featuredEntriesForUser(final IPerson user, final Set<PortletCategory> categories) {
        Validate.notNull(user, "Cannot determine relevant featured portlets for null user.");

        final Set<MarketplaceEntry> browseablePortlets = browseableMarketplaceEntriesFor(user, categories);
        final Set<MarketplaceEntry> featuredPortlets = new HashSet<>();

        for (final MarketplaceEntry entry : browseablePortlets) {
            final IPortletDefinition portletDefinition = entry.getMarketplacePortletDefinition();
            for (final PortletCategory category : this.portletCategoryRegistry.getParentCategories(portletDefinition)) {

                if ( FEATURED_CATEGORY_NAME.equalsIgnoreCase(category.getName())){
                    featuredPortlets.add(entry);
                }

            }
        }

        return featuredPortlets;
    }

    @Override
    public MarketplacePortletDefinition getOrCreateMarketplacePortletDefinition(IPortletDefinition portletDefinition) {
        Element element = marketplacePortletDefinitionCache.get(portletDefinition.getFName());
        if (element == null) {
            final MarketplacePortletDefinition mpd =
                new MarketplacePortletDefinition(portletDefinition, this, portletCategoryRegistry);
            element = new Element(portletDefinition.getFName(), mpd);
            this.marketplacePortletDefinitionCache.put(element);
        }
        return (MarketplacePortletDefinition) element.getObjectValue();
    }

    @Override
    public MarketplacePortletDefinition getOrCreateMarketplacePortletDefinitionIfTheFnameExists(String fname) {
        IPortletDefinition portletDefinition = portletDefinitionRegistry.getPortletDefinitionByFname(fname);
        if(portletDefinition != null) {
            return getOrCreateMarketplacePortletDefinition(portletDefinition);
        }
        return null;
    }

    // Private stateless static utility methods below here

    /**
     * True if the principal has UP_PORTLET_SUBSCRIBE.BROWSE on the target id.
     * The target ID must be fully resolved.  This method will not e.g. prepend
     * the portlet prefix to target ids that seem like they might be portlet IDs.
     * <p>
     * Implementation note: technically this method is not stateless since
     * asking an AuthorizationPrincipal about its permissions has caching side
     * effects in the permissions system, but it's stateless as far as this
     * Service is concerned.
     *
     * @param principal Non-null IAuthorizationPrincipal who might have permission
     * @param targetId Non-null identifier of permission target
     * @return true if principal has BROWSE permissions, false otherwise
     */
    private static boolean mayBrowse(final IAuthorizationPrincipal principal, final String targetId) {
        Validate.notNull(principal, "Cannot determine permissions for a null user.");
        Validate.notNull(targetId, "Cannot determine permissions on a null target.");

        return (principal.hasPermission(
                IPermission.PORTAL_SUBSCRIBE,
                IPermission.PORTLET_BROWSE_ACTIVITY,
                targetId));

    }

    /**
     * Called recursively to gather all specified categories and descendants 
     */
    private void collectSpecifiedAndDescendantCategories(PortletCategory specified, Set<PortletCategory> gathered) {
        final Set<PortletCategory> children = portletCategoryRegistry.getAllChildCategories(specified);
        for (PortletCategory child : children) {
            collectSpecifiedAndDescendantCategories(child, gathered);
        }
        gathered.add(specified);
    }

    /**
     * Answers whether the given user may add the portlet to their layout
     * @param user a non-null IPerson who might be permitted to add
     * @param portletDefinition a non-null portlet definition
     * @return true if permitted, false otherwise
     * @throws IllegalArgumentException if user is null
     * @throws IllegalArgumentException if portletDefinition is null
     * @since uPortal 4.2
     */
    @RequestCache
    public boolean mayAddPortlet(final IPerson user, final IPortletDefinition portletDefinition) {
        Validate.notNull(user, "Cannot determine if null users can browse portlets.");
        Validate.notNull(portletDefinition, "Cannot determine whether a user can browse a null portlet definition.");
        //short-cut for guest user, it will always be false for guest, otherwise evaluate
        return user.isGuest() ? false : authorizationService.canPrincipalSubscribe(AuthorizationPrincipalHelper.principalFromUser(user), portletDefinition.getPortletDefinitionId().getStringId());
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
