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
package org.jasig.portal.rest.layout;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.layout.IStylesheetUserPreferencesService;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.rendering.IPortalRenderingPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Eric Dalquist
 */
@Controller
public class LayoutJsonV1RenderingController {

    public static final String STRUCTURE_STYLESHEET_NAME = "DLMMobileColumns";
    public static final String THEME_STYLESHEET_NAME = "JsonLayout";
    public static final String URL = "/v1/dlm/layout.json";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private IPortalRenderingPipeline portalRenderingPipeline;
    private IPortletWindowRegistry portletWindowRegistry;

    @Autowired
    private IStylesheetUserPreferencesService stylesheetUserPrefService;
    
    @Autowired
    @Qualifier("json")
    public void setPortalRenderingPipeline(IPortalRenderingPipeline portalRenderingPipeline) {
        this.portalRenderingPipeline = portalRenderingPipeline;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @RequestMapping(value=URL, method = RequestMethod.GET)
    public void v1RenderRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.internalRenderRequest(request, response);
    }

    private void internalRenderRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setStructureStylesheetName(request);
        setThemeStylesheetName(request);
        portletWindowRegistry.disablePersistentWindowStates(request);
        portalRenderingPipeline.renderState(request, response);
    }

    private void setStructureStylesheetName(final HttpServletRequest request) {
        stylesheetUserPrefService.setStructureStylesheetOverride(request, STRUCTURE_STYLESHEET_NAME);
    }

    private void setThemeStylesheetName(final HttpServletRequest request) {
        stylesheetUserPrefService.setThemeStyleSheetOverride(request, THEME_STYLESHEET_NAME);
    }

}
