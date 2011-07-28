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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.WindowState;

import org.apache.commons.lang.Validate;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityDescriptor;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreference;

/**
 * Portlet entity data that is not persisted. Used when the entity doesn't have any customizations.
 * 
 * @author Lennard Fuller
 * @version
 */
class SessionPortletEntityImpl implements IPortletEntity, IPortletEntityDescriptor {
    private final IPortletDefinition portletDefinition;
    private final PortletEntityData portletEntityData;
    private final Map<Long, WindowState> windowStates = new ConcurrentHashMap<Long, WindowState>();
    private List<IPortletPreference> portletPreferences = new ArrayList<IPortletPreference>(0);

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
    public Map<Long, WindowState> getWindowStates() {
        return Collections.unmodifiableMap(this.windowStates);
    }

    @Override
    public WindowState getWindowState(IStylesheetDescriptor stylesheetDescriptor) {
        return windowStates.get(stylesheetDescriptor.getId());
    }

    @Override
    public void setWindowState(IStylesheetDescriptor stylesheetDescriptor, WindowState state) {
        if (state == null) {
            windowStates.remove(stylesheetDescriptor.getId());
        }
        else {
            windowStates.put(stylesheetDescriptor.getId(), state);
        }
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.om.IPortletEntity#getPortletPreferences()
	 */
	@Override
	public List<IPortletPreference> getPortletPreferences() {
		return portletPreferences;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.om.IPortletEntity#setPortletPreferences(java.util.List)
	 */
	@Override
	public void setPortletPreferences(List<IPortletPreference> portletPreferences) {
		if (portletPreferences == null) {
			this.portletPreferences.clear();
		}
		
		this.portletPreferences = portletPreferences;
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.portletEntityData.getLayoutNodeId() == null) ? 0 : this.portletEntityData.getLayoutNodeId().hashCode());
        result = prime * result + ((this.portletDefinition == null) ? 0 : this.portletDefinition.hashCode());
        result = prime * result + this.portletEntityData.getUserId();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!IPortletEntity.class.isAssignableFrom(obj.getClass()))
            return false;
        IPortletEntity other = (IPortletEntity) obj;
        if (this.portletEntityData.getLayoutNodeId() == null) {
            if (other.getLayoutNodeId() != null)
                return false;
        }
        else if (!this.portletEntityData.getLayoutNodeId().equals(other.getLayoutNodeId()))
            return false;
        if (this.portletDefinition == null) {
            if (other.getPortletDefinition() != null)
                return false;
        }
        else if (!this.portletDefinition.equals(other.getPortletDefinition()))
            return false;
        if (this.portletEntityData.getUserId() != other.getUserId())
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletEntity [" +
                "portletEntityId=" + this.portletEntityData.getPortletEntityId() + ", " +
                "layoutNodeId=" + this.portletEntityData.getLayoutNodeId() + ", " +
                "userId=" + this.portletEntityData.getUserId() + ", " +
                "portletDefinition=" + this.portletDefinition + "]";
    }
}
