/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.api.portlet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * The resulting state of the delegation request
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DelegationResponse {
    private final DelegateState delegateState;
    
    public DelegationResponse(DelegateState delegateState) {
        this(delegateState, false);
    }
    
    public DelegationResponse(DelegateState delegateState, boolean redirected) {
        this.delegateState = delegateState;
    }

    public DelegateState getDelegateState() {
        return this.delegateState;
    }


    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DelegationResponse)) {
            return false;
        }
        DelegationResponse rhs = (DelegationResponse) object;
        return new EqualsBuilder()
            .append(this.delegateState, rhs.delegateState)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(1445247369, -1009176817)
            .append(this.delegateState)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("delegateState", this.delegateState)
            .toString();
    }
}
