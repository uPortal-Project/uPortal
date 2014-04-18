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

package org.jasig.portal.portlets.dynamicskin;

import java.io.IOException;
import java.util.Enumeration;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

/**
 * DynamicRespondrSkin portlet includes a CONFIG mode interface that allows an
 * admin to set various skin properties.  Supports the optional 'dynamic'
 * strategy for skinning Respondr.
 *
 * @since 4.1.0
 * @author James Wennmacher, jwennmacher@unicon.net
 */
@Controller
@RequestMapping("CONFIG")
public class DynamicRespondrSkinConfigController {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @ActionMapping(params = "action=update")
    public void updateSkinConfiguration(ActionRequest request, ActionResponse response, PortletPreferences prefs)
            throws IOException, ReadOnlyException, ValidatorException, PortletModeException {

        // Get the list of preferences and pull them from the request and store into preferences
        Enumeration<String> preferenceNames = prefs.getNames();
        while (preferenceNames.hasMoreElements()) {
            String name = preferenceNames.nextElement();
            if (name.startsWith(DynamicSkinService.CONFIGURABLE_PREFIX)) {
                String formValue = request.getParameter(name);
                prefs.setValue(name, formValue != null ? formValue : "");
            }
        }

        prefs.store();
        log.debug("Saved updated configuration");
    }

    @ActionMapping(params = "action=cancel")
    public void cancelUpdate(ActionResponse response)
            throws IOException, ReadOnlyException, ValidatorException, PortletModeException {
        response.setPortletMode(PortletMode.VIEW);
    }

    /**
     * Display a form to manage skin choices.
     */
    @RenderMapping
    public String showConfigPage(PortletPreferences preferences, Model model) {
        // Get the list of preferences and add them to the model
        Enumeration<String> preferenceNames = preferences.getNames();
        while (preferenceNames.hasMoreElements()) {
            String name = preferenceNames.nextElement();
            if (name.startsWith(DynamicSkinService.CONFIGURABLE_PREFIX)) {
                model.addAttribute(name, preferences.getValue(name, ""));
            }
        }

        return "jsp/DynamicRespondrSkin/skinConfig";
    }

}
