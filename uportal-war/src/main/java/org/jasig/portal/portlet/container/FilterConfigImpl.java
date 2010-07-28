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

/**
 * 
 */
package org.jasig.portal.portlet.container;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.filter.FilterConfig;

import org.apache.pluto.container.om.portlet.InitParam;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class FilterConfigImpl implements FilterConfig {

	private final String filterName;
	private final PortletContext portletContext;
	private Map<String, String> parametersMap = new HashMap<String, String>();

	/**
	 * 
	 * @param filterName
	 * @param initParameters
	 * @param portletContext
	 */
	public FilterConfigImpl(String filterName, List<? extends InitParam> initParameters, PortletContext portletContext){
		this.filterName = filterName;
		this.portletContext = portletContext;
		for(InitParam p : initParameters) {
			parametersMap.put(p.getParamName(), p.getParamValue());
		}
	}

	/* (non-Javadoc)
	 * @see javax.portlet.filter.FilterConfig#getFilterName()
	 */
	@Override
	public String getFilterName() {
		return this.filterName;
	}

	/* (non-Javadoc)
	 * @see javax.portlet.filter.FilterConfig#getInitParameter(java.lang.String)
	 */
	@Override
	public String getInitParameter(String name) {
		String value = this.parametersMap.get(name);
		return value;
	}

	/* (non-Javadoc)
	 * @see javax.portlet.filter.FilterConfig#getInitParameterNames()
	 */
	@Override
	public Enumeration<String> getInitParameterNames() {
		Enumeration<String> result = Collections.enumeration(this.parametersMap.keySet());
		return result;
	}

	/* (non-Javadoc)
	 * @see javax.portlet.filter.FilterConfig#getPortletContext()
	 */
	@Override
	public PortletContext getPortletContext() {
		return this.portletContext;
	}

}
