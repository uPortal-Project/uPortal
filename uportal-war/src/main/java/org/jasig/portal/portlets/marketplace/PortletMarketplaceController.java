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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.i18n.ILocaleStore;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.dlm.remoting.registry.ChannelBean;
import org.jasig.portal.layout.dlm.remoting.registry.ChannelCategoryBean;
import org.jasig.portal.portlet.dao.IMarketplaceRatingDao;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.spring.spel.IPortalSpELService;
import org.jasig.portal.url.IPortalRequestUtils;
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
	
	private IPortalRequestUtils portalRequestUtils;
	private IPortletDefinitionRegistry portletDefinitionRegistry;
	private IPersonManager personManager;
	private ILocaleStore localeStore;
	private IPortletCategoryRegistry portletCategoryRegistry;
	private IPortalSpELService spELService;
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
    public void setSpELService(IPortalSpELService spELService) {
        this.spELService = spELService;
    }
	
	@Autowired
    public void setPortletCategoryRegistry(IPortletCategoryRegistry portletCategoryRegistry) {
        this.portletCategoryRegistry = portletCategoryRegistry;
    }
	
	@Autowired
    public void setLocaleStore(ILocaleStore localeStore) {
        this.localeStore = localeStore;
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
	 * @param webRequest
	 * @param portletRequest
	 * @param model
	 * @return A string of the view.  Also sets the model attributes
	 * 
	 * Uses the user that is logged in.  Uses the public method getListOfChannelBeans to
	 * initialize list.  Doesn't return list of portlets that user is not allowed to use, but is allowed to manage
	 */
	@RenderMapping
	public String initializeView(WebRequest webRequest, PortletRequest portletRequest, Model model){
		this.setUpInitialView(webRequest, portletRequest, model);
		return "jsp/Marketplace/view";
	}
	
    @RenderMapping(params="action=view")
    public String entryView(RenderRequest renderRequest, RenderResponse renderResponse, WebRequest webRequest, PortletRequest portletRequest, Model model){
        IPortletDefinition result = this.portletDefinitionRegistry.getPortletDefinitionByFname(portletRequest.getParameter("fName"));
        if(result == null){
            this.setUpInitialView(webRequest, portletRequest, model);
            return "jsp/Marketplace/view";
        }
        MarketplacePortletDefinition mpDefinition = new MarketplacePortletDefinition(result, this.portletCategoryRegistry);
        IMarketplaceRating tempRatingImpl = marketplaceRatingDAO.getRating(portletRequest.getRemoteUser(),
                portletDefinitionDao.getPortletDefinitionByFname(result.getFName()));
        model.addAttribute("rating", tempRatingImpl==null ? null:tempRatingImpl.getRating());
        model.addAttribute("portlet", mpDefinition);
        model.addAttribute("deepLink",getDeepLink(portalRequestUtils.getPortletHttpRequest(portletRequest), mpDefinition));
        return "jsp/Marketplace/entry";
    }
	
	/**
	 * Use to save the rating of portlet
	 * @param request
	 * @param response
	 * @param portletRequest
	 * 
	 */
    @ResourceMapping("saveRating")
    public void saveRating(ResourceRequest request, ResourceResponse response, PortletRequest portletRequest, @RequestParam String portletFName, @RequestParam String rating){
        Validate.notNull(rating, "Please supply a rating - should not be null");
        Validate.notNull(portletFName, "Please supply a portlet to rate - should not be null");
        marketplaceRatingDAO.createOrUpdateRating(Integer.parseInt(rating), 
            portletRequest.getRemoteUser(), 
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
	
	private void setUpInitialView(WebRequest webRequest, PortletRequest portletRequest, Model model){
		final HttpServletRequest servletRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
		IPerson user = personManager.getPerson(servletRequest);
		ArrayList<ChannelBean> portletList = (ArrayList<ChannelBean>) getListOfChannelBeans(webRequest, user, true);
		model.addAttribute("channelBeanList", portletList);
	}
	
	/**
	 * @param request 
	 * @param user - the user to limit results by
	 * @param seeManage - true if would like portlet listing managed by user.
	 * @return a list of ChannelBeans representing porlets
	 */
	public List<ChannelBean> getListOfChannelBeans(WebRequest request, IPerson user, Boolean seeManage){
		Map<String,SortedSet<?>> registry = getRegistry(request, user, seeManage);
		@SuppressWarnings("unchecked")
		ArrayList<ChannelBean> channelList = (ArrayList<ChannelBean>) this.getListOfChannelBeansByChannelBean((SortedSet<ChannelBean>) registry.get("channels"));
		@SuppressWarnings("unchecked")
		ArrayList<ChannelBean> channelList2 = (ArrayList<ChannelBean>) this.getListOfChannelBeansByChannelCategoryBean((SortedSet<ChannelCategoryBean>) registry.get("categories"));
		channelList.addAll(channelList2);
		return channelList;
    }
	
	/**
	 * @param request 
	 * @param seeManage - true if would like portlet listing managed by user.
	 * @return a list of ChannelBeans representing portlets
	 */
	public List<ChannelBean> getListOfChannelBeans(WebRequest request){
		Map<String,SortedSet<?>> registry = getRegistry(request);
		@SuppressWarnings("unchecked")
		ArrayList<ChannelBean> channelList = (ArrayList<ChannelBean>) this.getListOfChannelBeansByChannelBean((SortedSet<ChannelBean>) registry.get("channels"));
		@SuppressWarnings("unchecked")
		ArrayList<ChannelBean> channelList2 = (ArrayList<ChannelBean>) this.getListOfChannelBeansByChannelCategoryBean((SortedSet<ChannelCategoryBean>) registry.get("categories"));
		channelList.addAll(channelList2);
		return channelList;
	}
	
	private List<ChannelBean> getListOfChannelBeansByChannelBean (SortedSet<ChannelBean> cbSet){
		ArrayList<ChannelBean> channelList = new ArrayList<ChannelBean>();
		Iterator<ChannelBean> it = cbSet.iterator();
		while(it.hasNext()){
			ChannelBean cb = it.next();
			channelList.add(cb);
		}
		return channelList;
	}
	
	private List<ChannelBean> getListOfChannelBeansByChannelCategoryBean (SortedSet<ChannelCategoryBean> ccbSet){
		ArrayList<ChannelBean> categoryList = new ArrayList<ChannelBean>();
		Iterator<ChannelCategoryBean> it = ccbSet.iterator();
		while(it.hasNext()){
			ChannelCategoryBean ccb = it.next();
			categoryList.addAll(ccb.getChannels());
			if(ccb.getCategories().size()>0){
				categoryList.addAll(getListOfChannelBeansByChannelCategoryBean(ccb.getCategories()));
			}
		}
		return categoryList;	
	}
	
    private Map<String,SortedSet<?>> getRegistry(WebRequest request) {
        // construct a new channel registry
        Map<String,SortedSet<?>> registry = new TreeMap<String,SortedSet<?>>();
        SortedSet<ChannelBean> channels = new TreeSet<ChannelBean>();
        registry.put("channels", channels);
        registry.put("categories", this.getAllCategories(request));
        return registry;
    }

    private Map<String,SortedSet<?>> getRegistry(WebRequest request, IPerson user, Boolean seeManage) {
        // get a list of all channels 
        List<IPortletDefinition> allChannels = portletDefinitionRegistry.getAllPortletDefinitions();
        // construct a new channel registry
        Map<String,SortedSet<?>> registry = new TreeMap<String,SortedSet<?>>();
        SortedSet<ChannelBean> channels = new TreeSet<ChannelBean>();
        // get user locale
        Locale[] locales = localeStore.getUserLocales(user);
        LocaleManager localeManager = new LocaleManager(user, locales);
        Locale locale = localeManager.getLocales()[0];
	    
        EntityIdentifier ei = user.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
	    
        if(seeManage == true){	    
            for (IPortletDefinition channel : allChannels) {
                if (ap.canManage(channel.getPortletDefinitionId().getStringId())) {
                    channels.add(getChannel(channel, request, locale));
                }
            }
        }
    
        registry.put("channels", channels);
        registry.put("categories", this.getAllCategories(request));
        return registry;
    }
	
	private ChannelCategoryBean addChildren(WebRequest request, PortletCategory category, List<IPortletDefinition> allChannels) {
		// construct a new channel category bean for this category
		ChannelCategoryBean categoryBean = new ChannelCategoryBean(category);
		// add the direct child channels for this category
		Set<IPortletDefinition> portlets = portletCategoryRegistry.getChildPortlets(category);		
		for(IPortletDefinition channelDef : portlets) {
			// remove the channel of the list of all channels
			allChannels.remove(channelDef);
		}
		/* Now add child categories. */
		for(PortletCategory childCategory : this.portletCategoryRegistry.getChildCategories(category)) {
			ChannelCategoryBean childCategoryBean = addChildren(request, childCategory, allChannels);
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
	
    private SortedSet<ChannelCategoryBean> getAllCategories(WebRequest request){
        // get a list of all channels 
        List<IPortletDefinition> allChannels = portletDefinitionRegistry.getAllPortletDefinitions();
        SortedSet<ChannelCategoryBean> categories = new TreeSet<ChannelCategoryBean>();
        PortletCategory rootCategory = portletCategoryRegistry.getTopLevelPortletCategory();
        categories.add(addChildren(request, rootCategory, allChannels));
        return categories;
    }
}
