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

package org.jasig.portal.portlet.registry;

import javax.portlet.WindowState;

import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.dao.jpa.PortletPreferencesImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityDescriptor;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreferences;

/**
 * Portlet entity data that is not persisted. Used when the entity doesn't have any customizations.
 * 
 * @author Lennard Fuller
 * @version
 */
class SessionPortletEntityImpl implements IPortletEntity, IPortletEntityDescriptor {
    private final IPortletDefinition portletDefinition;
    private final PortletEntityData portletEntityData;
    private WindowState windowState;
    private IPortletPreferences portletPreferences = new PortletPreferencesImpl();

    public SessionPortletEntityImpl(IPortletDefinition portletDefinition, PortletEntityData portletEntityData) {
        Validate.notNull(portletDefinition, "portletDefinition cannot be null");
        Validate.notNull(portletEntityData, "portletEntityData cannot be null");
        
        this.portletDefinition = portletDefinition;
        this.portletEntityData = portletEntityData;
    }
    
    public PortletEntityData getPortletEntityData() {
        return this.portletEntityData;
    }

    @Override
    public IPortletDefinitionId getPortletDefinitionId() {
        return this.portletDefinition.getPortletDefinitionId();
    }

    @Override
    public IPortletEntityId getPortletEntityId() {
        return this.portletEntityData.getPortletEntityId();
    }

    @Override
    public IPortletDefinition getPortletDefinition() {
        return this.portletDefinition;
    }

    @Override
    public String getLayoutNodeId() {
        return this.portletEntityData.getLayoutNodeId();
    }

    @Override
    public int getUserId() {
        return this.portletEntityData.getUserId();
    }

    @Override
    public WindowState getWindowState() {
        return this.windowState;
    }

    @Override
    public void setWindowState(WindowState state) {
        this.windowState = state;
    }

    @Override
    public IPortletPreferences getPortletPreferences() {
        return this.portletPreferences;
    }

    @Override
    public void setPortletPreferences(IPortletPreferences portletPreferences) {
        this.portletPreferences = portletPreferences;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.portletDefinition == null) ? 0 : this.portletDefinition.hashCode());
        result = prime * result + ((this.portletEntityData == null) ? 0 : this.portletEntityData.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SessionPortletEntityImpl other = (SessionPortletEntityImpl) obj;
        if (this.portletDefinition == null) {
            if (other.portletDefinition != null)
                return false;
        }
        else if (!this.portletDefinition.equals(other.portletDefinition))
            return false;
        if (this.portletEntityData == null) {
            if (other.portletEntityData != null)
                return false;
        }
        else if (!this.portletEntityData.equals(other.portletEntityData))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SessionPortletEntityImpl [portletDefinition=" + this.portletDefinition + ", portletEntityData="
                + this.portletEntityData + ", windowState=" + this.windowState + ", portletPreferences="
                + this.portletPreferences + "]";
    }
}
