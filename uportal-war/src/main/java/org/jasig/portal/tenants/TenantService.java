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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.Validate;
import org.jasig.portal.events.IPortalTenantEventFactory;
import org.jasig.portal.url.IPortalRequestUtils;
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

    @Autowired
    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    private IPortalTenantEventFactory tenantEventFactory;

    /**
     * Provides the complete collection of tenants in the system in the default
     * order (alphabetically by name).
     */
    public List<ITenant> getTenantsList() {
        List<ITenant> rslt = new ArrayList<ITenant>(tenantDao.getAllTenants());
        Collections.sort(rslt);
        return rslt;
    }

    public ITenant createTenant(final String name, final String fname, 
            final Map<String,String> attributes) {

        // Input validation
        Validate.validState(TENANT_NAME_VALIDATOR_PATTERN.matcher(name).matches(),
                "Invalid tenant name '%s'  -- names must match %s .", name, 
                TENANT_NAME_VALIDATOR_REGEX);
        Validate.validState(TENANT_FNAME_VALIDATOR_PATTERN.matcher(fname).matches(),
                "Invalid tenant fname '%s'  -- fnames must match %s .", name, 
                TENANT_FNAME_VALIDATOR_PATTERN);

        // Create the concrete tenant object
        final ITenant rslt = tenantDao.instantiate();
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
                listener.onCreate(rslt);
            } catch (Exception e) {
                log.error("Error invoking ITenantOperationsListener '{}' for tenant:  {}", 
                                    listener.toString(), rslt.toString(), e);
                if (listener.isFailOnError()) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Fire an appropriate PortalEvent
        final HttpServletRequest request = portalRequestUtils.getCurrentPortalRequest();
        tenantEventFactory.publishTenantCreatedTenantEvent(request, this, rslt);

        return rslt;
    }

    public void deleteTenantByFName(String fname) {

        // Invoke the listeners
        final ITenant tenant = tenantDao.getTenantByFName(fname);
        for (ITenantOperationsListener listener : this.tenantOperationsListeners) {
            try {
                listener.onDelete(tenant);
            } catch (Exception e) {
                log.error("Error invoking ITenantOperationsListener '{}' for tenant:  {}", 
                                    listener.toString(), tenant.toString(), e);
                if (listener.isFailOnError()) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Fire an appropriate PortalEvent
        final HttpServletRequest request = portalRequestUtils.getCurrentPortalRequest();
        tenantEventFactory.publishTenantCreatedTenantEvent(request, this, tenant);

    }

}
