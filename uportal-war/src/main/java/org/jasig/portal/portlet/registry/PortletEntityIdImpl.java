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

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntityId;

/**
 * Standard IPortletEntityId
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletEntityIdImpl implements IPortletEntityId {
    private static final long serialVersionUID = 1L;

    private static final String ID_SEPERATOR = "_";

    private final String id;
    
    /**
     * Constructor that takes a previously constructed entity ID. If not of the
     * correct syntax an {@link IllegalArgumentException} is thrown.
     */
    public PortletEntityIdImpl(String portletEntityId) {
        final String[] idParts = StringUtils.split(portletEntityId, ID_SEPERATOR);
        if (idParts.length != 3) {
            throw new IllegalArgumentException(portletEntityId + " is not valid");
        }
        
        this.id = portletEntityId;
    }
    
    public PortletEntityIdImpl(IPortletDefinitionId portletDefinitionId, String channelSubscribeId, int userId) {
        this.id = portletDefinitionId + ID_SEPERATOR + channelSubscribeId + ID_SEPERATOR + userId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IObjectId#getStringId()
     */
    @Override
    public String getStringId() {
        return this.id;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IPortletEntityId other = (PortletEntityIdImpl) obj;
        if (this.id == null) {
            if (other.getStringId() != null)
                return false;
        }
        else if (!this.id.equals(other.getStringId()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return this.id.toString();
    }
}
