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

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.utils.Populator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation which provides default implementations
 * 
 * @author Eric Dalquist
 */
public abstract class BaseRequestPropertiesManager implements
		IRequestPropertiesManager {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean setResponseProperty(HttpServletRequest portletRequest, IPortletWindow portletWindow,
            String property, String value) {
        return false;
    }

    @Override
    public boolean addResponseProperty(HttpServletRequest portletRequest, IPortletWindow portletWindow,
            String property, String value) {
        return false;
    }

    @Override
    public <P extends Populator<String, String>> void populateRequestProperties(HttpServletRequest portletRequest,
            IPortletWindow portletWindow, P propertiesPopulator) {
    }
}
