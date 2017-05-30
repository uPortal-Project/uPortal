/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.security;

import org.apache.commons.lang3.Validate;
import org.apereo.portal.portlet.om.IPortletDefinition;

/**
 * Stateless static utility convenience methods for working with uPortal permissions.
 *
 * @since 4.1
 */
public class PermissionHelper {

    /**
     * Static utility method computing the permission target ID for a portlet definition.
     *
     * @param portletDefinition a portlet definition
     * @return String permission target ID for the portlet definition.
     * @throws IllegalArgumentException if portletDefinition is null
     * @since 4.1
     */
    public static String permissionTargetIdForPortletDefinition(
            final IPortletDefinition portletDefinition) {

        Validate.notNull(
                portletDefinition,
                "Cannot compute permission target ID for a null portlet definition.");

        final String portletPublicationId =
                portletDefinition.getPortletDefinitionId().getStringId();

        return IPermission.PORTLET_PREFIX.concat(portletPublicationId);
    }
}
