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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.utils.ConcurrentMapUtils;

import com.google.common.base.Preconditions;

/**
 * Builds a portal URL
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class PortalUrlBuilder extends AbstractUrlBuilder implements IPortalActionUrlBuilder {
    final IUrlSyntaxProvider urlGenerator;
    final HttpServletRequest httpServletRequest;
    
    private final String targetFolderId;
    private final IPortletWindowId targetPortletWindowId;
    private final UrlType urlType;
    
    private final ConcurrentMap<IPortletWindowId, IPortletUrlBuilder> portletUrlBuilders = new ConcurrentHashMap<IPortletWindowId, IPortletUrlBuilder>(); 
    private String location;
    private String renderUrlParamName;
    
    
    public PortalUrlBuilder(
            IUrlSyntaxProvider urlSyntaxProvider, HttpServletRequest httpServletRequest,
            String targetFolderId, IPortletWindowId targetPortletWindowId, UrlType urlType) {
        
        Preconditions.checkNotNull(urlSyntaxProvider, "IUrlSyntaxProvider can not be null");
        Preconditions.checkNotNull(httpServletRequest, "HttpServletRequest can not be null");
        Preconditions.checkNotNull(urlType, "UrlType can not be null");
        
        this.urlGenerator = urlSyntaxProvider;
        this.httpServletRequest = httpServletRequest;
        this.targetFolderId = targetFolderId;
        this.targetPortletWindowId = targetPortletWindowId;
        this.urlType = urlType;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlBuilder#getTargetFolderId()
     */
    @Override
    public String getTargetFolderId() {
        return this.targetFolderId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlBuilder#getTargetPortletWindowId()
     */
    @Override
    public IPortletWindowId getTargetPortletWindowId() {
        return this.targetPortletWindowId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlBuilder#getUrlType()
     */
    @Override
    public UrlType getUrlType() {
        return this.urlType;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalActionUrlBuilder#setRedirectLocation(java.lang.String)
     */
    @Override
    public void setRedirectLocation(String location) {
        this.setRedirectLocation(location, null);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalActionUrlBuilder#setRedirectLocation(java.lang.String, java.lang.String)
     */
    @Override
    public void setRedirectLocation(String location, String renderUrlParamName) {
        this.location = location;
        this.renderUrlParamName = renderUrlParamName;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalActionUrlBuilder#getRedirectLocation()
     */
    @Override
    public String getRedirectLocation() {
        return this.location;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalActionUrlBuilder#getRenderUrlParamName()
     */
    @Override
    public String getRenderUrlParamName() {
        return this.renderUrlParamName;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlBuilder#getPortletUrlBuilder(org.jasig.portal.portlet.om.IPortletWindowId)
     */
    @Override
    public IPortletUrlBuilder getPortletUrlBuilder(IPortletWindowId portletWindowId) {
        IPortletUrlBuilder portletUrlBuilder;
        
        portletUrlBuilder = this.portletUrlBuilders.get(portletWindowId);
        if (portletUrlBuilder == null) {
            portletUrlBuilder = ConcurrentMapUtils.putIfAbsent(this.portletUrlBuilders, portletWindowId, new PortletUrlBuilder(portletWindowId, this));
        }
        
        return portletUrlBuilder;
    }
    
    @Override
    public IPortletUrlBuilder getTargetedPortletUrlBuilder() {
        if (this.targetPortletWindowId == null) {
            throw new IllegalStateException("This url must target a portlet for this call to be valid");
        }
        
        return this.getPortletUrlBuilder(this.targetPortletWindowId);
    }

    @Override
    public Map<IPortletWindowId, IPortletUrlBuilder> getPortletUrlBuilders() {
        return Collections.unmodifiableMap(this.portletUrlBuilders);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlBuilder#getUrlString()
     */
    @Override
    public String getUrlString() {
        return this.urlGenerator.generateUrl(this.httpServletRequest, this);
    }

    @Override
    public String toString() {
        return "PortalUrlBuilder [targetFolderId=" + this.targetFolderId + ", targetPortletWindowId="
                + this.targetPortletWindowId + ", urlType=" + this.urlType + ", parameters="
                + this.getParameters() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.targetFolderId == null) ? 0 : this.targetFolderId.hashCode());
        result = prime * result + ((this.targetPortletWindowId == null) ? 0 : this.targetPortletWindowId.hashCode());
        result = prime * result + ((this.urlType == null) ? 0 : this.urlType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PortalUrlBuilder other = (PortalUrlBuilder) obj;
        if (this.targetFolderId == null) {
            if (other.targetFolderId != null)
                return false;
        }
        else if (!this.targetFolderId.equals(other.targetFolderId))
            return false;
        if (this.targetPortletWindowId == null) {
            if (other.targetPortletWindowId != null)
                return false;
        }
        else if (!this.targetPortletWindowId.equals(other.targetPortletWindowId))
            return false;
        if (this.urlType != other.urlType)
            return false;
        return true;
    }
}
