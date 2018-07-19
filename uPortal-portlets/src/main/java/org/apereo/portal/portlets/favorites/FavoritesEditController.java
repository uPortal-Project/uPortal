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
package org.apereo.portal.portlets.favorites;

import java.util.List;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.IUserPreferencesManager;
import org.apereo.portal.UserPreferencesManager;
import org.apereo.portal.layout.IUserLayout;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.user.IUserInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

/**
 * Spring PortletMVC Controller for Favorites Portlet implementing EDIT mode.
 *
 * @since 4.1
 */
@Controller
@RequestMapping("EDIT")
public class FavoritesEditController extends AbstractFavoritesController {

    @Autowired private FavoritesUtils favoritesUtils;

    /**
     * Handles all Favorites portlet EDIT mode renders. Populates model with user's favorites and
     * selects a view to display those favorites.
     *
     * <p>View selection:
     *
     * <p>Returns "jsp/Favorites/edit" in the normal case where the user has at least one favorited
     * portlet or favorited collection.
     *
     * <p>Returns "jsp/Favorites/edit_zero" in the edge case where the user has zero favorited
     * portlets AND zero favorited collections.
     *
     * <p>Model: marketPlaceFname --> String functional name of Marketplace portlet, or null if not
     * available. collections --> List of favorited collections (IUserLayoutNodeDescription s)
     * favorites --> List of favorited individual portlets (IUserLayoutNodeDescription s)
     * successMessageCode --> String success message bundle key, or null if none errorMessageCode
     * --> String error message bundle key, or null if none nameOfFavoriteActedUpon --> Name of
     * favorite acted upon, intended as parameter to success or error message
     *
     * @param model . Spring model. This method adds five model attributes.
     * @return jsp/Favorites/edit[_zero]
     */
    @RenderMapping
    public String initializeView(Model model, RenderRequest renderRequest) {

        IUserInstance ui =
                userInstanceManager.getUserInstance(portalRequestUtils.getCurrentPortalRequest());
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();

        IUserLayout userLayout = ulm.getUserLayout();

        // TODO: the portlet could predicate including a non-null marketplace portlet fname
        // on the accessing user having permission to render the portlet referenced by that fname
        // so that portlet would gracefully degrade when configured with bad marketplace portlet
        // fname
        // and also gracefully degrade when the accessing user doesn't have permission to access an
        // otherwise
        // viable configured marketplace.  This complexity may not be worth it.  Anyway it is not
        // yet implemented.

        model.addAttribute("marketplaceFname", this.marketplaceFName);

        List<IUserLayoutNodeDescription> collections =
                favoritesUtils.getFavoriteCollections(userLayout);
        model.addAttribute("collections", collections);

        List<IUserLayoutNodeDescription> favorites = favoritesUtils.getFavoritePortlets(userLayout);
        model.addAttribute("favorites", favorites);

        model.addAttribute("successMessageCode", renderRequest.getParameter("successMessageCode"));
        model.addAttribute("errorMessageCode", renderRequest.getParameter("errorMessageCode"));

        model.addAttribute(
                "nameOfFavoriteActedUpon", renderRequest.getParameter("nameOfFavoriteActedUpon"));

        // default to the regular old edit view
        String viewName = "jsp/Favorites/edit";

        if (collections.isEmpty() && favorites.isEmpty()) {
            // use the special case view
            viewName = "jsp/Favorites/edit_zero";
        }

        logger.trace(
                "Favorites Portlet EDIT mode built model [{}] and selected view {}.",
                model,
                viewName);

        return viewName;
    }

    /**
     * Un-favorite a favorite node (portlet or collection) identified by node ID. Routed by the
     * action=delete parameter. If no favorites remain after un-favoriting, switches portlet mode to
     * VIEW.
     *
     * <p>Sets render parameters: successMessageCode: message code of success message if applicable
     * errorMessageCode: message code of error message if applicable nameOfFavoriteActedUpon:
     * user-facing name of favorite acted upon. action: will be set to "list" to facilitate not
     * repeatedly attempting delete.
     *
     * <p>Exactly one of [successMessageCode|errorMessageCode] render parameters will be set.
     * nameOfFavoriteActedUpon and action will always be set.
     *
     * @param nodeId identifier of target node
     * @param response ActionResponse onto which render parameters will, mode may, be set
     */
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
            response.setRenderParameter("nameOfFavoriteActedUpon", userFacingNodeName);

            if (nodeDescription.isDeleteAllowed()) {

                boolean nodeSuccessfullyDeleted = layoutManager.deleteNode(nodeId);

                if (nodeSuccessfullyDeleted) {
                    layoutManager.saveUserLayout();

                    response.setRenderParameter(
                            "successMessageCode", "favorites.unfavorite.success.parameterized");

                    IUserLayout updatedLayout = layoutManager.getUserLayout();

                    // if removed last favorite, return to VIEW mode
                    if (!favoritesUtils.hasAnyFavorites(updatedLayout)) {
                        response.setPortletMode(PortletMode.VIEW);
                    }

                    logger.debug("Successfully unfavorited [{}]", nodeDescription);

                } else {
                    logger.error(
                            "Failed to delete node [{}] on unfavorite request, but this should have succeeded?",
                            nodeDescription);

                    response.setRenderParameter(
                            "errorMessageCode", "favorites.unfavorite.fail.parameterized");
                }

            } else {
                logger.warn(
                        "Attempt to unfavorite [{}] failed because user lacks permission to delete that layout node.",
                        nodeDescription);

                response.setRenderParameter(
                        "errorMessageCode",
                        "favorites.unfavorite.fail.lack.permission.parameterized");
            }

        } catch (Exception e) {

            // TODO: this log message is kind of useless without the username to put the node in
            // context
            logger.error("Something went wrong unfavoriting nodeId [{}].", nodeId);

            // may have failed to load node description, so fall back on describing by id
            final String fallbackUserFacingNodeName = "node with id " + nodeId;

            response.setRenderParameter(
                    "errorMessageCode", "favorites.unfavorite.fail.parameterized");
            response.setRenderParameter("nameOfFavoriteActedUpon", fallbackUserFacingNodeName);
        }

        response.setRenderParameter("action", "list");
    }
}
