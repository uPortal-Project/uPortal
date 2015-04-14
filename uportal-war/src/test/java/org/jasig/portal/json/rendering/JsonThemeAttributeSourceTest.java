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

import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * JUnit test class for {@link JsonThemeAttributeSource}.
 */
public class JsonThemeAttributeSourceTest {

	@Mock HttpServletRequest request;
	@Mock IStylesheetDescriptorDao styleSheetDescriptorDao;
	private JsonThemeAttributeSource source;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.source = new JsonThemeAttributeSource();
		this.source.setStylesheetDescriptorDao(this.styleSheetDescriptorDao);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void getStylesheetDescriptorMethodShouldReturnDefaultWhenNoOverrideIsSpecified() {
		// when
		this.source.getStylesheetDescriptor(this.request);
		// then
		verify(this.styleSheetDescriptorDao).getStylesheetDescriptorByName("JsonLayout");
	}

	@Test
	public void getStylesheetDescriptorMethodShouldReturnSpecifiedStylesheetWhenOverrideIsSpecified() {
		// given
		final String versionOverride = "V2";
		given(this.request.getAttribute(JsonThemeAttributeSource.STYLESHEET_VERSION_OVERRIDE_REQUEST_ATTRIBUTE_NAME)).willReturn(versionOverride);
		// when
		this.source.getStylesheetDescriptor(this.request);
		// then
		verify(this.styleSheetDescriptorDao).getStylesheetDescriptorByName("JsonLayout"+versionOverride);
	}

}
