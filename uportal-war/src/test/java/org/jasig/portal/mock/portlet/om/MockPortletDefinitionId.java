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

package org.jasig.portal.mock.portlet.om;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jasig.portal.portlet.om.IPortletDefinitionId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MockPortletDefinitionId implements IPortletDefinitionId {
    private static final long serialVersionUID = 1L;
    
    private Long portletDefinitionId;
    
    public MockPortletDefinitionId() {
        this.portletDefinitionId = -1l;
    }
    
    public MockPortletDefinitionId(long portletDefinitionId) {
        this.portletDefinitionId = portletDefinitionId;
    }
    
    @Override
    public long getLongId() {
        return portletDefinitionId;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletDefinitionID#getStringId()
     */
    public String getStringId() {
        return this.portletDefinitionId.toString();
    }

    public Long getPortletDefinitionId() {
        return portletDefinitionId;
    }

    public void setPortletDefinitionId(Long portletDefinitionId) {
        this.portletDefinitionId = portletDefinitionId;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IPortletDefinitionId)) {
            return false;
        }
        IPortletDefinitionId rhs = (IPortletDefinitionId) object;
        return new EqualsBuilder()
            .append(this.portletDefinitionId, rhs.getStringId())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-917388291, 674832463)
            .append(this.portletDefinitionId)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getStringId();
    }
}
