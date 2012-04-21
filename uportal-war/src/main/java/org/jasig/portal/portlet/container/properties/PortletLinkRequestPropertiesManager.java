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

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.springframework.stereotype.Service;

/**
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
@Service
public class PortletLinkRequestPropertiesManager extends BaseRequestPropertiesManager {
    
    /**
     * @deprecated use {@link IPortletRenderer#EXTERNAL_PORTLET_LINK_PROPERTY}
     */
    @Deprecated
    protected static final String LINK_PROPERTY = "externalPortletLink";

    @Override
    public boolean setResponseProperty(HttpServletRequest portletRequest, IPortletWindow portletWindow, String property, String value) {
        if ((LINK_PROPERTY.equals(property) || IPortletRenderer.EXTERNAL_PORTLET_LINK_PROPERTY.equals(property)) && StringUtils.isNotBlank(value)) {
            portletRequest.setAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_LINK, value);
            return true;
        }
        
        return false;
    }
}
