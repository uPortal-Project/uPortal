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

import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntityId;

/**
 * Standard IPortletEntityId
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class PortletEntityIdImpl implements IPortletEntityId {
    private static final long serialVersionUID = 1L;

    private final IPortletDefinitionId portletDefinitionId;
    private final String layoutNodeId;
    private final int userId;
    
    private final String compositeIdString;
    
    public PortletEntityIdImpl(IPortletDefinitionId portletDefinitionId, String layoutNodeId, int userId, String compositeIdString) {
        this.portletDefinitionId = portletDefinitionId;
        this.layoutNodeId = layoutNodeId;
        this.userId = userId;
        this.compositeIdString = compositeIdString;
    }
    
    public IPortletDefinitionId getPortletDefinitionId() {
        return this.portletDefinitionId;
    }

    public String getLayoutNodeId() {
        return this.layoutNodeId;
    }

    public int getUserId() {
        return this.userId;
    }



    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IObjectId#getStringId()
     */
    @Override
    public String getStringId() {
        return this.compositeIdString;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.layoutNodeId == null) ? 0 : this.layoutNodeId.hashCode());
        result = prime * result + ((this.portletDefinitionId == null) ? 0 : this.portletDefinitionId.hashCode());
        result = prime * result + this.userId;
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
        PortletEntityIdImpl other = (PortletEntityIdImpl) obj;
        if (this.layoutNodeId == null) {
            if (other.layoutNodeId != null)
                return false;
        }
        else if (!this.layoutNodeId.equals(other.layoutNodeId))
            return false;
        if (this.portletDefinitionId == null) {
            if (other.portletDefinitionId != null)
                return false;
        }
        else if (!this.portletDefinitionId.equals(other.portletDefinitionId))
            return false;
        if (this.userId != other.userId)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return this.compositeIdString.toString();
    }
}
