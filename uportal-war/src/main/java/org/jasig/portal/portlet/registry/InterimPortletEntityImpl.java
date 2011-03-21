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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.portlet.dao.jpa.PortletPreferencesImpl;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreferences;

/**
 * The purpose of this implementation is two fold. First and foremost it is
 * intended to improve performance by providing an temporary non persistent
 * object for use in rendering the users layout. Secondly this entity exists to
 * avoid further complicating the existing IPortletEntity and IPortletEntityDao
 * implementations.
 * 
 * @author Lennard Fuller
 * @version
 * 
 */
class InterimPortletEntityImpl implements IPortletEntity {
    private String channelSubscribeId;
    private int userId;
    private WindowState windowState;
    private IPortletDefinitionId portletDefinitionId;
    private IPortletPreferences portletPreferences = null;
    private IPortletEntityId portletEntityId = null;
    

    public InterimPortletEntityImpl(IPortletDefinitionId portletDefinitionId,
            String channelSubscribeId, int userId) {
        this.portletDefinitionId = portletDefinitionId;
        this.channelSubscribeId = channelSubscribeId;
        this.userId = userId;
        this.portletEntityId = new PortletEntityIdImpl(portletDefinitionId, channelSubscribeId, userId);
        this.portletPreferences = new PortletPreferencesImpl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.portlet.om.IPortletEntity#getChannelSubscribeId()
     */
    @Override
    public String getChannelSubscribeId() {
        return channelSubscribeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.portlet.om.IPortletEntity#getPortletDefinitionId()
     */
    @Override
    public IPortletDefinitionId getPortletDefinitionId() {
        return portletDefinitionId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.portlet.om.IPortletEntity#getPortletEntityId()
     */
    @Override
    public IPortletEntityId getPortletEntityId() {
        return portletEntityId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.portlet.om.IPortletEntity#getPortletPreferences()
     */
    @Override
    public IPortletPreferences getPortletPreferences() {
        return portletPreferences;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.portlet.om.IPortletEntity#getUserId()
     */
    @Override
    public int getUserId() {
        return userId;
    }
    
    @Override
    public WindowState getWindowState() {
        return this.windowState;
    }

    @Override
    public void setWindowState(WindowState state) {
        this.windowState = state;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jasig.portal.portlet.om.IPortletEntity#setPortletPreferences(org.
     * jasig.portal.portlet.om.IPortletPreferences)
     */
    @Override
    public void setPortletPreferences(IPortletPreferences portletPreferences) {
        this.portletPreferences = portletPreferences;

    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IPortletEntity)) {
            return false;
        }
        IPortletEntity rhs = (IPortletEntity) object;
        return new EqualsBuilder().append(this.channelSubscribeId,
                rhs.getChannelSubscribeId()).append(this.userId,
                rhs.getUserId()).append(this.getPortletDefinitionId(),
                rhs.getPortletDefinitionId()).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143).append(
                this.channelSubscribeId).append(this.userId).append(
                this.getPortletDefinitionId()).toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("portletEntityId", this.portletEntityId).append(
                        "channelSubscribeId", this.channelSubscribeId).append(
                        "userId", this.userId).append("portletDefinitionId",
                        this.getPortletDefinitionId()).toString();
    }
}
