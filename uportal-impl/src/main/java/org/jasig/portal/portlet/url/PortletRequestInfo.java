/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents data targeting a portlet for a request
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRequestInfo {
    private final RequestType requestType;
    private final Map<String, String[]> parameters;
    
    public PortletRequestInfo(RequestType requestType) {
        this(requestType, null);
    }

    public PortletRequestInfo(RequestType requestType, Map<String, String[]> parameters) {
        Validate.notNull(requestType, "requestType can not be null");
        
        this.requestType = requestType;
        this.parameters = parameters;
    }

    /**
     * @return the requestType
     */
    public RequestType getRequestType() {
        return this.requestType;
    }

    /**
     * @return the parameters, null if no parameters are passed
     */
    public Map<String, String[]> getParameters() {
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