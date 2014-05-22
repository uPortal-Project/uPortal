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

package org.jasig.portal.api.permissions;

import java.util.Set;

public interface PermissionsService {

	/**
	 * Portlet requests can access the currently registered implementation of this interface
	 * by accessing the portlet context attribute having this name.
	 */
	static final String PORTLET_CONTEXT_ATTRIBUTE_NAME = 
			PermissionsService.class.getName() 
			+ ".PORTLET_CONTEXT_ATTRIBUTE_NAME";

	/**
	 * This indirection exists as an attempt to allow for injection of
	 * security constraints in the future, esp on {@link #set(PermissionsService)},
	 * e.g. to control via a {@code SecurityManager} which components can
	 * set the current impl.
	 */
	static final class PermissionsServiceAccessor {
		private static volatile PermissionsService IMPL;
		public PermissionsService get() {
			return IMPL;
		}
		public void set(PermissionsService impl) {
			IMPL = impl;
		}
	}

	/** Allows access to the PermissionsService impl to non-Portlet requests */
	static final PermissionsServiceAccessor IMPL = new PermissionsServiceAccessor();

	Set<Assignment> getAssignmentsForPerson(String username, boolean includeInherited);

}
