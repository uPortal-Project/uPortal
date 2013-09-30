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
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.IdentitySwapperManager;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.UrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;

public class LaunchPortletController extends AbstractController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private String launchUrl;
    private String viewLocation;
    private IPortalUrlProvider portalUrlProvider;
    private IPortalRequestUtils portalRequestUtils;
    private static final String VIEW_LOCATION = "viewLocation";
    private static final String LAUNCH_URL = "launchUrl";
    private IPersonManager personManager;
    private IdentitySwapperManager identitySwapperManager;
    private String launchUrlPortletFname;
    private UrlType launchUrlType;
    
    @Autowired(required = true)
    public void setPortalUrlProvider(IPortalUrlProvider urlProvider) {
        this.portalUrlProvider = urlProvider;
    }
    
    @Autowired(required = true)
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        Validate.notNull(portalRequestUtils);
        this.portalRequestUtils = portalRequestUtils;
    }

    @Autowired(required = true)
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired(required = true)
    public void setIdentitySwapperManager(IdentitySwapperManager identitySwapperManager) {
        this.identitySwapperManager = identitySwapperManager;
    }

    @Override
    protected ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) throws Exception {
        final Map<String,Object> model = new HashMap<String, Object>();
        setUserInformationInModel(request, model);
        String urlForModel = getUrlForLauncherModel(request);
        String viewLocationUrl = getViewLocation(request);
        logger.debug("launchUrl {}", urlForModel);
        model.put("launchUrl", urlForModel);
        return new ModelAndView(viewLocationUrl, model);
    }

    private void setUserInformationInModel(PortletRequest portletRequest, final Map<String, Object> model) {
        final HttpServletRequest servletRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        final boolean impersonating = this.identitySwapperManager.isImpersonating(servletRequest);
        final IPerson person = personManager.getPerson(servletRequest);
        logUserInfoDebug(impersonating, person);
        model.put("userName", person.getUserName());
        model.put("displayName", person.getFullName());
        model.put("userImpersonating", impersonating);
    }

    private void logUserInfoDebug(final boolean impersonating, final IPerson person) {
        if (logger.isDebugEnabled()) {
            logger.debug("userName {}", person.getUserName());
            logger.debug("displayName {}", person.getFullName());
            logger.debug("userImpersonating {}", impersonating);
        }
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
            urlForModel = getGeneratedUrlForLauncherModel(portletRequest);
        }
        return urlForModel;
    }

    private String getGeneratedUrlForLauncherModel(PortletRequest portletRequest) {
        assert (this.launchUrlPortletFname != null) : "launchUrlPortletFname cannot be null";
        assert (this.launchUrlType != null) : "launchUrlType cannot be null";
        final HttpServletRequest httpRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        final IPortalUrlBuilder portalUrlBuilder = this.portalUrlProvider.getPortalUrlBuilderByPortletFName(httpRequest, this.launchUrlPortletFname, this.launchUrlType);
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

    public void setLaunchUrlPortletFname(String launchUrlPortletFname) {
        this.launchUrlPortletFname = launchUrlPortletFname;
    }

    public void setLaunchUrlType(UrlType launchUrlType) {
        this.launchUrlType = launchUrlType;
    }
}
