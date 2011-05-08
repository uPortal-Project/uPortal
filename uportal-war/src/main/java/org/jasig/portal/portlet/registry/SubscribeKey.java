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

final class SubscribeKey {
    private final int userId;
    private final String layoutNodeId;
    
    public SubscribeKey(int userId, String layoutNodeId) {
        this.userId = userId;
        this.layoutNodeId = layoutNodeId;
    }
    
    @Override
    public String toString() {
        return "[userId=" + userId + ", layoutNodeId=" + layoutNodeId + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((layoutNodeId == null) ? 0 : layoutNodeId.hashCode());
        result = prime * result + userId;
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
        SubscribeKey other = (SubscribeKey) obj;
        if (layoutNodeId == null) {
            if (other.layoutNodeId != null)
                return false;
        }
        else if (!layoutNodeId.equals(other.layoutNodeId))
            return false;
        if (userId != other.userId)
            return false;
        return true;
    }
}