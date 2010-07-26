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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.sql.DataSource;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;

/**
 * This portlet executes a (configurable) SQL query against a (configurable)
 * DataSource accessed via the Spring application context, translates the 
 * ResultSet into a collection of row Map objects, and feeds that object to 
 * a JSP page.
 * 
 * This portlet is useful for exposing dashboard components with relatively
 * low usage.  It does not presently implement caching and so is not suitable
 * for high volume use.
 * 
 * This portlet is eminently useful for simple administrative queries.
 * 
 * Potentially useful future enhancements of this portlet might include an
 * an ability to bind user attributes to parameters of the query.
 * 
 * This portlet is a modern port of the CSqlQuery channel to Spring PortletMVC.
 * 
 * @author Andrew Petro
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 */
public class SqlQueryPortletController extends AbstractController {

    /**
     * The bean name of the DataSource against which this channel will
     * execute the SQL query is specified as a channel parameter named 
     * "dataSource".  This parameter is optional, defaulting to the uPortal 
     * DataSource (PortalDb).
     */
    public static final String DATASOURCE_BEAN_NAME_PARAM_NAME = "dataSource";

    /**
     * The SQL query this channel will execute is specified as a channel
     * parameter named "sql".  This parameter is required.
     */
    public static final String SQL_QUERY_PARAM_NAME = "sql";

    public static final String VIEW_PARAM_NAME = "view";
    
	@Override
	public ModelAndView handleRenderRequest(RenderRequest request,
			RenderResponse response) throws Exception {
		
		// find the configured SQL statement
		PortletPreferences preferences = request.getPreferences();
		String sqlQuery = preferences.getValue(SQL_QUERY_PARAM_NAME, null);
		String dsName = preferences.getValue(DATASOURCE_BEAN_NAME_PARAM_NAME, "PortalDb");
		String viewName = preferences.getValue(VIEW_PARAM_NAME, "jsp/SqlQuery/results");
		
		// generate a JDBC template for the requested data source
		DataSource ds = (DataSource) getApplicationContext().getBean(dsName);
		JdbcTemplate template = new JdbcTemplate(ds);
		
		// Execute the SQL query and build a results object.  This result
		// will consist of one rowname -> rowvalue map for each row in the
		// result set

		List<Map<String, Object>> results = template.query(sqlQuery, new ColumnMapRowMapper());
		
		// build the model
		
		ModelAndView modelandview = new ModelAndView(viewName);
		modelandview.addObject("results", results);
		return modelandview;
	}

}
