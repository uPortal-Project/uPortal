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
import java.util.Enumeration;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ActionMapping;

/**
 * SkinManagerPortletConfigController has a CONFIG-mode portlet that allows an admin to set various skin properties.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */
@Controller
@RequestMapping("CONFIG")
public class SkinManagerPortletConfigController {
    public static final String PRIMARY_COLOR = "color1";
    public static final String SECONDARY_COLOR = "color2";
    public static final String TERTIARY_COLOR = "color3";
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @ActionMapping
    public void updateSkinConfiguration(ActionRequest request, ActionResponse response,
                                        PortletPreferences prefs, SkinPreferencesDto form,
                                        @RequestParam(value="save", required=false) String save)
            throws IOException, ReadOnlyException, ValidatorException, PortletModeException {
        if (StringUtils.isNotBlank(save)) {
            prefs.setValue(PRIMARY_COLOR, form.getColor1());
            prefs.setValue(SECONDARY_COLOR, form.getColor2());
            prefs.setValue(TERTIARY_COLOR, form.getColor3());
            prefs.store();
            log.debug("Saved updated configuration");
        }
        response.setPortletMode(PortletMode.VIEW);
    }

    /**
     * Display an edit form
     */
    @RequestMapping
    public String showConfigPage(RenderRequest request, RenderResponse response, PortletPreferences preferences,
                                 Model model) {
        // Get the list of preferences and add them to the model
        Enumeration<String> preferenceNames = preferences.getNames();
        while (preferenceNames.hasMoreElements()) {
            String name = preferenceNames.nextElement();
            model.addAttribute(name, preferences.getValue(name, ""));
        }

        return "jsp/SkinManager/skinManagerConfig";
    }

}
