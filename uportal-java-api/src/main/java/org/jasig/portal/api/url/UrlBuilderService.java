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
package org.jasig.portal.api.url;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Map;

public interface UrlBuilderService {

	/**
	 * Portlet requests can access the currently registered implementation of
	 * this interface by accessing the portlet context attribute having this
	 * name.
	 */
	static final String PORTLET_CONTEXT_ATTRIBUTE_NAME = UrlBuilderService.class
			.getName() + ".PORTLET_CONTEXT_ATTRIBUTE_NAME";

	/**
	 * This indirection exists as an attempt to allow for injection of security
	 * constraints in the future, esp on {@link #set(UrlBuilderService)}, e.g.
	 * to control via a {@code SecurityManager} which components can set the
	 * current impl.
	 */
	static final class UrlBuilderAccessor {
		private static volatile UrlBuilderService IMPL;

		public UrlBuilderService get() {
			return IMPL;
		}

		public void set(UrlBuilderService impl) {
			IMPL = impl;
		}
	}

	/** Allows access to the UrlBuilderService impl to non-Portlet requests */
	static final UrlBuilderAccessor IMPL = new UrlBuilderAccessor();

	/**
	 * Create a Platfrom Url for the given {@code urlTemplateName} and {@code parameters} list.
	 *
	 * @param urlTemplateName
	 *            the key in the urlTemplates map currently hardcoded in the
	 *            ApiRulBuilderService.
	 * @param parameters
	 *            set of key values that urlBuilder takes responsibility for
	 *            properly converting to portal acceptable keys
	 * @return
	 */
	String buildUrl(BuildUrlRequest buildRequest)
			throws UnsupportedEncodingException, MalformedURLException;

}
