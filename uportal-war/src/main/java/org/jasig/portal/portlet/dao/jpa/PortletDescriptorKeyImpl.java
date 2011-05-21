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

package org.jasig.portal.portlet.dao.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.jasig.portal.portlet.om.IPortletDescriptorKey;

@Embeddable
class PortletDescriptorKeyImpl implements IPortletDescriptorKey, Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "PORTLET_WEBAPP_NAME", length = 255)
    private String webAppName;
    
    @Column(name = "PORTLET_FRAMEWORK", nullable = false)
    private boolean frameworkPortlet;
    
    @Column(name = "PORTLET_APPLICATION_NAME", length = 255, nullable = false)
    private String portletName;

    @Override
    public String getWebAppName() {
        return webAppName;
    }

    @Override
    public void setWebAppName(String webAppName) {
        this.webAppName = webAppName;
    }

    @Override
    public boolean isFrameworkPortlet() {
        return frameworkPortlet;
    }

    @Override
    public void setFrameworkPortlet(boolean frameworkPortlet) {
        this.frameworkPortlet = frameworkPortlet;
    }

    @Override
    public String getPortletName() {
        return portletName;
    }

    @Override
    public void setPortletName(String portletName) {
        this.portletName = portletName;
    }

}
