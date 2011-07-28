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

import java.util.List;
import java.util.Map;

import javax.portlet.WindowState;

import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreference;

/**
 * Wrapper for portlet entities that are persistent. Overrides the entity ID to be a consistent value 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class PersistentPortletEntityWrapper implements IPortletEntity {
    private final IPortletEntity persistentEntity;
    private final IPortletEntityId standardEntityId;
    
    public PersistentPortletEntityWrapper(IPortletEntity persistentEntity, IPortletEntityId standardEntityId) {
        this.persistentEntity = persistentEntity;
        this.standardEntityId = standardEntityId;
    }

    /**
     * @return The wrapped entity
     */
    public IPortletEntity getPersistentEntity() {
        return this.persistentEntity;
    }
    
    @Override
    public IPortletDefinitionId getPortletDefinitionId() {
        return this.persistentEntity.getPortletDefinitionId();
    }

    @Override
    public IPortletEntityId getPortletEntityId() {
        return this.standardEntityId;
    }

    @Override
    public IPortletDefinition getPortletDefinition() {
        return this.persistentEntity.getPortletDefinition();
    }

    @Override
    public String getLayoutNodeId() {
        return this.persistentEntity.getLayoutNodeId();
    }
    @Override
    public int getUserId() {
        return this.persistentEntity.getUserId();
    }
    @Override
    public Map<Long, WindowState> getWindowStates() {
        return this.persistentEntity.getWindowStates();
    }
    @Override
    public WindowState getWindowState(IStylesheetDescriptor stylesheetDescriptor) {
        return this.persistentEntity.getWindowState(stylesheetDescriptor);
    }
    @Override
    public void setWindowState(IStylesheetDescriptor stylesheetDescriptor, WindowState state) {
        this.persistentEntity.setWindowState(stylesheetDescriptor, state);
    }
	@Override
	public List<IPortletPreference> getPortletPreferences() {
		return this.persistentEntity.getPortletPreferences();
	}
	@Override
	public void setPortletPreferences(List<IPortletPreference> portletPreferences) {
		this.persistentEntity.setPortletPreferences(portletPreferences);
	}

	@Override
    public int hashCode() {
        return this.persistentEntity.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.persistentEntity.equals(obj);
    }

    @Override
    public String toString() {
        return "PortletEntity [" +
                "portletEntityId=" + this.standardEntityId + ", " +
                "layoutNodeId=" + this.persistentEntity.getLayoutNodeId() + ", " +
                "userId=" + this.persistentEntity.getUserId() + ", " +
                "portletDefinition=" + this.persistentEntity.getPortletDefinition() + "]";
    }
}
