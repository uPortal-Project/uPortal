/**
 * Licensed to Jasig under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Jasig
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.permission.target;

import java.io.Serializable;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * PermissionTargetImpl represents a simple default implementation of IPermissionTarget.
 *
 * @since 3.3
 */
@SuppressWarnings("ComparableType")
public class PermissionTargetImpl
        implements IPermissionTarget, Comparable<IPermissionTarget>, Serializable {

    private static final long serialVersionUID = 1L;

    private final String key;
    private final String name;
    private final TargetType type;

    /**
     * Construct a new PermissionTargetImpl with the specified key and human-readable name.
     *
     * @param key
     * @param name
     */
    public PermissionTargetImpl(String key, String name, TargetType type) {
        this.key = key;
        this.name = name;
        this.type = type;
    }

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.permission.target.IPermissionTarget#getKey()
     */
    @Override
    public String getKey() {
        return key;
    }

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.permission.target.IPermissionTarget#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public TargetType getTargetType() {
        return type;
    }

    /** @see java.lang.Object#equals(Object) */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IPermissionTarget)) {
            return false;
        }

        IPermissionTarget target = (IPermissionTarget) obj;
        return new EqualsBuilder()
                .append(this.key, target.getKey())
                .append(this.name, target.getName())
                .append(this.type, target.getTargetType())
                .isEquals();
    }

    /** @see java.lang.Object#hashCode() */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143)
                .append(this.key)
                .append(this.name)
                .append(this.type)
                .toHashCode();
    }

    /** @see java.lang.Object#toString() */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("key", this.key)
                .append("name", this.name)
                .toString();
    }

    /** @see java.lang.Comparable#compareTo(java.lang.Object) */
    @Override
    public int compareTo(IPermissionTarget target) {
        return new CompareToBuilder()
                .append(this.name, target.getName())
                .append(this.key, target.getKey())
                .toComparison();
    }
}
