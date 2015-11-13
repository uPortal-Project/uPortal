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
package org.jasig.portal.portlets.backgroundpreference;

import org.jasig.portal.rest.AjaxSuccessController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.portlet.ActionRequest;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.PortletPreferences;

@Controller
@RequestMapping("VIEW")
public class ViewBackgroundPreferenceController {

    BackgroundSetSelectionStrategy imageSetSelectionStrategy = new RoleBasedBackgroundSetSelectionStrategy();

    public void setImageSetSelectionStrategy(BackgroundSetSelectionStrategy imageSetSelectionStrategy) {
        this.imageSetSelectionStrategy = imageSetSelectionStrategy;
    }

    /**
     * Display the main user-facing view of the portlet.
     * 
     * @param request
     * @return
     */
    @RenderMapping
    public String getView(RenderRequest req, Model model) {

        String[] images = imageSetSelectionStrategy.getImageSet(req);
        model.addAttribute("images", images);

        String[] thumbnailImages = imageSetSelectionStrategy.getImageThumbnailSet(req);
        model.addAttribute("thumbnailImages", thumbnailImages);

        String preferredBackgroundImage = imageSetSelectionStrategy.getSelectedImage(req);
        model.addAttribute("backgroundImage", preferredBackgroundImage);

        String backgroundContainerSelector = imageSetSelectionStrategy.getBackgroundContainerSelector(req);
        model.addAttribute("backgroundContainerSelector", backgroundContainerSelector);

        PortletPreferences prefs = req.getPreferences();
        model.addAttribute("applyOpacityTo", prefs.getValue("applyOpacityTo", null));
        model.addAttribute("opacityCssValue", prefs.getValue("opacityCssValue", "1.0"));

        return "/jsp/BackgroundPreference/viewBackgroundPreference";
    }

    @ActionMapping(params = {"action=savePreferences"})
    public void savePreferences(ActionRequest req, ActionResponse res,
            @RequestParam(required=false) String backgroundImage) throws Exception {

        imageSetSelectionStrategy.setSelectedImage(req, backgroundImage);

        // Reirect to a basic HTTP 200 success response to save a full page cycle
        res.sendRedirect(req.getContextPath() + "/api" + AjaxSuccessController.SUCCESS_URL);

    }

}
