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

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This listener handles the JPA create/update/delete operations in the tenancy
 * subsystem.  It's implemented as an {@link ITenantOperationsListener}, instead
 * of hard-coded in {@link TenantService}, so it can be placed strategically in
 * the listener chain (often last).
 * 
 * @since 4.1
 * @author awills
 */
public final class JpaPersistenceTenantOperationsListener extends AbstractTenantOperationsListener {

    @Autowired
    private ITenantDao tenantDao;

    @Override
    public void onCreate(final ITenant tenant) {
        tenantDao.createOrUpdateTenant(tenant);
    }

    @Override
    public void onUpdate(final ITenant tenant) {
        tenantDao.createOrUpdateTenant(tenant);
    }

    @Override
    public void onDelete(final ITenant tenant) {
        tenantDao.removeTenant(tenant);
    }

}
