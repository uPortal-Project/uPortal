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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This listener handles the JPA create/update/delete operations in the tenancy subsystem. It's
 * implemented as an {@link ITenantOperationsListener}, instead of hard-coded in {@link
 * TenantService}, so it can be placed strategically in the listener chain (often last).
 *
 * @since 4.1
 */
public final class JpaPersistenceTenantOperationsListener extends AbstractTenantOperationsListener {

    private static final String TENANT_CREATED_SUCCESSFULLY = "tenant.created.successfully";
    private static final String TENANT_UPDATED_SUCCESSFULLY = "tenant.updated.successfully";
    private static final String TENANT_DELETED_SUCCESSFULLY = "tenant.deleted.successfully";

    private static final String FAILED_TO_CREATE_TENANT = "failed.to.create.tenant";
    private static final String FAILED_TO_UPDATE_TENANT = "failed.to.update.tenant";
    private static final String FAILED_TO_DELETE_TENANT = "failed.to.delete.tenant";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired private ITenantDao tenantDao;

    public JpaPersistenceTenantOperationsListener() {
        super("jpa-persistence");
    }

    @Override
    public TenantOperationResponse onCreate(final ITenant tenant) {
        try {
            tenantDao.createOrUpdateTenant(tenant);
        } catch (Exception e) {
            log.error("Failed to create tenant {}", tenant.getName(), e);
            final TenantOperationResponse error =
                    new TenantOperationResponse(this, TenantOperationResponse.Result.ABORT);
            error.addMessage(
                    createLocalizedMessage(
                            FAILED_TO_CREATE_TENANT, new String[] {tenant.getName()}));
            return error;
        }
        final TenantOperationResponse result =
                new TenantOperationResponse(this, TenantOperationResponse.Result.SUCCESS);
        result.addMessage(
                createLocalizedMessage(
                        TENANT_CREATED_SUCCESSFULLY, new String[] {tenant.getName()}));
        return result;
    }

    @Override
    public TenantOperationResponse onUpdate(final ITenant tenant) {
        try {
            tenantDao.createOrUpdateTenant(tenant);
        } catch (Exception e) {
            log.error("Failed to update tenant {}", tenant.getName(), e);
            final TenantOperationResponse error =
                    new TenantOperationResponse(this, TenantOperationResponse.Result.ABORT);
            error.addMessage(
                    createLocalizedMessage(
                            FAILED_TO_UPDATE_TENANT, new String[] {tenant.getName()}));
            return error;
        }
        final TenantOperationResponse result =
                new TenantOperationResponse(this, TenantOperationResponse.Result.SUCCESS);
        result.addMessage(
                createLocalizedMessage(
                        TENANT_UPDATED_SUCCESSFULLY, new String[] {tenant.getName()}));
        return result;
    }

    @Override
    public TenantOperationResponse onDelete(final ITenant tenant) {
        try {
            tenantDao.removeTenant(tenant);
        } catch (Exception e) {
            log.error("Failed to remove tenant {}", tenant.getName(), e);
            final TenantOperationResponse error =
                    new TenantOperationResponse(this, TenantOperationResponse.Result.ABORT);
            error.addMessage(
                    createLocalizedMessage(
                            FAILED_TO_DELETE_TENANT, new String[] {tenant.getName()}));
            return error;
        }
        final TenantOperationResponse result =
                new TenantOperationResponse(this, TenantOperationResponse.Result.SUCCESS);
        result.addMessage(
                createLocalizedMessage(
                        TENANT_DELETED_SUCCESSFULLY, new String[] {tenant.getName()}));
        return result;
    }
}
