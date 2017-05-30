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
package org.apereo.portal.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.layout.LayoutPortlet;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.PortletCategory;
import org.apereo.portal.portlet.registry.IPortletCategoryRegistry;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.portlet.rendering.IPortletExecutionManager;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.services.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * This controller logic is derived from {@link
 * org.apereo.portal.layout.dlm.remoting.ChannelListController}
 *
 * @since 4.1
 */
@Controller
public class PortletsRESTController {

    @Autowired private IPortletDefinitionRegistry portletDefinitionRegistry;

    @Autowired private IPortletCategoryRegistry portletCategoryRegistry;

    @Autowired private IPersonManager personManager;

    @Autowired private IPortletWindowRegistry portletWindowRegistry;

    @Autowired private IPortletExecutionManager portletExecutionManager;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Provides information about all portlets in the portlet registry. NOTE: The response is
     * governed by the <code>IPermission.PORTLET_MANAGER_xyz</code> series of permissions. The
     * actual level of permission required is based on the current lifecycle state of the portlet.
     */
    @RequestMapping(value = "/portlets.json", method = RequestMethod.GET)
    public ModelAndView getManageablePortlets(
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get a list of all channels
        List<IPortletDefinition> allPortlets = portletDefinitionRegistry.getAllPortletDefinitions();
        IAuthorizationPrincipal ap = getAuthorizationPrincipal(request);

        List<PortletTuple> rslt = new ArrayList<PortletTuple>();
        for (IPortletDefinition pdef : allPortlets) {
            if (ap.canManage(pdef.getPortletDefinitionId().getStringId())) {
                rslt.add(new PortletTuple(pdef));
            }
        }

        return new ModelAndView("json", "portlets", rslt);
    }

    /**
     * Provides information about a single portlet in the registry. NOTE: Access to this API enpoint
     * requires only <code>IPermission.PORTAL_SUBSCRIBE</code> permission.
     */
    @RequestMapping(value = "/portlet/{fname}.json", method = RequestMethod.GET)
    public ModelAndView getPortlet(
            HttpServletRequest request, HttpServletResponse response, @PathVariable String fname)
            throws Exception {
        IAuthorizationPrincipal ap = getAuthorizationPrincipal(request);
        IPortletDefinition portletDef =
                portletDefinitionRegistry.getPortletDefinitionByFname(fname);
        if (portletDef != null && ap.canRender(portletDef.getPortletDefinitionId().getStringId())) {
            LayoutPortlet portlet = new LayoutPortlet(portletDef);
            return new ModelAndView("json", "portlet", portlet);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ModelAndView("json");
        }
    }

    /**
     * Provides a single, fully-rendered portlet. NOTE: Access to this API enpoint requires only
     * <code>IPermission.PORTAL_SUBSCRIBE</code> permission.
     */
    @RequestMapping(value = "/v4-3/portlet/{fname}.html", method = RequestMethod.GET)
    public @ResponseBody String getRenderedPortlet(
            HttpServletRequest req, HttpServletResponse res, @PathVariable String fname)
            throws Exception {

        // Does the portlet exist in the registry?
        final IPortletDefinition portletDef =
                portletDefinitionRegistry.getPortletDefinitionByFname(fname);
        if (portletDef == null) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "Portlet not found";
        }

        // Is the user permitted to access it?
        final IAuthorizationPrincipal ap = getAuthorizationPrincipal(req);
        if (!ap.canRender(portletDef.getPortletDefinitionId().getStringId())) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "Access denied";
        }

        // Proceed...
        try {
            final IPortletWindow portletWindow =
                    portletWindowRegistry.getOrCreateDefaultPortletWindow(
                            req, portletDef.getPortletDefinitionId());
            final String rslt =
                    portletExecutionManager.getPortletOutput(
                            portletWindow.getPortletWindowId(), req, res);
            return rslt;
        } catch (Exception e) {
            logger.error("Failed to render the requested portlet '{}'", fname, e);
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "Internal error";
        }
    }

    /*
     * Implementation
     */

    private IAuthorizationPrincipal getAuthorizationPrincipal(HttpServletRequest req) {
        IPerson user = personManager.getPerson(req);
        EntityIdentifier ei = user.getEntityIdentifier();
        IAuthorizationPrincipal rslt =
                AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        return rslt;
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
