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

package org.jasig.portal.portlets.search;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("CONFIG")
public class SearchPortletConfigurationController {

    @RequestMapping
    public String getConfigurationView() {
        return "/jsp/Search/searchConfiguration";
    }
    
    @RequestMapping(params="action=save")
    public void saveConfiguration(ActionRequest request,
            ActionResponse response,
            @ModelAttribute("form") SearchPortletConfigurationForm form) throws PortletModeException {
        
        PortletPreferences prefs = request.getPreferences();
        
        try {
            
            prefs.setValue("gsaEnabled", String.valueOf(form.isGsaEnabled()));
            prefs.setValue("gsaBaseUrl", form.getGsaBaseUrl());
            prefs.setValue("gsaSite", form.getGsaSite());
            prefs.setValue("directoryEnabled", String.valueOf(form.isDirectoryEnabled()));
            prefs.setValue("portletRegistryEnabled", String.valueOf(form.isPortletRegistryEnabled()));
            prefs.store();
            
        } catch (ValidatorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ReadOnlyException e) {
            e.printStackTrace();
        }
        
        response.setPortletMode(PortletMode.VIEW);
        
    }
    
    @ModelAttribute("form")
    public SearchPortletConfigurationForm getForm(PortletRequest request) {
        SearchPortletConfigurationForm form = new SearchPortletConfigurationForm();

        PortletPreferences prefs = request.getPreferences();
        form.setGsaEnabled(Boolean.valueOf(prefs.getValue("gsaEnabled", "false")));
        form.setGsaBaseUrl(prefs.getValue("gsaBaseUrl", ""));
        form.setGsaSite(prefs.getValue("gsaSite", ""));
        form.setDirectoryEnabled(Boolean.valueOf(prefs.getValue("directoryEnabled", "false")));
        form.setPortletRegistryEnabled(Boolean.valueOf(prefs.getValue("portletRegistryEnabled", "false")));
        
        return form;
    }
    
}
