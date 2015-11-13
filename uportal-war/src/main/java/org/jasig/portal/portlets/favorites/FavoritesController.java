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
package org.jasig.portal.portlets.favorites;


import org.jasig.portal.UserPreferencesManager;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.user.IUserInstance;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import java.util.List;

import javax.portlet.PortletPreferences;

/**
 * View controller for Favorites portlet.
 *
 * Requires an IUserInstanceManager and an IPortalRequestUtils, both auto-wired in, with those
 * dependencies declared and that auto-wiring happening in the AbstractFavoritesController super-class.
 *
 * Supports but does not require a Marketplace portlet.
 * Configure with a functional name referencing a Marketplace if you'd the portlet to include convenient
 * links for the user to readily access Marketplace to add favorites.
 *
 * Does not currently support the Marketplace link invoking the Customize Drawer rather than referencing a
 * Portlet.  Sorry.  Implement the Marketplace portlet when it's available.  You'll thank me later.
 */
@Controller
@RequestMapping("VIEW")
public class FavoritesController extends AbstractFavoritesController {

    /**
     * Single-value preference that (optionally) restricts the height of the
     * favorites list, in view mode, to the specified number of pixels.
     */
    public static final String MAX_HEIGHT_PIXELS_PREFERENCE = "FavoritesController.maxHeightPixels";

    /**
     * Handles all Favorites portlet VIEW mode renders.
     * Populates model with user's favorites and selects a view to display those favorites.
     *
     * View selection:
     *
     * Returns "jsp/Favorites/view" in the normal case where the user has
     * at least one favorited portlet or favorited collection.
     *
     * Returns "jsp/Favorites/view_zero" in the edge case where the user has
     * zero favorited portlets AND zero favorited collections.
     *
     * Model:
     * marketPlaceFname --> String functional name of Marketplace portlet, or null if not available.
     * collections      --> List of favorited collections (IUserLayoutNodeDescription s)
     * favorites        --> List of favorited individual portlets (IUserLayoutNodeDescription s)
     *
     * @param model . Spring model.  This method adds three model attributes.
     * @return jsp/Favorites/view[_zero]
     */
    @RenderMapping
    public String initializeView(Model model) {
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

        // default to the regular old view
        String viewName = "jsp/Favorites/view";

        if (collections.isEmpty() && favorites.isEmpty()) {
            // special edge case of zero favorites, switch to special view
            viewName = "jsp/Favorites/view_zero";
        }

        logger.trace("Favorites Portlet VIEW mode render populated model [{}] for render by view {}.",
                model, viewName);
        return viewName;
    }

    @ModelAttribute("maxHeightPixels")
    public Integer getMaxHeightPixels(PortletPreferences prefs) {
        String value = prefs.getValue(MAX_HEIGHT_PIXELS_PREFERENCE, null);
        Integer rslt = value != null ? Integer.valueOf(value) : null;
        return rslt;
    }

}
