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

package org.jasig.portal.portlets.marketplace;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;

import org.jasig.portal.portlet.dao.IMarketplaceRatingDao;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.marketplace.IMarketplaceRating;
import org.jasig.portal.portlet.marketplace.IMarketplaceService;
import org.jasig.portal.portlet.marketplace.MarketplacePortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

/**
 * 
 * @author vertein
 * A controller with a public method to return a list of portlets
 */
@Controller
@RequestMapping("VIEW")
public class PortletMarketplaceController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static String SHOW_ROOT_CATEGORY_PREFERENCE_NAME="showRootCategory";

    private IMarketplaceService marketplaceService;
	private IPortalRequestUtils portalRequestUtils;
	private IPortletDefinitionRegistry portletDefinitionRegistry;
	private IPersonManager personManager;
	private IPortletCategoryRegistry portletCategoryRegistry;
	private IPortletDefinitionDao portletDefinitionDao;
	private IMarketplaceRatingDao marketplaceRatingDAO;


    @Autowired
    public void setMarketplaceService(IMarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

	@Autowired
	public void setPortletDefinitionDao(IPortletDefinitionDao portletDefinitionDao) {
        this.portletDefinitionDao = portletDefinitionDao;
    }

	@Autowired
	public void setMarketplaceRatingDAO(IMarketplaceRatingDao marketplaceRatingDAO) {
        this.marketplaceRatingDAO = marketplaceRatingDAO;
    }
	
	@Autowired
    public void setPortletCategoryRegistry(IPortletCategoryRegistry portletCategoryRegistry) {
        this.portletCategoryRegistry = portletCategoryRegistry;
    }
	
	@Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
	
	@Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }
	
	@Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    /**
     * Returns a view of the marketplace landing page
     * @param webRequest
     * @param portletRequest
     * @param model
     * @param initialFilter - optional request paramter.  Use to init filter on initial view
     * @return a string representing the initial view.
     */
    @RenderMapping
    public String initializeView(WebRequest webRequest, PortletRequest portletRequest, Model model, @RequestParam(required=false) String initialFilter){
        this.setUpInitialView(webRequest, portletRequest, model, initialFilter);
        return "jsp/Marketplace/portlet/view";
    }

    @RenderMapping(params="action=view")
    public String entryView(RenderRequest renderRequest, RenderResponse renderResponse, WebRequest webRequest, PortletRequest portletRequest, Model model){
        IPortletDefinition result = this.portletDefinitionRegistry.getPortletDefinitionByFname(portletRequest.getParameter("fName"));

        if(result == null){
            this.setUpInitialView(webRequest, portletRequest, model, null);
            return "jsp/Marketplace/portlet/view";
        }

        final HttpServletRequest servletRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        final IPerson user = personManager.getPerson(servletRequest);

        if (! this.marketplaceService.mayBrowsePortlet(user, result)) {
            // TODO: provide an error experience
            // currently at least blocks rendering the entry for the portlet the user is not authorized to see.
            this.setUpInitialView(webRequest, portletRequest, model, null);
            return "jsp/Marketplace/portlet/view";
        }


        MarketplacePortletDefinition mpDefinition = new MarketplacePortletDefinition(result, this.portletCategoryRegistry);
        IMarketplaceRating tempRatingImpl = marketplaceRatingDAO.getRating(portletRequest.getRemoteUser(),
                portletDefinitionDao.getPortletDefinitionByFname(result.getFName()));
        model.addAttribute("marketplaceRating", tempRatingImpl);
        model.addAttribute("reviewMaxLength", IMarketplaceRating.REVIEW_MAX_LENGTH);
        model.addAttribute("portlet", mpDefinition);
        model.addAttribute("shortURL",mpDefinition.getShortURL());
        return "jsp/Marketplace/portlet/entry";
    }
	
	/**
	 * Use to save the rating of portlet
	 * @param request
	 * @param response
	 * @param portletFName fname of the portlet to rate
	 * @param rating will be parsed to int
	 * @param review optional review to be saved along with rating
	 * @throws NumberFormatException if rating cannot be parsed to an int
	 */
    @ResourceMapping("saveRating")
    public void saveRating(ResourceRequest request, ResourceResponse response,
            PortletRequest portletRequest, @RequestParam String portletFName,
            @RequestParam String rating, @RequestParam(required=false) String review){
        Validate.notNull(rating, "Please supply a rating - should not be null");
        Validate.notNull(portletFName, "Please supply a portlet to rate - should not be null");
        marketplaceRatingDAO.createOrUpdateRating(Integer.parseInt(rating), 
            portletRequest.getRemoteUser(),
            review,
            portletDefinitionDao.getPortletDefinitionByFname(portletFName));
    }
	
    /**
     * @param request
     * @param response
     * @param portletRequest
     * @return 'rating' as a JSON object.  Can be null if rating doesn't exist.
     */
     @ResourceMapping("getRating")
         public String getRating(ResourceRequest request, ResourceResponse response, @RequestParam String portletFName,  PortletRequest portletRequest, Model model){
         Validate.notNull(portletFName, "Please supply a portlet to get rating for - should not be null");
         IMarketplaceRating tempRating = marketplaceRatingDAO.getRating(portletRequest.getRemoteUser(), portletDefinitionDao.getPortletDefinitionByFname(portletFName));
         model.addAttribute("rating",tempRating==null? null:tempRating.getRating());
         return "json";
     }


    private void setUpInitialView(WebRequest webRequest, PortletRequest portletRequest, Model model, String initialFilter){
        final HttpServletRequest servletRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        final PortletPreferences preferences = portletRequest.getPreferences();
        final boolean isLogLevelDebug = logger.isDebugEnabled();

        final IPerson user = personManager.getPerson(servletRequest);

        final Map<String,Set<?>> registry = getRegistry(user);
        @SuppressWarnings("unchecked")
        Set<MarketplacePortletDefinition> portletList = (Set<MarketplacePortletDefinition>) registry.get("portlets");
        model.addAttribute("channelBeanList", portletList);
        @SuppressWarnings("unchecked")
        Set<PortletCategory> categoryList = (Set<PortletCategory>) registry.get("categories");

        @SuppressWarnings("unchecked")
        Set<MarketplacePortletDefinition> featuredPortlets = (Set<MarketplacePortletDefinition>) registry.get("featured");
        model.addAttribute("featuredList", featuredPortlets);
        
        //Determine if the marketplace is going to show the root category
        String showRootCategoryPreferenceValue = preferences.getValue(SHOW_ROOT_CATEGORY_PREFERENCE_NAME, "false");
        boolean showRootCategory = Boolean.parseBoolean(showRootCategoryPreferenceValue);

        if(isLogLevelDebug){
            logger.debug("Going to show Root Category?: {}", Boolean.toString(showRootCategory));
        }

        if(showRootCategory == false){
            categoryList.remove(this.portletCategoryRegistry.getTopLevelPortletCategory());
        }

        model.addAttribute("categoryList", categoryList);
        model.addAttribute("initialFilter", initialFilter);
    }


    /**
     * Returns a set of MarketplacePortletDefinitions.  Supply a user to limit 
     * the set to only portlets the user can use.  If user is null, this will
     * return all portlets.  Setting user to null will superscede all other
     * parameters.
     *
     * @param user - non-null user to limit results by. This will filter results to
     *               only portlets that user can use.
     * @return a set of portlets filtered that user can use, and other parameters
    */
    public Map<String,Set<?>> getRegistry(final IPerson user){

        Map<String,Set<?>> registry = new TreeMap<String,Set<?>>();

        final Set<MarketplacePortletDefinition> visiblePortlets =
                this.marketplaceService.browseableMarketplaceEntriesFor(user);
        
        @SuppressWarnings("unchecked")
        final Set<PortletCategory> visibleCategories =
                this.marketplaceService.browseableNonEmptyPortletCategoriesFor(user);

        Set<MarketplacePortletDefinition> featuredPortlets = this.marketplaceService.featuredPortletsForUser(user);

        registry.put("portlets", visiblePortlets);
        registry.put("categories", visibleCategories);
        registry.put("featured", featuredPortlets);
        return registry;
    }
}
