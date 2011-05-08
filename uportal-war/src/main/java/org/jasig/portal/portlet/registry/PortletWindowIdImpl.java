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

import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
class PortletWindowIdImpl implements IPortletWindowId {
    private static final long serialVersionUID = 1L;

    private final IPortletEntityId portletEntityId;
    private final String windowInstanceId;
    private final String compositeIdString;
    
    public PortletWindowIdImpl(IPortletEntityId portletEntityId, String windowInstanceId, String compositeIdString) {
        Validate.notNull(portletEntityId, "portletEntityId can not be null");

        this.portletEntityId = portletEntityId;
        this.windowInstanceId = windowInstanceId;
        this.compositeIdString = compositeIdString;
    }
    
    public IPortletEntityId getPortletEntityId() {
        return this.portletEntityId;
    }

    public String getWindowInstanceId() {
        return this.windowInstanceId;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletWindowID#getStringId()
     */
    @Override
    public String getStringId() {
        return this.compositeIdString;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.portletEntityId == null) ? 0 : this.portletEntityId.hashCode());
        result = prime * result + ((this.windowInstanceId == null) ? 0 : this.windowInstanceId.hashCode());
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
        PortletWindowIdImpl other = (PortletWindowIdImpl) obj;
        if (this.portletEntityId == null) {
            if (other.portletEntityId != null)
                return false;
        }
        else if (!this.portletEntityId.equals(other.portletEntityId))
            return false;
        if (this.windowInstanceId == null) {
            if (other.windowInstanceId != null)
                return false;
        }
        else if (!this.windowInstanceId.equals(other.windowInstanceId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return this.getStringId();
    }
}
