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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.PortletMode;
import javax.portlet.ResourceURL;
import javax.portlet.WindowState;

import org.jasig.portal.portlet.om.IPortletWindowId;

import com.google.common.base.Preconditions;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
class PortletRequestInfoImpl implements IPortletRequestInfo {
    private final Object readOnlySync = new Object();
    
    private final IPortletWindowId targetWindowId;
    private final IPortalRequestInfo portalRequestInfo;
    
    private volatile boolean readOnly = false;
    private Map<String, List<String>> portletParameters = new LinkedHashMap<String, List<String>>();
    private WindowState windowState;
    private PortletMode portletMode;
    private IPortletWindowId delegateParentWindowId;
    private String resourceId;
    private String cacheability = ResourceURL.PAGE;
    
    public PortletRequestInfoImpl(IPortletWindowId targetWindowId, IPortalRequestInfo portalRequestInfo) {
        this.targetWindowId = targetWindowId;
        this.portalRequestInfo = portalRequestInfo;
    }
    
    public void makeReadOnly() {
        synchronized (readOnlySync) {
            if (readOnly) {
                return;
            }
            
            for (final Entry<String, List<String>> paramEntry : this.portletParameters.entrySet()) {
                paramEntry.setValue(Collections.unmodifiableList(paramEntry.getValue()));
            }
            
            this.portletParameters = Collections.unmodifiableMap(this.portletParameters);
            
            readOnly = true;
        }
    }
    
    private void checkReadOnly() {
        if (readOnly) {
            throw new UnsupportedOperationException("makeReadOnly has been called, this object is in read-only mode");
        }
    }

    @Override
    public Map<String, List<String>> getPortletParameters() {
        return this.portletParameters;
    }

    @Override
	public IPortletWindowId getDelegateParentWindowId() {
        return delegateParentWindowId;
    }

    public void setDelegateParentWindowId(IPortletWindowId delegateParentWindowId) {
        this.checkReadOnly();
        this.delegateParentWindowId = delegateParentWindowId;
    }

    @Override
    public WindowState getWindowState() {
        return this.windowState;
    }

    public void setWindowState(WindowState windowState) {
        this.checkReadOnly();
        this.windowState = windowState;
    }

    @Override
    public PortletMode getPortletMode() {
        return this.portletMode;
    }

    public void setPortletMode(PortletMode portletMode) {
        this.checkReadOnly();
        this.portletMode = portletMode;
    }
    
	@Override
    public String getResourceId() {
	    final UrlType urlType = this.portalRequestInfo.getUrlType();
	    Preconditions.checkArgument(urlType == UrlType.RESOURCE, "UrlType must be %s but was %s", UrlType.RESOURCE, urlType);
		return resourceId;
	}

	public void setResourceId(String resourceId) {
	    this.checkReadOnly();
		this.resourceId = resourceId;
	}

	@Override
    public String getCacheability() {
	    final UrlType urlType = this.portalRequestInfo.getUrlType();
        Preconditions.checkArgument(urlType == UrlType.RESOURCE, "UrlType must be %s but was %s", UrlType.RESOURCE, urlType);
        return cacheability;
	}

	public void setCacheability(String cacheability) {
	    this.checkReadOnly();
		this.cacheability = cacheability;
	}

    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.targetWindowId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.targetWindowId == null) ? 0 : this.targetWindowId.hashCode());
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
        PortletRequestInfoImpl other = (PortletRequestInfoImpl) obj;
        if (this.targetWindowId == null) {
            if (other.targetWindowId != null)
                return false;
        }
        else if (!this.targetWindowId.equals(other.targetWindowId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletRequestInfoImpl [targetWindowId=" + this.targetWindowId + ", portletParameters="
                + this.portletParameters + ", windowState=" + this.windowState + ", portletMode=" + this.portletMode
                + ", resourceId=" + this.resourceId + ", cacheability=" + this.cacheability + "]";
    }
}
