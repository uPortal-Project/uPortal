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
package org.jasig.portal.portlets.sqlquery;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handles CONFIG mode for the SQL query portlet.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
@RequestMapping("CONFIG")
public class SqlQueryConfigurationController {
	
	private String defaultDataSource = BasePortalJpaDao.PERSISTENCE_UNIT_NAME;
	private String defaultView = "jsp/SqlQuery/results";

    @RequestMapping
    public String getAccountFormView(PortletRequest request, Model model) {
        model.addAttribute("form", getConfigurationForm(request));
        return "jsp/SqlQuery/config";
    }

    @RequestMapping(params = "action=updateConfiguration")
    public void saveConfiguration(ActionRequest request, ActionResponse response,
                                         @ModelAttribute("form") SqlQueryConfigForm form,
                                         BindingResult errors,
                                         @RequestParam(value="Save", required=false) String save)
            throws PortletModeException, IOException, ValidatorException, ReadOnlyException {

        if (StringUtils.isNotBlank(save)) {

            PortletPreferences prefs = request.getPreferences();
            prefs.setValue(SqlQueryPortletController.DATASOURCE_BEAN_NAME_PARAM_NAME, form.getDataSource());
            prefs.setValue(SqlQueryPortletController.SQL_QUERY_PARAM_NAME, form.getSqlQuery());
            prefs.setValue(SqlQueryPortletController.VIEW_PARAM_NAME, form.getViewName());
            prefs.setValue(SqlQueryPortletController.PREF_CACHE_NAME,form.getCacheName());
            prefs.store();
        }
		
		response.setPortletMode(PortletMode.VIEW);

	}

    public SqlQueryConfigForm getConfigurationForm(PortletRequest request) {
		PortletPreferences prefs = request.getPreferences();
		SqlQueryConfigForm form = new SqlQueryConfigForm();
		
		form.setDataSource(prefs.getValue(SqlQueryPortletController.DATASOURCE_BEAN_NAME_PARAM_NAME, defaultDataSource));
		form.setViewName(prefs.getValue(SqlQueryPortletController.VIEW_PARAM_NAME, defaultView));
		form.setSqlQuery(prefs.getValue(SqlQueryPortletController.SQL_QUERY_PARAM_NAME, ""));
        form.setCacheName(prefs.getValue(SqlQueryPortletController.PREF_CACHE_NAME, SqlQueryPortletController.DEFAULT_CACHE_NAME));

		return form;
	}

}
