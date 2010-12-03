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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MockPortalRequestInfo implements IPortalRequestInfo {
    public UrlState urlState = UrlState.NORMAL;
    public UrlType urlType = UrlType.RENDER;
    public Map<String, List<String>> portalParameters = Collections.emptyMap();
    public String targetedLayoutNodeId;
    public Map<String, List<String>> layoutParameters = Collections.emptyMap();
    public IPortletRequestInfo portletRequestInfo;
    public String urlString;
    public Map<IPortletWindowId, IPortletRequestInfo> additionalPortletRequestInfos = new HashMap<IPortletWindowId, IPortletRequestInfo>();
    @Override
    public String getCanonicalUrl() {
        return this.urlString;
    }
    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }
    @Override
    public UrlState getUrlState() {
        return this.urlState;
    }
    public void setUrlState(UrlState urlState) {
        this.urlState = urlState;
    }
    @Override
    public UrlType getUrlType() {
        return this.urlType;
    }
    public void setUrlType(UrlType urlType) {
        this.urlType = urlType;
    }
    @Override
    public Map<String, List<String>> getPortalParameters() {
        return this.portalParameters;
    }
    public void setPortalParameters(Map<String, List<String>> portalParameters) {
        this.portalParameters = portalParameters;
    }
    @Override
    public String getTargetedLayoutNodeId() {
        return this.targetedLayoutNodeId;
    }
    public void setTargetedLayoutNodeId(String targetedLayoutNodeId) {
        this.targetedLayoutNodeId = targetedLayoutNodeId;
    }
    @Override
    public Map<String, List<String>> getLayoutParameters() {
        return this.layoutParameters;
    }
    public void setLayoutParameters(Map<String, List<String>> layoutParameters) {
        this.layoutParameters = layoutParameters;
    }
    @Override
    public IPortletRequestInfo getPortletRequestInfo() {
        return this.portletRequestInfo;
    }
    public void setPortletRequestInfo(IPortletRequestInfo portletRequestInfo) {
        this.portletRequestInfo = portletRequestInfo;
    }
    @Override
	public IPortletRequestInfo getAdditionalPortletRequestInfo(
			IPortletWindowId portletWindowId) {
		return this.additionalPortletRequestInfos.get(portletWindowId);
	}
	@Override
	public Collection<IPortletRequestInfo> getAdditionalPortletRequestInfos() {
		return this.additionalPortletRequestInfos.values();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((additionalPortletRequestInfos == null) ? 0
						: additionalPortletRequestInfos.hashCode());
		result = prime
				* result
				+ ((layoutParameters == null) ? 0 : layoutParameters.hashCode());
		result = prime
				* result
				+ ((portalParameters == null) ? 0 : portalParameters.hashCode());
		result = prime
				* result
				+ ((portletRequestInfo == null) ? 0 : portletRequestInfo
						.hashCode());
		result = prime
				* result
				+ ((targetedLayoutNodeId == null) ? 0 : targetedLayoutNodeId
						.hashCode());
		result = prime * result
				+ ((urlState == null) ? 0 : urlState.hashCode());
		result = prime * result
				+ ((urlString == null) ? 0 : urlString.hashCode());
		result = prime * result + ((urlType == null) ? 0 : urlType.hashCode());
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
		if (!(obj instanceof MockPortalRequestInfo)) {
			return false;
		}
		MockPortalRequestInfo other = (MockPortalRequestInfo) obj;
		if (additionalPortletRequestInfos == null) {
			if (other.additionalPortletRequestInfos != null) {
				return false;
			}
		} else if (!additionalPortletRequestInfos
				.equals(other.additionalPortletRequestInfos)) {
			return false;
		}
		if (layoutParameters == null) {
			if (other.layoutParameters != null) {
				return false;
			}
		} else if (!layoutParameters.equals(other.layoutParameters)) {
			return false;
		}
		if (portalParameters == null) {
			if (other.portalParameters != null) {
				return false;
			}
		} else if (!portalParameters.equals(other.portalParameters)) {
			return false;
		}
		if (portletRequestInfo == null) {
			if (other.portletRequestInfo != null) {
				return false;
			}
		} else if (!portletRequestInfo.equals(other.portletRequestInfo)) {
			return false;
		}
		if (targetedLayoutNodeId == null) {
			if (other.targetedLayoutNodeId != null) {
				return false;
			}
		} else if (!targetedLayoutNodeId.equals(other.targetedLayoutNodeId)) {
			return false;
		}
		if (urlState == null) {
			if (other.urlState != null) {
				return false;
			}
		} else if (!urlState.equals(other.urlState)) {
			return false;
		}
		if (urlString == null) {
			if (other.urlString != null) {
				return false;
			}
		} else if (!urlString.equals(other.urlString)) {
			return false;
		}
		if (urlType == null) {
			if (other.urlType != null) {
				return false;
			}
		} else if (!urlType.equals(other.urlType)) {
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
		builder.append("MockPortalRequestInfo [additionalPortletRequestInfos=");
		builder.append(additionalPortletRequestInfos);
		builder.append(", layoutParameters=");
		builder.append(layoutParameters);
		builder.append(", portalParameters=");
		builder.append(portalParameters);
		builder.append(", portletRequestInfo=");
		builder.append(portletRequestInfo);
		builder.append(", targetedLayoutNodeId=");
		builder.append(targetedLayoutNodeId);
		builder.append(", urlState=");
		builder.append(urlState);
		builder.append(", urlString=");
		builder.append(urlString);
		builder.append(", urlType=");
		builder.append(urlType);
		builder.append("]");
		return builder.toString();
	}
	
}
