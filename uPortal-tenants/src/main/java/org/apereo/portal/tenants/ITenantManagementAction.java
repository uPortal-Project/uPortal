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
package org.apereo.portal.tenants;

/**
 * Represents a behavior of an {@link ITenantOperationsListener} that may be invoked independent of
 * a Create, Update, or Delete operation. Actions are available from the Tenant Details screen.
 *
 * @since 4.3
 */
public interface ITenantManagementAction {

    /** The (short) string that identifies this action uniquely. */
    String getFname();

    /**
     * The key used with the Spring messageSource to obtain the internationalized UI string for this
     * operation.
     */
    String getMessageCode();

    /** Invoke the intended behavior on the specified tenant. */
    TenantOperationResponse invoke(ITenant tenant);
}
