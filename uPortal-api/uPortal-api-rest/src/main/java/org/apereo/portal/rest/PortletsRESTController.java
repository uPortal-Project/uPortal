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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.UserPreferencesManager;
import org.apereo.portal.layout.IUserLayout;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.LayoutPortlet;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.PortletCategory;
import org.apereo.portal.portlet.registry.IPortletCategoryRegistry;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.portlet.rendering.IPortletExecutionManager;
import org.apereo.portal.portlets.favorites.FavoritesUtils;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.url.PortalHttpServletFactoryService;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    public static final String FAVORITE_FLAG = "favorite";
    public static final String REQUIRED_PERMISSION_TYPE = "requiredPermissionType";

    @Autowired private IAuthorizationService authorizationService;

    @Autowired private IPortletDefinitionRegistry portletDefinitionRegistry;

    @Autowired private IPortletCategoryRegistry portletCategoryRegistry;

    @Autowired private IPersonManager personManager;

    @Autowired private PortalHttpServletFactoryService servletFactoryService;

    @Autowired private IPortletWindowRegistry portletWindowRegistry;

    @Autowired private IPortletExecutionManager portletExecutionManager;

    @Autowired private IUserInstanceManager userInstanceManager;

    @Autowired private FavoritesUtils favoritesUtils;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public enum PortletPermissionType {
        BROWSE,
        CONFIGURE,
        MANAGE,
        RENDER,
        SUBSCRIBE
    }

    /**
     * Provides information about all portlets in the portlet registry. NOTE: The response is
     * governed by the <code>IPermission.PORTLET_MANAGER_xyz</code> series of permissions. The
     * actual level of permission required is based on the current lifecycle state of the portlet.
     */
    @GetMapping(value = "/portlets.json")
    public ModelAndView getPortlets(HttpServletRequest request) {
        final boolean limitByFavoriteFlag = request.getParameter(FAVORITE_FLAG) != null;
        final boolean favoriteValueToMatch =
                Boolean.parseBoolean(request.getParameter(FAVORITE_FLAG));
        final String requiredPermissionTypeParameter =
                request.getParameter(REQUIRED_PERMISSION_TYPE);
        final PortletPermissionType requiredPermissionType =
                (requiredPermissionTypeParameter == null)
                        ? PortletPermissionType.MANAGE
                        : PortletPermissionType.valueOf(
                                requiredPermissionTypeParameter.toUpperCase());

        final Set<IPortletDefinition> favorites =
                limitByFavoriteFlag ? getFavorites(request) : Collections.emptySet();
        final List<IPortletDefinition> allPortlets =
                portletDefinitionRegistry.getAllPortletDefinitions();
        final IAuthorizationPrincipal ap = getAuthorizationPrincipal(request);

        final Predicate<IPortletDefinition> favoritesPredicate =
                p -> !limitByFavoriteFlag || favorites.contains(p) == favoriteValueToMatch;
        final Predicate<IPortletDefinition> permissionsPredicate =
                p -> this.doesUserHavePermissionToViewPortlet(ap, p, requiredPermissionType);
        final List<PortletTuple> results =
                allPortlets.stream()
                        .filter(favoritesPredicate.and(permissionsPredicate))
                        .map(PortletTuple::new)
                        .collect(Collectors.toList());
        return new ModelAndView("json", "portlets", results);
    }

    private boolean doesUserHavePermissionToViewPortlet(
            IAuthorizationPrincipal ap,
            IPortletDefinition portletDefinition,
            PortletPermissionType requiredPermissionType) {
        switch (requiredPermissionType) {
            case BROWSE:
                return this.authorizationService.canPrincipalBrowse(ap, portletDefinition);
            case CONFIGURE:
                return this.authorizationService.canPrincipalConfigure(
                        ap, portletDefinition.getPortletDefinitionId().getStringId());
            case MANAGE:
                return this.authorizationService.canPrincipalManage(
                        ap, portletDefinition.getPortletDefinitionId().getStringId());
            case RENDER:
                return this.authorizationService.canPrincipalRender(
                        ap, portletDefinition.getPortletDefinitionId().getStringId());
            case SUBSCRIBE:
                return this.authorizationService.canPrincipalSubscribe(
                        ap, portletDefinition.getPortletDefinitionId().getStringId());
            default:
                throw new IllegalArgumentException(
                        "Unknown requiredPermissionType: " + requiredPermissionType);
        }
    }

    /**
     * Provides information about a single portlet in the registry. NOTE: Access to this API
     * endpoint requires only <code>IPermission.PORTAL_SUBSCRIBE</code> permission.
     */
    @GetMapping(value = "/portlet/{fname}.json")
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
     * Provides a single, fully-rendered portlet. NOTE: Access to this API endpoint requires only
     * <code>IPermission.PORTAL_SUBSCRIBE</code> permission.
     */
    @GetMapping(value = "/v4-3/portlet/{fname}.html")
    public @ResponseBody String getRenderedPortlet(
            HttpServletRequest req, HttpServletResponse res, @PathVariable String fname) {

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

            final PortalHttpServletFactoryService.RequestAndResponseWrapper wrapper =
                    servletFactoryService.createRequestAndResponseWrapper(req, res);

            final IPortletWindow portletWindow =
                    portletWindowRegistry.getOrCreateDefaultPortletWindow(
                            wrapper.getRequest(), portletDef.getPortletDefinitionId());
            final String result =
                    portletExecutionManager.getPortletOutput(
                            portletWindow.getPortletWindowId(),
                            wrapper.getRequest(),
                            wrapper.getResponse());
            return result;
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
        IAuthorizationPrincipal result =
                authorizationService.newPrincipal(ei.getKey(), ei.getType());
        return result;
    }

    private Set<String> getPortletCategories(IPortletDefinition pdef) {
        Set<PortletCategory> categories = portletCategoryRegistry.getParentCategories(pdef);
        Set<String> result = new HashSet<String>();
        for (PortletCategory category : categories) {
            result.add(StringUtils.capitalize(category.getName().toLowerCase()));
        }
        return result;
    }

    private Set<IPortletDefinition> getFavorites(HttpServletRequest request) {
        final IUserInstance ui = userInstanceManager.getUserInstance(request);
        final UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        final IUserLayoutManager ulm = upm.getUserLayoutManager();
        final IUserLayout layout = ulm.getUserLayout();
        return favoritesUtils.getFavoritePortletDefinitions(layout);
    }

    /*
     * Nested Types
     */

    @SuppressWarnings("unused")
    protected /* non-static */ final class PortletTuple implements Serializable {

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
