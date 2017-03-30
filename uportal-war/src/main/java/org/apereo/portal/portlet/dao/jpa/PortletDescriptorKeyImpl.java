/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlet.dao.jpa;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.apereo.portal.portlet.om.IPortletDescriptorKey;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.frameworkPortlet ? 1231 : 1237);
        result = prime * result + ((this.portletName == null) ? 0 : this.portletName.hashCode());
        result = prime * result + ((this.webAppName == null) ? 0 : this.webAppName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!IPortletDescriptorKey.class.isAssignableFrom(obj.getClass())) return false;
        IPortletDescriptorKey other = (IPortletDescriptorKey) obj;
        if (this.frameworkPortlet != other.isFrameworkPortlet()) return false;
        if (this.portletName == null) {
            if (other.getPortletName() != null) return false;
        } else if (!this.portletName.equals(other.getPortletName())) return false;
        if (this.webAppName == null) {
            if (other.getWebAppName() != null) return false;
        } else if (!this.webAppName.equals(other.getWebAppName())) return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletDescriptorKey [frameworkPortlet="
                + this.frameworkPortlet
                + ", webAppName="
                + this.webAppName
                + ", portletName="
                + this.portletName
                + "]";
    }
}
