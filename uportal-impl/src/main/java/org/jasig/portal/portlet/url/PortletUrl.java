/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.url;

import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Simple bean that describes a Portlet URL, all properties are null by default except targetWindowId
 * which will never be null
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletUrl {
    private final IPortletWindowId targetWindowId;
    private RequestType requestType = null;
    private Map<String, List<String>> parameters = null;
    private WindowState windowState = null;
    private PortletMode portletMode = null;
    private Boolean secure = null;
    private PortletUrl delegatePortletUrl = null;
    
    
    public PortletUrl(IPortletWindowId targetWindowId) {
        Validate.notNull(targetWindowId);
        this.targetWindowId = targetWindowId;
    }
    
    /**
     * @return The {@link IPortletWindowId} this URL targets
     */
    public IPortletWindowId getTargetWindowId() {
        return this.targetWindowId;
    }

    public RequestType getRequestType() {
        return requestType;
    }
    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public Map<String, List<String>> getParameters() {
        return parameters;
    }
    public void setParameters(Map<String, List<String>> parameters) {
        this.parameters = parameters;
    }

    public WindowState getWindowState() {
        return windowState;
    }
    public void setWindowState(WindowState windowState) {
        this.windowState = windowState;
    }

    public PortletMode getPortletMode() {
        return portletMode;
    }
    public void setPortletMode(PortletMode portletMode) {
        this.portletMode = portletMode;
    }

    public Boolean getSecure() {
        return secure;
    }
    public void setSecure(Boolean secure) {
        this.secure = secure;
    }
    
    public PortletUrl getDelegatePortletUrl() {
        return this.delegatePortletUrl;
    }
    /**
     * @param delegatePortletUrl URL data for a delegate portlet involved with rendering the url
     */
    public void setDelegatePortletUrl(PortletUrl delegatePortletUrl) {
        this.delegatePortletUrl = delegatePortletUrl;
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
        return new EqualsBuilder()
            .append(this.targetWindowId, rhs.targetWindowId)
            .append(this.secure, rhs.secure)
            .append(this.requestType, rhs.requestType)
            .append(this.windowState, rhs.windowState)
            .append(this.portletMode, rhs.portletMode)
            .append(this.parameters, rhs.parameters)
            .append(this.delegatePortletUrl, rhs.delegatePortletUrl)
            .isEquals();
    }
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(836501397, 1879998837)
            .append(this.targetWindowId)
            .append(this.secure)
            .append(this.requestType)
            .append(this.windowState)
            .append(this.parameters)
            .append(this.portletMode)
            .append(this.delegatePortletUrl)
            .toHashCode();
    }
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("targetWindowId", this.targetWindowId)
        .append("secure", this.secure)
        .append("requestType", this.requestType)
        .append("windowState", this.windowState)
        .append("parameters", this.parameters)
        .append("portletMode", this.portletMode)
        .append("delegatePortletUrl", this.delegatePortletUrl)
        .toString();
    }
}
