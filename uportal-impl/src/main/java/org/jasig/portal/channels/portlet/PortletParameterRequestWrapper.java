/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.channels.portlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Wraps a request to present a different set of request parameters.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletParameterRequestWrapper extends HttpServletRequestWrapper {
    private final Map<String, String[]> parameters;
    
    /**
     * @param request
     */
    public PortletParameterRequestWrapper(HttpServletRequest request, Map<String, String[]> parameters) {
        super(request);
        Validate.notNull(parameters, "parameters can not be null");
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
     */
    @Override
    public String getParameter(String name) {
        final String[] values = this.getParameterValues(name);
        if (values != null && values.length > 0) {
            return values[0];
        }

        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterMap()
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return this.parameters;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterNames()
     */
    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
     */
    @Override
    public String[] getParameterValues(String name) {
        return this.parameters.get(name);
    }

    
    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PortletParameterRequestWrapper)) {
            return false;
        }
        PortletParameterRequestWrapper rhs = (PortletParameterRequestWrapper) object;
        return new EqualsBuilder()
            .append(this.getRequest(), rhs.getRequest())
            .append(this.parameters, rhs.parameters)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(500546767, -122181035)
            .append(this.getRequest())
            .append(this.parameters)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("wrappedRequest", this.getRequest())
            .append("parameters", this.parameters)
            .toString();
    }
}
