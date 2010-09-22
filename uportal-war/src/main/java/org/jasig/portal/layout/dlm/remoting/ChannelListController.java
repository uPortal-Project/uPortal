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
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.ChannelCategory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;
import org.jasig.portal.layout.dlm.remoting.registry.ChannelBean;
import org.jasig.portal.layout.dlm.remoting.registry.ChannelCategoryBean;
import org.jasig.portal.layout.dlm.remoting.registry.ChannelParameterBean;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.spring.spel.IPortalSpELService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/channelList")
public class ChannelListController {

	private IChannelRegistryStore channelRegistryStore;
	private IPersonManager personManager;
	private IPortalSpELService spELService;
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
		List<IChannelDefinition> allChannels = channelRegistryStore.getChannelDefinitions();

		// construct a new channel registry
		Map<String,SortedSet<?>> registry = new TreeMap<String,SortedSet<?>>();
	    SortedSet<ChannelCategoryBean> categories = new TreeSet<ChannelCategoryBean>();
	    SortedSet<ChannelBean> channels = new TreeSet<ChannelBean>();

		
		// add the root category and all its children to the registry
		ChannelCategory rootCategory = channelRegistryStore.getTopLevelChannelCategory();
		categories.add(addChildren(request, rootCategory, allChannels, user, type));

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
	        for (IChannelDefinition channel : allChannels) {
	            if (ap.canManage(channel.getId())) {
	                channels.add(getChannel(channel, request));
	            }
	        }
	    }
		
	    registry.put("channels", channels);
	    registry.put("categories", categories);
	    
		return registry;
	}
	
	private ChannelCategoryBean addChildren(WebRequest request, ChannelCategory category, List<IChannelDefinition> allChannels, IPerson user, String type) {
		
		// construct a new channel category bean for this category
		ChannelCategoryBean categoryBean = new ChannelCategoryBean(category);
		
		// add the direct child channels for this category
		IChannelDefinition[] channels;		
		if(type.equals(TYPE_MANAGE)) {
			channels = channelRegistryStore.getManageableChildChannels(category, user);
		} else {
			channels = channelRegistryStore.getChildChannels(category, user);
		}
		
		for(IChannelDefinition channelDef : channels) {
			
			// construct a new channel bean from this channel
			ChannelBean channel = getChannel(channelDef, request);
			categoryBean.addChannel(channel);
			
			// remove the channel of the list of all channels
			allChannels.remove(channel);
		}

		/* Now add child categories. */
		for(ChannelCategory childCategory : channelRegistryStore.getChildCategories(category)) {
			ChannelCategoryBean childCategoryBean = addChildren(request, childCategory, allChannels, user, type);
			
			categoryBean.addCategory(childCategoryBean);
		}
		
		return categoryBean;
		
	}
	
	private ChannelBean getChannel(IChannelDefinition definition, WebRequest request) {
	    ChannelBean channel = new ChannelBean();
	    channel.setId(definition.getId());
        channel.setDescription(definition.getDescription());
        channel.setFname(definition.getFName());
        channel.setName(definition.getName());
        channel.setState(definition.getLifecycleState().toString());
        channel.setTitle(definition.getTitle());
        channel.setTypeId(definition.getType().getId());
	        
        IChannelParameter iconParameter = definition.getParameter("iconUrl");
        if (iconParameter != null) {
            String iconUrl = spELService.parseString(iconParameter.getValue(), request);
            channel.setIconUrl(iconUrl);
        }

        return channel;
	}
	
	/**
	 * @param channelRegistryStore
	 */
	@Autowired(required=true)
	public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
		this.channelRegistryStore = channelRegistryStore;
	}

	/**
	 * <p>For injection of the person manager.  Used for authorization.</p>
	 * @param personManager IPersonManager instance
	 */
	@Autowired(required=true)
	public void setPersonManager(IPersonManager personManager) {
		this.personManager = personManager;
	}
	
	@Autowired(required=true)
	public void setPortalSpELProvider(IPortalSpELService spELProvider) {
	    this.spELService = spELProvider;
	}
}
