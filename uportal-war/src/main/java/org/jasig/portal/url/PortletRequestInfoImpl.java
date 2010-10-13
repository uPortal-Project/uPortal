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
import javax.portlet.ResourceURL;
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
    private String resourceId;
    private String cacheability = ResourceURL.PAGE;
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
    

    /**
	 * @return the resourceId
	 */
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * @param resourceId the resourceId to set
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * @return the cacheability
	 */
	public String getCacheability() {
		return cacheability;
	}

	/**
	 * @param cacheability the cacheability to set
	 */
	public void setCacheability(String cacheability) {
		this.cacheability = cacheability;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cacheability == null) ? 0 : cacheability.hashCode());
		result = prime
				* result
				+ ((delegatePortletRequestInfo == null) ? 0
						: delegatePortletRequestInfo.hashCode());
		result = prime * result
				+ ((portletMode == null) ? 0 : portletMode.hashCode());
		result = prime
				* result
				+ ((portletParameters == null) ? 0 : portletParameters
						.hashCode());
		result = prime
				* result
				+ ((publicPortletParameters == null) ? 0
						: publicPortletParameters.hashCode());
		result = prime * result
				+ ((resourceId == null) ? 0 : resourceId.hashCode());
		result = prime * result
				+ ((targetWindowId == null) ? 0 : targetWindowId.hashCode());
		result = prime * result
				+ ((windowState == null) ? 0 : windowState.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PortletRequestInfoImpl)) {
			return false;
		}
		PortletRequestInfoImpl other = (PortletRequestInfoImpl) obj;
		if (cacheability == null) {
			if (other.cacheability != null) {
				return false;
			}
		} else if (!cacheability.equals(other.cacheability)) {
			return false;
		}
		if (delegatePortletRequestInfo == null) {
			if (other.delegatePortletRequestInfo != null) {
				return false;
			}
		} else if (!delegatePortletRequestInfo
				.equals(other.delegatePortletRequestInfo)) {
			return false;
		}
		if (portletMode == null) {
			if (other.portletMode != null) {
				return false;
			}
		} else if (!portletMode.equals(other.portletMode)) {
			return false;
		}
		if (portletParameters == null) {
			if (other.portletParameters != null) {
				return false;
			}
		} else if (!portletParameters.equals(other.portletParameters)) {
			return false;
		}
		if (publicPortletParameters == null) {
			if (other.publicPortletParameters != null) {
				return false;
			}
		} else if (!publicPortletParameters
				.equals(other.publicPortletParameters)) {
			return false;
		}
		if (resourceId == null) {
			if (other.resourceId != null) {
				return false;
			}
		} else if (!resourceId.equals(other.resourceId)) {
			return false;
		}
		if (targetWindowId == null) {
			if (other.targetWindowId != null) {
				return false;
			}
		} else if (!targetWindowId.equals(other.targetWindowId)) {
			return false;
		}
		if (windowState == null) {
			if (other.windowState != null) {
				return false;
			}
		} else if (!windowState.equals(other.windowState)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PortletRequestInfoImpl [cacheability=");
		builder.append(cacheability);
		builder.append(", delegatePortletRequestInfo=");
		builder.append(delegatePortletRequestInfo);
		builder.append(", portletMode=");
		builder.append(portletMode);
		builder.append(", portletParameters=");
		builder.append(portletParameters);
		builder.append(", publicPortletParameters=");
		builder.append(publicPortletParameters);
		builder.append(", resourceId=");
		builder.append(resourceId);
		builder.append(", targetWindowId=");
		builder.append(targetWindowId);
		builder.append(", windowState=");
		builder.append(windowState);
		builder.append("]");
		return builder.toString();
	}
    
}
