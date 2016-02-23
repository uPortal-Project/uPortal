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
package org.jasig.portal.tenants;

import java.util.Set;


/**
 * Defines the (subsystem-private) contract for Tenant persistence.
 * 
 * @since uPortal 4.1
 * @author awills
 */
/* package-private */ interface ITenantDao {

    /**
     * Obtains the complete set of {@link ITenant} objects contained 
     * within this data source.
     */
    Set<ITenant> getAllTenants();

    /**
     * Obtains the {@link ITenant} object with the specified name.
     * 
     * @param name The human-readable name for a tenant
     * @return The tenant with the corresponding name, or <code>null</code>
     */
    ITenant getTenantByName(String name);

    /**
     * Obtains the {@link ITenant} object with the specified fname.
     * 
     * @param fname The unique identifier for a tenant
     * @return The tenant with the corresponding fname, or <code>null</code>
     */
    ITenant getTenantByFName(String fname);

    /**
     * Returns a new, empty {@link ITenant} instance of the runtime type 
     * supported by this ITenantDao implementation.
     * 
     * @param fname The unique identifier for a tenant
     * @return The tenant with the corresponding fname, or <code>null</code>
     */
    ITenant instantiate();

    /**
     * Adds the specified {@link ITenant} to the data source.
     * 
     * @param tenant A tenant that has not yet been persisted to the data source
     * @return 
     */
    void createOrUpdateTenant(ITenant tenant);

    /**
     * Deletes the specified {@link ITenant} within the data source.
     * 
     * @param tenant A tenant that exists within this data source
     */
    void removeTenant(ITenant tenant);

}
