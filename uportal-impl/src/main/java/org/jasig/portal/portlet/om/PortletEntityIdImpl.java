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
public class PortletEntityIdImpl implements IPortletEntityId {
    private static final long serialVersionUID = 1L;
    
    private final String portletEntityId;
    
    public PortletEntityIdImpl(String portletEntityId) {
        Validate.notNull(portletEntityId, "portletEntityId can not be null");

        this.portletEntityId = portletEntityId;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletEntityID#getStringId()
     */
    public String getStringId() {
        return this.portletEntityId;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IPortletEntityId)) {
            return false;
        }
        IPortletEntityId rhs = (IPortletEntityId) object;
        return new EqualsBuilder()
            .append(this.portletEntityId, rhs.getStringId())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-917388291, 674832463)
            .append(this.portletEntityId)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getStringId();
    }
}
