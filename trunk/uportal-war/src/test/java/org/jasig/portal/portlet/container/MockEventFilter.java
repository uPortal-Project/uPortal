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

package org.jasig.portal.portlet.container;

import java.io.IOException;

import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletException;
import javax.portlet.filter.EventFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Revision$
 */
public class MockEventFilter implements EventFilter {

	/* (non-Javadoc)
	 * @see javax.portlet.filter.EventFilter#doFilter(javax.portlet.EventRequest, javax.portlet.EventResponse, javax.portlet.filter.FilterChain)
	 */
	@Override
	public void doFilter(EventRequest request, EventResponse response,
			FilterChain chain) throws IOException, PortletException {
		request.setAttribute("MockEventFilter doFilter", "was here");
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
