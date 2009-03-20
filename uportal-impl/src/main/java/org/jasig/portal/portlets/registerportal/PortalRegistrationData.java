/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal;

import java.util.Map;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Data object that is submitted to Jasig
 * 
 * @author Eric Dalquist
 * @version $Revision: 45508 $
 */
public class PortalRegistrationData extends PortalRegistrationRequest {
    private static final long serialVersionUID = 1L;

    private Map<String, Map<String, String>> collectedData;
    
    public PortalRegistrationData(PortalRegistrationRequest registrationRequest) {
        super(registrationRequest);
    }

    /**
     * @return the collectedData
     */
    public Map<String, Map<String, String>> getCollectedData() {
        return collectedData;
    }
    /**
     * @param collectedData the collectedData to set
     */
    public void setCollectedData(Map<String, Map<String, String>> collectedData) {
        this.collectedData = collectedData;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PortalRegistrationData)) {
            return false;
        }
        PortalRegistrationData rhs = (PortalRegistrationData) object;
        return new EqualsBuilder()
            .appendSuper(super.equals(object))
            .append(this.collectedData, rhs.collectedData)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(229556677, 2120134195)
            .appendSuper(super.hashCode())
            .append(this.collectedData)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .appendSuper(super.toString())
            .append("collectedData", this.collectedData)
            .toString();
    }
}
