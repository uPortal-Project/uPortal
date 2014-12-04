package org.jasig.portal.rest;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.dao.IMarketplaceRatingDao;
import org.jasig.portal.portlet.marketplace.IMarketplaceRating;
import org.jasig.portal.portlet.marketplace.IMarketplaceService;
import org.jasig.portal.rest.layout.MarketplaceEntry;
import org.jasig.portal.rest.layout.MarketplaceEntryRating;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
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

        Set<MarketplaceEntry> marketplaceEntries = marketplaceService.browseableMarketplaceEntriesFor(user);

        return new ModelAndView("json", "portlets", marketplaceEntries);
    }
    
    @RequestMapping(value="/marketplace/{fname}/getRating", method = RequestMethod.GET)
    public ModelAndView getUserRating(HttpServletRequest request, @PathVariable String fname) {
        Validate.notNull(fname, "Please supply a portlet to get rating for - should not be null");
        final IPerson user = personManager.getPerson(request);
        final IMarketplaceRating tempRating =
            marketplaceRatingDAO.getRating(request.getRemoteUser(),
            marketplaceService.marketplacePortletDefinitionByFname(fname, user));
        if(tempRating != null) {
            return new ModelAndView("json", "rating", new MarketplaceEntryRating(tempRating.getRating(), tempRating.getReview()));
        }
        return new ModelAndView("json", "rating", null);
    }
    
    @RequestMapping(value="/marketplace/{fname}/rating/{rating}", method = RequestMethod.POST)
    public ModelAndView saveUserRating(HttpServletRequest request, 
            @PathVariable String fname, 
            @PathVariable String rating,
            @RequestParam(required = false) String review) {
        Validate.notNull(rating, "Please supply a rating - should not be null");
        Validate.notNull(fname, "Please supply a portlet to rate - should not be null");

        final IPerson person = personManager.getPerson(request);

        marketplaceRatingDAO.createOrUpdateRating(Integer.parseInt(rating), 
            request.getRemoteUser(),
            review,
            marketplaceService.marketplacePortletDefinitionByFname(fname, person));
        return new ModelAndView("json", "rating", new MarketplaceEntryRating(Integer.parseInt(rating), review));
    }

}
