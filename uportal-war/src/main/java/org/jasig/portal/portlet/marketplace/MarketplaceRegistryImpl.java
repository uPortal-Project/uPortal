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

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.Validate;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.IPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * Marketplace registry implementation.
 * The registry layer is responsible for caching and for instantiating
 * MarketplacePortletDefinition implementations.
 *
 * @since uPortal 4.2
 */
@Repository
public class MarketplaceRegistryImpl
    implements IMarketplaceRegistry {

    //autowired via @Autowired on setMarketplacePortletDefinitionCache
    /**
     * Cache from fname-and-username to MarketplacePortletDefinition.
     * Key: String [portlet_fname]_[username]
     * Value: MarketplacePortletDefinition.
     */
    private Cache marketplacePortletDefinitionCache;

    // dependencies

    /**
     * Auto-wired via corresponding setter method.
     */
    private IPortletDefinitionRegistry portletDefinitionRegistry;

    /**
     * Constructor-injected into new MarketplacePortletDefinition instances and otherwise unused.
     * Relying upon this service would create an unfortunate circular dependency between service
     * and registry.
     * Auto-wired via corresponding setter method.
     */
    private IMarketplaceService marketplaceService;

    /**
     * Constructor-injected into new MarketplacePortletDefinition instances and otherwise unused.
     * Auto-wired via corresponding setter method.
     */
    private IPortletCategoryRegistry marketplaceCategoryRegistry;


    @Override public MarketplacePortletDefinition marketplacePortletDefinition(
        final String fname, final IPerson user) {

        Validate.notNull(fname, "MarketplacePortletDefinitions cannot "
            + "represent portlet publications with null fnames.");

        final String usernameSuffixToKey;

        if (null != user && null != user.getUserName()) {
            usernameSuffixToKey = user.getUserName();
        } else {
            usernameSuffixToKey = "";
        }

        final String cacheKey = fname + "_" + usernameSuffixToKey;

        // This caching probably could be implemented more cleanly using aspects or
        // by making the Cache self-populating.

        final Element cacheElement = marketplacePortletDefinitionCache.get(cacheKey);

        if (null != cacheElement) {
            return (MarketplacePortletDefinition) cacheElement.getObjectValue();
        }

        final IPortletDefinition portletDefinition =
            this.portletDefinitionRegistry.getPortletDefinitionByFname(fname);
        if (null == portletDefinition) {
            return null;
        }
        MarketplacePortletDefinition mpd = new MarketplacePortletDefinition
            (portletDefinition, user, marketplaceService, marketplaceCategoryRegistry);
        final Element newCacheElement = new Element(cacheKey, mpd);
        marketplacePortletDefinitionCache.put(newCacheElement);

        return mpd;
    }

    @Autowired
    public void setPortletDefinitionRegistry(
        final IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    @Qualifier
        ("org.jasig.portal.portlet.marketplace.MarketplaceRegistryImpl.marketplacePortletDefinitionCache")
    public void setMarketplacePortletDefinitionCache(
        final Cache marketplacePortletDefinitionCache) {
        this.marketplacePortletDefinitionCache = marketplacePortletDefinitionCache;
    }

    @Autowired
    public void setMarketplaceService(final IMarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @Autowired
    public void setMarketplaceCategoryRegistry(
        final IPortletCategoryRegistry marketplaceCategoryRegistry) {
        this.marketplaceCategoryRegistry = marketplaceCategoryRegistry;
    }

}
