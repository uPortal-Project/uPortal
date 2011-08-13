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

package org.jasig.portal.portlet.dao.trans;

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
 * @author Eric Dalquist
 * @version $Revision$
 */
class TransientPortletEntity implements IPortletEntity {
    private final IPortletEntity delegatePortletEntity;
    private final String transientLayoutNodeId;
    
    public TransientPortletEntity(IPortletEntity portletEntity, String transientLayoutNodeId) {
        this.delegatePortletEntity = portletEntity;
        this.transientLayoutNodeId = transientLayoutNodeId;
    }
    
    protected IPortletEntity getDelegatePortletEntity() {
        return this.delegatePortletEntity;
    }
    
    @Override
    public IPortletDefinitionId getPortletDefinitionId() {
        return this.delegatePortletEntity.getPortletDefinitionId();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletEntity#getChannelSubscribeId()
     */
    @Override
    public String getLayoutNodeId() {
        return this.transientLayoutNodeId;
    }

    @Override
    public IPortletDefinition getPortletDefinition() {
        return this.delegatePortletEntity.getPortletDefinition();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletEntity#getPortletEntityId()
     */
    @Override
    public IPortletEntityId getPortletEntityId() {
        return this.delegatePortletEntity.getPortletEntityId();
    }
    
    @Override
    public Map<Long, WindowState> getWindowStates() {
        return this.delegatePortletEntity.getWindowStates();
    }

    @Override
    public WindowState getWindowState(IStylesheetDescriptor stylesheetDescriptor) {
        return this.delegatePortletEntity.getWindowState(stylesheetDescriptor);
    }

    @Override
    public void setWindowState(IStylesheetDescriptor stylesheetDescriptor, WindowState state) {
        this.delegatePortletEntity.setWindowState(stylesheetDescriptor, state);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletEntity#getUserId()
     */
    @Override
    public int getUserId() {
        return this.delegatePortletEntity.getUserId();
    }

    @Override
	public List<IPortletPreference> getPortletPreferences() {
		return delegatePortletEntity.getPortletPreferences();
	}

    @Override
	public void setPortletPreferences(List<IPortletPreference> portletPreferences) {
		delegatePortletEntity.setPortletPreferences(portletPreferences);
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.transientLayoutNodeId == null) ? 0 : this.transientLayoutNodeId.hashCode());
        result = prime * result + ((this.delegatePortletEntity.getPortletDefinition() == null) ? 0 : this.delegatePortletEntity.getPortletDefinition().hashCode());
        result = prime * result + this.delegatePortletEntity.getUserId();
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
        if (this.transientLayoutNodeId == null) {
            if (other.getLayoutNodeId() != null)
                return false;
        }
        else if (!this.transientLayoutNodeId.equals(other.getLayoutNodeId()))
            return false;
        if (this.delegatePortletEntity.getPortletDefinition() == null) {
            if (other.getPortletDefinition() != null)
                return false;
        }
        else if (!this.delegatePortletEntity.getPortletDefinition().equals(other.getPortletDefinition()))
            return false;
        if (this.delegatePortletEntity.getUserId() != other.getUserId())
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletEntity [" +
                "portletEntityId=" + this.delegatePortletEntity.getPortletEntityId() + ", " +
                "layoutNodeId=" + this.transientLayoutNodeId + ", " +
                "userId=" + this.delegatePortletEntity.getUserId() + ", " +
                "portletDefinition=" + this.delegatePortletEntity.getPortletDefinition() + "]";
    }
}
