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
 * Provides endpoint that returns a JSON representation of the user's layout.  The purpose of this 
 * data is to support the Javascript-driven rendering of the uPortal UI.
 * 
 * @author Gary Roybal
 * @since uPortal 4.3
 */
@Controller
public class LayoutJsonV43RenderingController {

    private static final String STRUCTURE_STYLESHEET_NAME = "DLMTabsColumnsJS";
    private static final String THEME_STYLESHEET_NAME = "JsonLayoutV4-3";
    
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IStylesheetUserPreferencesService stylesheetUserPrefService;
    
    private IPortalRenderingPipeline portalRenderingPipeline;
    private IPortletWindowRegistry portletWindowRegistry;

    @Autowired
    @Qualifier("json")
    public void setPortalRenderingPipeline(IPortalRenderingPipeline portalRenderingPipeline) {
        this.portalRenderingPipeline = portalRenderingPipeline;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @RequestMapping(value="/v4-3/dlm/layout.json", method = RequestMethod.GET)
    public void renderRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.setStructureStylesheetNameForJavascriptDrivenContentRendering(request);
        this.setThemeStylesheetVersionForJavascriptDrivenContentRendering(request);
        this.portletWindowRegistry.disablePersistentWindowStates(request);
        this.portalRenderingPipeline.renderState(request, response);
    }

    private void setStructureStylesheetNameForJavascriptDrivenContentRendering(
            final HttpServletRequest request) {
        stylesheetUserPrefService.setStructureStylesheetOverride(request, STRUCTURE_STYLESHEET_NAME);
    }

    private void setThemeStylesheetVersionForJavascriptDrivenContentRendering(
            final HttpServletRequest request) {
        stylesheetUserPrefService.setThemeStyleSheetOverride(request, THEME_STYLESHEET_NAME);
        
    }

}
