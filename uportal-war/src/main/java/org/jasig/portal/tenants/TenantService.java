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

package org.jasig.portal.tenants;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Defines the contract for Tenant operations vis-a-vis other subsystems.
 * 
 * @since uPortal 4.1
 * @author awills
 */
@Service
public final class TenantService {

    private static final String TENANT_NAME_VALIDATOR_REGEX = "^[\\w ]{5,32}$";
    private static final Pattern TENANT_NAME_VALIDATOR_PATTERN = Pattern.compile(TENANT_NAME_VALIDATOR_REGEX);
    private static final String TENANT_FNAME_VALIDATOR_REGEX = "^[a-z_0-9]{5,32}$";
    private static final Pattern TENANT_FNAME_VALIDATOR_PATTERN = Pattern.compile(TENANT_FNAME_VALIDATOR_REGEX);

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ITenantDao tenantDao;

    @Resource(name="tenantOperationsListeners")
    private List<ITenantOperationsListener> tenantOperationsListeners;

    public List<ITenant> getTenantsList() {
        return tenantDao.getAllTenants();
    }

    public ITenant createTenant(final ActionRequest req, final String name, 
            final String fname, final Map<String,String> attributes) {

        // Input validation
        if (!TENANT_NAME_VALIDATOR_PATTERN.matcher(name).matches()) {
            String msg = "Invalid tenant name '" + name +
                    "' --  names must be between 5 and 32 " +
                    "characters and may contain only a-z, A-Z, 0-9, spaces, " +
                    "and underscores";
            throw new IllegalArgumentException(msg);
        }
        if (!TENANT_FNAME_VALIDATOR_PATTERN.matcher(fname).matches()) {
            String msg = "Invalid tenant fname '" + fname +
                    "' -- fnames must be between 5 and 32 " +
                    "characters and may contain only lower case letters, numbers, " +
                    "and underscores";
            throw new IllegalArgumentException(msg);
        }

        // Create the concrete tenant object
        final ITenant rslt = new JpaTenant();
        rslt.setName(name);
        rslt.setFname(fname);
        for (Map.Entry<String,String> y : attributes.entrySet()) {
            rslt.setAttribute(y.getKey(), y.getValue());
        }

        if (log.isInfoEnabled()) {
            log.info("Creating new tenant:  " + rslt.toString());
        }

        // Invoke the listeners
        for (ITenantOperationsListener listener : this.tenantOperationsListeners) {
            try {
                listener.onCreate(req, rslt);
            } catch (Exception e) {
                log.error("Error invoking ITenantOperationsListener '{}' for tenant:  {}", 
                                    listener.getClass().getName(), rslt.toString(), e);
                if (listener.isFailOnError()) {
                    throw new RuntimeException(e);
                }
            }
        }

        return rslt;
    }

    public void deleteTenantByFName(ActionRequest req, String fname) {
        final ITenant tenant = tenantDao.getTenantByFName(fname);

        // Invoke the listeners
        for (ITenantOperationsListener listener : this.tenantOperationsListeners) {
            listener.onDelete(req, tenant);
        }
    }

}
