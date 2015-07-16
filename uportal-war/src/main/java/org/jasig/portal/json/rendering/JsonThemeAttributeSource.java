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
package org.jasig.portal.json.rendering;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jasig.portal.layout.IStylesheetUserPreferencesService.PreferencesScope;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.rendering.StylesheetAttributeSource;

/**
 * {@link StylesheetAttributeSource} specific to stylesheets used to produce layout data in JSON 
 * format.  This class originally hardcoded the stylesheet name as "JsonLayout" and was used to 
 * support the "/uPortal/layout.json" call (used for mobile layout rendering).  This class has 
 * been updated to support new layout JSON endpoints that will be versioned.  For the newer 
 * endpoints, this class is now able to use stylesheets with the name "JsonLayout<version>", 
 * where "<version>" specifies the version (e.g. "JsonLayoutV2").  This class now looks for a 
 * request attribute that specifies the version.  The version will typically be specified in the 
 * controller class for the endpoint.  For backwards compatibility, if no version request 
 * attribute is found, the default stylesheet used will be the original "JsonLayout".
 * 
 * Note that the only difference in the rendering pipeline between the original endpoint and the 
 * new versioned endpoints are the stylesheets used.  Because of the number of beans required to 
 * set up the pipeline (due to the deep chaining of {@link PipelineComponents}), it is desireable 
 * to reuse the Spring Framework configuration that sets this up rather than duplicating it for 
 * each endpoint.  Using a request attribute to specify the stylesheet version (instead of using a 
 * configured value) allows us to do this as each endpoint class or method can specify a different 
 * version.
 * 
 * @see {@link LayoutJsonV1RenderingController}
 * @see {@link LayoutJsonV2RenderingController}
 * @see {@link JsonThemeTransformerSource}
 */
public class JsonThemeAttributeSource extends StylesheetAttributeSource {

	public static final String STYLESHEET_ROOT_NAME = "JsonLayout";
	public static final String DEFAULT_STYLESHEET_NAME = STYLESHEET_ROOT_NAME;
    public static final String STYLESHEET_VERSION_OVERRIDE_REQUEST_ATTRIBUTE_NAME = 
            JsonThemeAttributeSource.class.getCanonicalName() + ".STYLESHEET_VERSION";

    @Override
    public IStylesheetDescriptor getStylesheetDescriptor(HttpServletRequest request) {
        return this.stylesheetDescriptorDao.getStylesheetDescriptorByName(
                this.getStyleSheetName(request));
    }

    protected String getStyleSheetName(final HttpServletRequest request) {
        final String stylesheetVersionFromRequest =
                (String)request.getAttribute(STYLESHEET_VERSION_OVERRIDE_REQUEST_ATTRIBUTE_NAME);
        if (StringUtils.isEmpty(stylesheetVersionFromRequest)) {
            return DEFAULT_STYLESHEET_NAME;
        } else {
            return STYLESHEET_ROOT_NAME + stylesheetVersionFromRequest;
        }
    }

    @Override
    public PreferencesScope getStylesheetPreferencesScope(HttpServletRequest request) {
        return PreferencesScope.THEME;
    }

}
