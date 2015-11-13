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
package org.jasig.portal.portlets.favorites;

import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

/**
 * The Favorites controllers handling the VIEW and EDIT modes share dependency auto-wiring needs.
 * This abstract class implements those dependencies and auto-wiring once so that the concrete controller
 * implementations can inherit that functionality.
 * @since uPortal 4.1
 */
public abstract class AbstractFavoritesController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected IUserInstanceManager userInstanceManager;
    protected IPortalRequestUtils portalRequestUtils;

    /**
    * Functional name of Marketplace portlet,
    * or null if links to a Marketplace are not desired or are not feasible.
    */
    protected String marketplaceFName;

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    /**
     * Configures FavoritesController to include a Marketplace portlet functional name in the Model,
     * which ultimately signals and enables the View to include convenient link to Marketplace for
     * user to add new favorites.
     *
     * When set to null, signals Favorites portlet to suppress links to Marketplace.
     * Setting to the empty String or to the literal value "null" (ignoring case)
     * is equivalent to setting to null.
     *
     * This is for convenience in expressing no-marketplace-fname-available via injected value from
     * properties file.  Defaults to the value of the property
     * "org.jasig.portal.portlets.favorites.MarketplaceFunctionalName", or null if that property is not set.
     *
     * The functional name can technically be the fname of any portlet.  It doesn't have to be
     * The Marketplace Portlet.  Perhaps you've got your own take on Marketplace.
     *
     * This allows Favorites to support integration with Marketplace without requiring a Marketplace,
     * gracefully degrading when no Marketplace available.
     *
     * @param marketplaceFunctionalName String fname of a marketplace portlet, or null.
     */
    @Value("${org.jasig.portal.portlets.favorites.MarketplaceFunctionalName:null}")
    public void setMarketplaceFName(String marketplaceFunctionalName) {

        // interpret null, non-text-having, or literal "null" as
        // signaling lack of Marketplace functional name.
        if (!StringUtils.hasText(marketplaceFunctionalName)
             || "null".equalsIgnoreCase(marketplaceFunctionalName)) {
            marketplaceFunctionalName = null;
        }

        this.marketplaceFName = marketplaceFunctionalName;
    }
}
