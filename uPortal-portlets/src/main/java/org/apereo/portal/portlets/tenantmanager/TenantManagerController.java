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
package org.apereo.portal.portlets.tenantmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.tenants.ITenant;
import org.apereo.portal.tenants.ITenantManagementAction;
import org.apereo.portal.tenants.ITenantOperationsListener;
import org.apereo.portal.tenants.TenantOperationResponse;
import org.apereo.portal.tenants.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "VIEW")
public class TenantManagerController {

    private static final String DEFAULT_VIEW_NAME = "/jsp/TenantManager/tenantManager";
    private static final String TENANT_DETAILS_VIEW_NAME = "/jsp/TenantManager/tenantDetails";
    private static final String ADD_TENANT_VIEW_NAME = "/jsp/TenantManager/addTenant";
    private static final String REPORT_VIEW_NAME = "/jsp/TenantManager/report";

    private static final String TENANT_MANAGER_ATTRIBUTES = "tenantManagerAttributes";
    private static final String OPTIONAL_OPERATIONS_LISTENERS = "optionalOperationsListeners";
    private static final String OPTIONAL_LISTENER_PARAMETER = "optionalListener";
    private static final String OPERATION_NAME_CODE = "operationNameCode";
    private static final String OPERATIONS_LISTENER_RESPONSES = "operationsListenerResponses";
    private static final String OPERATIONS_LISTENER_AVAILABLE_ACTIONS =
            "operationsListenerAvailableActions";
    private static final String INVALID_FIELDS = "invalidFields";
    private static final String PREVIOUS_RESPONSES = "previousResponses";

    private static final String CURRENT_TENANT_SESSION_ATTRIBUTE =
            TenantManagerController.class.getName() + ".currentTenant";

    /**
     * Handy collection of things that might be stored in the {@link PortletSession} for easy
     * cleanup.
     */
    private static final String[] SESSION_KEYS =
            new String[] {
                CURRENT_TENANT_SESSION_ATTRIBUTE,
                INVALID_FIELDS,
                PREVIOUS_RESPONSES,
                OPERATION_NAME_CODE,
                OPERATIONS_LISTENER_RESPONSES
            };

    @Autowired private TenantService tenantService;

    @Resource(name = "tenantManagerAttributes")
    private Map<String, String> tenantManagerAttributes;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @RenderMapping
    public ModelAndView showDefault(final RenderRequest req) {

        // First reset any workflows the user may have undertaken
        clearState(req);

        final Map<String, Object> model = new HashMap<String, Object>();
        final List<ITenant> tenantsList = tenantService.getTenantsList();
        model.put("tenantsList", tenantsList);
        return new ModelAndView(DEFAULT_VIEW_NAME, model);
    }

    @RenderMapping(params = "action=showAddTenant")
    public ModelAndView showAddTenant(final PortletSession session) {

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(TENANT_MANAGER_ATTRIBUTES, Collections.unmodifiableMap(tenantManagerAttributes));
        model.put(OPTIONAL_OPERATIONS_LISTENERS, tenantService.getOptionalOperationsListeners());

        /*
         * The following 2 items are empty the first time you visit the screen,
         * but may contain data if you attempted to create a tenant but your
         * inputs failed validation.
         */
        Map<String, String> previousResponses = Collections.emptyMap(); // default
        if (session.getAttributeMap().containsKey(PREVIOUS_RESPONSES)) {
            previousResponses = (Map<String, String>) session.getAttribute(PREVIOUS_RESPONSES);
        }
        model.put(PREVIOUS_RESPONSES, previousResponses);
        Map<String, Object> invalidFields = Collections.emptyMap(); // default
        if (session.getAttributeMap().containsKey(INVALID_FIELDS)) {
            invalidFields = (Map<String, Object>) session.getAttribute(INVALID_FIELDS);
        }
        model.put(INVALID_FIELDS, invalidFields);

        return new ModelAndView(ADD_TENANT_VIEW_NAME, model);
    }

    /** @since 4.3 */
    @RenderMapping(params = "action=showTenantDetails")
    public ModelAndView showTenantDetails(final RenderRequest req, final PortletSession session) {

        // Should the user chose to perform an action on the details screen, we
        // will need to know which tenant upon which to invoke it.  Not crazy
        // about storing this object in the PortletSession, but taking it as a
        // @RequestParameter could increase the challenges of URL-hacking
        // diligence down the road.  Every pass through this method will re-set
        // the tenancy under the microscope.
        ITenant tenant = null;

        // There are two possibilities that work...
        final String fnameParameter = req.getParameter("fname");
        if (!StringUtils.isBlank(fnameParameter)) {
            // An fname came in the request;  this possibility trumps others
            tenant = tenantService.getTenantByFName(fnameParameter);
            session.setAttribute(CURRENT_TENANT_SESSION_ATTRIBUTE, tenant);
        } else if (session.getAttributeMap().containsKey(CURRENT_TENANT_SESSION_ATTRIBUTE)) {
            // A tenant was previously identified;  we are most likely
            // re-playing the tenant details after failed validation
            tenant = (ITenant) session.getAttribute(CURRENT_TENANT_SESSION_ATTRIBUTE);
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("tenant", tenant);
        model.put("tenantManagerAttributes", Collections.unmodifiableMap(tenantManagerAttributes));
        model.put(OPERATIONS_LISTENER_AVAILABLE_ACTIONS, tenantService.getAllAvailableActions());

        /*
         * The following 2 items are empty the first time you visit the screen,
         * but may contain data if you attempted to create a tenant but your
         * inputs failed validation.
         */
        Map<String, String> previousResponses = Collections.emptyMap(); // default
        if (session.getAttributeMap().containsKey(PREVIOUS_RESPONSES)) {
            previousResponses = (Map<String, String>) session.getAttribute(PREVIOUS_RESPONSES);
        }
        model.put(PREVIOUS_RESPONSES, previousResponses);
        Map<String, Object> invalidFields = Collections.emptyMap(); // default
        if (session.getAttributeMap().containsKey(INVALID_FIELDS)) {
            invalidFields = (Map<String, Object>) session.getAttribute(INVALID_FIELDS);
        }
        model.put(INVALID_FIELDS, invalidFields);

        return new ModelAndView(TENANT_DETAILS_VIEW_NAME, model);
    }

    /** @since 4.3 */
    @RenderMapping(params = "action=showReport")
    public ModelAndView showReport(final RenderRequest req) {
        Map<String, Object> model = new HashMap<String, Object>();
        PortletSession session = req.getPortletSession();
        model.put(OPERATION_NAME_CODE, session.getAttribute(OPERATION_NAME_CODE));
        model.put(
                OPERATIONS_LISTENER_RESPONSES, session.getAttribute(OPERATIONS_LISTENER_RESPONSES));
        return new ModelAndView(REPORT_VIEW_NAME, model);
    }

    @ActionMapping(params = "action=doAddTenant")
    public void doAddTenant(
            ActionRequest req,
            ActionResponse res,
            final PortletSession session,
            @RequestParam("name") String name) {

        final Map<String, String> attributes = gatherAttributesFromPortletRequest(req);

        final String fname = calculateFnameFromName(name);
        // Validation
        final Set<String> invalidFields = detectInvalidFields(name, fname, attributes);
        if (!invalidFields.isEmpty()) {
            /*
             * Something wasn't valid;  return the user to the addTenant screen.
             */
            this.returnToInvalidForm(req, res, name, attributes, invalidFields, "showAddTenant");
            return;
        }

        // Honor the user's choices as far as optional listeners
        final List<String> selectedListenerFnames =
                (req.getParameterValues(OPTIONAL_LISTENER_PARAMETER) != null)
                        ? Arrays.asList(req.getParameterValues(OPTIONAL_LISTENER_PARAMETER))
                        : new ArrayList<String>(0); // None were selected
        final Set<String> skipListenerFnames = new HashSet<>();
        for (ITenantOperationsListener listener : tenantService.getOptionalOperationsListeners()) {
            if (!selectedListenerFnames.contains(listener.getFname())) {
                skipListenerFnames.add(listener.getFname());
            }
        }

        final List<TenantOperationResponse> responses = new ArrayList<>();
        tenantService.createTenant(name, fname, attributes, skipListenerFnames, responses);

        forwardToReportScreen(req, res, "tenant.manager.add", responses);
    }

    @ActionMapping(params = "action=doUpdateTenant")
    public void doUpdateTenant(
            final ActionRequest req, final ActionResponse res, final PortletSession session) {

        final ITenant tenant = (ITenant) session.getAttribute(CURRENT_TENANT_SESSION_ATTRIBUTE);
        if (tenant == null) {
            throw new IllegalStateException("No current tenant");
        }

        final Map<String, String> attributes = gatherAttributesFromPortletRequest(req);

        // Validation
        final Set<String> invalidFields =
                detectInvalidFields(tenant.getName(), tenant.getFname(), attributes);
        if (!invalidFields.isEmpty()) {
            /*
             * Something wasn't valid;  return the user to the addTenant screen.
             */
            this.returnToInvalidForm(
                    req, res, tenant.getName(), attributes, invalidFields, "showTenantDetails");
            return;
        }

        final List<TenantOperationResponse> responses = new ArrayList<>();
        tenantService.updateTenant(tenant, attributes, responses);

        forwardToReportScreen(req, res, "tenant.manager.update.attributes", responses);
    }

    @ActionMapping(params = "action=doRemoveTenant")
    public void doRemoveTenant(
            ActionRequest req,
            ActionResponse res,
            final PortletSession session,
            @RequestParam("fname") String fname) {

        List<TenantOperationResponse> responses = new ArrayList<>();
        tenantService.deleteTenantByFName(fname, responses);

        forwardToReportScreen(req, res, "tenant.manager.remove.tenant", responses);
    }

    /** @since 4.3 */
    @ActionMapping(params = "action=doListenerAction")
    public void doListenerAction(
            ActionRequest req,
            ActionResponse res,
            @RequestParam("fname") String fname,
            final PortletSession session) {

        final ITenantManagementAction action = tenantService.getAction(fname);
        final ITenant tenant = (ITenant) session.getAttribute(CURRENT_TENANT_SESSION_ATTRIBUTE);
        if (tenant == null) {
            throw new IllegalStateException("No current tenant");
        }
        TenantOperationResponse response = action.invoke(tenant);

        forwardToReportScreen(
                req, res, action.getMessageCode(), Collections.singletonList(response));
    }

    /*
     * Implementation
     */

    private void returnToInvalidForm(
            final ActionRequest req,
            final ActionResponse res,
            final String name,
            final Map<String, String> attributes,
            final Set<String> invalidFields,
            final String actionParameter) {

        /*
         * JSP/JSTL/EL is not good at collection.contains();  Convert the
         * invalidFields to a format that's easy to read in the JSP.
         */
        final Map<String, Object> invalidFieldsMap = new HashMap<>();
        for (String fieldName : invalidFields) {
            invalidFieldsMap.put(fieldName, Boolean.TRUE);
        }

        // Need to store some items to display invalid fields;  would be
        // handy to have support for javax.portlet.actionScopedRequestAttributes
        final PortletSession session = req.getPortletSession();
        session.setAttribute(INVALID_FIELDS, invalidFieldsMap);
        final Map<String, String> previousResponses = new HashMap<>();
        previousResponses.put("name", name);
        previousResponses.putAll(attributes);
        session.setAttribute(PREVIOUS_RESPONSES, previousResponses);

        // Send the user to the report screen
        res.setRenderParameter("action", actionParameter);
    }

    private void forwardToReportScreen(
            final ActionRequest req,
            final ActionResponse res,
            final String operationNameCode,
            final List<TenantOperationResponse> responses) {
        final PortletSession session = req.getPortletSession();
        // Need to store some items to share with user in the report;  would be
        // handy to have support for javax.portlet.actionScopedRequestAttributes
        session.setAttribute(OPERATION_NAME_CODE, operationNameCode);
        session.setAttribute(OPERATIONS_LISTENER_RESPONSES, responses);

        // Send the user to the report screen
        res.setRenderParameter("action", "showReport");
    }

    /** Returns a collection of invalid fields, if any. */
    private Set<String> detectInvalidFields(
            final String name, final String fname, final Map<String, String> attributes) {
        final Set<String> result = new HashSet<>();

        // Name & Fname
        try {
            tenantService.validateName(name);
            // Fname is generated from name;  the only way to
            // fix an invalid fname is to change the name.
            tenantService.validateFname(fname);
        } catch (Exception e) {
            log.warn("Validation failure for tenant name={}", name, e);
            result.add("name");
        }

        // Attributes
        for (String attributeName : tenantManagerAttributes.keySet()) {
            try {
                final String value = attributes.get(attributeName);
                tenantService.validateAttribute(attributeName, value);
            } catch (Exception e) {
                log.warn("Validation failure for tenant name={}", name, e);
                result.add(attributeName);
            }
        }

        return result;
    }

    private String calculateFnameFromName(final String name) {
        return name.replaceAll("[\\s']", "_").toLowerCase();
    }

    private Map<String, String> gatherAttributesFromPortletRequest(ActionRequest req) {
        Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<String, String> y : tenantManagerAttributes.entrySet()) {
            final String key = y.getKey();
            final String value = req.getParameter(key);
            if (StringUtils.isNotBlank(value)) {
                result.put(key, value);
            }
        }
        return result;
    }

    private void clearState(final PortletRequest req) {
        final PortletSession session = req.getPortletSession();
        for (String key : SESSION_KEYS) {
            session.removeAttribute(key);
        }
    }
}
