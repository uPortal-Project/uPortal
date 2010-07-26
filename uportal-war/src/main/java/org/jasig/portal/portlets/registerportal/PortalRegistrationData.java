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
