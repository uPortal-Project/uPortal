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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.layout.IStylesheetUserPreferencesService;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.xml.XmlUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * JUnit test class for {@link JsonThemeTransformerSource}.
 */
public class JsonThemeTransformerSourceTest {

    final private long id = 1L;
    final private String stylesheetName1 = "JsonLayout";
    final private String stylesheetName2 = "JsonLayoutV2";
    final private String stylesheetResourceLocation1 = "/path/to/json.xsl";
    final private String stylesheetResourceLocation2 = "/path/to/json-v2.xsl";

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private IUserInstance userInstance;
    @Mock private IUserPreferencesManager userPreferencesManager;
    @Mock private IStylesheetDescriptor stylesheetDescriptor1;
    @Mock private IStylesheetDescriptor stylesheetDescriptor2;
    @Mock private IStylesheetDescriptorDao stylesheetDescriptorDao;
    @Mock private IStylesheetUserPreferencesService stylesheetUserPreferencesService;
    @Mock private IUserInstanceManager userInstanceManager;
    @Mock private Resource stylesheetResource1;
    @Mock private Resource stylesheetResource2;
    @Mock private ResourceLoader resourseLoader;
    @Mock private XmlUtilities xmlUtilities;

    private JsonThemeTransformerSource source;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        given(this.userInstanceManager.getUserInstance(this.request)).willReturn(this.userInstance);
        given(this.userInstance.getPreferencesManager()).willReturn(this.userPreferencesManager);
        given(this.stylesheetDescriptorDao.getStylesheetDescriptor(this.id)).willReturn(this.stylesheetDescriptor1);
        given(this.stylesheetDescriptorDao.getStylesheetDescriptorByName(this.stylesheetName1)).willReturn(this.stylesheetDescriptor1);
        given(this.stylesheetDescriptorDao.getStylesheetDescriptorByName(this.stylesheetName2)).willReturn(this.stylesheetDescriptor2);
        given(this.stylesheetDescriptorDao.getStylesheetDescriptorByName(JsonThemeTransformerSource.DEFAULT_STYLESHEET_NAME)).willReturn(this.stylesheetDescriptor1);
        given(this.stylesheetDescriptorDao.getStylesheetDescriptorByName(JsonThemeTransformerSource.DEFAULT_STYLESHEET_NAME)).willReturn(this.stylesheetDescriptor1);
        given(this.stylesheetDescriptor1.getId()).willReturn(this.id);
        given(this.resourseLoader.getResource(this.stylesheetResourceLocation1)).willReturn(this.stylesheetResource1);
        given(this.resourseLoader.getResource(this.stylesheetResourceLocation2)).willReturn(this.stylesheetResource2);
        this.source = new JsonThemeTransformerSource();
        this.source.setStylesheetDescriptorDao(this.stylesheetDescriptorDao);
        this.source.setResourceLoader(this.resourseLoader);
        this.source.setUserInstanceManager(this.userInstanceManager);
        this.source.setXmlUtilities(this.xmlUtilities);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getTransformerMethodShouldUseDefaultStylesheetWhenNoVersionOverrideSpecified() {
        // given
        given(this.request.getAttribute(JsonThemeTransformerSource.STYLESHEET_VERSION_OVERRIDE_REQUEST_ATTRIBUTE_NAME)).willReturn(null);
        // when
        this.source.getTransformer(this.request, this.response);
        // then
        verify(this.stylesheetDescriptorDao).getStylesheetDescriptorByName(JsonThemeTransformerSource.DEFAULT_STYLESHEET_NAME);
    }

    @Test
    public void getTransformerMethodShouldUseStylesheetVersionSpecifiecInRequestAttributeValue() {
        // given
        final String versionOverride = "V2";
        given(this.request.getAttribute(JsonThemeTransformerSource.STYLESHEET_VERSION_OVERRIDE_REQUEST_ATTRIBUTE_NAME)).willReturn(versionOverride);
        // when
        this.source.getTransformer(this.request, this.response);
        // then
        verify(this.stylesheetDescriptorDao).getStylesheetDescriptorByName(JsonThemeTransformerSource.STYLESHEET_ROOT_NAME + versionOverride);
    }

}
