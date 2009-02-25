/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.mock.portlet.om;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jasig.portal.portlet.om.IPortletDefinitionId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MockPortletDefinitionId implements IPortletDefinitionId {
    private static final long serialVersionUID = 1L;
    
    private String portletDefinitionId;
    
    public MockPortletDefinitionId() {
        this.portletDefinitionId = null;
    }
    
    public MockPortletDefinitionId(String portletDefinitionId) {
        this.portletDefinitionId = portletDefinitionId;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletDefinitionID#getStringId()
     */
    public String getStringId() {
        return this.portletDefinitionId;
    }
    /**
     * @return the portletDefinitionId
     */
    public String getPortletDefinitionId() {
        return portletDefinitionId;
    }

    /**
     * @param portletDefinitionId the portletDefinitionId to set
     */
    public void setPortletDefinitionId(String portletDefinitionId) {
        this.portletDefinitionId = portletDefinitionId;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IPortletDefinitionId)) {
            return false;
        }
        IPortletDefinitionId rhs = (IPortletDefinitionId) object;
        return new EqualsBuilder()
            .append(this.portletDefinitionId, rhs.getStringId())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-917388291, 674832463)
            .append(this.portletDefinitionId)
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
