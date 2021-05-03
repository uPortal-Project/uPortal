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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import org.apereo.portal.UserPreferencesManager;
import org.apereo.portal.layout.IUserLayout;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.node.IUserLayoutChannelDescription;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.PersonFactory;
import org.apereo.portal.user.IUserInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

/**
 * View controller for Favorites portlet.
 *
 * <p>Requires an IUserInstanceManager and an IPortalRequestUtils, both auto-wired in, with those
 * dependencies declared and that auto-wiring happening in the AbstractFavoritesController
 * super-class.
 *
 * <p>Supports but does not require a Marketplace portlet. Configure with a functional name
 * referencing a Marketplace if you'd the portlet to include convenient links for the user to
 * readily access Marketplace to add favorites.
 *
 * <p>Does not currently support the Marketplace link invoking the Customize Drawer rather than
 * referencing a Portlet. Sorry. Implement the Marketplace portlet when it's available. You'll thank
 * me later.
 */
@Controller
@RequestMapping("VIEW")
public class FavoritesController extends AbstractFavoritesController {

    /**
     * Single-value preference that (optionally) restricts the height of the favorites list, in view
     * mode, to the specified number of pixels.
     */
    public static final String MAX_HEIGHT_PIXELS_PREFERENCE = "FavoritesController.maxHeightPixels";

    @Autowired private IAuthorizationService authorizationService;

    @Autowired private FavoritesUtils favoritesUtils;

    /**
     * Handles all Favorites portlet VIEW mode renders. Populates model with user's favorites and
     * selects a view to display those favorites.
     *
     * <p>View selection:
     *
     * <p>Returns "jsp/Favorites/view" in the normal case where the user has at least one favorited
     * portlet or favorited collection.
     *
     * <p>Returns "jsp/Favorites/view_zero" in the edge case where the user has zero favorited
     * portlets AND zero favorited collections.
     *
     * <p>Model: marketPlaceFname --> String functional name of Marketplace portlet, or null if not
     * available. collections --> List of favorited collections (IUserLayoutNodeDescription s)
     * favorites --> List of favorited individual portlets (IUserLayoutNodeDescription s)
     *
     * @param model . Spring model. This method adds three model attributes.
     * @return jsp/Favorites/view[_zero]
     */
    @RenderMapping
    public String initializeView(PortletRequest req, Model model) {
        final IUserInstance ui =
                userInstanceManager.getUserInstance(portalRequestUtils.getCurrentPortalRequest());
        final UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        final IUserLayoutManager ulm = upm.getUserLayoutManager();

        final IUserLayout userLayout = ulm.getUserLayout();

        // TODO: the portlet could predicate including a non-null marketplace portlet fname
        // on the accessing user having permission to render the portlet referenced by that fname
        // so that portlet would gracefully degrade when configured with bad marketplace portlet
        // fname
        // and also gracefully degrade when the accessing user doesn't have permission to access an
        // otherwise
        // viable configured marketplace.  This complexity may not be worth it.  Anyway it is not
        // yet implemented.
        model.addAttribute("marketplaceFname", this.marketplaceFName);

        final List<IUserLayoutNodeDescription> collections =
                favoritesUtils.getFavoriteCollections(userLayout);
        model.addAttribute("collections", collections);

        final List<IUserLayoutNodeDescription> rawFavorites =
                favoritesUtils.getFavoritePortletLayoutNodes(userLayout);

        /*
         * Filter the collection by SUBSCRIBE permission.
         *
         * NOTE:  In the "regular" (non-Favorites) layout, this permissions check is handled by
         * the rendering engine.  It will refuse to spawn a worker for a portlet to which you
         * cannot SUBSCRIBE.
         */
        final String username =
                req.getRemoteUser() != null
                        ? req.getRemoteUser()
                        : PersonFactory.getGuestUsernames().get(0); // First item is the default
        final IAuthorizationPrincipal principal =
                authorizationService.newPrincipal(username, IPerson.class);
        final List<IUserLayoutNodeDescription> favorites = new ArrayList<>();
        for (IUserLayoutNodeDescription nodeDescription : rawFavorites) {
            if (nodeDescription instanceof IUserLayoutChannelDescription) {
                final IUserLayoutChannelDescription channelDescription =
                        (IUserLayoutChannelDescription) nodeDescription;
                if (principal.canRender(channelDescription.getChannelPublishId())) {
                    favorites.add(nodeDescription);
                }
            }
        }


        /*
           Filter returned favorites list to unique list.
        */

        final List<IUserLayoutNodeDescription> uniqueFavorites =
                FavoritesUtils.filterFavoritesToUnique(favorites);


        model.addAttribute("favorites", uniqueFavorites);

        // default to the regular old view
        String viewName = "jsp/Favorites/view";

        if (collections.isEmpty() && favorites.isEmpty()) {
            // special edge case of zero favorites, switch to special view
            viewName = "jsp/Favorites/view_zero";
        }

        logger.debug(
                "Favorites Portlet VIEW mode render populated model [{}] for render by view {}.",
                model,
                viewName);
        return viewName;
    }

    @ModelAttribute("maxHeightPixels")
    public Integer getMaxHeightPixels(PortletPreferences prefs) {
        final String value = prefs.getValue(MAX_HEIGHT_PIXELS_PREFERENCE, null);
        final Integer rslt = value != null ? Integer.valueOf(value) : null;
        return rslt;
    }
}
