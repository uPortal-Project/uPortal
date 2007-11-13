/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.om;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.pluto.PortletWindowID;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletWindowIdImpl implements IPortletWindowId {
    private static final long serialVersionUID = 1L;
    
    private final String portletWindowId;
    
    public PortletWindowIdImpl(String portletWindowId) {
        Validate.notNull(portletWindowId, "portletWindowId can not be null");

        this.portletWindowId = portletWindowId;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletWindowID#getStringId()
     */
    public String getStringId() {
        return this.portletWindowId;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PortletWindowID)) {
            return false;
        }
        PortletWindowID rhs = (PortletWindowID) object;
        return new EqualsBuilder()
            .append(this.portletWindowId, rhs.getStringId())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-917388291, 674832463)
            .append(this.portletWindowId)
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
