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

package org.jasig.portal.events.handlers.db;

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * This domain object will be used to hold user information for the stats that
 * are being collected (i.e. userId and group affilations)
 * 
 * @author Stan Schwartz, sschwartz@unicon.net
 * @version $Revision: 1.3 $
 */
public class StatsSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private long sessionId = 0;
    private String userName;
    private Set<String> groups;


    /**
     * @return Returns the sessionId.
     */
    public long getSessionId() {
        return this.sessionId;
    }
    /**
     * @return Returns the userName.
     */
    public String getUserName() {
        return this.userName;
    }
    /**
     * @param sessionId The sessionId to set.
     */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }
    /**
     * @param userName The userName to set.
     */
    public void setUserName(String sessionUserId) {
        this.userName = sessionUserId;
    }
    /**
     * @return the groups
     */
    public Set<String> getGroups() {
        return groups;
    }
    /**
     * @param groups the groups to set
     */
    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }
    

    
    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof StatsSession)) {
            return false;
        }
        StatsSession rhs = (StatsSession) object;
        return new EqualsBuilder()
            .append(this.sessionId, rhs.getSessionId())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143)
            .append(this.sessionId)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("sessionId", this.sessionId)
            .append("userName", this.userName)
            .append("groups", this.groups)
            .toString();
    }
}

