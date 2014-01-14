/*
 * Licensed to Apereo under one or more contributor license
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

package org.jasig.portal.portlets.favorites;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.UserPreferencesManager;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import javax.portlet.ActionResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Spring PortletMVC Controller for Favorites Portlet implementing EDIT mode.
 * @since uPortal 4.1
 */
@Controller
@RequestMapping("EDIT")
public class FavoritesEditController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private FavoritesHelper favoritesHelper;

    private IUserInstanceManager userInstanceManager;
    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Autowired
    public void setFavoritesHelper( FavoritesHelper helper) {
        this.favoritesHelper = helper;
    }

    @RenderMapping
    public String initializeView(Model model) {
        IUserInstance ui = userInstanceManager.getUserInstance(portalRequestUtils.getCurrentPortalRequest());
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        IUserLayout userLayout = ulm.getUserLayout();

        List<IUserLayoutNodeDescription> collections = favoritesHelper.getFavoriteCollections(userLayout);
        model.addAttribute("collections", collections);

        List<IUserLayoutNodeDescription> favorites = favoritesHelper.getFavoritePortlets(userLayout);
        model.addAttribute("favorites", favorites);



        return "jsp/Favorites/edit";
    }



    @RequestMapping(params = {"action=delete"})
    public void unFavoriteNode(@RequestParam("nodeId") String nodeId, ActionResponse response) {

        // ferret out the layout manager
        HttpServletRequest servletRequest = this.portalRequestUtils.getCurrentPortalRequest();
        IUserInstance userInstance = this.userInstanceManager.getUserInstance(servletRequest);
        IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        IUserLayoutManager layoutManager = preferencesManager.getUserLayoutManager();

        layoutManager.deleteNode(nodeId);

        layoutManager.saveUserLayout();

        // TODO: care about the boolean success return from deleteNode()

        response.setRenderParameter("action", "list");
        
    }

}
