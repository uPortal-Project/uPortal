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
package org.jasig.portal.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.rest.layout.LayoutPortlet;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * This controller logic is derived from {@link org.jasig.portal.layout.dlm.remoting.ChannelListController}
 * 
 * @since 4.1
 * @author Shawn Connolly, sconnolly@unicon.net
 */

@Controller
public class PortletsRESTController {

    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletCategoryRegistry portletCategoryRegistry;
    private IPersonManager personManager;

    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setPortletCategoryRegistry(IPortletCategoryRegistry portletCategoryRegistry) {
        this.portletCategoryRegistry = portletCategoryRegistry;
    }

    @RequestMapping(value="/portlets.json", method = RequestMethod.GET)
    public ModelAndView getPortlets(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get a list of all channels
        List<IPortletDefinition> allPortlets = portletDefinitionRegistry.getAllPortletDefinitions();
        IPerson user = personManager.getPerson(request);
        EntityIdentifier ei = user.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

        List<PortletTuple> rslt = new ArrayList<PortletTuple>();
        for (IPortletDefinition pdef : allPortlets) {
            if (ap.canManage(pdef.getPortletDefinitionId().getStringId())) {
                rslt.add(new PortletTuple(pdef));
            }
        }

        return new ModelAndView("json", "portlets", rslt);

    }
    
    @RequestMapping(value="/portlet/{fname}.json", method = RequestMethod.GET)
    public ModelAndView getPortlet(HttpServletRequest request, HttpServletResponse response, @PathVariable String fname) throws Exception {
      IPerson user = personManager.getPerson(request);
      EntityIdentifier ei = user.getEntityIdentifier();
      IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
      IPortletDefinition portletDef = portletDefinitionRegistry.getPortletDefinitionByFname(fname);
      if(portletDef != null && ap.canRender(portletDef.getPortletDefinitionId().getStringId())) {
        LayoutPortlet portlet = new LayoutPortlet(portletDef);
        return new ModelAndView("json", "portlet", portlet);
      } else {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return new ModelAndView("json");
      }
    }

    private Set<String> getPortletCategories(IPortletDefinition pdef) {
        Set<PortletCategory> categories = portletCategoryRegistry.getParentCategories(pdef);
        Set<String> rslt = new HashSet<String>();
        for (PortletCategory category : categories) {
            rslt.add(StringUtils.capitalize(category.getName().toLowerCase()));
        }
        return rslt;
    }

    /*
     * Nested Types
     */

    @SuppressWarnings("unused")
    private /* non-static */ final class PortletTuple implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String id;
        private final String name;
        private final String fname;
        private final String description;
        private final String type;
        private final String lifecycleState;
        private final Set<String> categories;

        public PortletTuple(IPortletDefinition pdef) {
            this.id = pdef.getPortletDefinitionId().getStringId();
            this.name = pdef.getName();
            this.fname = pdef.getFName();
            this.description = pdef.getDescription();
            this.type = pdef.getType().getName();
            this.lifecycleState = pdef.getLifecycleState().toString();
            this.categories = getPortletCategories(pdef);
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getFname() {
            return fname;
        }

        public String getDescription() {
            return description;
        }

        public String getType() {
            return type;
        }

        public String getLifecycleState() {
            return lifecycleState;
        }

        public Set<String> getCategories() {
            return categories;
        }

    }

}
