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

package org.jasig.portal.portlets.skinmanager;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

/**
 * SkinManagerPortletController has a CONFIG-mode portlet that allows an admin to set various skin properties,
 * and a view controller that compiles the LESS skin files if they haven't been compiled yet for the configured
 * skin values and renders a link for the user that points to the configured skin (CSS file).
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */
@Controller
@RequestMapping("VIEW")
public class SkinManagerPortletController {
    private static final String RELATIVE_ROOT = "/media/skins/respondr";
    private static final MessageFormat CSS_PATH_FORMAT = new MessageFormat(RELATIVE_ROOT + "/skin{0}.css");
    private static final String DEFAULT_CSS_PATH = RELATIVE_ROOT + "/defaultSkin.css";

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private String defaultHash = null;  // Synchronized -- do not access directly
    private String defaultCssPath = DEFAULT_CSS_PATH;

    @Autowired(required = true)
    SkinManagerService service;

    public void setDefaultCssPath(String defaultCssPath) {
        this.defaultCssPath = defaultCssPath;
    }

    public void setService(SkinManagerService service) {
        this.service = service;
    }

    private synchronized String getDefaultHash() {
        return defaultHash;
    }

    @ModelAttribute("isAdmin")
    public boolean userIsAdmin(PortletRequest request) {
        // determine if this user belongs to the defined administration group and store the result
        return request.isUserInRole("skinAdmin");
    }

    /**
     * Display Skin CSS include based on skin's configuration values.
     */
    @RequestMapping
    public String displaySkinCssHeader(RenderRequest request, Model model) throws IOException {
        String cssUrl = calculateSkinUrlPathToUse(request);
        model.addAttribute("skinCssUrl", cssUrl);
        return "jsp/SkinManager/skinHeader";
    }

    /**
     * Return URL of skin CSS file
     */
    @ResourceMapping(value = "retriveSkinCssURL")
    public ModelAndView jsonRetrieveSkinCssUrl(PortletRequest request) throws IOException {

        final Map<String,Object> model = new HashMap<String, Object>();
        model.put("skinCssUrl", calculateSkinUrlPathToUse(request));
        return new ModelAndView("json", model);
    }

    /**
     * Calculate the default skin URL path or the path to a skin CSS file that is specific to the set of
     * portlet preference values currently defined.
     * @param request
     * @return
     * @throws IOException
     */
    private String calculateSkinUrlPathToUse(PortletRequest request) throws IOException {
        String computedDefaultHashcode = computeDefaultHashIfNeeded(request);
        String skinCssHashcode = service.calculateSkinHash(request);
        String cssUrl = defaultCssPath;
        if (!skinCssHashcode.equals(computedDefaultHashcode)) {
            String cssFilepath = calculateSkinAbsoluteFilePath(request, skinCssHashcode);

            if (!service.skinFileExists(cssFilepath)) {
                service.createSkinCssFile(request, cssFilepath, skinCssHashcode);
            }
            cssUrl = calculateSkinUrlPath(skinCssHashcode);
        }
        return cssUrl;
    }

    private String calculateSkinUrlPath(String skinCssFilename) {
        return CSS_PATH_FORMAT.format(new Object[]{skinCssFilename});
    }

    private String calculateSkinAbsoluteFilePath (PortletRequest request, String skinCssFilename) {
        String relative = CSS_PATH_FORMAT.format(new Object[]{skinCssFilename});
        String path = request.getPortletSession().getPortletContext().getRealPath(relative);
        return path;
    }

    /**
     * Return or compute the hashcode of the variables used in the build-generated defaultSkin.css. If the preference
     * values are the same as the original less variables we can avoid generating a preference-specific css file.
     * This saves several seconds of time after a deploy-war if the user will basically see the build-generated
     * defaultSkin anyway.
     * @param request
     */
    private synchronized String computeDefaultHashIfNeeded(PortletRequest request) throws IOException {
        if (defaultHash == null ) {
            // Update within the synchronized block
            defaultHash = service.computeDefaultHashcode(request);
        }
        // Return it so calling code doesn't need to invoke another synchronized block to get the value.
        return defaultHash;
    }

}
