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
package org.apereo.portal.portlets.error;

/**
 * Indicates the portlet is out-of-service and will be re-enabled (by an administrator) at a later
 * date. The rendering stack uses this exception to signal to the error portlet that the portlet is
 * in MAINTENANCE so it knows to display the proper message (instead of 'An error occurred...'). In
 * the future, this class could be enhanced to cary a custom message about the state of the portlet
 * from the administrator who put it in MAINTENANCE.
 *
 * @since 4.2
 */
public class MaintenanceModeException extends RuntimeException {

    private final String customMaintenanceMessage;

    public MaintenanceModeException() {
        this(null);
    }

    public MaintenanceModeException(String customMaintenanceMessage) {
        this.customMaintenanceMessage = customMaintenanceMessage;
    }

    public String getCustomMaintenanceMessage() {
        return customMaintenanceMessage;
    }
}
