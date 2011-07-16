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

package org.jasig.portal.portlet.container.properties;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

/**
 * Do nothing base class for IRequestPropertiesManager impls.
 * {@link #getRequestProperties(HttpServletRequest, IPortletWindow)} returns
 * {@link Collections#emptyList()}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseRequestPropertiesManager implements
		IRequestPropertiesManager, Ordered {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jasig.portal.portlet.container.services.IRequestPropertiesManager
	 * #addResponseProperty(javax.servlet.http.HttpServletRequest,
	 * org.jasig.portal.portlet.om.IPortletWindow, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void addResponseProperty(HttpServletRequest portletRequest,
			IPortletWindow portletWindow, String property, String value) {
		// noop
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jasig.portal.portlet.container.services.IRequestPropertiesManager
	 * #getRequestProperties(javax.servlet.http.HttpServletRequest,
	 * org.jasig.portal.portlet.om.IPortletWindow)
	 */
	@Override
	public Map<String, String[]> getRequestProperties(
			HttpServletRequest portletRequest, IPortletWindow portletWindow) {
		return Collections.emptyMap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jasig.portal.portlet.container.services.IRequestPropertiesManager
	 * #setResponseProperty(javax.servlet.http.HttpServletRequest,
	 * org.jasig.portal.portlet.om.IPortletWindow, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void setResponseProperty(HttpServletRequest portletRequest,
			IPortletWindow portletWindow, String property, String value) {
		// noop
	}
}
