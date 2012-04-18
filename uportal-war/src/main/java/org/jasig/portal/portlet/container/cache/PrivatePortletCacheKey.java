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
package org.jasig.portal.portlet.container.cache;

import java.io.Serializable;

import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Key for privately scoped portlet data
 */
public class PrivatePortletCacheKey implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String sessionId;
    private final IPortletWindowId portletWindowId;
    private final IPortletEntityId portletEntityId;
    private final PublicPortletCacheKey publicPortletCacheKey;
    
    private final int hash;
    
    PrivatePortletCacheKey(String sessionId, IPortletWindowId portletWindowId,
            IPortletEntityId portletEntityId, PublicPortletCacheKey publicPortletCacheKey) {
        this.sessionId = sessionId;
        this.portletWindowId = portletWindowId;
        this.portletEntityId = portletEntityId;
        this.publicPortletCacheKey = publicPortletCacheKey;
        
        this.hash = internalHashCode();
    }
    
    public String getSessionId() {
        return sessionId;
    }

    public IPortletWindowId getPortletWindowId() {
        return portletWindowId;
    }

    public IPortletEntityId getPortletEntityId() {
        return portletEntityId;
    }

    public PublicPortletCacheKey getPublicPortletCacheKey() {
        return publicPortletCacheKey;
    }

    @Override
    public int hashCode() {
        return this.hash; 
    }
    
    private int internalHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((portletEntityId == null) ? 0 : portletEntityId.hashCode());
        result = prime * result + ((portletWindowId == null) ? 0 : portletWindowId.hashCode());
        result = prime * result + ((publicPortletCacheKey == null) ? 0 : publicPortletCacheKey.hashCode());
        result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
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
        PrivatePortletCacheKey other = (PrivatePortletCacheKey) obj;
        if (portletEntityId == null) {
            if (other.portletEntityId != null)
                return false;
        }
        else if (!portletEntityId.equals(other.portletEntityId))
            return false;
        if (portletWindowId == null) {
            if (other.portletWindowId != null)
                return false;
        }
        else if (!portletWindowId.equals(other.portletWindowId))
            return false;
        if (publicPortletCacheKey == null) {
            if (other.publicPortletCacheKey != null)
                return false;
        }
        else if (!publicPortletCacheKey.equals(other.publicPortletCacheKey))
            return false;
        if (sessionId == null) {
            if (other.sessionId != null)
                return false;
        }
        else if (!sessionId.equals(other.sessionId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (this.publicPortletCacheKey.isRenderHeader()) {
            return "PrivatePortletRenderHeaderCacheKey [sessionId=" + sessionId + ", portletWindowId=" + portletWindowId
                    + ", portletEntityId=" + portletEntityId + ", " + publicPortletCacheKey + "]";
        }
        if (this.publicPortletCacheKey.getResourceId() != null) {
            return "PrivatePortletResourceCacheKey [sessionId=" + sessionId + ", portletWindowId=" + portletWindowId
                    + ", portletEntityId=" + portletEntityId + ", " + publicPortletCacheKey + "]";
        }
        return "PrivatePortletRenderCacheKey [sessionId=" + sessionId + ", portletWindowId=" + portletWindowId
                + ", portletEntityId=" + portletEntityId + ", " + publicPortletCacheKey + "]";
    }
}