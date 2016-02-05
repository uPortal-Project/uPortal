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
package org.jasig.portal.portlets.tenantmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.tenants.ITenant;
import org.jasig.portal.tenants.ITenantManagementAction;
import org.jasig.portal.tenants.TenantOperationResponse;
import org.jasig.portal.tenants.TenantService;
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

    private static final String OPERATION_NAME_CODE = "operationNameCode";
    private static final String OPERATIONS_LINTENER_RESPONSES = "operationsListenerResponses";
    private static final String OPERATIONS_LINTENER_AVAILABLE_ACTIONS = "operationsListenerAvailableActions";

    private static final String CURRENT_TENANT_SESSION_ATTRIBUTE = TenantManagerController.class.getName() + ".currentTenant";

    @Autowired
    private TenantService tenantService;

    @Resource(name="tenantManagerAttributes")
    private Map<String,String> tenantManagerAttributes;

    @RenderMapping
    public ModelAndView showDefault() {
        final Map<String,Object> model = new HashMap<String,Object>();
        final List<ITenant> tenantsList = tenantService.getTenantsList();
        model.put("tenantsList", tenantsList);
        return new ModelAndView(DEFAULT_VIEW_NAME, model);
    }

    @RenderMapping(params="action=showAddTenant")
    public ModelAndView showAddTenant() {
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("tenantManagerAttributes", Collections.unmodifiableMap(tenantManagerAttributes));
        return new ModelAndView(ADD_TENANT_VIEW_NAME, model);
    }

    @RenderMapping(params="action=showTenantDetails")
    public ModelAndView showTenantDetails(@RequestParam("fname") final String fname, final PortletSession session) {

        final ITenant tenant = tenantService.getTenantByFName(fname);

        // Should the user chose to perform an action on the details screen, we
        // will need to know which tenant upon which to invoke it.  Not crazy
        // about storing this object in the PortletSession, but taking it as a
        // @RequestParameter could increase the challenges of URL-hacking
        // diligence down the road.  Every pass through this method will re-set
        // the tenancy under the microscope.
        session.setAttribute(CURRENT_TENANT_SESSION_ATTRIBUTE, tenant);

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("tenant", tenant);
        model.put("tenantManagerAttributes", Collections.unmodifiableMap(tenantManagerAttributes));
        model.put(OPERATIONS_LINTENER_AVAILABLE_ACTIONS, tenantService.getAllAvaialableActions());
        return new ModelAndView(TENANT_DETAILS_VIEW_NAME, model);
    }

    @RenderMapping(params="action=showReport")
    public ModelAndView showReport(final RenderRequest req) {
        Map<String,Object> model = new HashMap<String,Object>();
        PortletSession session = req.getPortletSession();
        model.put(OPERATION_NAME_CODE, session.getAttribute(OPERATION_NAME_CODE));
        model.put(OPERATIONS_LINTENER_RESPONSES, session.getAttribute(OPERATIONS_LINTENER_RESPONSES));
        return new ModelAndView(REPORT_VIEW_NAME, model);
    }

    @ActionMapping(params="action=doAddTenant")
    public void doAddTenant(ActionRequest req, ActionResponse res, final PortletSession session,
            @RequestParam("name") String name, @RequestParam("fname") String fname) {

        final Map<String,String> attributes = new HashMap<String,String>();
        for (Map.Entry<String,String> y : tenantManagerAttributes.entrySet()) {
            final String key = y.getKey();
            final String value = req.getParameter(key);
            if (StringUtils.isNotBlank(value)) {
                attributes.put(key, value);
            }
        }

        List<TenantOperationResponse> responses = new ArrayList<>();
        tenantService.createTenant(name, fname, attributes, responses);

        // Need to store some items to share with user in the report;  would be
        // handy to have support for javax.portlet.actionScopedRequestAttributes
        session.setAttribute(OPERATION_NAME_CODE, "tenant.manager.add");
        session.setAttribute(OPERATIONS_LINTENER_RESPONSES, responses);

        // Send the user to the report screen
        res.setRenderParameter("action", "showReport");

    }

    @ActionMapping(params="action=doRemoveTenant")
    public void doRemoveTenant(ActionRequest req, ActionResponse res,
            final PortletSession session, @RequestParam("fname") String fname) {

        List<TenantOperationResponse> responses = new ArrayList<>();
        tenantService.deleteTenantByFName(fname, responses);

        // Need to store some items to share with user in the report;  would be
        // handy to have support for javax.portlet.actionScopedRequestAttributes
        session.setAttribute(OPERATION_NAME_CODE, "tenant.manager.remove.tenant");
        session.setAttribute(OPERATIONS_LINTENER_RESPONSES, responses);

        // Send the user to the report screen
        res.setRenderParameter("action", "showReport");

    }

    @ActionMapping(params="action=doListenerAction")
    public void doListenerAction(ActionRequest req, ActionResponse res,
            @RequestParam("fname") String fname, final PortletSession session) {

        final ITenantManagementAction action = tenantService.getAction(fname);
        final ITenant tenant = (ITenant) session.getAttribute(CURRENT_TENANT_SESSION_ATTRIBUTE);
        if (tenant == null) {
            throw new IllegalStateException("No current tenant");
        }
        TenantOperationResponse response = action.invoke(tenant);

        // Need to store some items to share with user in the report;  would be
        // handy to have support for javax.portlet.actionScopedRequestAttributes
        session.setAttribute(OPERATION_NAME_CODE, action.getMessageCode());
        session.setAttribute(OPERATIONS_LINTENER_RESPONSES, Collections.singletonList(response));

        // Send the user to the report screen
        res.setRenderParameter("action", "showReport");

    }

}
