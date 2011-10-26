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

package org.jasig.portal.layout.dlm.remoting;

import java.util.List;
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
 * <p>A Spring controller that returns a JSON or XML view of channels.  For
 * non-admins, this will only display the channels the user is allowed to
 * manage or subscribe to.  Admins have a choice of viewing manageable,
 * subscribable, or all channels by the "type" request parameter.</p>
 * <p>Request parameters:</p>
 * <ul>
 *   <li>xml: if "true", return an XML view of the channels rather than a
 *   JSON view</li>
 *   <li>type: "subscribe", "manage", or "all".  Displays subscribable,
 *   manageable, or all channels (admin only).  Default is subscribable.
 * </ul>
 *
 * @author Drew Mazurek
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revsion$
 */
@Controller
@RequestMapping("/portletList")
public class ChannelListController {

	private IPortletDefinitionRegistry portletDefinitionRegistry;
	private IPortletCategoryRegistry portletCategoryRegistry;
	private IPersonManager personManager;
	private IPortalSpELService spELService;
	private ILocaleStore localeStore;
	private MessageSource messageSource;
	private static final String TYPE_SUBSCRIBE = "subscribe";
	private static final String TYPE_MANAGE = "manage";
	
	/**
	 * 
	 * @param request
	 * @param type
	 * @param asXml
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView listChannels(WebRequest webRequest, HttpServletRequest request, @RequestParam(value="type",required=false) String type) {
		if(type == null || (!type.equals(TYPE_MANAGE))) {
			type = TYPE_SUBSCRIBE;
		}
		IPerson user = personManager.getPerson(request);

		Map<String,SortedSet<?>> registry = getRegistry(webRequest, user, type);

		return new ModelAndView("jsonView", "registry", registry);
	}
	
	private Map<String,SortedSet<?>> getRegistry(WebRequest request, IPerson user, String type) {
		
		// get a list of all channels 
		List<IPortletDefinition> allChannels = portletDefinitionRegistry.getAllPortletDefinitions();
		
		// construct a new channel registry
		Map<String,SortedSet<?>> registry = new TreeMap<String,SortedSet<?>>();
	    SortedSet<ChannelCategoryBean> categories = new TreeSet<ChannelCategoryBean>();
	    SortedSet<ChannelBean> channels = new TreeSet<ChannelBean>();

	    // get user locale
	    Locale[] locales = localeStore.getUserLocales(user);
	    LocaleManager localeManager = new LocaleManager(user, locales);
	    Locale locale = localeManager.getLocales()[0];
		
		// add the root category and all its children to the registry
		PortletCategory rootCategory = portletCategoryRegistry.getTopLevelPortletCategory();
		categories.add(addChildren(request, rootCategory, allChannels, user, type, locale));

	    /*
	     * uPortal historically has provided for a convention that channels
	     * not in any category may potentially be viewed by users but may not
	     * be subscribed to.  We'd like administrators to still be able to 
	     * modify these channels through the portlet administration tool.  The
	     * logic below takes any channels that have not already been identified
	     * as belonging to a category and adds them to the top-level of the 
	     * registry, assuming the current user has manage permissions.
	     */
	    
		EntityIdentifier ei = user.getEntityIdentifier();
	    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

	    if (type.equals(TYPE_MANAGE)) {
	        for (IPortletDefinition channel : allChannels) {
	            if (ap.canManage(channel.getPortletDefinitionId().getStringId())) {
	                channels.add(getChannel(channel, request, locale));
	            }
	        }
	    }
		
	    registry.put("channels", channels);
	    registry.put("categories", categories);
	    
		return registry;
	}
	
	private ChannelCategoryBean addChildren(WebRequest request, PortletCategory category, List<IPortletDefinition> allChannels, IPerson user, String type, Locale locale) {
		
		// construct a new channel category bean for this category
		ChannelCategoryBean categoryBean = new ChannelCategoryBean(category);
        categoryBean.setName(messageSource.getMessage(category.getName(), new Object[] {}, locale));
		
		// add the direct child channels for this category
		Set<IPortletDefinition> portlets = portletCategoryRegistry.getChildPortlets(category);		
		EntityIdentifier ei = user.getEntityIdentifier();
	    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
		boolean isManage = type.equals(TYPE_MANAGE);
		
		for(IPortletDefinition channelDef : portlets) {
			
			if ((isManage && ap.canManage(channelDef.getPortletDefinitionId()
					.getStringId()))
					|| (!isManage && ap.canSubscribe(channelDef
							.getPortletDefinitionId().getStringId()))) {
				// construct a new channel bean from this channel
				ChannelBean channel = getChannel(channelDef, request, locale);
				categoryBean.addChannel(channel);
			}
			
			// remove the channel of the list of all channels
			allChannels.remove(channelDef);
		}

		/* Now add child categories. */
		for(PortletCategory childCategory : this.portletCategoryRegistry.getChildCategories(category)) {
			
			// TODO subscribe check?
			ChannelCategoryBean childCategoryBean = addChildren(request, childCategory, allChannels, user, type, locale);
			
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
	
	/**
	 * @param channelRegistryStore
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
}
