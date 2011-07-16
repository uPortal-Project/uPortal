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

package org.jasig.portal.portlets.sqlquery;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.springframework.validation.BindException;
import org.springframework.web.portlet.mvc.SimpleFormController;

/**
 * Handles CONFIG mode for the SQL query portlet.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class SqlQueryConfigurationController extends SimpleFormController {
	
	private String defaultDataSource = "PortalDb";
	private String defaultView = "jsp/SqlQuery/results";

	@Override
	protected void onSubmitAction(ActionRequest request,
			ActionResponse response, Object command, BindException errors)
			throws Exception {
		SqlQueryConfigForm form = (SqlQueryConfigForm) command;

		PortletPreferences prefs = request.getPreferences();
		prefs.setValue(SqlQueryPortletController.DATASOURCE_BEAN_NAME_PARAM_NAME, form.getDataSource());
		prefs.setValue(SqlQueryPortletController.SQL_QUERY_PARAM_NAME, form.getSqlQuery());
		prefs.setValue(SqlQueryPortletController.VIEW_PARAM_NAME, form.getViewName());
		prefs.store();
		
		response.setPortletMode(PortletMode.VIEW);

	}

	@Override
	protected Object formBackingObject(PortletRequest request) throws Exception {
		
		PortletPreferences prefs = request.getPreferences();
		SqlQueryConfigForm form = new SqlQueryConfigForm();
		
		form.setDataSource(prefs.getValue(SqlQueryPortletController.DATASOURCE_BEAN_NAME_PARAM_NAME, defaultDataSource));
		form.setViewName(prefs.getValue(SqlQueryPortletController.VIEW_PARAM_NAME, defaultView));
		form.setSqlQuery(prefs.getValue(SqlQueryPortletController.SQL_QUERY_PARAM_NAME, ""));
		
		return form;
	}

}
