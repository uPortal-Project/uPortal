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

package org.jasig.portal.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.i18n.ILocaleStore;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.rest.models.DataTablesResponse;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * This controller logic is derived from {@link org.jasig.portal.layout.dlm.remoting.ChannelListController}
 * @author Shawn Connolly, sconnolly@unicon.net
 *
 */

@Controller
public class PortletsRESTController {

	private IPortletDefinitionRegistry portletDefinitionRegistry;
	private IPortletCategoryRegistry portletCategoryRegistry;
	private ILocaleStore localeStore;
	private IPersonManager personManager;
	@Autowired
	public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
		this.portletDefinitionRegistry = portletDefinitionRegistry;
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
	public void setPortletCategoryRegistry(IPortletCategoryRegistry portletCategoryRegistry) {
		this.portletCategoryRegistry = portletCategoryRegistry;
	}
	
    @RequestMapping(value="/dataTable/ManagePortlets/List", method = RequestMethod.GET)
    public ModelAndView getPortlets(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// get a list of all channels
		List<IPortletDefinition> allChannels = portletDefinitionRegistry.getAllPortletDefinitions();
		IPerson user = personManager.getPerson(request);
		EntityIdentifier ei = user.getEntityIdentifier();
	    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

	    // get user locale
	    Locale[] locales = localeStore.getUserLocales(user);
	    LocaleManager localeManager = new LocaleManager(user, locales);
	    Locale locale = localeManager.getLocales()[0];
        ModelAndView mv = new ModelAndView();
        
        List<List<?>> dataContent = new ArrayList<List<?>>();
        for (IPortletDefinition channel : allChannels) {
            if (ap.canManage(channel.getPortletDefinitionId().getStringId())) {
            	dataContent.add(getChannel(channel, locale));
            }
        }
        DataTablesResponse dataTableResponse = new DataTablesResponse(dataContent.size());
        dataTableResponse.setAaData(dataContent);
        mv.addObject(dataTableResponse);
        mv.setViewName("json");
        
        return mv;
    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List getChannel(IPortletDefinition definition, Locale locale) {
	    return Arrays.asList(
	    definition.getName(locale.toString()),
	    definition.getType().getName(),
        StringUtils.capitalize(definition.getLifecycleState().toString().toLowerCase(locale)),
	    definition.getPortletDefinitionId().getStringId(), // used for edit and delete url
        "", // placeholder for delete column
	    getPortletCategories(definition)
        );
	}
	
	private String getPortletCategories(IPortletDefinition definition) {
		Set<PortletCategory> categories = portletCategoryRegistry.getParentCategories(definition);
		StringBuilder allCategories = new StringBuilder();
		for (PortletCategory category: categories) {
			if(allCategories.length() != 0) {
				allCategories.append(",");
			}
			allCategories.append(StringUtils.capitalize(category.getName().toLowerCase()));
		}
		return allCategories.toString();
	}
}
