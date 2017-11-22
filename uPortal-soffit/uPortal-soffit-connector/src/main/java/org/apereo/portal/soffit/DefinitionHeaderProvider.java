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
package org.apereo.portal.soffit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apereo.portal.i18n.ILocaleStore;
import org.apereo.portal.i18n.LocaleManager;
import org.apereo.portal.portlet.marketplace.IMarketplaceService;
import org.apereo.portal.portlet.marketplace.MarketplacePortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.om.PortletCategory;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.soffit.connector.AbstractHeaderProvider;
import org.apereo.portal.soffit.model.v1_0.Definition;
import org.apereo.portal.soffit.service.DefinitionService;
import org.apereo.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Prepares the custom HTTP X-Soffit-Definition header. This component is defined explicitly in the
 * portlet context (not by annotation).
 *
 * @since 5.0
 */
public class DefinitionHeaderProvider extends AbstractHeaderProvider {

    @Autowired private IPortalRequestUtils portalRequestUtils;

    @Autowired private IPortletWindowRegistry portletWindowRegistry;

    @Autowired private IMarketplaceService marketplaceService;

    @Autowired private IPersonManager personManager;

    @Autowired private ILocaleStore localeStore;

    @Autowired private DefinitionService definitionService;

    @Override
    public Header createHeader(RenderRequest renderRequest, RenderResponse renderResponse) {

        // Username
        final String username = getUsername(renderRequest);

        // Obtain the MarketplacePortletDefinition for this soffit
        final HttpServletRequest httpr = portalRequestUtils.getCurrentPortalRequest();
        final IPortletWindowId portletWindowId =
                portletWindowRegistry.getPortletWindowId(httpr, renderRequest.getWindowID());
        final IPortletWindow portletWindow =
                portletWindowRegistry.getPortletWindow(httpr, portletWindowId);
        final IPortletDefinition pdef = portletWindow.getPortletEntity().getPortletDefinition();
        final MarketplacePortletDefinition mpdef =
                this.marketplaceService.getOrCreateMarketplacePortletDefinition(pdef);

        final IPerson user = personManager.getPerson(httpr);
        final Locale locale = getUserLocale(user);

        // Title
        final String title = mpdef.getTitle(locale.toString());

        // FName
        final String fname = mpdef.getFName();

        // Description
        final String description = mpdef.getDescription(locale.toString());

        // Categories
        List<String> categories = new ArrayList<>();
        for (PortletCategory pc : mpdef.getCategories()) {
            categories.add(pc.getName());
        }

        // Parameters
        Map<String, List<String>> parameters = new HashMap<>();
        for (IPortletDefinitionParameter param : mpdef.getParameters()) {
            parameters.put(param.getName(), Collections.singletonList(param.getValue()));
        }

        final Definition definition =
                definitionService.createDefinition(
                        title,
                        fname,
                        description,
                        categories,
                        parameters,
                        username,
                        getExpiration(renderRequest));
        final Header rslt =
                new BasicHeader(Headers.DEFINITION.getName(), definition.getEncryptedToken());
        logger.debug(
                "Produced the following {} header for username='{}':  {}",
                Headers.DEFINITION.getName(),
                username,
                rslt);

        return rslt;
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
