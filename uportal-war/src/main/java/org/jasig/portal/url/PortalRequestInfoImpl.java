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

/**
 * 
 */
package org.jasig.portal.url;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.portlet.om.IPortletWindowId;


/**
 * Basic implementation of {@link IPortalRequestInfo} - a straightforward java bean.
 * Package private by design - see {@link IPortalUrlProvider} for a means to retrieve
 * an instance.
 * 
 * Default value for "action" is <strong>false</strong>.
 * Default urlState is {@link UrlState#NORMAL}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 *
 */
class PortalRequestInfoImpl implements IPortalRequestInfo {
    private String targetedLayoutNodeId;
    private Map<String, List<String>> layoutParameters;
    private IPortletRequestInfo portletRequestInfo;
    private UrlState urlState = UrlState.NORMAL;
    private UrlType urlType = UrlType.RENDER;
    private Map<String, List<String>> portalParameters;
    private String urlString;
    private Map<IPortletWindowId, IPortletRequestInfo> additionalPortletRequestInfoMap = new HashMap<IPortletWindowId, IPortletRequestInfo>();
    
    @Override
    public String getTargetedLayoutNodeId() {
        return this.targetedLayoutNodeId;
    }
    public void setTargetedLayoutNodeId(String targetedLayoutNodeId) {
        this.targetedLayoutNodeId = targetedLayoutNodeId;
    }
    @Override
    public IPortletRequestInfo getPortletRequestInfo() {
        return this.portletRequestInfo;
    }
    public void setPortletRequestInfo(IPortletRequestInfo portletRequestInfo) {
        this.portletRequestInfo = portletRequestInfo;
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
    public Map<String, List<String>> getLayoutParameters() {
        return this.layoutParameters;
    }
    public void setLayoutParameters(Map<String, List<String>> layoutParameters) {
        this.layoutParameters = layoutParameters;
    }
    @Override
    public Map<String, List<String>> getPortalParameters() {
        return this.portalParameters;
    }
    public void setPortalParameters(Map<String, List<String>> portalParameters) {
        this.portalParameters = portalParameters;
    }
    @Override
    public String getCanonicalUrl() {
        return this.urlString;
    }
    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestInfo#getAdditionalPortletRequestInfo(org.jasig.portal.portlet.om.IPortletWindowId)
     */
	@Override
	public IPortletRequestInfo getAdditionalPortletRequestInfo(
			IPortletWindowId portletWindowId) {
		return this.additionalPortletRequestInfoMap.get(portletWindowId);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.url.IPortalRequestInfo#getAdditionalPortletRequestInfos()
	 */
	@Override
	public Collection<IPortletRequestInfo> getAdditionalPortletRequestInfos() {
		return this.additionalPortletRequestInfoMap.values();
	}
	/**
	 * @return the additionalPortletRequestInfoMap
	 */
	public Map<IPortletWindowId, IPortletRequestInfo> getAdditionalPortletRequestInfoMap() {
		return additionalPortletRequestInfoMap;
	}

	/**
	 * @param additionalPortletRequestInfoMap the additionalPortletRequestInfoMap to set
	 */
	public void setAdditionalPortletRequestInfoMap(
			Map<IPortletWindowId, IPortletRequestInfo> additionalPortletRequestInfoMap) {
		this.additionalPortletRequestInfoMap = additionalPortletRequestInfoMap;
	}
	public void addChildAdditionalPortletRequestInfo(IPortletWindowId portletWindowId, IPortletRequestInfo portletRequestInfo) {
		this.additionalPortletRequestInfoMap.put(portletWindowId, portletRequestInfo);
	}
}
