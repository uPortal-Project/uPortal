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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.tenants.ITenant;
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
    private static final String ADD_TENANT_VIEW_NAME = "/jsp/TenantManager/addTenant";

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

    @ActionMapping(params="action=doAddTenant")
    public void doAddTenant(ActionRequest req, @RequestParam("name") String name, 
            @RequestParam("fname") String fname) {

        final Map<String,String> attributes = new HashMap<String,String>();
        for (Map.Entry<String,String> y : tenantManagerAttributes.entrySet()) {
            final String key = y.getKey();
            final String value = req.getParameter(key);
            if (StringUtils.isNotBlank(value)) {
                attributes.put(key, value);
            }
        }

        tenantService.createTenant(name, fname, attributes);

    }

    @ActionMapping(params="action=doRemoveTenant")
    public void doRemoveTenant(ActionRequest req, @RequestParam("fname") String fname) {
        tenantService.deleteTenantByFName(fname);
    }

}
