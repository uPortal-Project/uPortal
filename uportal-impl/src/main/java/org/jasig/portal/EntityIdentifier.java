/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * A key and type that uniquely identify a portal entity.
 * @author Dan Ellentuck
 * @version $Revision$
 * @see IBasicEntity
 */
public class EntityIdentifier implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final String key;
    protected final Class<? extends IBasicEntity> type;

    /**
     * KeyTypePair constructor.
     */
    public EntityIdentifier(String entityKey, Class<? extends IBasicEntity> entityType) {
        key = entityKey;
        type = entityType;
    }

    /**
     * @return java.lang.String
     */
    public String getKey() {
        return key;
    }

    /**
     * @return java.lang.Class
     */
    public Class<? extends IBasicEntity> getType() {
        return type;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof EntityIdentifier)) {
            return false;
        }
        EntityIdentifier rhs = (EntityIdentifier) object;
        return new EqualsBuilder()
            .append(this.type, rhs.type)
            .append(this.key, rhs.key)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-646446001, 994968607)
            .append(this.type)
            .append(this.key)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("type", this.type)
                .append("key", this.key)
                .toString();
    }
}
