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
package org.jasig.portal.portlets.googleanalytics;

import java.io.IOException;

import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ReadOnlyException;
import javax.portlet.ResourceRequest;
import javax.portlet.ValidatorException;

import org.jasig.portal.portlets.PortletPreferencesJsonDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.fasterxml.jackson.databind.JsonNode;


@Controller
@RequestMapping("CONFIG")
public class GoogleAnalyticsConfigController {
    public static final String CONFIG_PREF_NAME = "config";

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private PortletPreferencesJsonDao portletPreferencesJsonDao;

    @Autowired
    public void setPortletPreferencesJsonDao(PortletPreferencesJsonDao portletPreferencesJsonDao) {
        this.portletPreferencesJsonDao = portletPreferencesJsonDao;
    }
    
    @RenderMapping
    public String renderAnalyticsHeader(PortletRequest request, ModelMap model) throws IOException {
        final PortletPreferences preferences = request.getPreferences();
        final JsonNode config = this.portletPreferencesJsonDao.getJsonNode(preferences, CONFIG_PREF_NAME);
        
        model.put("data", config);
        
        return "jsp/GoogleAnalytics/config";
    }

    @ResourceMapping("getData")
    public String getData(PortletRequest portletRequest, ModelMap model) throws IOException {
        final PortletPreferences preferences = portletRequest.getPreferences();
        final JsonNode config = this.portletPreferencesJsonDao.getJsonNode(preferences, CONFIG_PREF_NAME);
        
        model.put("data", config);
        
        return "jsonView";
    }
    
    @ResourceMapping("storeData")
    public String storeData(ResourceRequest request, @RequestParam JsonNode config) throws ValidatorException, IOException, ReadOnlyException {
        final PortletPreferences preferences = request.getPreferences();
        
        this.portletPreferencesJsonDao.storeJson(preferences, CONFIG_PREF_NAME, config);
        
        return "jsonView";
    }
    
    @ActionMapping("configDone")
    public void configComplete(ActionResponse response) throws PortletModeException {
        response.setPortletMode(PortletMode.VIEW);
    }
}
