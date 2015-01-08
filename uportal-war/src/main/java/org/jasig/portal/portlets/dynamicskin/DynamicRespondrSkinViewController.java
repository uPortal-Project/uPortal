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
package org.jasig.portal.portlets.dynamicskin;

import java.io.IOException;
import java.text.MessageFormat;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.security.AuthorizationPrincipalHelper;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

/**
 * The DynamicRespondrSkin portlet includes a CONFIG mode interface that allows
 * an admin to set various skin properties, together with a VIEW mode controller
 * that renders a link to the compiled skin (CSS file) and generates that file if
 * necessary.
 *
 * @since 4.1.0
 * @author James Wennmacher, jwennmacher@unicon.net
 */
@Controller
@RequestMapping("VIEW")
public class DynamicRespondrSkinViewController {
    private static final String RELATIVE_ROOT = "/media/skins/respondr";
    private static final MessageFormat CSS_PATH_FORMAT = new MessageFormat(RELATIVE_ROOT + "/{0}{1}.css");
    private static final String DYNAMIC_SKIN_FILENAME_BASE = "skin";
    private static final String DEFAULT_SKIN_NAME = "defaultSkin";
    private static final String PREF_SKIN_NAME = DynamicSkinService.CONFIGURABLE_PREFIX + "dynamicSkinName";
    private static final String PREF_DYNAMIC = DynamicSkinService.CONFIGURABLE_PREFIX +"dynamicSkinEnabled";

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    DynamicSkinService service;

    @Autowired
    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    private IPersonManager personManager;

    @Autowired
    private IPortletWindowRegistry portletWindowRegistry;

    /**
     * Display Skin CSS include based on skin's configuration values from portlet preferences.<br/>
     * dynamic=false: load the pre-built css file from the skins directory and default skin name; e.g.
     *                RELATIVE_ROOT/{defaultSkin}.css
     * dynamic=true: Process the default skin less file if needed at RELATIVE_ROOT/{defaultSkin}.less
     *               to create a customized skin css file (RELATIVE_ROOT/skin-ID#.css to load.
     */
    @RenderMapping
    public ModelAndView displaySkinCssHeader(RenderRequest request, RenderResponse response, Model model) throws IOException {

        // NOTE:  RENDER_HEADERS phase may be called before or at the same time as the RENDER_MARKUP. The spec is
        // silent on this issue and uPortal does not guarantee order or timing of render execution, but does
        // guarantee order of render output processing (output of RENDER_HEADERS phase is included before
        // RENDER_MARKUP phase).  uPortal inserts the HTML markup returned from RENDER_HEADERS execution into the HEAD
        // section of the page.

        if (PortletRequest.RENDER_HEADERS.equals(request.getAttribute(PortletRequest.RENDER_PART))) {

            PortletPreferences prefs = request.getPreferences();
            Boolean enabled = Boolean.valueOf(prefs.getValue(PREF_DYNAMIC, "false"));
            String defaultSkinName = prefs.getValue(PREF_SKIN_NAME, DEFAULT_SKIN_NAME);
            String cssUrl = enabled ? calculateDynamicSkinUrlPathToUse(request, defaultSkinName)
                    : calculateCssLocationInWebapp(defaultSkinName, "");
            model.addAttribute("skinCssUrl", cssUrl);
            return new ModelAndView("jsp/DynamicRespondrSkin/skinHeader");
        } else {
            // We need to know if this user can CONFIG this skin
            boolean canAccessSkinConfig = false;  // Default
            final HttpServletRequest httpr = portalRequestUtils.getCurrentPortalRequest();
            final IPerson user = personManager.getPerson(httpr);
            final IAuthorizationPrincipal principal = AuthorizationPrincipalHelper.principalFromUser(user);
            final IPortletWindowId portletWindowId = portletWindowRegistry.getPortletWindowId(httpr, request.getWindowID());
            final IPortletWindow portletWindow = portletWindowRegistry.getPortletWindow(httpr, portletWindowId);
            final IPortletEntity portletEntity = portletWindow.getPortletEntity();
            if (principal.canConfigure(portletEntity.getPortletDefinitionId().toString())) {
                canAccessSkinConfig = true;
            }
            // RENDER_MARKUP
            return new ModelAndView("jsp/DynamicRespondrSkin/skinBody", "canAccessSkinConfig", canAccessSkinConfig);
        }
    }

    /**
     * Calculate the default skin URL path or the path to a skin CSS file that is specific to the set of
     * portlet preference values currently defined.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    private String calculateDynamicSkinUrlPathToUse(PortletRequest request, String lessfileBaseName) throws IOException {
        final String skinToken = service.calculateTokenForCurrentSkin(request);
        final String locationOnDisk = calculateCssLocationOnDisk(request, skinToken);

        if (!service.skinFileExists(locationOnDisk)) {
            // Trigger the LESS compilation
            service.generateSkinCssFile(request, locationOnDisk, skinToken, lessfileBaseName);
        }

        return calculateCssLocationInWebapp(DYNAMIC_SKIN_FILENAME_BASE, skinToken);
    }

    private String calculateCssLocationOnDisk(PortletRequest request, String skinCssHashcode) {
        final String relative = calculateCssLocationInWebapp(DYNAMIC_SKIN_FILENAME_BASE, skinCssHashcode);
        return request.getPortletSession().getPortletContext().getRealPath(relative);
    }

    /**
     * Calculates the relative URL of the CSS file.
     * @param skinName skin filename
     * @param skinCssHashcode If dynamic skin, unique string based on preference values. For static skin, may be
     *                        empty string.
     * @return Relative URL of the CSS file for the user.
     */
    private String calculateCssLocationInWebapp(String skinName, String skinCssHashcode) {
        return CSS_PATH_FORMAT.format(new Object[] {skinName, skinCssHashcode});
    }

}
