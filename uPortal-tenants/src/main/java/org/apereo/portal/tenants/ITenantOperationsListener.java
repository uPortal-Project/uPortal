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

import java.util.Set;

/**
 * Interface that supports pluggable behavior when tenants are created, updated, or removed.
 *
 * @since 4.1
 */
public interface ITenantOperationsListener {

    /**
     * Provides a human-readable name for this listener suitable for displaying in the UI.
     * Internationalized.
     */
    String getName();

    /**
     * The (short) string that identifies this listener uniquely.
     *
     * @since 4.3
     */
    String getFname();

    /**
     * Is skipping this listener permissable?
     *
     * @since 4.3
     */
    boolean isOptional();

    /** Allows the listener to respond to the creation of a new tenant. */
    TenantOperationResponse onCreate(ITenant tenant);

    /**
     * Allows the listener to respond to a change in the metadata originally used to define a
     * tenant.
     */
    TenantOperationResponse onUpdate(ITenant tenant);

    /** Allows the listener to respond to the removal of a tenant. */
    TenantOperationResponse onDelete(ITenant tenant);

    /**
     * Listeners may optionally define one or more operations that may be performed on an existing
     * tenant. Normally these actions might be the same as, or similar too, the behavior they
     * implement for tenant CrUD operations. Example: 'Re-send Tenant Admin Email'
     *
     * @since 4.3
     */
    Set<ITenantManagementAction> getAvailableActions();

    /**
     * Throws an exception if the specified value isn't allowable for the specified attribute.
     *
     * @since 4.3
     */
    void validateAttribute(String key, String value) throws Exception;
}
