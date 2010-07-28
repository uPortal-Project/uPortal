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

package org.jasig.portal.url;

import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
class PortletRequestInfoImpl implements IPortletRequestInfo {
    private final IPortletWindowId targetWindowId;
    private Map<String, List<String>> portletParameters;
    private Map<String, List<String>> publicPortletParameters;
    private WindowState windowState;
    private PortletMode portletMode;
    private IPortletRequestInfo delegatePortletRequestInfo;
    
    public PortletRequestInfoImpl(IPortletWindowId targetWindowId) {
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

    public IPortletWindowId getTargetWindowId() {
        return this.targetWindowId;
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
        PortletRequestInfoImpl other = (PortletRequestInfoImpl) obj;
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
        return "PortletRequestInfoImpl [delegatePortletRequestInfo=" + this.delegatePortletRequestInfo
                + ", portletMode=" + this.portletMode + ", portletParameters=" + this.portletParameters
                + ", publicPortletParameters=" + this.publicPortletParameters + ", targetWindowId="
                + this.targetWindowId + ", windowState=" + this.windowState + "]";
    }
    
}
