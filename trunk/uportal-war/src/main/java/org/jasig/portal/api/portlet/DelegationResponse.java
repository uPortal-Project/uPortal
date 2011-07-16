/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
