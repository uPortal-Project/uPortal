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

package org.jasig.portal.layout.dlm.remoting;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.i18n.ILocaleStore;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.dlm.remoting.registry.ChannelBean;
import org.jasig.portal.layout.dlm.remoting.registry.ChannelCategoryBean;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.spring.spel.IPortalSpELService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>A Spring controller that returns a JSON representation of portlets the
 * user may access in the portal.</p>
 *
 * <p>As of uPortal 4.2, this will return the portlets the user is allowed to browse,
 * regardless whether the portlet has a category (previously it returned portlets
 * the user could subscribe to and left out portlets with no categories but this change
 * makes this API in sync with search and the marketplace and uses the BROWSE permission
 * properly without overloading the meaning of categories).</p>
 *
 * @author Drew Mazurek
 * @author Jen Bourey, jbourey@unicon.net
 */
@Controller
public class ChannelListController {

    private static final String UNCATEGORIZED = "uncategorized";
    private static final String UNCATEGORIZED_DESC = "uncategorized.description";

    /**
     * @deprecated Moved to PortletRESTController under /api/portlets.json
     */
    private static final String TYPE_MANAGE = "manage";

    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletCategoryRegistry portletCategoryRegistry;
    private IPersonManager personManager;
    private IPortalSpELService spELService;
    private ILocaleStore localeStore;
    private MessageSource messageSource;
    private IAuthorizationService authorizationService;

    /**
     * @param portletDefinitionRegistry
     */
    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    public void setPortletCategoryRegistry(IPortletCategoryRegistry portletCategoryRegistry) {
        this.portletCategoryRegistry = portletCategoryRegistry;
    }

    /**
     * <p>For injection of the person manager.  Used for authorization.</p>
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
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Autowired
    public void setAuthorizationService(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * Original, pre-4.3 version of this API.  Always returns the entire contents
     * of the Portlet Registry, including uncategorized portlets,  to which the
     * user has access.  Access is based on the SUBSCRIBE permission.
     */
    @RequestMapping(value="/portletList", method=RequestMethod.GET)
    public ModelAndView listChannels(WebRequest webRequest, HttpServletRequest request,
            @RequestParam(value="type",required=false) String type) {

        if(type != null && TYPE_MANAGE.equals(type)) {
            throw new UnsupportedOperationException("Moved to PortletRESTController under /api/portlets.json");
        }

        final IPerson user = personManager.getPerson(request);
        final PortletCategory rootCategory = portletCategoryRegistry.getTopLevelPortletCategory();
        final Map<String,SortedSet<?>> registry = getRegistry(webRequest, user, rootCategory, true);

        // Since type=manage was deprecated channels is always empty but retained for backwards compatibility
        registry.put("channels", new TreeSet<ChannelBean>());

        return new ModelAndView("jsonView", "registry", registry);
    }

    /**
     * Updated version of this API.  Supports an optional 'categoryId' parameter.
     * If provided, this URL will return the portlet registry beginning with the
     * specified category, including all descendants, and <em>excluding</em>
     * uncategorized portlets.  If no 'categoryId' is provided, this method
     * returns the portlet registry beginning with 'All Categories' (the root)
     * and <em>including</em> uncategorized portlets.  Access is based on the
     * SUBSCRIBE permission.
     *
     * @since 4.3
     */
    @RequestMapping(value="/v4-3/dlm/portletRegistry.json", method=RequestMethod.GET)
    public ModelAndView getPortletRegistry(WebRequest webRequest, HttpServletRequest request,
            @RequestParam(value="categoryId", required=false) String categoryId) {

        final PortletCategory rootCategory = categoryId != null
                ? portletCategoryRegistry.getPortletCategory(categoryId)
                : portletCategoryRegistry.getTopLevelPortletCategory();
        final boolean includeUncategorized = categoryId != null
                ? false  // Don't provide uncategorized portlets
                : true;  // if a specific category was requested

        final IPerson user = personManager.getPerson(request);
        final Map<String,SortedSet<?>> registry = getRegistry(webRequest, user, rootCategory, includeUncategorized);

        return new ModelAndView("jsonView", "registry", registry);
    }

    /*
     * Implementation
     */

    /**
     * Gathers and organizes the response based on the specified rootCategory
     * and the permissions of the specified user.
     */
    private Map<String,SortedSet<?>> getRegistry(WebRequest request, IPerson user,
            PortletCategory rootCategory, boolean includeUncategorized) {

        // get user locale
        Locale[] locales = localeStore.getUserLocales(user);
        LocaleManager localeManager = new LocaleManager(user, locales);
        Locale locale = localeManager.getLocales()[0];

        /*
         * This collection of all the portlets in the portal is for the sake of
         * tracking which ones are uncategorized.  They will be added to the
         * output if includeUncategorized=true.
         */
        Set<IPortletDefinition> portletsNotYetCategorized = includeUncategorized
                ? new HashSet<IPortletDefinition>(portletDefinitionRegistry.getAllPortletDefinitions())
                : new HashSet<IPortletDefinition>();  // Not necessary to fetch them if we're not tracking them

        // construct a new channel registry
        Map<String,SortedSet<?>> rslt = new TreeMap<String,SortedSet<?>>();
        SortedSet<ChannelCategoryBean> categories = new TreeSet<ChannelCategoryBean>();

        // add the root category and all its children to the registry
        categories.add(prepareCategoryBean(request, rootCategory, portletsNotYetCategorized, user, locale));

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
            IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

            // construct a new channel category bean for this category
            String uncategorizedString = messageSource.getMessage(UNCATEGORIZED, new Object[] {}, locale);
            ChannelCategoryBean uncategorizedPortletsBean = new ChannelCategoryBean(new PortletCategory(uncategorizedString));
            uncategorizedPortletsBean.setName(UNCATEGORIZED);
            uncategorizedPortletsBean.setDescription(messageSource.getMessage(UNCATEGORIZED_DESC, new Object[] {}, locale));

            for (IPortletDefinition portlet : portletsNotYetCategorized) {
                if (authorizationService.canPrincipalBrowse(ap, portlet)) {
                    // construct a new channel bean from this channel
                    ChannelBean channel = getChannel(portlet, request, locale);
                    uncategorizedPortletsBean.addChannel(channel);
                }
            }
            // Add even if no portlets in category
            categories.add(uncategorizedPortletsBean);
        }

        rslt.put("categories", categories);
        return rslt;
    }

    private ChannelCategoryBean prepareCategoryBean(WebRequest request, PortletCategory category,
            Set<IPortletDefinition> portletsNotYetCategorized, IPerson user, Locale locale) {

        // construct a new channel category bean for this category
        ChannelCategoryBean categoryBean = new ChannelCategoryBean(category);
        categoryBean.setName(messageSource.getMessage(category.getName(), new Object[] {}, locale));

        // add the direct child channels for this category
        Set<IPortletDefinition> portlets = portletCategoryRegistry.getChildPortlets(category);
        EntityIdentifier ei = user.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

        for(IPortletDefinition portlet : portlets) {

            if (authorizationService.canPrincipalBrowse(ap, portlet)) {
                // construct a new channel bean from this channel
                ChannelBean channel = getChannel(portlet, request, locale);
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
        for(PortletCategory childCategory : this.portletCategoryRegistry.getChildCategories(category)) {
            ChannelCategoryBean childCategoryBean = prepareCategoryBean(request, childCategory, portletsNotYetCategorized, user, locale);
            categoryBean.addCategory(childCategoryBean);
        }

        return categoryBean;

    }

    private ChannelBean getChannel(IPortletDefinition definition, WebRequest request, Locale locale) {
        ChannelBean channel = new ChannelBean();
        channel.setId(definition.getPortletDefinitionId().getStringId());
        channel.setDescription(definition.getDescription(locale.toString()));
        channel.setFname(definition.getFName());
        channel.setName(definition.getName(locale.toString()));
        channel.setState(definition.getLifecycleState().toString());
        channel.setTitle(definition.getTitle(locale.toString()));
        channel.setTypeId(definition.getType().getId());

        IPortletDefinitionParameter iconParameter = definition.getParameter("iconUrl");
        if (iconParameter != null) {
            String iconUrl = spELService.parseString(iconParameter.getValue(), request);
            channel.setIconUrl(iconUrl);
        }

        return channel;
    }

}
