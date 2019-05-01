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
package org.apereo.portal.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.Validate;
import org.apereo.portal.portlet.dao.IMarketplaceRatingDao;
import org.apereo.portal.portlet.marketplace.IMarketplaceRating;
import org.apereo.portal.portlet.marketplace.IMarketplaceService;
import org.apereo.portal.portlet.marketplace.MarketplacePortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.PortletCategory;
import org.apereo.portal.rest.layout.MarketplaceEntry;
import org.apereo.portal.rest.layout.MarketplaceEntryRating;
import org.apereo.portal.security.AuthorizationPrincipalHelper;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MarketplaceRESTController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IMarketplaceService marketplaceService;
    private IMarketplaceRatingDao marketplaceRatingDAO;
    private IPersonManager personManager;

    @Autowired
    public void setMarketplaceService(IMarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setMarketplaceRatingDAO(IMarketplaceRatingDao marketplaceRatingDAO) {
        this.marketplaceRatingDAO = marketplaceRatingDAO;
    }

    @RequestMapping(value = "/marketplace/entries.json", method = RequestMethod.GET)
    public ModelAndView marketplaceEntriesFeed(HttpServletRequest request) {
        final IPerson user = personManager.getPerson(request);

        final Set<PortletCategory> empty =
                Collections.emptySet(); // Produces an complete/unfiltered collection
        final Set<MarketplaceEntry> marketplaceEntries =
                marketplaceService.browseableMarketplaceEntriesFor(user, empty);

        return new ModelAndView("json", "portlets", marketplaceEntries);
    }

    @RequestMapping(value = "/marketplace/entry/{fname}.json")
    public ModelAndView marketplaceEntryFeed(
            HttpServletRequest request, HttpServletResponse response, @PathVariable String fname) {
        final IPerson user = personManager.getPerson(request);
        final IAuthorizationPrincipal principal =
                AuthorizationPrincipalHelper.principalFromUser(user);

        final MarketplacePortletDefinition marketplacePortletDefinition =
                marketplaceService.getOrCreateMarketplacePortletDefinitionIfTheFnameExists(fname);
        if (marketplacePortletDefinition != null
                && marketplaceService.mayBrowsePortlet(principal, marketplacePortletDefinition)) {
            MarketplaceEntry entry = new MarketplaceEntry(marketplacePortletDefinition, true, user);
            entry.setCanAdd(marketplaceService.mayAddPortlet(user, marketplacePortletDefinition));

            return new ModelAndView("json", "entry", entry);
        }

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return null;
    }

    /** @since 5.0 */
    @RequestMapping(value = "/v5-0/marketplace/{fname}/ratings", method = RequestMethod.GET)
    public ModelAndView getPortletRatings(HttpServletRequest request, @PathVariable String fname) {

        // TODO:  This method should send 404 or 403 in appropriate circumstances

        Validate.notNull(fname, "Please supply a portlet to get rating for - should not be null");
        IPortletDefinition marketplacePortletDefinition =
                (IPortletDefinition)
                        marketplaceService.getOrCreateMarketplacePortletDefinitionIfTheFnameExists(
                                fname);

        final IPerson user = personManager.getPerson(request);
        final IAuthorizationPrincipal principal =
                AuthorizationPrincipalHelper.principalFromUser(user);
        if (principal.canManage(
                marketplacePortletDefinition.getPortletDefinitionId().getStringId())) {
            Set<IMarketplaceRating> portletRatings = marketplaceRatingDAO.getRatingsByFname(fname);

            if (portletRatings != null) {
                List<MarketplaceEntryRating> ratingResults = new ArrayList<>();
                for (IMarketplaceRating imr : portletRatings) {
                    ratingResults.add(new MarketplaceEntryRating(imr.getRating(), imr.getReview()));
                }

                return new ModelAndView("json", "ratings", ratingResults);
            }
        }

        return new ModelAndView("json", "ratings", null);
    }

    @RequestMapping(value = "/marketplace/{fname}/getRating", method = RequestMethod.GET)
    public ModelAndView getUserRating(HttpServletRequest request, @PathVariable String fname) {
        Validate.notNull(fname, "Please supply a portlet to get rating for - should not be null");
        IMarketplaceRating tempRating =
                marketplaceRatingDAO.getRating(
                        request.getRemoteUser(),
                        marketplaceService.getOrCreateMarketplacePortletDefinitionIfTheFnameExists(
                                fname));
        if (tempRating != null) {
            return new ModelAndView(
                    "json",
                    "rating",
                    new MarketplaceEntryRating(tempRating.getRating(), tempRating.getReview()));
        }
        return new ModelAndView("json", "rating", null);
    }

    @RequestMapping(value = "/marketplace/{fname}/rating/{rating}", method = RequestMethod.POST)
    public ModelAndView saveUserRating(
            HttpServletRequest request,
            @PathVariable String fname,
            @PathVariable String rating,
            @RequestParam(required = false) String review) {
        Validate.notNull(rating, "Please supply a rating - should not be null");
        Validate.notNull(fname, "Please supply a portlet to rate - should not be null");
        marketplaceRatingDAO.createOrUpdateRating(
                Integer.parseInt(rating),
                request.getRemoteUser(),
                review,
                marketplaceService.getOrCreateMarketplacePortletDefinitionIfTheFnameExists(fname));
        return new ModelAndView(
                "json", "rating", new MarketplaceEntryRating(Integer.parseInt(rating), review));
    }

    /**
     * Get all ratings done on portlets since daysBack ago.
     *
     * @param request
     * @param daysBack - number of days back to search for portlet ratings
     * @return
     */
    @RequestMapping(value = "/marketplace/{daysBack}/getRatings", method = RequestMethod.GET)
    public ModelAndView getPortletRatings(HttpServletRequest request, @PathVariable int daysBack) {

        Set<IMarketplaceRating> portlets = marketplaceRatingDAO.getAllRatingsInLastXDays(daysBack);

        List<MarketplaceEntryRating> ratings = new ArrayList<>();

        for (IMarketplaceRating portlet : portlets) {

            MarketplaceEntryRating portletRating =
                    new MarketplaceEntryRating(
                            portlet.getMarketplaceRatingPK().getUserName(),
                            portlet.getMarketplaceRatingPK().getPortletDefinition().getName(),
                            portlet.getMarketplaceRatingPK().getPortletDefinition().getFName(),
                            portlet.getRating(),
                            portlet.getReview(),
                            portlet.getRatingDate());

            ratings.add(portletRating);
        }

        return new ModelAndView("json", "ratings", ratings);
    }

    /**
     * Get all ratings done on a specific portlet since daysBack ago.
     *
     * @param request
     * @param fname - portlet fname
     * @param daysBack - number of days back to search for portlet ratings
     * @return
     */
    @RequestMapping(
        value = "/marketplace/{fname}/{daysBack}/getRatings",
        method = RequestMethod.GET
    )
    public ModelAndView getPortletRatings(
            HttpServletRequest request, @PathVariable String fname, @PathVariable int daysBack) {

        Set<IMarketplaceRating> portlets =
                marketplaceRatingDAO.getAllRatingsForPortletInLastXDays(daysBack, fname);

        List<MarketplaceEntryRating> ratings = new ArrayList<>();

        for (IMarketplaceRating portlet : portlets) {

            MarketplaceEntryRating portletRating =
                    new MarketplaceEntryRating(
                            portlet.getMarketplaceRatingPK().getUserName(),
                            portlet.getMarketplaceRatingPK().getPortletDefinition().getName(),
                            portlet.getMarketplaceRatingPK().getPortletDefinition().getFName(),
                            portlet.getRating(),
                            portlet.getReview(),
                            portlet.getRatingDate());

            ratings.add(portletRating);
        }

        return new ModelAndView("json", "ratings", ratings);
    }
}
