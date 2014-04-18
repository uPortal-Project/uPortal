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

import java.util.HashSet;
import java.util.List;
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

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.portlet.dao.IMarketplaceRatingDao;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
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

    private static String SHOW_ALL_PORTLETS_PREFERENCE_NAME = "showAllPortlets";
    private static String SHOW_MANAGED_PORTLETS_PREFERENCE_NAME = "showManagedPortlets";
    private static String SHOW_ROOT_CATEGORY_PREFERENCE_NAME="showRootCategory";
	
	private IPortalRequestUtils portalRequestUtils;
	private IPortletDefinitionRegistry portletDefinitionRegistry;
	private IPersonManager personManager;
	private IPortletCategoryRegistry portletCategoryRegistry;
	private IPortletDefinitionDao portletDefinitionDao;
	private IMarketplaceRatingDao marketplaceRatingDAO;
	
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
        return "jsp/Marketplace/view";
    }

    @RenderMapping(params="action=view")
    public String entryView(RenderRequest renderRequest, RenderResponse renderResponse, WebRequest webRequest, PortletRequest portletRequest, Model model){
        IPortletDefinition result = this.portletDefinitionRegistry.getPortletDefinitionByFname(portletRequest.getParameter("fName"));
        if(result == null){
            this.setUpInitialView(webRequest, portletRequest, model, null);
            return "jsp/Marketplace/view";
        }
        MarketplacePortletDefinition mpDefinition = new MarketplacePortletDefinition(result, this.portletCategoryRegistry);
        IMarketplaceRating tempRatingImpl = marketplaceRatingDAO.getRating(portletRequest.getRemoteUser(),
                portletDefinitionDao.getPortletDefinitionByFname(result.getFName()));
        model.addAttribute("marketplaceRating", tempRatingImpl);
        model.addAttribute("reviewMaxLength", IMarketplaceRating.REVIEW_MAX_LENGTH);
        model.addAttribute("portlet", mpDefinition);
        model.addAttribute("deepLink",getDeepLink(portalRequestUtils.getPortletHttpRequest(portletRequest), mpDefinition));
        model.addAttribute("shortURL",mpDefinition.getShortURL());
        return "jsp/Marketplace/entry";
    }
	
	/**
	 * Use to save the rating of portlet
	 * @param request
	 * @param response
	 * @param portletFName fname of the portlet to rate
	 * @param rating will be parsed to int
	 * @param Review optional review to be saved along with rating
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
	
	/**
	 * Given a portlet and a servlet request, you get a deeplink to this portlet
	 * @param request servlet request contains the request URL
	 * @param portlet portlet contains the fname
	 * @return A direct URL to that portlet that can be shared with the world
	 */
	private String getDeepLink(HttpServletRequest request, MarketplacePortletDefinition portlet) {
		final String requestURL = request.getRequestURL().toString();
		final String requestURI = request.getRequestURI();
		StringBuilder deepLinkSB = new StringBuilder();
		deepLinkSB.append(requestURL != null ? requestURL.substring(0,requestURL.indexOf(requestURI)) : null);
		deepLinkSB.append(request.getServletContext().getContextPath());
		deepLinkSB.append("/p/").append(portlet.getFName());
		return deepLinkSB.toString();
	}
	
    private void setUpInitialView(WebRequest webRequest, PortletRequest portletRequest, Model model, String initialFilter){
        final HttpServletRequest servletRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        final PortletPreferences preferences = portletRequest.getPreferences();
        final boolean isLogLevelDebug = logger.isDebugEnabled();

        //Determine if the marketplace is going to show all the portlets
        //Default behavior is to show just the portlets a user can subscribe to
        String showAllPortletsPreferenceValue = preferences.getValue(SHOW_ALL_PORTLETS_PREFERENCE_NAME, "false");
        boolean showAllPortlets = Boolean.parseBoolean(showAllPortletsPreferenceValue);
        IPerson user = null;
        if(showAllPortlets == false){
            user = personManager.getPerson(servletRequest);
        }

        //Determine if the marketplace is going to show portlets that a user can manage, but can't subscribe to.
        //Note - if showing all portlets, this becomes moot.
        //Default behavior is to show portlets that users manage
        String showManagedPortletsPreferenceValue = preferences.getValue(SHOW_MANAGED_PORTLETS_PREFERENCE_NAME, "true");
        boolean showManagedPortlets = Boolean.parseBoolean(showManagedPortletsPreferenceValue);

        if(isLogLevelDebug){
            logger.debug("Going to show all portlets?: {}", Boolean.toString(showAllPortlets));
            logger.debug("Going to show managed portlets?: {}", Boolean.toString(showManagedPortlets));
        }

        Map<String,Set<?>> registry = getRegistry(user, showManagedPortlets);
        @SuppressWarnings("unchecked")
        Set<MarketplacePortletDefinition> portletList = (Set<MarketplacePortletDefinition>) registry.get("portlets");
        model.addAttribute("channelBeanList", portletList);
        @SuppressWarnings("unchecked")
        Set<PortletCategory> categoryList = (Set<PortletCategory>) registry.get("categories");

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
     * @param user - the user to limit results by. This will filter results to 
     *               only portlets that user can use. Null will return all portlets.
     * @param seeManage - additive parameter that will add/not add portlets to returned set.
     *                    true will add portlets to the returned set the user can manage.
     *                    false will not add additional portlets that user can manage.
     *                    If user is null, this parameter doesn't matter.
     * @return a set of portlets filtered that user can use, and other parameters
    */
    public Map<String,Set<?>> getRegistry(IPerson user, Boolean seeManage){
        // get a list of all channels 
        List<IPortletDefinition> allChannels = portletDefinitionRegistry.getAllPortletDefinitions();
        // sets up permissions if user is not null
        EntityIdentifier ei = null;
        IAuthorizationPrincipal ap = null;
        if(user !=null){
            ei = user.getEntityIdentifier();
            ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        }
        Map<String,Set<?>> registry = new TreeMap<String,Set<?>>();
        Set<MarketplacePortletDefinition> visiblePortlets = new HashSet<MarketplacePortletDefinition>();
        Set<PortletCategory> visibleCategories = new HashSet<PortletCategory>();
        for (IPortletDefinition channel : allChannels) {
            String chanPubId = channel.getPortletDefinitionId().getStringId();
            if(user==null                                   //if user is null, add portlet
                || ap.canSubscribe(chanPubId)               //if can subscribe, add
                || (seeManage && ap.canManage(chanPubId))){ //if can manage, add 
                    visiblePortlets.add(new MarketplacePortletDefinition(channel, portletCategoryRegistry));
                    visibleCategories.addAll(this.portletCategoryRegistry.getParentCategories(channel));
            }
        }
        registry.put("portlets", visiblePortlets);
        registry.put("categories", visibleCategories);
        return registry;
    }
}
