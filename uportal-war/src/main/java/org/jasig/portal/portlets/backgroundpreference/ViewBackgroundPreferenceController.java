/**
 * Licensed to Jasig under one or more contributor license
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
package org.jasig.portal.portlets.backgroundpreference;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;

import org.jasig.portal.rest.AjaxSuccessController;
import org.jasig.portal.spring.spel.IPortalSpELService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.context.PortletWebRequest;

@Controller
@RequestMapping("VIEW")
public class ViewBackgroundPreferenceController {

    private BackgroundSetSelectionStrategy imageSetSelectionStrategy;

    /**
     * Getter of member imageSetSelectionStrategy.
     * @return <code>BackgroundSetSelectionStrategy</code> the attribute imageSetSelectionStrategy
     */
    public BackgroundSetSelectionStrategy getImageSetSelectionStrategy() {
        if (imageSetSelectionStrategy == null) {
            imageSetSelectionStrategy = new RoleBasedBackgroundSetSelectionStrategy(this.portalSpELService);
        }
        return imageSetSelectionStrategy;
    }

    public void setImageSetSelectionStrategy(BackgroundSetSelectionStrategy imageSetSelectionStrategy) {
        this.imageSetSelectionStrategy = imageSetSelectionStrategy;
    }

    private IPortalSpELService portalSpELService;

    @Autowired
    public void setPortalSpELService(IPortalSpELService portalSpELService) {
        this.portalSpELService = portalSpELService;
    }

    /**
     * Display the main user-facing view of the portlet.
     *
     * @param request
     * @return
     */
    @RenderMapping
    public String getView(RenderRequest req, Model model) {

        final PortletWebRequest webRequest = new PortletWebRequest(req);

        String[] images = getImageSetSelectionStrategy().getImageSet(req);
        model.addAttribute("images", setPrefContextpath(images, webRequest));

        String[] thumbnailImages = getImageSetSelectionStrategy().getImageThumbnailSet(req);
        model.addAttribute("thumbnailImages", setPrefContextpath(thumbnailImages, webRequest));

        String preferredBackgroundImage = getImageSetSelectionStrategy().getSelectedImage(req);
        model.addAttribute("backgroundImage", setPrefContextpath(preferredBackgroundImage, webRequest));

        String backgroundContainerSelector = getImageSetSelectionStrategy().getBackgroundContainerSelector(req);
        model.addAttribute("backgroundContainerSelector", setPrefContextpath(backgroundContainerSelector, webRequest));

        PortletPreferences prefs = req.getPreferences();
        model.addAttribute("applyOpacityTo", prefs.getValue("applyOpacityTo", null));
        model.addAttribute("opacityCssValue", prefs.getValue("opacityCssValue", "1.0"));

        return "/jsp/BackgroundPreference/viewBackgroundPreference";
    }

    @ActionMapping(params = {"action=savePreferences"})
    public void savePreferences(ActionRequest req, ActionResponse res,
            @RequestParam(required=false) String backgroundImage) throws Exception {

        getImageSetSelectionStrategy().setSelectedImage(req, backgroundImage);

        // Reirect to a basic HTTP 200 success response to save a full page cycle
        res.sendRedirect(req.getContextPath() + "/api" + AjaxSuccessController.SUCCESS_URL);

    }

    protected String[] setPrefContextpath(String[] values, WebRequest request) {
        if (values != null && values.length > 0) {
            for (int i = 0; i < values.length; i++)
                values[i] = this.setPrefContextpath(values[i], request);
        }

        return values;
    }

    protected String setPrefContextpath(String value, WebRequest request) {
        if (value != null) {
            return this.portalSpELService.parseString(value, request);
        }

        return value;
    }

}
