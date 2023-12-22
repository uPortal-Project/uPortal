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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apereo.portal.events.IPortalTenantEventFactory;
import org.apereo.portal.url.IPortalRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Defines the contract for Tenant operations vis-a-vis other subsystems.
 *
 * @since 4.1
 */
@Service
public class TenantService {

    private static final String TENANT_NAME_VALIDATOR_REGEX = "^[\\w ]{5,32}$";
    private static final Pattern TENANT_NAME_VALIDATOR_PATTERN =
            Pattern.compile(TENANT_NAME_VALIDATOR_REGEX);
    private static final String TENANT_FNAME_VALIDATOR_REGEX = "^[a-z_0-9]{5,32}$";
    private static final Pattern TENANT_FNAME_VALIDATOR_PATTERN =
            Pattern.compile(TENANT_FNAME_VALIDATOR_REGEX);

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired private ITenantDao tenantDao;

    @Resource(name = "tenantOperationsListeners")
    private List<ITenantOperationsListener> tenantOperationsListeners;

    private List<ITenantOperationsListener> optionalOperationsListeners;

    private Map<String, ITenantManagementAction> operationsListenerAvailableActions;

    @Autowired private IPortalRequestUtils portalRequestUtils;

    @Autowired private IPortalTenantEventFactory tenantEventFactory;

    /** @since 4.3 */
    @PostConstruct
    public void init() {
        Map<String, ITenantManagementAction> map = new HashMap<>();
        List<ITenantOperationsListener> optional = new ArrayList<>();
        for (ITenantOperationsListener listener : tenantOperationsListeners) {
            if (listener.isOptional()) {
                optional.add(listener);
            }
            for (ITenantManagementAction action : listener.getAvailableActions()) {
                map.put(action.getFname(), action);
            }
        }
        optionalOperationsListeners = Collections.unmodifiableList(optional);
        operationsListenerAvailableActions = Collections.unmodifiableMap(map);
    }

    /** @since 4.3 */
    public ITenant getTenantByFName(final String fname) {
        // Assertions
        if (StringUtils.isBlank(fname)) {
            String msg = "Argument 'fname' cannot be blank";
            throw new IllegalArgumentException(msg);
        }
        return tenantDao.getTenantByFName(fname);
    }

    /**
     * Provides the complete collection of tenants in the system in the default order
     * (alphabetically by name).
     */
    public List<ITenant> getTenantsList() {
        List<ITenant> result = new ArrayList<ITenant>(tenantDao.getAllTenants());
        Collections.sort(result);
        return result;
    }

    public ITenant createTenant(
            final String name,
            final String fname,
            final Map<String, String> attributes,
            final Set<String> skipListenerFnames,
            final List<TenantOperationResponse> responses) {

        /*
         * NB:  Ideally this method should be annotated with @PortalTransactional,
         * but (unfortunately) it doesn't work.  There are multiple approaches to
         * persistence (JPA and pre-JPA) at play in the concrete
         * ITenantOperationsListener objects.
         */

        // Input validation
        Validate.validState(
                TENANT_NAME_VALIDATOR_PATTERN.matcher(name).matches(),
                "Invalid tenant name '%s'  -- names must match %s .",
                name,
                TENANT_NAME_VALIDATOR_REGEX);
        Validate.validState(
                TENANT_FNAME_VALIDATOR_PATTERN.matcher(fname).matches(),
                "Invalid tenant fname '%s'  -- fnames must match %s .",
                fname,
                TENANT_FNAME_VALIDATOR_REGEX);

        // Create the concrete tenant object
        final ITenant result = tenantDao.instantiate();
        result.setName(name);
        result.setFname(fname);
        for (Map.Entry<String, String> y : attributes.entrySet()) {
            result.setAttribute(y.getKey(), y.getValue());
        }

        log.info("Creating new tenant:  {}", result.toString());

        // Invoke the listeners
        for (ITenantOperationsListener listener : this.tenantOperationsListeners) {
            // Skip listeners as requested
            if (skipListenerFnames != null
                    && listener.isOptional()
                    && skipListenerFnames.contains(listener.getFname())) {
                continue;
            }
            TenantOperationResponse res = null; // default
            try {
                res = listener.onCreate(result);
                if (!TenantOperationResponse.Result.IGNORE.equals(res.getResult())) {
                    responses.add(res);
                }
            } catch (Exception e) {
                final String msg =
                        "Error invoking ITenantOperationsListener '"
                                + listener.toString()
                                + "' for tenant:  "
                                + result.toString();
                throw new RuntimeException(msg, e);
            }
            if (res.getResult().equals(TenantOperationResponse.Result.ABORT)) {
                log.warn(
                        "ITenantOperationsListener {} aborted the creation of tenant:  ",
                        listener.toString(),
                        result.toString());
                // TODO:  Can we rollback somehow?
                break;
            }
        }

        // Fire an appropriate PortalEvent
        final HttpServletRequest request = portalRequestUtils.getCurrentPortalRequest();
        tenantEventFactory.publishTenantCreatedTenantEvent(request, this, result);

        return result;
    }

    public ITenant updateTenant(
            final ITenant tenant,
            final Map<String, String> attributes,
            final List<TenantOperationResponse> responses) {

        /*
         * NB:  Ideally this method should be annotated with @PortalTransactional,
         * but (unfortunately) it doesn't work.  There are multiple approaches to
         * persistence (JPA and pre-JPA) at play in the concrete
         * ITenantOperationsListener objects.
         */

        for (Map.Entry<String, String> y : attributes.entrySet()) {
            tenant.setAttribute(y.getKey(), y.getValue());
        }

        log.info("Updating tenant:  {}", tenant.toString());

        // Invoke the listeners
        for (ITenantOperationsListener listener : this.tenantOperationsListeners) {
            TenantOperationResponse res = null; // default
            try {
                res = listener.onUpdate(tenant);
                if (!TenantOperationResponse.Result.IGNORE.equals(res.getResult())) {
                    responses.add(res);
                }
            } catch (Exception e) {
                final String msg =
                        "Error invoking ITenantOperationsListener '"
                                + listener.toString()
                                + "' for tenant:  "
                                + tenant.toString();
                throw new RuntimeException(msg, e);
            }
            if (res.getResult().equals(TenantOperationResponse.Result.ABORT)) {
                log.warn(
                        "ITenantOperationsListener {} aborted updating tenant:  ",
                        listener.toString(),
                        tenant.toString());
                // TODO:  Can we rollback somehow?
                break;
            }
        }

        // Fire an appropriate PortalEvent
        final HttpServletRequest request = portalRequestUtils.getCurrentPortalRequest();
        tenantEventFactory.publishTenantUpdatedTenantEvent(request, this, tenant);

        return tenant;
    }

    public void deleteTenantByFName(String fname, List<TenantOperationResponse> responses) {

        /*
         * NB:  Ideally this method should be annotated with @PortalTransactional,
         * but (unfortunately) it doesn't work.  There are multiple approaches to
         * persistence (JPA and pre-JPA) at play in the concrete
         * ITenantOperationsListener objects.
         */

        // Invoke the listeners
        final ITenant tenant = tenantDao.getTenantByFName(fname);
        for (ITenantOperationsListener listener : this.tenantOperationsListeners) {
            TenantOperationResponse res = null; // default
            try {
                res = listener.onDelete(tenant);
                if (!TenantOperationResponse.Result.IGNORE.equals(res.getResult())) {
                    responses.add(res);
                }
            } catch (Exception e) {
                final String msg =
                        "Error invoking ITenantOperationsListener '"
                                + listener.toString()
                                + "' for tenant:  "
                                + tenant.toString();
                throw new RuntimeException(msg, e);
            }
            if (res != null && res.getResult().equals(TenantOperationResponse.Result.ABORT)) {
                final String msg =
                        "ITenantOperationsListener '"
                                + listener.toString()
                                + "' aborted the operation for tenant:  "
                                + tenant.toString();
                throw new RuntimeException(msg);
            }
        }

        // Fire an appropriate PortalEvent
        final HttpServletRequest request = portalRequestUtils.getCurrentPortalRequest();
        tenantEventFactory.publishTenantCreatedTenantEvent(request, this, tenant);
    }

    /**
     * List of the fnames of currently configured {@link ITenantOperationsListener} objects that may
     * be omitted, presented in their natural (sequential) order.
     *
     * @since 4.3
     */
    public List<ITenantOperationsListener> getOptionalOperationsListeners() {
        return optionalOperationsListeners;
    }

    /**
     * Complete set of actions from all listeners
     *
     * @since 4.3
     */
    public Set<ITenantManagementAction> getAllAvailableActions() {
        return new HashSet<ITenantManagementAction>(operationsListenerAvailableActions.values());
    }

    /** @since 4.3 */
    public ITenantManagementAction getAction(final String fname) {
        // Assertions
        if (StringUtils.isBlank(fname)) {
            String msg = "Argument 'fname' cannot be blank";
            throw new IllegalArgumentException(msg);
        }
        ITenantManagementAction result = this.operationsListenerAvailableActions.get(fname);
        if (result == null) {
            String msg = "Action not found:  " + fname;
            throw new RuntimeException(msg);
        }
        return result;
    }

    /**
     * Returns true if a tenant with the specified name exists, otherwise false.
     *
     * @since 4.3
     */
    public boolean nameExists(final String name) {
        boolean result = false; // default
        try {
            final ITenant tenant = this.tenantDao.getTenantByName(name);
            result = tenant != null;
        } catch (IllegalArgumentException iae) {
            // This exception is completely fine;  it simply
            // means there is no tenant with this name.
            result = false;
        }
        return result;
    }

    /**
     * Returns true if a tenant with the specified fname exists, otherwise false.
     *
     * @since 4.3
     */
    public boolean fnameExists(final String fname) {
        boolean result = false; // default
        try {
            final ITenant tenant = getTenantByFName(fname);
            result = tenant != null;
        } catch (IllegalArgumentException iae) {
            // This exception is completely fine;  it simply
            // means there is no tenant with this fname.
            result = false;
        }
        return result;
    }

    /**
     * Throws an exception if the specified String isn't a valid tenant name.
     *
     * @since 4.3
     */
    public void validateName(final String name) {
        Validate.validState(
                TENANT_NAME_VALIDATOR_PATTERN.matcher(name).matches(),
                "Invalid tenant name '%s'  -- names must match %s .",
                name,
                TENANT_NAME_VALIDATOR_REGEX);
    }

    /**
     * Throws an exception if the specified String isn't a valid tenant fname.
     *
     * @since 4.3
     */
    public void validateFname(final String fname) {
        Validate.validState(
                TENANT_FNAME_VALIDATOR_PATTERN.matcher(fname).matches(),
                "Invalid tenant fname '%s'  -- fnames must match %s .",
                fname,
                TENANT_FNAME_VALIDATOR_REGEX);
    }

    /**
     * Throws an exception if any {@linkITenantOperationsListener} indicates that the specified
     * value isn't allowable for the specified attribute.
     *
     * @throws Exception
     * @since 4.3
     */
    public void validateAttribute(final String key, final String value) throws Exception {
        for (ITenantOperationsListener listener : tenantOperationsListeners) {
            // Will throw an exception if not valid
            listener.validateAttribute(key, value);
        }
    }
}
