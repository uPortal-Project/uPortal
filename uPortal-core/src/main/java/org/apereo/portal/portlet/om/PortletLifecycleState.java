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
package org.apereo.portal.portlet.om;

public enum PortletLifecycleState {
    CREATED(0),
    APPROVED(1),
    PUBLISHED(2),
    EXPIRED(3),
    MAINTENANCE(4);

    public static final String MAINTENANCE_MODE_PARAMETER_NAME =
            "PortletLifecycleState.inMaintenanceMode";

    private int order;

    private PortletLifecycleState(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    /** Ordering methods are used in Import/Export and Webflow XML. */
    public boolean isBefore(PortletLifecycleState state) {
        return (this.getOrder() < state.getOrder());
    }

    /** Ordering methods are used in Import/Export and Webflow XML. */
    public boolean isEqualToOrBefore(PortletLifecycleState state) {
        return (this.getOrder() <= state.getOrder());
    }

    /** Ordering methods are used in Import/Export and Webflow XML. */
    public boolean isAfter(PortletLifecycleState state) {
        return (this.getOrder() > state.getOrder());
    }

    /** Ordering methods are used in Import/Export and Webflow XML. */
    public boolean isEqualToOrAfter(PortletLifecycleState state) {
        return (this.getOrder() >= state.getOrder());
    }
}
