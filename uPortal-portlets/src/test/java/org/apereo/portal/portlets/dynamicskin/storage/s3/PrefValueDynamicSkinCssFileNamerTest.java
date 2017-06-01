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
package org.apereo.portal.portlets.dynamicskin.storage.s3;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apereo.portal.portlets.dynamicskin.DynamicSkinException;
import org.apereo.portal.portlets.dynamicskin.DynamicSkinInstanceData;
import org.apereo.portal.portlets.dynamicskin.storage.s3.PrefValueDynamicSkinCssFileNamer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * JUnit test class for {@link PrefValueDynamicSkinCssFileNamer}.
 */
public class PrefValueDynamicSkinCssFileNamerTest {

    @Mock private DynamicSkinInstanceData data;
    @Mock private PortletRequest request;
    @Mock private PortletPreferences preferences;
    private PrefValueDynamicSkinCssFileNamer namer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.namer = new PrefValueDynamicSkinCssFileNamer();
        given(this.data.getPortletRequest()).willReturn(this.request);
        given(this.request.getPreferences()).willReturn(this.preferences);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test (expected = IllegalArgumentException.class)
    public void throwsExceptionIfConstructorPreferenceNameArgumentIsNull() {
        new PrefValueDynamicSkinCssFileNamer(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void throwsExceptionIfConstructorPreferenceNameArgumentIsEmpty() {
        new PrefValueDynamicSkinCssFileNamer("");
    }

    @Test (expected = IllegalArgumentException.class)
    public void throwsExceptionIfConstructorPreferenceNameArgumentIsWhitespace() {
        new PrefValueDynamicSkinCssFileNamer(" ");
    }

    @Test (expected = DynamicSkinException.class)
    public void throwsExceptionIfPreferenceNotFound() {
        this.preferenceNotFound(PrefValueDynamicSkinCssFileNamer.SKIN_CSS_FILE_NAME_PREFERENCE_NAME);
        this.namer.generateCssFileName(this.data);
    }

    @Test (expected = DynamicSkinException.class)
    public void throwsExceptionIfPreferenceValueIsEmpty() {
        this.preferenceFound(PrefValueDynamicSkinCssFileNamer.SKIN_CSS_FILE_NAME_PREFERENCE_NAME, "");
        this.namer.generateCssFileName(this.data);
    }

    @Test
    public void returnsPreferenceValue() {
        final String prefValue = "blah.css";
        this.preferenceFound(PrefValueDynamicSkinCssFileNamer.SKIN_CSS_FILE_NAME_PREFERENCE_NAME, prefValue);
        final String result = this.namer.generateCssFileName(this.data);
        assertEquals(prefValue, result);
    }

    private void preferenceNotFound(final String name) {
        given(this.preferences.getValue(name, null)).willReturn(null);
    }

    private void preferenceFound(final String name, final String value) {
        given(this.preferences.getValue(name, null)).willReturn(value);
    }

}
