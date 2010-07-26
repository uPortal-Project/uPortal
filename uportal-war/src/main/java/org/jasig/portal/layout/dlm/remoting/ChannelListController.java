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

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.ChannelCategory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.layout.dlm.remoting.registry.ChannelBean;
import org.jasig.portal.layout.dlm.remoting.registry.ChannelCategoryBean;
import org.jasig.portal.layout.dlm.remoting.registry.ChannelRegistryBean;
import org.jasig.portal.security.AdminEvaluator;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.thoughtworks.xstream.XStream;

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
	public ModelAndView listChannels(HttpServletRequest request,  @RequestParam("xml") boolean asXml, @RequestParam(value="type",required=false) String type) {
		if(type == null || (!type.equals(TYPE_MANAGE))) {
			type = TYPE_SUBSCRIBE;
		}
		IPerson user = personManager.getPerson(request);

		ChannelRegistryBean registry = getRegistry(user, type);

		if(asXml) {
			XStream stream = new XStream();
			stream.processAnnotations(ChannelRegistryBean.class);
			String xml = stream.toXML(registry);
			return new ModelAndView("xmlView", "xml", xml);
		} else {
			return new ModelAndView("jsonView", "registry", registry);
		}
	}
	
	private ChannelRegistryBean getRegistry(IPerson user, String type) {
		
		// get a list of all channels 
		List<IChannelDefinition> allChannels = channelRegistryStore.getChannelDefinitions();

		// construct a new channel registry
		ChannelRegistryBean registry = new ChannelRegistryBean();
		
		// add the root category and all its children to the registry
		ChannelCategory rootCategory = channelRegistryStore.getTopLevelChannelCategory();
		registry.addCategory(addChildren(rootCategory, allChannels, user, type));

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
	                registry.addChannel(new ChannelBean(channel));
	            }
	        }
	    }
		
		return registry;
	}
	
	private ChannelCategoryBean addChildren(ChannelCategory category, List<IChannelDefinition> allChannels, IPerson user, String type) {
		
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
			ChannelBean channel = new ChannelBean(channelDef);
			categoryBean.addChannel(channel);
			
			// remove the channel of the list of all channels
			allChannels.remove(channel);
		}

		/* Now add child categories. */
		for(ChannelCategory childCategory : channelRegistryStore.getChildCategories(category)) {
			ChannelCategoryBean childCategoryBean = addChildren(childCategory, allChannels, user, type);
			
			categoryBean.addCategory(childCategoryBean);
		}
		
		return categoryBean;
		
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
}
