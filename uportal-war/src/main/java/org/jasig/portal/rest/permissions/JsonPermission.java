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

package org.jasig.portal.rest.permissions;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;


public class JsonPermission implements Comparable<JsonPermission> {

    private String ownerKey;
    private String ownerName;
    private String activityKey;
    private String activityName;
    private String principalKey;
    private String principalName;
    private String targetKey;
    private String targetName;
    private boolean inherited;

    public String getOwnerKey() {
        return ownerKey;
    }

    public void setOwnerKey(String ownerKey) {
        this.ownerKey = ownerKey;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getActivityKey() {
        return activityKey;
    }

    public void setActivityKey(String activityKey) {
        this.activityKey = activityKey;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getPrincipalKey() {
        return principalKey;
    }

    public void setPrincipalKey(String principalKey) {
        this.principalKey = principalKey;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getTargetKey() {
        return targetKey;
    }

    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public boolean isInherited() {
        return inherited;
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    public int compareTo(JsonPermission permission) {
        return new CompareToBuilder()
            .append(this.ownerName, permission.ownerName)
            .append(this.activityName, permission.activityName)
            .append(this.principalName, permission.principalName)
            .append(this.targetName, permission.targetName)
            .append(this.inherited, permission.inherited)
            .toComparison();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.activityName == null) ? 0 : this.activityName.hashCode());
        result = prime * result + (this.inherited ? 1231 : 1237);
        result = prime * result + ((this.ownerName == null) ? 0 : this.ownerName.hashCode());
        result = prime * result + ((this.principalName == null) ? 0 : this.principalName.hashCode());
        result = prime * result + ((this.targetName == null) ? 0 : this.targetName.hashCode());
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
        JsonPermission other = (JsonPermission) obj;
        if (this.activityName == null) {
            if (other.activityName != null)
                return false;
        }
        else if (!this.activityName.equals(other.activityName))
            return false;
        if (this.inherited != other.inherited)
            return false;
        if (this.ownerName == null) {
            if (other.ownerName != null)
                return false;
        }
        else if (!this.ownerName.equals(other.ownerName))
            return false;
        if (this.principalName == null) {
            if (other.principalName != null)
                return false;
        }
        else if (!this.principalName.equals(other.principalName))
            return false;
        if (this.targetName == null) {
            if (other.targetName != null)
                return false;
        }
        else if (!this.targetName.equals(other.targetName))
            return false;
        return true;
    }
}
