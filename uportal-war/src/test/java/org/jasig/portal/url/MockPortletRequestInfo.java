/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.url;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MockPortletRequestInfo implements IPortletRequestInfo {
    public IPortletWindowId targetWindowId;
    public Map<String, List<String>> portletParameters = Collections.emptyMap();
    public Map<String, List<String>> publicPortletParameters = Collections.emptyMap();
    public WindowState windowState;
    public PortletMode portletMode;
    public IPortletRequestInfo delegatePortletRequestInfo;
    public IPortletWindowId getTargetWindowId() {
        return this.targetWindowId;
    }
    public void setTargetWindowId(IPortletWindowId targetWindowId) {
        this.targetWindowId = targetWindowId;
    }
    public Map<String, List<String>> getPortletParameters() {
        return this.portletParameters;
    }
    public void setPortletParameters(Map<String, List<String>> portletParameters) {
        this.portletParameters = portletParameters;
    }
    public Map<String, List<String>> getPublicPortletParameters() {
        return this.publicPortletParameters;
    }
    public void setPublicPortletParameters(Map<String, List<String>> publicPortletParameters) {
        this.publicPortletParameters = publicPortletParameters;
    }
    public WindowState getWindowState() {
        return this.windowState;
    }
    public void setWindowState(WindowState windowState) {
        this.windowState = windowState;
    }
    public PortletMode getPortletMode() {
        return this.portletMode;
    }
    public void setPortletMode(PortletMode portletMode) {
        this.portletMode = portletMode;
    }
    public IPortletRequestInfo getDelegatePortletRequestInfo() {
        return this.delegatePortletRequestInfo;
    }
    public void setDelegatePortletRequestInfo(IPortletRequestInfo delegatePortletRequestInfo) {
        this.delegatePortletRequestInfo = delegatePortletRequestInfo;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((this.delegatePortletRequestInfo == null) ? 0 : this.delegatePortletRequestInfo.hashCode());
        result = prime * result + ((this.portletMode == null) ? 0 : this.portletMode.hashCode());
        result = prime * result + ((this.portletParameters == null) ? 0 : this.portletParameters.hashCode());
        result = prime * result
                + ((this.publicPortletParameters == null) ? 0 : this.publicPortletParameters.hashCode());
        result = prime * result + ((this.targetWindowId == null) ? 0 : this.targetWindowId.hashCode());
        result = prime * result + ((this.windowState == null) ? 0 : this.windowState.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MockPortletRequestInfo other = (MockPortletRequestInfo) obj;
        if (this.delegatePortletRequestInfo == null) {
            if (other.delegatePortletRequestInfo != null) {
                return false;
            }
        }
        else if (!this.delegatePortletRequestInfo.equals(other.delegatePortletRequestInfo)) {
            return false;
        }
        if (this.portletMode == null) {
            if (other.portletMode != null) {
                return false;
            }
        }
        else if (!this.portletMode.equals(other.portletMode)) {
            return false;
        }
        if (this.portletParameters == null) {
            if (other.portletParameters != null) {
                return false;
            }
        }
        else if (!this.portletParameters.equals(other.portletParameters)) {
            return false;
        }
        if (this.publicPortletParameters == null) {
            if (other.publicPortletParameters != null) {
                return false;
            }
        }
        else if (!this.publicPortletParameters.equals(other.publicPortletParameters)) {
            return false;
        }
        if (this.targetWindowId == null) {
            if (other.targetWindowId != null) {
                return false;
            }
        }
        else if (!this.targetWindowId.equals(other.targetWindowId)) {
            return false;
        }
        if (this.windowState == null) {
            if (other.windowState != null) {
                return false;
            }
        }
        else if (!this.windowState.equals(other.windowState)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "MockPortletRequestInfo [delegatePortletRequestInfo=" + this.delegatePortletRequestInfo
                + ", portletMode=" + this.portletMode + ", portletParameters=" + this.portletParameters
                + ", publicPortletParameters=" + this.publicPortletParameters + ", targetWindowId="
                + this.targetWindowId + ", windowState=" + this.windowState + "]";
    }
}
