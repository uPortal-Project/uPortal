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
import org.jasig.portal.user.IUserInstance;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Spring PortletMVC Controller for Favorites Portlet implementing EDIT mode.
 * @since uPortal 4.1
 */
@Controller
@RequestMapping("EDIT")
public class FavoritesEditController
    extends AbstractFavoritesController {

    /**
     * Handles all Favorites portlet EDIT mode renders.
     * Populates model with user's favorites and selects a view to display those favorites.
     *
     * View selection:
     *
     * Returns "jsp/Favorites/edit" in the normal case where the user has
     * at least one favorited portlet or favorited collection.
     *
     * Returns ""jsp/Favorites/view_zero" in the edge case where the user has
     * zero favorited portlets AND zero favorited collections.
     *
     * Model:
     * marketPlaceFname --> String functional name of Marketplace portlet, or null if not available.
     * collections      --> List of favorited collections (IUserLayoutNodeDescription s)
     * favorites        --> List of favorited individual portlets (IUserLayoutNodeDescription s)
     * successMessage   --> String success message, or null if none
     * errorMessage     --> String error message, or null if none
     *
     * @param model . Spring model.  This method adds five model attributes.
     * @return jsp/Favorites/edit[_zero]
     */
    @RenderMapping
    public String initializeView(Model model, RenderRequest renderRequest) {

        IUserInstance ui = userInstanceManager.getUserInstance(portalRequestUtils.getCurrentPortalRequest());
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        IUserLayout userLayout = ulm.getUserLayout();

        // TODO: the portlet could predicate including a non-null marketplace portlet fname
        // on the accessing user having permission to render the portlet referenced by that fname
        // so that portlet would gracefully degrade when configured with bad marketplace portlet fname
        // and also gracefully degrade when the accessing user doesn't have permission to access an otherwise
        // viable configured marketplace.  This complexity may not be worth it.  Anyway it is not yet implemented.

        model.addAttribute("marketplaceFname", this.marketplaceFName);

        List<IUserLayoutNodeDescription> collections = FavoritesUtils.getFavoriteCollections(userLayout);
        model.addAttribute("collections", collections);

        List<IUserLayoutNodeDescription> favorites = FavoritesUtils.getFavoritePortlets(userLayout);
        model.addAttribute("favorites", favorites);

        model.addAttribute("successMessage", renderRequest.getParameter("successMessage"));

        model.addAttribute("errorMessage", renderRequest.getParameter("errorMessage"));

        // default to the regular old edit view
        String viewName = "jsp/Favorites/edit";

        if (collections.isEmpty() && favorites.isEmpty()) {
            // use the special case view
            viewName = "jsp/Favorites/edit_zero";
        }

        logger.trace("Favorites Portlet EDIT mode built model [{}] and selected view {}.",
                model, viewName);

        return viewName;
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
