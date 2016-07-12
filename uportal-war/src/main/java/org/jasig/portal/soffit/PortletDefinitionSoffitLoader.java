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

package org.jasig.portal.soffit;

import java.util.Collections;
import java.util.Locale;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.apereo.portlet.soffit.connector.AbstractSoffitLoader;
import org.apereo.portlet.soffit.connector.ISoffitLoader;
import org.apereo.portlet.soffit.model.v1_0.Definition;
import org.jasig.portal.i18n.ILocaleStore;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.portlet.marketplace.IMarketplaceService;
import org.jasig.portal.portlet.marketplace.MarketplacePortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Concrete {@link ISoffitLoader} implementation responsible for loading the
 * payload with metadata from the {@link IPortletDefinition}.
 *
 * @author drewwills
 */
@Component
public class PortletDefinitionSoffitLoader extends AbstractSoffitLoader {

    @Autowired
    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    private IPortletWindowRegistry portletWindowRegistry;

    @Autowired
    private IMarketplaceService marketplaceService;

    @Autowired
    private IPersonManager personManager;

    @Autowired
    private ILocaleStore localeStore;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PortletDefinitionSoffitLoader() {
        super(ISoffitLoader.DEFAULT_LOADER_ORDER + 1);
    }

    @Override
    public void load(org.apereo.portlet.soffit.model.v1_0.Payload soffit,
            RenderRequest renderRequest, RenderResponse renderResponse) {

        // Obtain the MarketplacePortletDefinition for this soffit
        final HttpServletRequest httpr = portalRequestUtils.getCurrentPortalRequest();
        final IPortletWindowId portletWindowId = portletWindowRegistry.getPortletWindowId(httpr, renderRequest.getWindowID());
        final IPortletWindow portletWindow = portletWindowRegistry.getPortletWindow(httpr, portletWindowId);
        final IPortletDefinition pdef = portletWindow.getPortletEntity().getPortletDefinition();
        final MarketplacePortletDefinition mpdef = this.marketplaceService.getOrCreateMarketplacePortletDefinition(pdef);

        final IPerson user = personManager.getPerson(httpr);
        final Locale locale = getUserLocale(user);

        // Load metadata into the payload Definition
        Definition definition = soffit.getDefinition();
        if (definition == null) {
            // Create & set
            definition = new Definition();
            soffit.setDefinition(definition);
        }
        definition.setDescription(mpdef.getDescription())
            .setFname(mpdef.getFName())
            .setName(mpdef.getName())
            .setTimeout(mpdef.getTimeout())
            .setTitle(mpdef.getTitle(locale.toString()));
        for (IPortletDefinitionParameter param : mpdef.getParameters()) {
            definition.setParameter(param.getName(), Collections.singletonList(param.getValue()));
        }
        for (PortletCategory category : mpdef.getCategories()) {
            definition.addCategory(category.getName());
        }

        logger.debug("Loading the following soffit Definition for user='{}':  ", renderRequest.getRemoteUser(), definition);

    }

    /*
     * Implementation
     */

    private Locale getUserLocale(IPerson user) {
        // get user locale
        Locale[] locales = localeStore.getUserLocales(user);
        LocaleManager localeManager = new LocaleManager(user, locales);
        Locale rslt = localeManager.getLocales()[0];
        return rslt;
    }

}
