/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.om;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractObjectId implements IObjectId {
    private final String objectId;

    public AbstractObjectId(String objectId) {
        Validate.notNull(objectId, "objectId can not be null");

        this.objectId = objectId;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletEntityID#getStringId()
     */
    public String getStringId() {
        return this.objectId;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IObjectId)) {
            return false;
        }
        IObjectId rhs = (IObjectId)object;
        return new EqualsBuilder().append(this.objectId, rhs.getStringId()).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-917388291, 674832463).append(this.objectId).toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getStringId();
    }
}
