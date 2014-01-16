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
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
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

    @RenderMapping
    public String initializeView(Model model, RenderRequest renderRequest) {
        IUserInstance ui = userInstanceManager.getUserInstance(portalRequestUtils.getCurrentPortalRequest());
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        IUserLayout userLayout = ulm.getUserLayout();

        List<IUserLayoutNodeDescription> collections = FavoritesUtils.getFavoriteCollections(userLayout);
        model.addAttribute("collections", collections);

        List<IUserLayoutNodeDescription> favorites = FavoritesUtils.getFavoritePortlets(userLayout);
        model.addAttribute("favorites", favorites);

        model.addAttribute("successMessage", renderRequest.getParameter("successMessage"));

        model.addAttribute("errorMessage", renderRequest.getParameter("errorMessage"));

        return "jsp/Favorites/edit";
    }



    @ActionMapping(params = {"action=delete"})
    public void unFavoriteNode(@RequestParam("nodeId") String nodeId, ActionResponse response) {

        try {

            // ferret out the layout manager
            HttpServletRequest servletRequest = this.portalRequestUtils.getCurrentPortalRequest();
            IUserInstance userInstance = this.userInstanceManager.getUserInstance(servletRequest);
            IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
            IUserLayoutManager layoutManager = preferencesManager.getUserLayoutManager();

            IUserLayoutNodeDescription nodeDescription = layoutManager.getNode(nodeId);

            String userFacingNodeName = nodeDescription.getName();

            if (nodeDescription.isDeleteAllowed()) {

                boolean nodeSuccessfullyDeleted = layoutManager.deleteNode(nodeId);

                if (nodeSuccessfullyDeleted) {
                    layoutManager.saveUserLayout();

                    // TODO: use a message bundle
                    response.setRenderParameter("successMessage", "Successfully un-favorited \"" + userFacingNodeName +
                            "\".");

                    logger.debug("Successfully un-favorited [{}]", nodeDescription);

                } else {
                    logger.error("Failed to delete node [{}] on un-favorite request, but this should have succeeded?",
                            nodeDescription);
                    // TODO: use a message bundle
                    response.setRenderParameter("errorMessage", "Failed to un-favorite \"" + userFacingNodeName
                            + "\"Please contact support if this problem persists.");
                }

            } else {

                logger.warn(
                        "Attempt to un-favorite [{}] failed because user lacks permission to delete that layout node.",
                        nodeDescription);

                // TODO: use a message bundle
                response.setRenderParameter("errorMessage", "You do not have sufficient privileges to un-favorite \"" +
                        userFacingNodeName + "\".");

            }

        } catch (Exception e) {

            // TODO: this log message is kind of useless without the username to put the node in context
            logger.error("Something went wrong un-favoriting nodeId [{}]", nodeId);

            // TODO: get error message from message bundle
            response.setRenderParameter("errorMessage", "Un-favoriting failed for an unknown reason.  This error has " +
                    "been logged.  Please contact support for assistance if this problem persists for you.");
        }

        response.setRenderParameter("action", "list");
        
    }

}
