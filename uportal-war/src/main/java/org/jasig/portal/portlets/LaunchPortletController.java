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

package org.jasig.portal.portlets;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.UrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;

public class LaunchPortletController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());    
    private String launchUrl;
    private String viewLocation;
    private IPortalUrlProvider portalUrlProvider;
    private IPortalRequestUtils portalRequestUtils;
    private static final String VIEW_LOCATION = "viewLocation";
    private static final String LAUNCH_URL = "launchUrl";

    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider urlProvider) {
        this.portalUrlProvider = urlProvider;
    }
    
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        Validate.notNull(portalRequestUtils);
        this.portalRequestUtils = portalRequestUtils;
    }

    @RequestMapping
    public ModelAndView getView(PortletRequest portletRequest) {
        final Map<String,Object> model = new HashMap<String, Object>();
        String urlForModel = getUrlForLauncherModel(portletRequest);
        String viewLocationUrl = getViewLocation(portletRequest);
        model.put("launchUrl", urlForModel);
        return new ModelAndView(viewLocationUrl, model);
    }

	private String getViewLocation(PortletRequest portletRequest) {
		String viewLocationUrl;
        PortletPreferences prefs = portletRequest.getPreferences();
        String preferenceViewLocation = prefs.getValue(VIEW_LOCATION, "");
        if(StringUtils.isNotBlank(preferenceViewLocation)) {
            viewLocationUrl = preferenceViewLocation;
        } else {
            viewLocationUrl = viewLocation;
        }
		return viewLocationUrl;
	}

    private String getUrlForLauncherModel(PortletRequest portletRequest) {
        String urlForModel;
        PortletPreferences prefs = portletRequest.getPreferences();
        String preferenceLaunchUrl = prefs.getValue(LAUNCH_URL, "");
        if (StringUtils.isNotBlank(preferenceLaunchUrl)) {
            urlForModel = preferenceLaunchUrl;
        } else if (StringUtils.isNotBlank(launchUrl)) {
            urlForModel = launchUrl;
        } else {
            urlForModel = getUrlForSearchLauncherModel(portletRequest);
        }
        return urlForModel;
    }

    private String getUrlForSearchLauncherModel(PortletRequest portletRequest) {
        final HttpServletRequest httpRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        final IPortalUrlBuilder portalUrlBuilder = this.portalUrlProvider.getPortalUrlBuilderByPortletFName(httpRequest, "search", UrlType.ACTION);
        return portalUrlBuilder.getUrlString();
    }

    public String getViewLocation() {
        return viewLocation;
    }

    public void setViewLocation(String viewLocation) {
        this.viewLocation = viewLocation;
    }

    public String getLaunchUrl() {
        return launchUrl;
    }

    public void setLaunchUrl(String launchUrl) {
        this.launchUrl = launchUrl;
    }
}
