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

import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.om.IPortletWindowId;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapConstraint;
import com.google.common.collect.MapConstraints;

/**
 * Builds a portlet URL
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class PortletUrlBuilder extends AbstractUrlBuilder implements IPortletUrlBuilder {
    private final IPortletWindowId portletWindowId;
    private final IPortalUrlBuilder portalUrlBuilder;
    private final UrlType urlType;
    private final Map<String, String[]> publicRenderParameters;
    
    private WindowState windowState = null;
    private PortletMode portletMode = null;
    private String resourceId = null;
    private String cacheability = null;
    private boolean copyCurrentRenderParameters = false;
    
    public PortletUrlBuilder(IPortletWindowId portletWindowId, IPortalUrlBuilder portalUrlBuilder) {
        Preconditions.checkNotNull(portletWindowId, "IPortletWindowId can not be null");
        Preconditions.checkNotNull(portalUrlBuilder, "IPortalUrlBuilder can not be null");
        
        this.portletWindowId = portletWindowId;
        this.portalUrlBuilder = portalUrlBuilder;
        this.urlType = this.portalUrlBuilder.getUrlType();
        
        this.publicRenderParameters = MapConstraints.constrainedMap(new ParameterMap(), new MapConstraint<String, String[]>() {
            @Override
            public void checkKeyValue(String key, String[] value) {
                Validate.notNull(key, "name can not be null");
                Validate.noNullElements(value, "values can not be null or contain null elements");
            }
        });
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getPortletWindowId()
     */
    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindowId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getPortalUrlBuilder()
     */
    @Override
    public IPortalUrlBuilder getPortalUrlBuilder() {
        return this.portalUrlBuilder;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#setCopyCurrentRenderParameters(boolean)
     */
    @Override
    public void setCopyCurrentRenderParameters(boolean copyCurrentRenderParameters) {
        this.copyCurrentRenderParameters = copyCurrentRenderParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getCopyCurrentRenderParameters()
     */
    @Override
    public boolean getCopyCurrentRenderParameters() {
        return this.copyCurrentRenderParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#setWindowState(javax.portlet.WindowState)
     */
    @Override
    public void setWindowState(WindowState windowState) {
        this.windowState = windowState;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getWindowState()
     */
    @Override
    public WindowState getWindowState() {
        return this.windowState;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#setPortletMode(javax.portlet.PortletMode)
     */
    @Override
    public void setPortletMode(PortletMode portletMode) {
        this.portletMode = portletMode;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getPortletMode()
     */
    @Override
    public PortletMode getPortletMode() {
        return this.portletMode;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#setResourceId(java.lang.String)
     */
    @Override
    public void setResourceId(String resourceId) {
        Preconditions.checkArgument(this.urlType == UrlType.RESOURCE, "UrlType must be %s but was %s", UrlType.RESOURCE, this.urlType);
        this.resourceId = resourceId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getResourceId()
     */
    @Override
    public String getResourceId() {
        Preconditions.checkArgument(this.urlType == UrlType.RESOURCE, "UrlType must be %s but was %s", UrlType.RESOURCE, this.urlType);
        return this.resourceId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#setCacheability(java.lang.String)
     */
    @Override
    public void setCacheability(String cacheability) {
        Preconditions.checkArgument(this.urlType == UrlType.RESOURCE, "UrlType must be %s but was %s", UrlType.RESOURCE, this.urlType);
        this.cacheability = cacheability;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortletUrlBuilder#getCacheability()
     */
    @Override
    public String getCacheability() {
        Preconditions.checkArgument(this.urlType == UrlType.RESOURCE, "UrlType must be %s but was %s", UrlType.RESOURCE, this.urlType);
        return this.cacheability;
    }
    
    
    @Override
    public Map<String, String[]> getPublicRenderParameters() {
        return this.publicRenderParameters;
    }

    @Override
    public String toString() {
        return "PortletUrlBuilder [portletWindowId=" + this.portletWindowId + ", windowState=" + this.windowState
                + ", portletMode=" + this.portletMode + ", resourceId=" + this.resourceId + ", cacheability="
                + this.cacheability + ", parameters=" + this.getParameters() + ", publicRenderParameters="
                + this.publicRenderParameters + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.cacheability == null) ? 0 : this.cacheability.hashCode());
        result = prime * result + ((this.portletMode == null) ? 0 : this.portletMode.hashCode());
        result = prime * result + ((this.portletWindowId == null) ? 0 : this.portletWindowId.hashCode());
        result = prime * result + ((this.resourceId == null) ? 0 : this.resourceId.hashCode());
        result = prime * result + ((this.windowState == null) ? 0 : this.windowState.hashCode());
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
        PortletUrlBuilder other = (PortletUrlBuilder) obj;
        if (this.cacheability == null) {
            if (other.cacheability != null)
                return false;
        }
        else if (!this.cacheability.equals(other.cacheability))
            return false;
        if (this.portletMode == null) {
            if (other.portletMode != null)
                return false;
        }
        else if (!this.portletMode.equals(other.portletMode))
            return false;
        if (this.portletWindowId == null) {
            if (other.portletWindowId != null)
                return false;
        }
        else if (!this.portletWindowId.equals(other.portletWindowId))
            return false;
        if (this.resourceId == null) {
            if (other.resourceId != null)
                return false;
        }
        else if (!this.resourceId.equals(other.resourceId))
            return false;
        if (this.windowState == null) {
            if (other.windowState != null)
                return false;
        }
        else if (!this.windowState.equals(other.windowState))
            return false;
        return true;
    }
}
