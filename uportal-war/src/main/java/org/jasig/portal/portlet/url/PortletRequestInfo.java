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

package org.jasig.portal.portlet.url;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.pluto.container.PortletURLProvider.TYPE;

/**
 * Represents data targeting a portlet for a request
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRequestInfo {
    private final TYPE requestType;
    private final Map<String, List<String>> parameters;
    
    public PortletRequestInfo(TYPE requestType) {
        this(requestType, null);
    }

    public PortletRequestInfo(TYPE requestType, Map<String, List<String>> parameters) {
        Validate.notNull(requestType, "requestType can not be null");
        
        this.requestType = requestType;
        this.parameters = parameters;
    }

    /**
     * @return the requestType
     */
    public TYPE getRequestType() {
        return this.requestType;
    }

    /**
     * @return the parameters, null if no parameters are passed
     */
    public Map<String, List<String>> getParameters() {
        return this.parameters;
    }
    
    
    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PortletRequestInfo)) {
            return false;
        }
        PortletRequestInfo rhs = (PortletRequestInfo) object;
        return new EqualsBuilder()
            .append(this.requestType, rhs.requestType)
            .append(this.parameters, rhs.parameters)
            .isEquals();
    }
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(665177213, 464849795)
            .append(this.requestType)
            .append(this.parameters)
            .toHashCode();
    }
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("requestType", this.requestType)
            .append("parameters", this.parameters)
            .toString();
    }
}