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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreferences;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
class TransientPortletEntity implements IPortletEntity {
    private final IPortletEntity delegatePortletEntity;
    private final String transientSubscribeId;
    
    public TransientPortletEntity(IPortletEntity portletEntity, String transientSubscribeId) {
        this.delegatePortletEntity = portletEntity;
        this.transientSubscribeId = transientSubscribeId;
    }
    
    protected IPortletEntity getDelegatePortletEntity() {
        return this.delegatePortletEntity;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletEntity#getChannelSubscribeId()
     */
    public String getChannelSubscribeId() {
        return this.transientSubscribeId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletEntity#getPortletDefinitionId()
     */
    public IPortletDefinitionId getPortletDefinitionId() {
        return this.delegatePortletEntity.getPortletDefinitionId();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletEntity#getPortletEntityId()
     */
    public IPortletEntityId getPortletEntityId() {
        return this.delegatePortletEntity.getPortletEntityId();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletEntity#getPortletPreferences()
     */
    public IPortletPreferences getPortletPreferences() {
        return this.delegatePortletEntity.getPortletPreferences();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletEntity#getUserId()
     */
    public int getUserId() {
        return this.delegatePortletEntity.getUserId();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletEntity#setPortletPreferences(org.jasig.portal.portlet.om.IPortletPreferences)
     */
    public void setPortletPreferences(IPortletPreferences portletPreferences) {
        this.delegatePortletEntity.setPortletPreferences(portletPreferences);
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof TransientPortletEntity)) {
            return false;
        }
        TransientPortletEntity rhs = (TransientPortletEntity) object;
        return new EqualsBuilder()
            .append(this.transientSubscribeId, rhs.transientSubscribeId)
            .append(this.delegatePortletEntity, rhs.delegatePortletEntity)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(1802746633, 68234451)
            .append(this.transientSubscribeId)
            .append(this.delegatePortletEntity)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .appendSuper(this.delegatePortletEntity.toString())
            .append("transientSubscribeId", this.transientSubscribeId)
            .toString();
    }
}
