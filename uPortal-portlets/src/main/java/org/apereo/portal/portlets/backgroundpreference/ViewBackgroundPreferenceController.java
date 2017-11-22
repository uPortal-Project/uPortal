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
package org.apereo.portal.portlets.backgroundpreference;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import org.apereo.portal.rest.AjaxSuccessController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

@Controller
@RequestMapping("VIEW")
public class ViewBackgroundPreferenceController {

    @Autowired private BackgroundSetSelectionStrategy imageSetSelectionStrategy;

    /**
     * Display the main user-facing view of the portlet.
     *
     * @param request
     * @return
     */
    @RenderMapping
    public String getView(RenderRequest req, Model model) {

        final String[] images = imageSetSelectionStrategy.getImageSet(req);
        model.addAttribute("images", images);

        final String[] thumbnailImages = imageSetSelectionStrategy.getImageThumbnailSet(req);
        model.addAttribute("thumbnailImages", thumbnailImages);

        final String[] imageCaptions = imageSetSelectionStrategy.getImageCaptions(req);
        model.addAttribute("imageCaptions", imageCaptions);

        final String preferredBackgroundImage = imageSetSelectionStrategy.getSelectedImage(req);
        model.addAttribute("backgroundImage", preferredBackgroundImage);

        final String backgroundContainerSelector =
                imageSetSelectionStrategy.getBackgroundContainerSelector(req);
        model.addAttribute("backgroundContainerSelector", backgroundContainerSelector);

        final PortletPreferences prefs = req.getPreferences();
        model.addAttribute("applyOpacityTo", prefs.getValue("applyOpacityTo", null));
        model.addAttribute("opacityCssValue", prefs.getValue("opacityCssValue", "1.0"));

        return "/jsp/BackgroundPreference/viewBackgroundPreference";
    }

    @ActionMapping(params = {"action=savePreferences"})
    public void savePreferences(
            ActionRequest req,
            ActionResponse res,
            @RequestParam(required = false) String backgroundImage)
            throws Exception {

        imageSetSelectionStrategy.setSelectedImage(req, backgroundImage);

        // Reirect to a basic HTTP 200 success response to save a full page cycle
        res.sendRedirect(req.getContextPath() + "/api" + AjaxSuccessController.SUCCESS_URL);
    }
}
