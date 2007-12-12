/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.mock.portlet.om;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.pluto.PortletWindowID;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MockPortletWindowId implements IPortletWindowId {
    private static final long serialVersionUID = 1L;
    
    private String portletWindowId;
    
    public MockPortletWindowId() {
        this.portletWindowId = null;
    }
    
    public MockPortletWindowId(String portletWindowId) {
        this.portletWindowId = portletWindowId;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletWindowID#getStringId()
     */
    public String getStringId() {
        return this.portletWindowId;
    }
    /**
     * @return the portletWindowId
     */
    public String getPortletWindowId() {
        return portletWindowId;
    }

    /**
     * @param portletWindowId the portletWindowId to set
     */
    public void setPortletWindowId(String portletWindowId) {
        this.portletWindowId = portletWindowId;
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
