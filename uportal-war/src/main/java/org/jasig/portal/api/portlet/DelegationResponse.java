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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.delegateState == null) ? 0 : this.delegateState.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DelegationResponse other = (DelegationResponse) obj;
        if (this.delegateState == null) {
            if (other.delegateState != null)
                return false;
        }
        else if (!this.delegateState.equals(other.delegateState))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DelegationResponse [delegateState=" + this.delegateState + "]";
    }
}
