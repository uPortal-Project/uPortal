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
package org.jasig.portal.portlet.container.cache;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Key for publicly scoped portlet data
 */
public class PublicPortletCacheKey implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final IPortletDefinitionId portletDefinitionId;
    private final Map<String, String[]> parameters;
    private final boolean renderHeader;
    private final String resourceId;
    private final Locale locale;
    private final String windowState;
    private final String portletMode;
    
    private final int hash;

    static PublicPortletCacheKey createPublicPortletRenderCacheKey(IPortletDefinitionId portletDefinitionId, IPortletWindow portletWindow,
            Locale locale) {
        return new PublicPortletCacheKey(portletDefinitionId, portletWindow, false, null, locale);
    }

    static PublicPortletCacheKey createPublicPortletRenderHeaderCacheKey(IPortletDefinitionId portletDefinitionId, IPortletWindow portletWindow,
            Locale locale) {
        return new PublicPortletCacheKey(portletDefinitionId, portletWindow, true, null, locale);
    }

    static PublicPortletCacheKey createPublicPortletResourceCacheKey(IPortletDefinitionId portletDefinitionId, IPortletWindow portletWindow,
            String resourceId, Locale locale) {
        if (resourceId == null) {
            resourceId = "";
        }
        return new PublicPortletCacheKey(portletDefinitionId, portletWindow, false, resourceId, locale);
    }
    
    private PublicPortletCacheKey(IPortletDefinitionId portletDefinitionId, IPortletWindow portletWindow,
            boolean renderHeader, String resourceId, Locale locale) {
        this.portletDefinitionId = portletDefinitionId;
        this.parameters = portletWindow.getRenderParameters();
        this.renderHeader = renderHeader;
        this.resourceId = resourceId;
        this.windowState = portletWindow.getWindowState().toString();
        this.portletMode = portletWindow.getPortletMode().toString();
        this.locale = locale;
        
        this.hash = internalHashCode();
    }
    
    public IPortletDefinitionId getPortletDefinitionId() {
        return portletDefinitionId;
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public boolean isRenderHeader() {
        return renderHeader;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getWindowState() {
        return windowState;
    }

    public String getPortletMode() {
        return portletMode;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }
    
    private int internalHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((locale == null) ? 0 : locale.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((portletDefinitionId == null) ? 0 : portletDefinitionId.hashCode());
        result = prime * result + ((portletMode == null) ? 0 : portletMode.hashCode());
        result = prime * result + (renderHeader ? 1231 : 1237);
        result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
        result = prime * result + ((windowState == null) ? 0 : windowState.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PublicPortletCacheKey other = (PublicPortletCacheKey) obj;
        if (locale == null) {
            if (other.locale != null)
                return false;
        }
        else if (!locale.equals(other.locale))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        }
        else if (!parameters.equals(other.parameters))
            return false;
        if (portletDefinitionId == null) {
            if (other.portletDefinitionId != null)
                return false;
        }
        else if (!portletDefinitionId.equals(other.portletDefinitionId))
            return false;
        if (portletMode == null) {
            if (other.portletMode != null)
                return false;
        }
        else if (!portletMode.equals(other.portletMode))
            return false;
        if (renderHeader != other.renderHeader)
            return false;
        if (resourceId == null) {
            if (other.resourceId != null)
                return false;
        }
        else if (!resourceId.equals(other.resourceId))
            return false;
        if (windowState == null) {
            if (other.windowState != null)
                return false;
        }
        else if (!windowState.equals(other.windowState))
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (renderHeader) {
            return "PublicPortletRenderHeaderCacheKey [portletDefinitionId=" + portletDefinitionId + ", parameters=" + parameters
                    + ", locale=" + locale + ", windowState=" + windowState + ", portletMode=" + portletMode + "]";
        }
        if (resourceId != null) {
            return "PublicPortletResourceCacheKey [portletDefinitionId=" + portletDefinitionId + ", parameters=" + parameters
                    + ", resourceId=" + resourceId + ", locale=" + locale + ", windowState=" + windowState + ", portletMode="
                    + portletMode + "]";
        }
        return "PublicPortletRenderCacheKey [portletDefinitionId=" + portletDefinitionId + ", parameters=" + parameters
                + ", locale=" + locale + ", windowState=" + windowState + ", portletMode=" + portletMode + "]";
    }
    
    
}