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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Service layer implementation for Marketplace.
 *
 * The Service is responsible for policy application (so, users can only lay hands on
 * MarketplacePortletDefinitions for portlets they can browse, portlet definitions can be
 * asynchronously loaded for a user on login, etc.)
 *
 * An underlying Registry handles Marketplace portlet definition instantiation and caching.
 *
 * @since uPortal 4.1
 */
@Service
public class MarketplaceService implements IMarketplaceService, ApplicationListener<LoginEvent> {

    public static String FEATURED_CATEGORY_NAME="Featured";
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IPortletDefinitionRegistry portletDefinitionRegistry;

    private IMarketplaceRegistry marketplaceRegistry;

    private IPortletCategoryRegistry portletCategoryRegistry;
    
    private IAuthorizationService authorizationService;
    private boolean enableMarketplacePreloading = false;
    
    @Autowired
    public void setAuthorizationService(IAuthorizationService service) {
        this.authorizationService = service;
    }

    /**
     * Cache of String Username -> Future<Map<String fname, MarketplacePortletDefinition>.
     */
    @Autowired
    @Qualifier(
        "org.jasig.portal.portlet.marketplace.MarketplaceService.perUserMarketplacePortletDefinitionsFutureCache")
    private Cache perUserMarketplacePortletDefinitionsFutureCache;

    /**
     * Cache of String Username --> Future<Map<String fname, MarketplaceEntry>.
     */
    @Autowired
    @Qualifier(
        "org.jasig.portal.portlet.marketplace.MarketplaceService.perUserMarketplaceEntriesFutureCache")
    private Cache perUserMarketplaceEntriesFutureCache;

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
            asyncMarketplaceEntriesBrowseableBy(person);
            asyncMarketplacePortletDefinitionsBrowseableBy(person);
        }
    }

    /**
     * Asynchronous.  Returns a Future conveying an Immutable Map from String fname to
     * MarketplacePortletDefinition, with the Map containing only entries representing portlets
     * the given user may BROWSE.
     * @param user
     * @return
     */
    @Async
    private  Future<ImmutableSet<MarketplacePortletDefinition>>
      asyncMarketplacePortletDefinitionsBrowseableBy(final IPerson user) {

        Validate.notNull(user, "Cannot compute portlet definitions browseable by a null user.");
        Validate.notNull(user.getUserName(),
            "Cannot compute portlet definitions browseable by a user with a null username.");

        final Element cachedFutureElement =
            this.perUserMarketplacePortletDefinitionsFutureCache.get(user.getUserName());

        if (cachedFutureElement != null) {
            return (Future<ImmutableSet<MarketplacePortletDefinition>>)
                cachedFutureElement.getObjectValue();
        }


        final List<IPortletDefinition> allPortletDefinitions =
            this.portletDefinitionRegistry.getAllPortletDefinitions();

        final Set<MarketplacePortletDefinition> browseablePortletDefinitions =
            new HashSet<>();

        for (final IPortletDefinition portletDefinition : allPortletDefinitions) {

            if (mayBrowsePortlet(user, portletDefinition)) {
                final MarketplacePortletDefinition marketplacePortletDefinition =
                    marketplacePortletDefinitionFor(portletDefinition, user);
                browseablePortletDefinitions.add(marketplacePortletDefinition);
            } else {
                logger.trace(
                    "User {} may not browse {} so omitting it from browseable set for user.",
                    user.getUserName(), portletDefinition.getFName());
            }

        }

        logger.debug("These portlet definitions {} are browseable by {}.",
            browseablePortletDefinitions, user.getUserName());

        final Future<ImmutableSet<MarketplacePortletDefinition>> future =
            new AsyncResult<>(ImmutableSet.copyOf(browseablePortletDefinitions));

        perUserMarketplacePortletDefinitionsFutureCache.put(new Element(user.getUserName(), future));

        return future;
    }

    /**
     * Asynchronously load a Set of marketplace entries for a user.
     *
     * This method is primarily intended for seeding data.  Most impls should call
     * marketplaceEntriesBrowseableBy() instead.
     *
     * Note:  The returned Set is immutable since it is potentially shared between threads.  If
     * clients of this Service need to mutate the Set, make a copy. No protections have been
     * provided against modifying the MarketplaceEntry itself, so be careful when modifying the
     * entities contained in the list.
     *
     * @param user the non-null user
     * @return a Future that will resolve to a Set of MarketplaceEntry objects
     *      the requested user has browse access to.
     * @throws java.lang.IllegalArgumentException if user is null
     * @since 4.2
     */
    @Async
    private Future<ImmutableSet<MarketplaceEntry>>
      asyncMarketplaceEntriesBrowseableBy(final IPerson user) {

        Validate.notNull(user, "Cannot compute Marketplace entries browseable by a null user");
        Validate.notNull(user.getUserName(),
            "Cannot compute Marketplace entries browseable by a user with a null usernane.");

        final Element cachedFuture = perUserMarketplaceEntriesFutureCache.get(user.getUserName());

        if (cachedFuture != null) {
            return (Future<ImmutableSet<MarketplaceEntry>>) cachedFuture.getObjectValue();
        }


        final List<IPortletDefinition> allPortletDefinitions =
                this.portletDefinitionRegistry.getAllPortletDefinitions();

        final Set<MarketplaceEntry> visiblePortletDefinitions = new HashSet<>();

        for (final IPortletDefinition portletDefinition : allPortletDefinitions) {

            if (mayBrowsePortlet(user, portletDefinition)) {

                final MarketplacePortletDefinition marketplacePortletDefinition =
                    marketplacePortletDefinitionFor(portletDefinition, user);
                MarketplaceEntry entry = new MarketplaceEntry(marketplacePortletDefinition);

                // flag whether this use can add the portlet...
                boolean canAdd = mayAddPortlet(user, portletDefinition);
                entry.setCanAdd(canAdd);

                visiblePortletDefinitions.add(entry);
            } else {
                logger.trace("User {} may not browse {} so omitting it from browseable entries.",
                    user.getUserName(), portletDefinition);
            }
        }

        logger.debug("These portlet definitions {} are browseable by {}.",
            visiblePortletDefinitions, user.getUserName());

        final Future<ImmutableSet<MarketplaceEntry>> result =
            new AsyncResult<>(ImmutableSet.copyOf(visiblePortletDefinitions));
        final Element cacheElement = new Element(user.getUserName(), result);
        perUserMarketplaceEntriesFutureCache.put(cacheElement);

        return result;
    }

    @Override
    public ImmutableSet<MarketplaceEntry> marketplaceEntriesBrowseableBy(final IPerson user) {

        final Future<ImmutableSet<MarketplaceEntry>> futureEntries =
            asyncMarketplaceEntriesBrowseableBy(user);

        try {
            return futureEntries.get();

        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            return ImmutableSet.of();
        }
    }

    @Override
    public ImmutableSet<MarketplacePortletDefinition> marketplacePortletDefinitionsBrowseableBy(
        final IPerson user) {

        final Future<ImmutableSet<MarketplacePortletDefinition>> future =
            asyncMarketplacePortletDefinitionsBrowseableBy(user);

        try {
            ImmutableSet<MarketplacePortletDefinition> browseablePortlets = future.get();
            logger.trace("User {} may browse {}.", user.getUserName(), browseablePortlets);
            return browseablePortlets;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to read asynch load MarketplacePortletDefinitions "
                + "for user " + user + ".", e);
        }
    }

    @Override
    public ImmutableSet<MarketplacePortletDefinition>
        marketplacePortletDefinitionsRelatedTo(final MarketplacePortletDefinition definition) {

        final Set<MarketplacePortletDefinition> allRelatedPortlets = new HashSet<>();

        for (final PortletCategory parentCategory:
            portletCategoryRegistry.getParentCategories(definition)) {

            final Set<IPortletDefinition> portletsInCategory =
                portletCategoryRegistry.getAllChildPortlets(parentCategory);

            for (final IPortletDefinition portletDefinition : portletsInCategory) {
                allRelatedPortlets.add(
                    this.marketplaceRegistry.marketplacePortletDefinition(
                        portletDefinition.getFName(), definition.getPerson()));
            }
        }

        allRelatedPortlets.remove(definition);

        logger.trace("Definition {} has related definitions {}.",
            definition, allRelatedPortlets);

        return ImmutableSet.copyOf(allRelatedPortlets);
    }

    @Override
    public Set<PortletCategory> nonEmptyPortletCategoriesBrowseableBy(final IPerson user) {

        Validate.notNull(user, "Cannot compute browseable categories for a null user.");

        final IAuthorizationPrincipal principal = AuthorizationPrincipalHelper.principalFromUser(user);

        final Set<MarketplaceEntry> browseablePortlets = marketplaceEntriesBrowseableBy(user);

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
                            portletDefinition, user.getUserName(), category.getName());
                }
            }
        }

        logger.trace("These categories {} are browseable by {}.", browseableCategories, user);

        return browseableCategories;

    }

    @Override
    public boolean mayBrowsePortlet(final IPerson user, final IPortletDefinition portletDefinition) {
        Validate.notNull(user, "Cannot determine if null users can browse portlets.");
        Validate.notNull(user.getUserName(),
            "Cannot determine if null username user can browse portlets.");
        Validate.notNull(portletDefinition,
            "Cannot determine whether a user can browse a null portlet definition.");

        final IAuthorizationPrincipal principal = AuthorizationPrincipalHelper.principalFromUser(user);

        final String portletPermissionEntityId = PermissionHelper.permissionTargetIdForPortletDefinition(portletDefinition);

        boolean mayBrowse = mayBrowse(principal, portletPermissionEntityId);

        if (mayBrowse) {
            logger.trace("User {} may browse {}.", user.getUserName(), portletDefinition);
        } else {
            logger.trace("User {} may NOT browse {}.", user.getUserName(), portletDefinition);
        }

        return mayBrowse;

    }

    @Override
    public Set<MarketplacePortletDefinition> featuredPortletsForUser(IPerson user) {
        // TODO: caching
        Validate.notNull(user, "Cannot determine relevant featured portlets for null user.");
        Validate.notNull(user.getUserName(),
            "Cannot determine relevant featured portelts for user with null username.");

        final Set<MarketplaceEntry> browseablePortlets = marketplaceEntriesBrowseableBy(user);
        final Set<MarketplacePortletDefinition> featuredPortlets = new HashSet<>();

        for (final MarketplaceEntry entry : browseablePortlets) {
            IPortletDefinition portletDefinition = entry.getMarketplacePortletDefinition();
            for (final PortletCategory category : this.portletCategoryRegistry.getParentCategories(portletDefinition)) {

                if ( FEATURED_CATEGORY_NAME.equalsIgnoreCase(category.getName())){
                    featuredPortlets.add(marketplacePortletDefinitionFor(portletDefinition, user));
                }

            }
        }

        logger.debug("User {} has featured portlets {}.", user.getUserName(), featuredPortlets);

        return featuredPortlets;
    }

    @Override
    public MarketplacePortletDefinition marketplacePortletDefinitionFor(
        final IPortletDefinition portletDefinition, final IPerson person) {

        Validate.notNull(portletDefinition, "Marketplace portlet definitions "
            + "cannot model portlets without an underlying IPortletDefinition");

        return marketplacePortletDefinitionByFname(portletDefinition.getFName(), person);
    }
    
    @Override
    public MarketplacePortletDefinition marketplacePortletDefinitionByFname(
        final String fname, final IPerson person) {

        Validate.notNull(fname, "Marketplace portlet definitions "
            + "cannot model portlets without an fname.");

        final MarketplacePortletDefinition definition =
            this.marketplaceRegistry.marketplacePortletDefinition(fname, person);

        if (person != null && !mayBrowsePortlet(person, definition)) {
            throw new RuntimeException("User " + person + " may not BROWSE portlet " + fname);
        }

        return definition;
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
    
    @Override
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

    @Autowired
    public void setMarketplaceRegistry(final IMarketplaceRegistry marketplaceRegistry) {
        this.marketplaceRegistry = marketplaceRegistry;
    }

}
