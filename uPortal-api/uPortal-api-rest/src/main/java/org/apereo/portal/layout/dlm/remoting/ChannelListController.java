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
package org.apereo.portal.layout.dlm.remoting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.UserPreferencesManager;
import org.apereo.portal.i18n.ILocaleStore;
import org.apereo.portal.i18n.LocaleManager;
import org.apereo.portal.i18n.LocaleManagerFactory;
import org.apereo.portal.layout.IUserLayout;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.dlm.remoting.registry.ChannelBean;
import org.apereo.portal.layout.dlm.remoting.registry.ChannelCategoryBean;
import org.apereo.portal.layout.dlm.remoting.registry.v43.PortletCategoryBean;
import org.apereo.portal.layout.dlm.remoting.registry.v43.PortletDefinitionBean;
import org.apereo.portal.portlet.marketplace.IMarketplaceService;
import org.apereo.portal.portlet.marketplace.MarketplacePortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.PortletCategory;
import org.apereo.portal.portlet.registry.IPortletCategoryRegistry;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlets.favorites.FavoritesUtils;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.services.AuthorizationServiceFacade;
import org.apereo.portal.spring.spel.IPortalSpELService;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.apereo.portal.utils.personalize.IPersonalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * A Spring controller that returns a JSON representation of portlets the user may access in the
 * portal.
 *
 * <p>As of uPortal 4.2, this will return the portlets the user is allowed to browse, regardless
 * whether the portlet has a category (previously it returned portlets the user could subscribe to
 * and left out portlets with no categories but this change makes this API in sync with search and
 * the marketplace and uses the BROWSE permission properly without overloading the meaning of
 * categories).
 */
@Slf4j
@Controller
public class ChannelListController {

    private static final String CATEGORIES_MAP_KEY = "categories";
    private static final String UNCATEGORIZED = "uncategorized";
    private static final String UNCATEGORIZED_DESC = "uncategorized.description";
    private static final String ICON_URL_PARAMETER_NAME = "iconUrl";

    /** Moved to PortletRESTController under /api/portlets.json */
    private static final String TYPE_MANAGE = "manage";

    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletCategoryRegistry portletCategoryRegistry;
    private IPersonManager personManager;
    private IPersonalizer personalizer;
    private IPortalSpELService spELService;
    private ILocaleStore localeStore;
    private LocaleManagerFactory localeManagerFactory;
    private MessageSource messageSource;
    private IAuthorizationService authorizationService;
    private IUserInstanceManager userInstanceManager;
    private FavoritesUtils favoritesUtils;

    @Autowired private IMarketplaceService marketplaceService;

    /** @param portletDefinitionRegistry The portlet registry bean */
    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    public void setPortletCategoryRegistry(IPortletCategoryRegistry portletCategoryRegistry) {
        this.portletCategoryRegistry = portletCategoryRegistry;
    }

    /**
     * For injection of the person manager. Used for authorization.
     *
     * @param personManager IPersonManager instance
     */
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setPortalSpELProvider(IPortalSpELService spELProvider) {
        this.spELService = spELProvider;
    }

    @Autowired
    public void setLocaleStore(ILocaleStore localeStore) {
        this.localeStore = localeStore;
    }

    @Autowired
    public void setLocaleManagerFactory(LocaleManagerFactory localeManagerFactory) {
        this.localeManagerFactory = localeManagerFactory;
    }

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Autowired
    public void setAuthorizationService(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setFavoritesUtils(FavoritesUtils favoritesUtils) {
        this.favoritesUtils = favoritesUtils;
    }

    @Autowired
    public void setPersonalizer(IPersonalizer personalizer) {
        this.personalizer = personalizer;
    }

    /**
     * Original, pre-4.3 version of this API. Always returns the entire contents of the Portlet
     * Registry, including uncategorized portlets, to which the user has access. Access is based on
     * the SUBSCRIBE permission.
     */
    @RequestMapping(value = "/portletList", method = RequestMethod.GET)
    public ModelAndView listChannels(
            WebRequest webRequest,
            HttpServletRequest request,
            @RequestParam(value = "type", required = false) String type) {

        if (TYPE_MANAGE.equals(type)) {
            throw new UnsupportedOperationException(
                    "Moved to PortletRESTController under /api/portlets.json");
        }

        final IPerson user = personManager.getPerson(request);
        final Map<String, SortedSet<?>> registry = getRegistryOriginal(webRequest, user);

        // Since type=manage was deprecated channels is always empty but retained for backwards
        // compatibility
        registry.put("channels", new TreeSet<ChannelBean>());

        return new ModelAndView("jsonView", "registry", registry);
    }

    /**
     * Updated version of this API. Supports an optional 'categoryId' parameter. If provided, this
     * URL will return the portlet registry beginning with the specified category, including all
     * descendants, and <em>excluding</em> uncategorized portlets. If no 'categoryId' is provided,
     * this method returns the portlet registry beginning with 'All Categories' (the root) and
     * <em>including</em> uncategorized portlets. Access is based on the SUBSCRIBE permission.
     *
     * @since 4.3
     */
    @RequestMapping(value = "/v4-3/dlm/portletRegistry.json", method = RequestMethod.GET)
    public ModelAndView getPortletRegistry(
            WebRequest webRequest,
            HttpServletRequest request,
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @RequestParam(value = "category", required = false) String categoryName,
            @RequestParam(value = "favorite", required = false) String favorite) {

        boolean includeUncategorizedPortlets = true; // default

        /*
         * Pick a category for the basis of the response
         */
        PortletCategory rootCategory =
                portletCategoryRegistry.getTopLevelPortletCategory(); // default
        if (StringUtils.isNotBlank(categoryId)) {
            // Callers can specify a category by Id...
            rootCategory = portletCategoryRegistry.getPortletCategory(categoryId);
            includeUncategorizedPortlets = false;
        } else if (StringUtils.isNotBlank(categoryName)) {
            // or by name...
            rootCategory = portletCategoryRegistry.getPortletCategoryByName(categoryName);
            includeUncategorizedPortlets = false;
        }

        if (rootCategory == null) {
            // A specific category was requested, but there was a problem obtaining it
            throw new IllegalArgumentException("Requested category not found");
        }

        final IPerson user = personManager.getPerson(request);

        /*
         * Gather the user's favorites
         */
        final Set<IPortletDefinition> favoritePortlets = calculateFavoritePortlets(request);

        Map<String, SortedSet<PortletCategoryBean>> rslt =
                getRegistry43(
                        webRequest,
                        user,
                        rootCategory,
                        includeUncategorizedPortlets,
                        favoritePortlets);

        /*
         * The 'favorite=true' option means return only portlets that this user has favorited.
         */
        if (Boolean.valueOf(favorite)) {
            log.debug(
                    "Filtering out non-favorite portlets because 'favorite=true' was included in the query string");
            rslt = filterRegistryFavoritesOnly(rslt);
        }

        return new ModelAndView("jsonView", "registry", rslt);
    }

    /*
     * Private methods that support the original (pre-4.3) version of the API
     */

    /**
     * Gathers and organizes the response based on the specified rootCategory and the permissions of
     * the specified user.
     */
    private Map<String, SortedSet<?>> getRegistryOriginal(WebRequest request, IPerson user) {

        /*
         * This collection of all the portlets in the portal is for the sake of
         * tracking which ones are uncategorized.
         */
        Set<IPortletDefinition> portletsNotYetCategorized =
                new HashSet<>(portletDefinitionRegistry.getAllPortletDefinitions());

        // construct a new channel registry
        Map<String, SortedSet<?>> rslt = new TreeMap<>();
        SortedSet<ChannelCategoryBean> categories = new TreeSet<>();

        // add the root category and all its children to the registry
        final PortletCategory rootCategory = portletCategoryRegistry.getTopLevelPortletCategory();
        final Locale locale = getUserLocale(user);
        categories.add(
                prepareCategoryBean(
                        request, rootCategory, portletsNotYetCategorized, user, locale));

        /*
         * uPortal historically has provided for a convention that portlets not in any category
         * may potentially be viewed by users but may not be subscribed to.
         *
         * As of uPortal 4.2, the logic below now takes any portlets the user has BROWSE access to
         * that have not already been identified as belonging to a category and adds them to a category
         * called Uncategorized.
         */

        EntityIdentifier ei = user.getEntityIdentifier();
        IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());

        // construct a new channel category bean for this category
        String uncategorizedString =
                messageSource.getMessage(UNCATEGORIZED, new Object[] {}, locale);
        ChannelCategoryBean uncategorizedPortletsBean =
                new ChannelCategoryBean(new PortletCategory(uncategorizedString));
        uncategorizedPortletsBean.setName(UNCATEGORIZED);
        uncategorizedPortletsBean.setDescription(
                messageSource.getMessage(UNCATEGORIZED_DESC, new Object[] {}, locale));

        for (IPortletDefinition portlet : portletsNotYetCategorized) {
            if (authorizationService.canPrincipalBrowse(ap, portlet)) {
                // construct a new channel bean from this channel
                ChannelBean channel = getChannel(portlet, request, locale, user);
                uncategorizedPortletsBean.addChannel(channel);
            }
        }
        // Add even if no portlets in category
        categories.add(uncategorizedPortletsBean);

        rslt.put(CATEGORIES_MAP_KEY, categories);
        return rslt;
    }

    private ChannelCategoryBean prepareCategoryBean(
            WebRequest request,
            PortletCategory category,
            Set<IPortletDefinition> portletsNotYetCategorized,
            IPerson user,
            Locale locale) {

        // construct a new channel category bean for this category
        ChannelCategoryBean categoryBean = new ChannelCategoryBean(category);
        categoryBean.setName(messageSource.getMessage(category.getName(), new Object[] {}, locale));

        // add the direct child channels for this category
        Set<IPortletDefinition> portlets = portletCategoryRegistry.getChildPortlets(category);
        EntityIdentifier ei = user.getEntityIdentifier();
        IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());

        for (IPortletDefinition portlet : portlets) {

            if (authorizationService.canPrincipalBrowse(ap, portlet)) {
                // construct a new channel bean from this channel
                ChannelBean channel = getChannel(portlet, request, locale, user);
                categoryBean.addChannel(channel);
            }

            /*
             * Remove the portlet from the uncategorized collection;
             * note -- this approach will not prevent portlets from
             * appearing in multiple categories (as appropriate).
             */
            portletsNotYetCategorized.remove(portlet);
        }

        /* Now add child categories. */
        for (PortletCategory childCategory :
                this.portletCategoryRegistry.getChildCategories(category)) {
            ChannelCategoryBean childCategoryBean =
                    prepareCategoryBean(
                            request, childCategory, portletsNotYetCategorized, user, locale);
            categoryBean.addCategory(childCategoryBean);
        }

        return categoryBean;
    }

    private ChannelBean getChannel(
            IPortletDefinition definition, WebRequest request, Locale locale, IPerson user) {
        ChannelBean channel = new ChannelBean();
        channel.setId(definition.getPortletDefinitionId().getStringId());
        channel.setDescription(definition.getDescription(locale.toString()));
        channel.setFname(definition.getFName());
        channel.setName(definition.getName(locale.toString()));
        channel.setState(definition.getLifecycleState().toString());
        channel.setTitle(definition.getTitle(locale.toString()));
        channel.setTypeId(definition.getType().getId());

        // See api docs for postProcessIconUrlParameter() below
        IPortletDefinitionParameter iconParameter =
                definition.getParameter(ICON_URL_PARAMETER_NAME);
        if (iconParameter != null) {
            IPortletDefinitionParameter evaluated =
                    postProcessIconUrlParameter(iconParameter, request);
            channel.setIconUrl(evaluated.getValue());
        }

        return channel;
    }

    /*
     * Private methods that support the 4.3 version of the API
     */

    /**
     * Gathers and organizes the response based on the specified rootCategory and the permissions of
     * the specified user.
     */
    private Map<String, SortedSet<PortletCategoryBean>> getRegistry43(
            WebRequest request,
            IPerson user,
            PortletCategory rootCategory,
            boolean includeUncategorized,
            Set<IPortletDefinition> favorites) {

        /*
         * This collection of all the portlets in the portal is for the sake of
         * tracking which ones are uncategorized.  They will be added to the
         * output if includeUncategorized=true.
         */
        Set<IPortletDefinition> portletsNotYetCategorized =
                includeUncategorized
                        ? new HashSet<>(portletDefinitionRegistry.getAllPortletDefinitions())
                        : new HashSet<>(); // Not necessary to fetch them if we're not
        // tracking them

        // construct a new channel registry
        Map<String, SortedSet<PortletCategoryBean>> rslt = new TreeMap<>();
        SortedSet<PortletCategoryBean> categories = new TreeSet<>();

        // add the root category and all its children to the registry
        final Locale locale = getUserLocale(user);
        categories.add(
                preparePortletCategoryBean(
                        request, rootCategory, portletsNotYetCategorized, user, locale, favorites));

        if (includeUncategorized) {
            /*
             * uPortal historically has provided for a convention that portlets not in any category
             * may potentially be viewed by users but may not be subscribed to.
             *
             * As of uPortal 4.2, the logic below now takes any portlets the user has BROWSE access to
             * that have not already been identified as belonging to a category and adds them to a category
             * called Uncategorized.
             */

            EntityIdentifier ei = user.getEntityIdentifier();
            IAuthorizationPrincipal ap =
                    AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());

            Set<PortletDefinitionBean> marketplacePortlets = new HashSet<>();
            for (IPortletDefinition portlet : portletsNotYetCategorized) {
                if (authorizationService.canPrincipalBrowse(ap, portlet)) {
                    PortletDefinitionBean pdb =
                            preparePortletDefinitionBean(
                                    request, portlet, locale, favorites.contains(portlet));
                    marketplacePortlets.add(pdb);
                }
            }

            // construct a new channel category bean for this category
            final String uncName = messageSource.getMessage(UNCATEGORIZED, new Object[] {}, locale);
            final String uncDescription =
                    messageSource.getMessage(UNCATEGORIZED_DESC, new Object[] {}, locale);
            PortletCategory pc =
                    new PortletCategory(
                            uncName); // Use of this String for Id matches earlier version of API
            pc.setName(uncName);
            pc.setDescription(uncDescription);
            PortletCategoryBean unc =
                    PortletCategoryBean.fromPortletCategory(pc, null, marketplacePortlets);

            // Add even if no portlets in category
            categories.add(unc);
        }

        rslt.put(CATEGORIES_MAP_KEY, categories);
        return rslt;
    }

    private PortletCategoryBean preparePortletCategoryBean(
            WebRequest req,
            PortletCategory category,
            Set<IPortletDefinition> portletsNotYetCategorized,
            IPerson user,
            Locale locale,
            Set<IPortletDefinition> favorites) {

        /* Prepare child categories. */
        Set<PortletCategoryBean> subcategories = new HashSet<>();
        for (PortletCategory childCategory :
                this.portletCategoryRegistry.getChildCategories(category)) {
            PortletCategoryBean childBean =
                    preparePortletCategoryBean(
                            req, childCategory, portletsNotYetCategorized, user, locale, favorites);
            subcategories.add(childBean);
        }

        // add the direct child channels for this category
        Set<IPortletDefinition> portlets = portletCategoryRegistry.getChildPortlets(category);
        EntityIdentifier ei = user.getEntityIdentifier();
        IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());

        Set<PortletDefinitionBean> marketplacePortlets = new HashSet<>();
        for (IPortletDefinition portlet : portlets) {

            if (authorizationService.canPrincipalBrowse(ap, portlet)) {
                PortletDefinitionBean pdb =
                        preparePortletDefinitionBean(
                                req, portlet, locale, favorites.contains(portlet));
                marketplacePortlets.add(pdb);
            }

            /*
             * Remove the portlet from the uncategorized collection;
             * note -- this approach will not prevent portlets from
             * appearing in multiple categories (as appropriate).
             */
            portletsNotYetCategorized.remove(portlet);
        }

        // construct a new portlet category bean for this category
        PortletCategoryBean categoryBean =
                PortletCategoryBean.fromPortletCategory(
                        category, subcategories, marketplacePortlets);
        categoryBean.setName(messageSource.getMessage(category.getName(), new Object[] {}, locale));

        return categoryBean;
    }

    private PortletDefinitionBean preparePortletDefinitionBean(
            WebRequest req, IPortletDefinition portlet, Locale locale, Boolean favorite) {
        MarketplacePortletDefinition mktpd =
                marketplaceService.getOrCreateMarketplacePortletDefinition(portlet);
        PortletDefinitionBean rslt =
                PortletDefinitionBean.fromMarketplacePortletDefinition(mktpd, locale, favorite);

        // See api docs for postProcessIconUrlParameter() below
        IPortletDefinitionParameter iconParameter =
                rslt.getParameters().get(ICON_URL_PARAMETER_NAME);
        if (iconParameter != null) {
            IPortletDefinitionParameter evaluated = postProcessIconUrlParameter(iconParameter, req);
            rslt.putParameter(evaluated);
        }

        return rslt;
    }

    /*
     * Implementation
     */

    private Locale getUserLocale(IPerson user) {
        // get user locale
        Locale[] locales = localeStore.getUserLocales(user);
        LocaleManager localeManager =
                localeManagerFactory.createLocaleManager(user, Arrays.asList(locales));
        return localeManager.getLocales().get(0);
    }

    /**
     * TODO: Clean this mess up some day; there are a few portlet-definitions that start with
     * ${request.contextPath} for the iconUrl parameter, presumably because uPortal can be deployed
     * to a context other than /uPortal. We should either...
     *
     * <p>- Discontinue SpEL in publishing parameters entirely; or - Extend it to parameters beyond
     * 'iconUrl'
     *
     * <p>And if we continue using SpEL in parameters, we should evaluate it when they're read out
     * of the database (long before now).
     *
     * <p>FWIW the /api/portlet/{fname}.json API does not process the SpEL and the
     * '${request.contextPath}' is included in the JSON output.
     */
    private IPortletDefinitionParameter postProcessIconUrlParameter(
            final IPortletDefinitionParameter iconUrl, WebRequest req) {
        if (!ICON_URL_PARAMETER_NAME.equals(iconUrl.getName())) {
            String msg =
                    "Only iconUrl should be processed this way;  parameter was:  "
                            + iconUrl.getName();
            throw new IllegalArgumentException(msg);
        }
        final String value = spELService.parseString(iconUrl.getValue(), req);
        return new IPortletDefinitionParameter() {
            @Override
            public String getName() {
                return ICON_URL_PARAMETER_NAME;
            }

            @Override
            public String getValue() {
                return value;
            }

            @Override
            public String getDescription() {
                return iconUrl.getDescription();
            }

            @Override
            public void setValue(String value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setDescription(String descr) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private Set<IPortletDefinition> calculateFavoritePortlets(HttpServletRequest request) {
        /*
         * It's not fantastic, but the storage strategy for favorites in the layout XML doc.  We
         * have to use it to know which portlets are favorites.
         */
        final IUserInstance ui = userInstanceManager.getUserInstance(request);
        final UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        final IUserLayoutManager ulm = upm.getUserLayoutManager();
        final IUserLayout layout = ulm.getUserLayout();
        final Set<IPortletDefinition> rslt = favoritesUtils.getFavoritePortletDefinitions(layout);

        log.debug(
                "Found the following favoritePortlets for user='{}':  {}",
                request.getRemoteUser(),
                rslt);
        return rslt;
    }

    private Map<String, SortedSet<PortletCategoryBean>> filterRegistryFavoritesOnly(
            Map<String, SortedSet<PortletCategoryBean>> registry) {

        final Set<PortletCategoryBean> inpt = registry.get(CATEGORIES_MAP_KEY);
        final SortedSet<PortletCategoryBean> otpt = new TreeSet<>();
        inpt.forEach(
                categoryIn -> {
                    final PortletCategoryBean categoryOut = filterCategoryFavoritesOnly(categoryIn);
                    if (categoryOut != null) {
                        otpt.add(categoryOut);
                    }
                });

        final Map<String, SortedSet<PortletCategoryBean>> rslt = new TreeMap<>();
        rslt.put(CATEGORIES_MAP_KEY, otpt);
        return rslt;
    }

    /**
     * Returns the filtered category, or <code>null</code> if there is no content remaining in the
     * category.
     */
    private PortletCategoryBean filterCategoryFavoritesOnly(PortletCategoryBean category) {

        // Subcategories
        final Set<PortletCategoryBean> subcategories = new HashSet<>();
        category.getSubcategories()
                .forEach(
                        sub -> {
                            final PortletCategoryBean filteredBean =
                                    filterCategoryFavoritesOnly(sub);
                            if (filteredBean != null) {
                                subcategories.add(filteredBean);
                            }
                        });

        // Portlets
        final Set<PortletDefinitionBean> portlets = new HashSet<>();
        category.getPortlets()
                .forEach(
                        child -> {
                            if (child.getFavorite()) {
                                log.debug(
                                        "Including portlet '{}' because it is a favorite:  {}",
                                        child.getFname());
                                portlets.add(child);
                            } else {
                                log.debug(
                                        "Skipping portlet '{}' because it IS NOT a favorite:  {}",
                                        child.getFname());
                            }
                        });

        return !subcategories.isEmpty() || !portlets.isEmpty()
                ? PortletCategoryBean.create(
                        category.getId(),
                        category.getName(),
                        category.getDescription(),
                        subcategories,
                        portlets)
                : null;
    }
}
