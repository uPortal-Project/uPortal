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

import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreferences;

/**
 * Wrapper for portlet entities that are persistent. Overrides the entity ID to be a consistent value 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class PersistentPortletEntityWrapper implements IPortletEntity {
    private final IPortletEntity persistentEntity;
    private final IPortletEntityId standardEntityId;
    /**
     * @param persistentEntity
     */
    public PersistentPortletEntityWrapper(IPortletEntity persistentEntity) {
        this.persistentEntity = persistentEntity;
        
        this.standardEntityId = new PortletEntityIdImpl(
                this.persistentEntity.getPortletDefinitionId(), 
                this.persistentEntity.getChannelSubscribeId(),
                this.persistentEntity.getUserId());
    }
    
    /**
     * @return The wrapped entity
     */
    public IPortletEntity getPersistentEntity() {
        return this.persistentEntity;
    }

    @Override
    public IPortletEntityId getPortletEntityId() {
        return this.standardEntityId;
    }
    @Override
    public IPortletDefinitionId getPortletDefinitionId() {
        return this.persistentEntity.getPortletDefinitionId();
    }
    @Override
    public String getChannelSubscribeId() {
        return this.persistentEntity.getChannelSubscribeId();
    }
    @Override
    public int getUserId() {
        return this.persistentEntity.getUserId();
    }
    @Override
    public WindowState getWindowState() {
        return this.persistentEntity.getWindowState();
    }
    @Override
    public void setWindowState(WindowState state) {
        this.persistentEntity.setWindowState(state);
    }
    @Override
    public IPortletPreferences getPortletPreferences() {
        return this.persistentEntity.getPortletPreferences();
    }
    @Override
    public void setPortletPreferences(IPortletPreferences portletPreferences) {
        this.persistentEntity.setPortletPreferences(portletPreferences);
    }

    @Override
    public int hashCode() {
        return this.standardEntityId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PersistentPortletEntityWrapper other = (PersistentPortletEntityWrapper) obj;
        if (this.persistentEntity == null) {
            if (other.persistentEntity != null)
                return false;
        }
        else if (!this.persistentEntity.equals(other.persistentEntity))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PersistentPortletEntityWrapper [standardEntityId=" + this.standardEntityId + ", persistentEntity="
                + this.persistentEntity + "]";
    }
}
