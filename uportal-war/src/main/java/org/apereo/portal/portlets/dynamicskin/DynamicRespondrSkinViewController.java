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
package org.apereo.portal.portlets.dynamicskin;

import java.io.IOException;
import java.text.MessageFormat;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.portlets.dynamicskin.storage.DynamicSkinService;
import org.apereo.portal.security.AuthorizationPrincipalHelper;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.url.IPortalRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

/**
 * The DynamicRespondrSkin portlet includes a CONFIG mode interface that allows an admin to set
 * various skin properties, together with a VIEW mode controller that renders a link to the compiled
 * skin (CSS file) and generates that file if necessary.
 *
 * @since 4.1.0
 */
@Controller
@RequestMapping("VIEW")
public class DynamicRespondrSkinViewController {
    private static final MessageFormat DEFAULT_SKIN_CSS_PATH_FORMAT =
            new MessageFormat(DynamicRespondrSkinConstants.DEFAULT_RELATIVE_ROOT_FOLDER + "/{0}.css");

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired private DynamicSkinService service;

    @Autowired private IPortalRequestUtils portalRequestUtils;

    @Autowired private IPersonManager personManager;

    @Autowired private IPortletWindowRegistry portletWindowRegistry;

    /**
     * Display Skin CSS include based on skin's configuration values from portlet preferences.<br>
     * dynamic=false: load the pre-built css file from the skins directory and default skin name;
     * e.g. RELATIVE_ROOT/{defaultSkin}.css dynamic=true: Process the default skin less file if
     * needed at RELATIVE_ROOT/{defaultSkin}.less to create a customized skin css file
     * (RELATIVE_ROOT/skin-ID#.css to load.
     */
    @RenderMapping
    public ModelAndView displaySkinCssHeader(
            RenderRequest request, RenderResponse response, Model model) throws IOException {

        // NOTE:  RENDER_HEADERS phase may be called before or at the same time as the RENDER_MARKUP. The spec is
        // silent on this issue and uPortal does not guarantee order or timing of render execution, but does
        // guarantee order of render output processing (output of RENDER_HEADERS phase is included before
        // RENDER_MARKUP phase).  uPortal inserts the HTML markup returned from RENDER_HEADERS execution into the HEAD
        // section of the page.

        if (PortletRequest.RENDER_HEADERS.equals(
                request.getAttribute(PortletRequest.RENDER_PART))) {

            PortletPreferences prefs = request.getPreferences();
            Boolean enabled = Boolean.valueOf(prefs.getValue(DynamicRespondrSkinConstants.PREF_DYNAMIC, "false"));
            String skinName = prefs.getValue(DynamicRespondrSkinConstants.PREF_SKIN_NAME, DynamicRespondrSkinConstants.DEFAULT_SKIN_NAME);
            String cssUrl = enabled ?
                    calculateDynamicSkinUrlPathToUse(request, skinName) : calculateDefaultSkinCssLocationInWebapp(skinName);
            model.addAttribute(DynamicRespondrSkinConstants.SKIN_CSS_URL_MODEL_ATTRIBUTE_NAME, cssUrl);
            return new ModelAndView("jsp/DynamicRespondrSkin/skinHeader");
        } else {
            // We need to know if this user can CONFIG this skin
            boolean canAccessSkinConfig = false; // Default
            final HttpServletRequest httpr = portalRequestUtils.getCurrentPortalRequest();
            final IPerson user = personManager.getPerson(httpr);
            final IAuthorizationPrincipal principal =
                    AuthorizationPrincipalHelper.principalFromUser(user);
            final IPortletWindowId portletWindowId =
                    portletWindowRegistry.getPortletWindowId(httpr, request.getWindowID());
            final IPortletWindow portletWindow =
                    portletWindowRegistry.getPortletWindow(httpr, portletWindowId);
            final IPortletEntity portletEntity = portletWindow.getPortletEntity();
            if (principal.canConfigure(portletEntity.getPortletDefinitionId().toString())) {
                canAccessSkinConfig = true;
            }
            // RENDER_MARKUP
            return new ModelAndView(
                    "jsp/DynamicRespondrSkin/skinBody",
                    DynamicRespondrSkinConstants.CAN_ACCESS_SKIN_CONFIG_MODEL_NAME,
                    canAccessSkinConfig);
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
        final DynamicSkinInstanceData data = new DefaultDynamicSkinInstanceDataImpl(request);
        if (!service.skinCssFileExists(data)) {
            // Trigger the LESS compilation
            service.generateSkinCssFile(data);
        }
        return service.getSkinCssPath(data);
    }

    /**
     * Calculates the relative URL of the default skin CSS file.
     * @param skinName skin filename
     * @return Relative URL of the default skin CSS file for the user.
     */
    private String calculateDefaultSkinCssLocationInWebapp(String skinName) {
        return DEFAULT_SKIN_CSS_PATH_FORMAT.format(new Object[] {skinName});
    }
}
