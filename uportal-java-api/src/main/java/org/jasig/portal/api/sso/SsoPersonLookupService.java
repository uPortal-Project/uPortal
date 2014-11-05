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
package org.jasig.portal.api.sso;

import java.util.List;
import java.util.Map;

/**
 * Exports SSO-specialized person lookup services to Java clients outside the Platform webapp.
 * SSO person lookup is specialized in the sense that the implementation often applies
 * disambiguation/filtering rules against query results, and tehse rules aren't relevant to more
 * general-purpose person search services (such as those exposed by {@code PeopleRESTController}).
 *
 * <p>These disambiguiation/filtering rules tend to be SSP specific. See
 * <a href="SSP-2770">https://issues.jasig.org/browse/SSP-2770</a> and
 * <a href="SSP-2772">https://issues.jasig.org/browse/SSP-2772</a></p>
 */
public interface SsoPersonLookupService {

	/**
	 * Results of person lookups via this service will have person usernames in the {@code Map} entry keyed by this
	 * value. See {@link #findSsoPersonByUsername(String)} for more detail on the layout of these search result maps.
	 */
	public static final String USERNAME_ATTRIBUTE_NAME = "name";

	/**
	 * Portlet requests can access the currently registered implementation of this interface
	 * by accessing the portlet context attribute having this name.
	 */
	static final String PORTLET_CONTEXT_ATTRIBUTE_NAME =
			SsoPersonLookupService.class.getName()
					+ ".PORTLET_CONTEXT_ATTRIBUTE_NAME";

	/**
	 * This indirection exists as an attempt to allow for injection of
	 * security constraints in the future, esp on {@link #set(SsoPersonLookupService)},
	 * e.g. to control via a {@code SecurityManager} which components can
	 * set the current impl.
	 */
	static final class SsoPersonLookupServiceAccessor {
		private static volatile SsoPersonLookupService IMPL;
		public SsoPersonLookupService get() {
			return IMPL;
		}
		public void set(SsoPersonLookupService impl) {
			IMPL = impl;
		}
	}

	/** Allows access to the SsoPersonLookupService impl to non-Portlet requests */
	static final SsoPersonLookupServiceAccessor IMPL = new SsoPersonLookupServiceAccessor();

	/**
	 * Search for person records having the given username. Username handling can be quirky in the underlying
	 * implementation. In particular, there is no good way to guess at the correct logical username attribute name
	 * to specify in Map-based search queries (a-la {@link #findSsoPerson(String, String)}). Because of that and
	 * because search by username should theoretically never return more than one result, we expose this method
	 * as a distinct operation from {@link #findSsoPerson(String, String)}.
	 *
	 * <p>The same ambiguity does not (typically) hold in the reverse, i.e. you can always get at the username
	 * attribute in the resulting map by getting the value keyed by {@link #USERNAME_ATTRIBUTE_NAME}. Note that
	 * this is a top-level key in the returned map. I.e. the attributes themselves are in a nested Map. The
	 * layout is:</p>
	 *
	 * <pre>
	 *     {
	 *         name: 'usernamefoo',
	 *         attributes: {
	 *           attr1: [ 'attr1val1', 'attr1val2' ],
	 *           attr2: [ 'attr2val1' ]
	 *         }
	 *     }
	 * </pre>
	 *
	 * @param attribute the attribute name to search on. Must not be {@code null}
	 * @param value the attribute value to search on
	 * @return matched person represented as simple name/value pairs (where the value may be a collection). Will
	 *   return {@link null} to indicate no results
	 */
	Map<String, Object> findSsoPersonByUsername(String username);

	/**
	 * Search for person records having the given attribute value. In theory, you're only looking for a single
	 * person as whom to process a SSO request. Hence the singular method name. But in reality, such a lookup
	 * can return multiple persons, even when SSO-lookup-specific filtering rules have been applied in the
	 * implementation. Hence the plural return type.
	 *
	 * <p>The response {@code List} layout is:</p>
	 *
	 * <pre>
	 *     [
	 *         {
	 *             name: 'usernamefoo',
	 *             attributes: {
	 *                 attr1: [ 'attr1val1, 'attr1val2' ],
	 *                 attr2: [ 'attr2val1 ]
	 *             }
	 *         },
	 *         {
	 *             name: 'usernamebar',
	 *             attributes: {
	 *                 attr1: [ 'attr1val3', 'attr1val4' ],
	 *                 attr2: [ 'attr2val3' ]
	 *             }
	 *         }
	 *     ]
	 * </pre>
	 *
	 * @param attribute the attribute name to search on. Must not be {@code null}
	 * @param value the attribute value to search on
	 * @return matched persons represented as simple name/value pairs (where the value may be a collection). Will
	 *   not return {@link null}
	 */
	List<Map<String, Object>> findSsoPerson(String attribute, String value);

}
