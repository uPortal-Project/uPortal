/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import java.util.Arrays;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Simple bean that describes a Portlet URL, all properties are null by default
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletUrl {
    private RequestType requestType = null;
    private Map<String, String[]> parameters = null;
    private WindowState windowState = null;
    private PortletMode portletMode = null;
    private Boolean secure = null;
    
    
    /**
     * @return the requestType
     */
    public RequestType getRequestType() {
        return requestType;
    }
    /**
     * @param requestType the requestType to set
     */
    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }
    /**
     * @return the parameters
     */
    public Map<String, String[]> getParameters() {
        return parameters;
    }
    /**
     * @param parameters the parameters to set
     */
    public void setParameters(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }
    /**
     * @return the windowState
     */
    public WindowState getWindowState() {
        return windowState;
    }
    /**
     * @param windowState the windowState to set
     */
    public void setWindowState(WindowState windowState) {
        this.windowState = windowState;
    }
    /**
     * @return the portletMode
     */
    public PortletMode getPortletMode() {
        return portletMode;
    }
    /**
     * @param portletMode the portletMode to set
     */
    public void setPortletMode(PortletMode portletMode) {
        this.portletMode = portletMode;
    }
    /**
     * @return the secure
     */
    public Boolean getSecure() {
        return secure;
    }
    /**
     * @param secure the secure to set
     */
    public void setSecure(Boolean secure) {
        this.secure = secure;
    }
    
    
    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PortletUrl)) {
            return false;
        }
        PortletUrl rhs = (PortletUrl) object;
        if (new EqualsBuilder()
            .append(this.secure, rhs.secure)
            .append(this.requestType, rhs.requestType)
            .append(this.windowState, rhs.windowState)
            .append(this.portletMode, rhs.portletMode)
            .isEquals()) {
            
            //Nasty logic for doing equality checking on the parameters Map that has String[] values
            if (this.parameters == rhs.parameters || (this.parameters != null && this.parameters.equals(rhs.parameters))) {
                return true;
            }
            else if ((this.parameters != rhs.parameters && (this.parameters == null || rhs.parameters == null)) || this.parameters.size() != rhs.parameters.size()) {
                return false;
            }
            else {
                for (final Map.Entry<String, String[]> paramEntry : this.parameters.entrySet()) {
                    final String key = paramEntry.getKey();
                    
                    if (!Arrays.equals(paramEntry.getValue(), rhs.parameters.get(key))) {
                        return false;
                    }
                }
                
                return true;
            }
        }
        
        return false;
    }
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(836501397, 1879998837)
            .append(this.secure)
            .append(this.requestType)
            .append(this.windowState)
            .append(this.parameters)
            .append(this.portletMode)
            .toHashCode();
    }
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("secure", this.secure)
        .append("requestType", this.requestType)
        .append("windowState", this.windowState)
        .append("parameters", this.parameters)
        .append("portletMode", this.portletMode)
        .toString();
    }
}
