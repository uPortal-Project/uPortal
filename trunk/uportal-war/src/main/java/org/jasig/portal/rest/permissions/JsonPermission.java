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
    public boolean equals(Object obj) {
        
        if ( !(obj instanceof JsonPermission )) {
            return false;
        }
        
        JsonPermission permission = (JsonPermission) obj;
        if (this == permission) {
            return true;
        }
        
        return new EqualsBuilder()
            .append(this.ownerName, permission.ownerName)
            .append(this.activityName, permission.activityName)
            .append(this.principalName, permission.principalName)
            .append(this.targetName, permission.targetName)
            .append(this.inherited, permission.inherited)
            .isEquals();
    }

}
