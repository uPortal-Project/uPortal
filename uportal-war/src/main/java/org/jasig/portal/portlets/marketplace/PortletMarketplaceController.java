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
package org.jasig.portal.portlets.marketplace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.Validate;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.UserPreferencesManager;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.dlm.DistributedUserLayout;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.UserLayoutChannelDescription;
import org.jasig.portal.portlet.dao.IMarketplaceRatingDao;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.marketplace.IMarketplaceRating;
import org.jasig.portal.portlet.marketplace.IMarketplaceService;
import org.jasig.portal.portlet.marketplace.MarketplacePortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlets.favorites.FavoritesUtils;
import org.jasig.portal.rest.layout.MarketplaceEntry;
import org.jasig.portal.security.AuthorizationPrincipalHelper;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.sf.ehcache.Cache;

import static java.lang.String.format;

/**
 * 
 * @author vertein
 * A controller with a public method to return a list of portlets
 */
@Controller
@RequestMapping("VIEW")
public class PortletMarketplaceController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static String SHOW_ROOT_CATEGORY_PREFERENCE = "showRootCategory";

    /**
     * Optional, multi-valued preference that (if specified) limits the portlets
     * displayed in the Marketplace to those belonging to one or more of these
     * categories.
     *
     * @since 4.3
     */
    private static String PERMITTED_CATEGORIES_PREFERENCE = "permittedCategories";

    private static String ENABLE_REVIEWS_PREFERENCE = "PortletMarketplaceController.enableReviews";
    private static String ENABLE_REVIEWS_DEFAULT = "true";

    /**
     * Caches objects related to the ability to limit the portlets displayed
     * in a single publication of the Marketplace.
     */
    @Autowired
    @Qualifier(value = "org.jasig.portal.portlet.marketplace.MarketplaceService.marketplaceCategoryCache")
    private Cache marketplaceCategoryCache;

    private IMarketplaceService marketplaceService;
    private IPortalRequestUtils portalRequestUtils;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPersonManager personManager;
    private IPortletCategoryRegistry portletCategoryRegistry;
    private IPortletDefinitionDao portletDefinitionDao;
    private IMarketplaceRatingDao marketplaceRatingDAO;
    private IUserInstanceManager userInstanceManager;
    private IUserLayoutStore userLayoutStore;

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

    @Autowired
    public void setUserInstanceManager(final IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setUserLayoutStore(final IUserLayoutStore userLayoutStore) {
        this.userLayoutStore = userLayoutStore;
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
        final IAuthorizationPrincipal principal = AuthorizationPrincipalHelper.principalFromUser(user);

        if (! this.marketplaceService.mayBrowsePortlet(principal, result)) {
            // TODO: provide an error experience
            // currently at least blocks rendering the entry for the portlet the user is not authorized to see.
            this.setUpInitialView(webRequest, portletRequest, model, null);
            return "jsp/Marketplace/portlet/view";
        }

        MarketplacePortletDefinition mpDefinition = marketplaceService.getOrCreateMarketplacePortletDefinition(result);
        IMarketplaceRating tempRatingImpl = marketplaceRatingDAO.getRating(portletRequest.getRemoteUser(),
                portletDefinitionDao.getPortletDefinitionByFname(result.getFName()));
        final MarketplaceEntry marketplaceEntry = new MarketplaceEntry(mpDefinition, user);
        model.addAttribute("marketplaceRating", tempRatingImpl);
        model.addAttribute("reviewMaxLength", IMarketplaceRating.REVIEW_MAX_LENGTH);
        model.addAttribute("marketplaceEntry", marketplaceEntry);
        model.addAttribute("shortURL", mpDefinition.getShortURL());

        // Reviews feature enabled?
        final PortletPreferences prefs = renderRequest.getPreferences();
        final String enableReviewsPreferenceValue = prefs.getValue(ENABLE_REVIEWS_PREFERENCE, ENABLE_REVIEWS_DEFAULT);
        model.addAttribute("enableReviews", Boolean.valueOf(enableReviewsPreferenceValue));

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

        // Make certain reviews are permitted before trying to save one
        final PortletPreferences prefs = request.getPreferences();
        final String enableReviewsPreferenceValue = prefs.getValue(ENABLE_REVIEWS_PREFERENCE, ENABLE_REVIEWS_DEFAULT);
        if (!Boolean.valueOf(enableReviewsPreferenceValue)) {
            // Clear the parameter if sent...
            review = null;
        }

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

    @ResourceMapping("layoutInfo")
    public String getLayoutInfo(ResourceRequest request, @RequestParam String portletFName, Model model) throws TransformerException {
        Validate.notNull(portletFName, "Please supply a portlet fname");

        final HttpServletRequest servletRequest = this.portalRequestUtils.getPortletHttpRequest(request);
        IUserInstance ui = userInstanceManager.getUserInstance(servletRequest);

        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        IPerson person = ui.getPerson();
        DistributedUserLayout userLayout = userLayoutStore.getUserLayout(person, upm.getUserProfile());

        List<PortletTab> tabs = getPortletTabInfo(userLayout, portletFName);
        boolean isFavorite = isPortletFavorited(ulm.getUserLayout(), portletFName);

        model.addAttribute("favorite", isFavorite);
        model.addAttribute("tabs", tabs);

        return "json";
    }

    private void setUpInitialView(WebRequest webRequest, PortletRequest portletRequest, Model model, String initialFilter){

        // We'll track and potentially log the time it takes to perform this initialization
        final long timestamp = System.currentTimeMillis();

        final HttpServletRequest servletRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        final PortletPreferences preferences = portletRequest.getPreferences();
        final boolean isLogLevelDebug = logger.isDebugEnabled();

        final IPerson user = personManager.getPerson(servletRequest);

        final Map<String,Set<?>> registry = getRegistry(user, portletRequest);
        @SuppressWarnings("unchecked")
        final Set<MarketplaceEntry> marketplaceEntries =
            (Set<MarketplaceEntry>) registry.get("portlets");
        model.addAttribute("marketplaceEntries", marketplaceEntries);
        @SuppressWarnings("unchecked")
        Set<PortletCategory> categoryList = (Set<PortletCategory>) registry.get("categories");

        @SuppressWarnings("unchecked")
        final Set<MarketplaceEntry> featuredPortlets =
            (Set<MarketplaceEntry>) registry.get("featured");

        model.addAttribute("featuredEntries", featuredPortlets);

        //Determine if the marketplace is going to show the root category
        String showRootCategoryPreferenceValue = preferences.getValue(SHOW_ROOT_CATEGORY_PREFERENCE, "false");
        boolean showRootCategory = Boolean.parseBoolean(showRootCategoryPreferenceValue);

        if(isLogLevelDebug){
            logger.debug("Going to show Root Category?: {}", Boolean.toString(showRootCategory));
        }

        if(showRootCategory == false){
            categoryList.remove(this.portletCategoryRegistry.getTopLevelPortletCategory());
        }

        model.addAttribute("categoryList", categoryList);
        model.addAttribute("initialFilter", initialFilter);

        logger.debug("Marketplace took {}ms in setUpInitialView for user '{}'",
                System.currentTimeMillis() - timestamp, user.getUserName());

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
    public Map<String,Set<?>> getRegistry(final IPerson user, final PortletRequest req){

        Map<String,Set<?>> registry = new TreeMap<String,Set<?>>();

        // Empty, or the set of categories that are permitted to
        // be displayed in the Portlet Marketplace (portlet)
        final Set<PortletCategory> permittedCategories = getPermittedCategories(req);

        final Set<MarketplaceEntry> visiblePortlets =
                this.marketplaceService.browseableMarketplaceEntriesFor(user, permittedCategories);

        final Set<PortletCategory> visibleCategories =
                this.marketplaceService.browseableNonEmptyPortletCategoriesFor(user, permittedCategories);

        final Set<MarketplaceEntry> featuredPortlets =
            this.marketplaceService.featuredEntriesForUser(user, permittedCategories);

        registry.put("portlets", visiblePortlets);
        registry.put("categories", visibleCategories);
        registry.put("featured", featuredPortlets);

        return registry;
    }

    private boolean isPortletFavorited(IUserLayout layout, String fname) {
        List<IUserLayoutNodeDescription> favorites = FavoritesUtils.getFavoritePortlets(layout);
        for (IUserLayoutNodeDescription favorite : favorites) {
            if (favorite instanceof UserLayoutChannelDescription) {
                String channelId = ((UserLayoutChannelDescription) favorite).getChannelPublishId();
                IPortletDefinition portletDefinition = portletDefinitionRegistry.getPortletDefinition(channelId);
                String favFName = portletDefinition.getFName();

                if (fname != null && fname.equals(favFName)) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<PortletTab> getPortletTabInfo(DistributedUserLayout layout, String fname) {
        final String XPATH_TAB = "/layout/folder/folder[@hidden = 'false' and @type = 'regular']";
        final String XPATH_COUNT_COLUMNS = "count(./folder[@hidden = \"false\"])";
        final String XPATH_COUNT_NON_EDITABLE_COLUMNS = "count(./folder[@hidden = \"false\" and @*[local-name() = \"editAllowed\"] = \"false\"])";
        final String XPATH_GET_TAB_PORTLET_FMT = ".//channel[@hidden = \"false\" and @fname = \"%s\"]";

        Document doc = layout.getLayout();

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        try {
            XPathExpression tabExpr = xpath.compile(XPATH_TAB);
            NodeList list = (NodeList)tabExpr.evaluate(doc, XPathConstants.NODESET);

            // Count columns and non-editable columns...
            XPathExpression columnCountExpr = xpath.compile(XPATH_COUNT_COLUMNS);
            XPathExpression nonEditableCountExpr = xpath.compile(XPATH_COUNT_NON_EDITABLE_COLUMNS);

            // get the list of tabs...
            String xpathStr = format(XPATH_GET_TAB_PORTLET_FMT, fname);
            XPathExpression portletExpr = xpath.compile(xpathStr);

            List<PortletTab> tabs = new ArrayList<>();
            for (int i = 0; i < list.getLength(); i++) {
                Node tab = list.item(i);
                String tabName = ((Element)tab).getAttribute("name");
                String tabId = ((Element)tab).getAttribute("ID");

                // check if tab is editable...
                Number columns = (Number)columnCountExpr.evaluate(tab, XPathConstants.NUMBER);
                Number nonEditColumns = (Number)nonEditableCountExpr.evaluate(tab, XPathConstants.NUMBER);
                // tab is not editable...  skip it...
                if (columns.intValue() > 0 && columns.intValue() == nonEditColumns.intValue()) {
                    continue;
                }

                // get all instances of this portlet on this tab...
                List<String> layoutIds = new ArrayList<>();
                NodeList fnameListPerTab = (NodeList)portletExpr.evaluate(tab, XPathConstants.NODESET);
                for (int j = 0; j < fnameListPerTab.getLength(); j++) {
                    Node channel = fnameListPerTab.item(j);

                    String layoutId = ((Element)channel).getAttribute("ID");
                    layoutIds.add(layoutId);
                }

                PortletTab tabInfo = new PortletTab(tabName, tabId, layoutIds);
                tabs.add(tabInfo);
            }

            return tabs;

        } catch (XPathExpressionException e) {
            logger.error("Error evaluating xpath", e);
        }

        return null;
    }

    public static final class PortletTab {
        private final String name;
        private final String id;
        private final List<String> layoutIds;

        public PortletTab(final String name, final String id, final List<String> layoutIds) {
            this.name = name;
            this.id = id;
            this.layoutIds = (layoutIds == null) ? Collections.<String>emptyList() : layoutIds;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public List<String> getLayoutIds() {
            return Collections.unmodifiableList(layoutIds);
        }
    }

    private Set<PortletCategory> getPermittedCategories(PortletRequest req) {

        Set<PortletCategory> rslt = Collections.emptySet();  // default
        final PortletPreferences prefs = req.getPreferences();
        final String[] permittedCategories = prefs.getValues(PERMITTED_CATEGORIES_PREFERENCE, new String[0]);

        if (permittedCategories.length != 0) {
            // Expensive to create, use cache for this collection...
            Set<String> cacheKey = new HashSet<>(Arrays.asList(permittedCategories));
            net.sf.ehcache.Element cacheElement = marketplaceCategoryCache.get(cacheKey);

            if (cacheElement == null) {
                // Nothing in cache currently;  need to populate cache
                HashSet<PortletCategory> portletCategories = new HashSet<>();
                for (final String categoryName : permittedCategories) {
                    EntityIdentifier[] cats = GroupService.searchForGroups(categoryName, IGroupConstants.IS, IPortletDefinition.class);
                    if (cats != null && cats.length > 0) {
                        PortletCategory pc = portletCategoryRegistry.getPortletCategory(cats[0].getKey());
                        if (pc != null) {
                            portletCategories.add(pc);
                        } else {
                            logger.warn("No PortletCategory found in portletCategoryRegistry for id '{}'", cats[0].getKey());
                        }
                    } else {
                        logger.warn("No category found in GroupService for name '{}'", categoryName);
                    }
                }
                /*
                 * Sanity Check:  Since at least 1 category name was specified, we
                 * need to make certain there's at least 1 PortletCategory in the
                 * set;  otherwise, a restricted Marketplace portlet would become
                 * an unrestricted one.
                 */
                if (portletCategories.isEmpty()) {
                    throw new IllegalStateException("None of the specified category "
                            + "names could be resolved to a PortletCategory:  "
                            + Arrays.asList(permittedCategories));
                }
                cacheElement = new net.sf.ehcache.Element(cacheKey, portletCategories);
                this.marketplaceCategoryCache.put(cacheElement);
            }
            rslt = (Set<PortletCategory>) cacheElement.getObjectValue();
        }
        return rslt;
    }

}
