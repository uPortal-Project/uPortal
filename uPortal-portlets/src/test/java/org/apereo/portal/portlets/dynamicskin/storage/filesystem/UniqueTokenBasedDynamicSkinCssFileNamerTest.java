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
package org.apereo.portal.portlets.dynamicskin.storage.filesystem;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apereo.portal.portlets.dynamicskin.DynamicSkinException;
import org.apereo.portal.portlets.dynamicskin.DynamicSkinInstanceData;
import org.apereo.portal.portlets.dynamicskin.DynamicSkinUniqueTokenGenerator;
import org.apereo.portal.portlets.dynamicskin.storage.filesystem.UniqueTokenBasedDynamicSkinCssFileNamer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * JUnit test class for {@link UniqueTokenBasedDynamicSkinCssFileNamer}.
 */
public class UniqueTokenBasedDynamicSkinCssFileNamerTest {

    @Mock private DynamicSkinInstanceData data;
    @Mock private DynamicSkinUniqueTokenGenerator generator;
    @Mock private PortletRequest request;
    @Mock private PortletPreferences preferences;
    private String skinName;
    private String uniqueToken;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.skinName = "myskin";
        this.uniqueToken = "1122334455";
        given(this.data.getPortletRequest()).willReturn(this.request);
        given(this.data.getSkinName()).willReturn(this.skinName);
        given(this.request.getPreferences()).willReturn(this.preferences);
        given(this.generator.generateToken(this.data)).willReturn(this.uniqueToken);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test (expected = IllegalArgumentException.class)
    public void throwsExceptionIfConstructorPrefixerArgumentIsNull() {
        new UniqueTokenBasedDynamicSkinCssFileNamer(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void throwsExceptionIfConstructorUniqueTokenGeneratorArgumentIsNull() {
        new UniqueTokenBasedDynamicSkinCssFileNamer("prefix", null);
    }

    @Test
    public void generatesProperNameUsingSpecifiedPrefixAndUniqueTokenGenerator() {
        final String prefix = "skin";
        final UniqueTokenBasedDynamicSkinCssFileNamer namer = new UniqueTokenBasedDynamicSkinCssFileNamer(prefix, this.generator);
        final String result = namer.generateCssFileName(this.data);
        assertEquals(prefix + this.uniqueToken + ".css", result);
    }

    @Test
    public void generatesProperNameUsingSkinNameAndUniqueTokenGenerator() {
        final UniqueTokenBasedDynamicSkinCssFileNamer namer = new UniqueTokenBasedDynamicSkinCssFileNamer(this.generator);
        final String result = namer.generateCssFileName(this.data);
        assertEquals(this.skinName + this.uniqueToken + ".css", result);
    }

    @Test (expected = DynamicSkinException.class)
    public void throwsExceptionIfUniqueTokenGeneratorReturnsNull() {
        given(this.generator.generateToken(this.data)).willReturn(null);
        final UniqueTokenBasedDynamicSkinCssFileNamer namer = new UniqueTokenBasedDynamicSkinCssFileNamer(this.generator);
        namer.generateCssFileName(this.data);
    }

    @Test (expected = DynamicSkinException.class)
    public void throwsExceptionIfUniqueTokenGeneratorReturnsEmptyString() {
        given(this.generator.generateToken(this.data)).willReturn("");
        final UniqueTokenBasedDynamicSkinCssFileNamer namer = new UniqueTokenBasedDynamicSkinCssFileNamer(this.generator);
        namer.generateCssFileName(this.data);
    }

    @Test (expected = DynamicSkinException.class)
    public void throwsExceptionIfUniqueTokenGeneratorReturnsWhitespaceString() {
        given(this.generator.generateToken(this.data)).willReturn(" ");
        final UniqueTokenBasedDynamicSkinCssFileNamer namer = new UniqueTokenBasedDynamicSkinCssFileNamer(this.generator);
        namer.generateCssFileName(this.data);
    }

}
