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

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class MockRenderFilter implements RenderFilter {

	/* (non-Javadoc)
	 * @see javax.portlet.filter.RenderFilter#doFilter(javax.portlet.RenderRequest, javax.portlet.RenderResponse, javax.portlet.filter.FilterChain)
	 */
	@Override
	public void doFilter(RenderRequest request, RenderResponse response,
			FilterChain chain) throws IOException, PortletException {
		request.setAttribute("MockRenderFilter doFilter", "was here");
	}

	/* (non-Javadoc)
	 * @see javax.portlet.filter.PortletFilter#destroy()
	 */
	@Override
	public void destroy() {
	}

	/* (non-Javadoc)
	 * @see javax.portlet.filter.PortletFilter#init(javax.portlet.filter.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws PortletException {
	}

}
