/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A key and type that uniquely identify a portal entity.
 *
 * <p>In the case of users, the key will be the username and the type will be {@link
 * org.apereo.portal.security.IPerson}.
 *
 * @see IBasicEntity
 */
public class EntityIdentifier implements Serializable {
    private static final long serialVersionUID = 1L;
    // Named constants for HashCodeBuilder
    private static final int INITIAL_NON_ZERO_ODD_NUMBER = -646446001;
    private static final int MULTIPLIER_NON_ZERO_ODD_NUMBER = 994968607;

    protected final String key;
    protected final Class<? extends IBasicEntity> type;

    /** KeyTypePair constructor. */
    public EntityIdentifier(String entityKey, Class<? extends IBasicEntity> entityType) {
        key = entityKey;
        type = entityType;
    }

    /** @return java.lang.String */
    public String getKey() {
        return key;
    }

    /** @return java.lang.Class */
    public Class<? extends IBasicEntity> getType() {
        return type;
    }

    /** @see java.lang.Object#equals(Object) */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof EntityIdentifier)) {
            return false;
        }
        EntityIdentifier rhs = (EntityIdentifier) object;
        return new EqualsBuilder().append(this.type, rhs.type).append(this.key, rhs.key).isEquals();
    }

    /** @see java.lang.Object#hashCode() */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(INITIAL_NON_ZERO_ODD_NUMBER, MULTIPLIER_NON_ZERO_ODD_NUMBER)
                .append(this.type)
                .append(this.key)
                .toHashCode();
    }

    /** @see java.lang.Object#toString() */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("type", this.type)
                .append("key", this.key)
                .toString();
    }
}
