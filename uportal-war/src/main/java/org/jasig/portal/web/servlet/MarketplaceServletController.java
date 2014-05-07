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

package org.jasig.portal.web.servlet;

import org.jasig.portal.portlet.dao.IMarketplaceRatingDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.marketplace.IMarketplaceRating;
import org.jasig.portal.portlet.marketplace.IMarketplaceService;
import org.jasig.portal.portlet.marketplace.MarketplacePortletDefinition;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * This is the Servlet version of PortletMarketplaceController.
 */
@Controller
@RequestMapping("/marketplace/**")
public class MarketplaceServletController {
    // TODO: this might be refactorable into the same class with the portlet controller.

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IMarketplaceService marketplaceService;

    private IPersonManager personManager;

    // TODO: eliminate dependency on registry in favor of getting this behavior from the service?
    private IPortletCategoryRegistry portletCategoryRegistry;

    // TODO: eliminate dependency on this registry in favor of getting this behavior from the service.
    private IPortletDefinitionRegistry portletDefinitionRegistry;



    // TODO: eliminate dependency on this DAO in favor of getting this behavior from the service.
    private IMarketplaceRatingDao marketplaceRatingDao;

    @RequestMapping("/marketplace/")
    public String listingRender(HttpServletRequest request, Model model,
                                @RequestParam(value="filter", required = false) String filter) {

        final IPerson user = personManager.getPerson(request);

        Set<MarketplacePortletDefinition> marketplaceEntries = marketplaceService.browseableMarketplaceEntriesFor(user);
        model.addAttribute("channelBeanList", marketplaceEntries);

        final Set<MarketplacePortletDefinition> featuredPortlets =
                this.marketplaceService.featuredPortletsForUser(user);
        model.addAttribute("featuredPortlets", featuredPortlets);

        Set<PortletCategory> categories = marketplaceService.browseableNonEmptyPortletCategoriesFor(user);
        boolean showRootCategory = false; // TODO: make configurable

        if (! showRootCategory) {
            categories.remove(this.portletCategoryRegistry.getTopLevelPortletCategory());
        }

        model.addAttribute("categoryList", categories);

        model.addAttribute("initialFilter", filter);

        return "jsp/Marketplace/servlet/view";
    }

    @RequestMapping("/marketplace/{fname}")
    public String detailRender(HttpServletRequest request, Model model, @PathVariable("fname") String fName) {

        IPerson user = this.personManager.getPerson(request);

        IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinitionByFname(fName);

        if (portletDefinition == null) {
            // entry not found, render listing view
            return listingRender(request, model, null);
        }

        if (! this.marketplaceService.mayBrowsePortlet(user, portletDefinition)) {
            // TODO: provide an error experience
            return listingRender(request, model, null);
        }

        MarketplacePortletDefinition selectedMarketplacePortletDefinition =
                new MarketplacePortletDefinition(portletDefinition, this.portletCategoryRegistry);

        IMarketplaceRating rating = this.marketplaceRatingDao.getRating(user.getUserName(), portletDefinition);

        model.addAttribute("marketplaceRating", rating);
        model.addAttribute("reviewMaxLength", IMarketplaceRating.REVIEW_MAX_LENGTH);
        model.addAttribute("portlet", selectedMarketplacePortletDefinition);
        // omitting deepLink
        model.addAttribute("shortUrl", selectedMarketplacePortletDefinition.getShortURL());

        return "jsp/Marketplace/servlet/entry";

    }


    // Mutators below here.
    // Setter methods omitted for lack of use cases for.


    @Autowired
    public void setMarketplaceService(IMarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setPortletCategoryRegistry(IPortletCategoryRegistry portletCategoryRegistry) {
        this.portletCategoryRegistry = portletCategoryRegistry;
    }

    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    public void setMarketplaceRatingDao(IMarketplaceRatingDao marketplaceRatingDao) {
        this.marketplaceRatingDao = marketplaceRatingDao;
    }

}
