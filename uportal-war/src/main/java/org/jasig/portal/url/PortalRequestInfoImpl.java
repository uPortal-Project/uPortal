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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jasig.portal.portlet.om.IPortletWindowId;

import com.google.common.base.Preconditions;


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
    private final Object readOnlySync = new Object();
    private boolean readOnly = false;
    
    private String targetedLayoutNodeId;
    private IPortletWindowId targetedPortletWindowId;
    private Map<IPortletWindowId, PortletRequestInfoImpl> portletRequestInfo = new LinkedHashMap<IPortletWindowId, PortletRequestInfoImpl>();
    private UrlState urlState = UrlState.NORMAL;
    private UrlType urlType = UrlType.RENDER;
    private Map<String, List<String>> portalParameters = new LinkedHashMap<String, List<String>>();

    
    public void makeReadOnly() {
        synchronized (readOnlySync) {
            if (readOnly) {
                return;
            }
            
            //Make all the PortletRequestInfoImpl intances read-only
            for (final PortletRequestInfoImpl portletRequestInfo : this.portletRequestInfo.values()) {
                portletRequestInfo.makeReadOnly();
            }
            this.portletRequestInfo = Collections.unmodifiableMap(this.portletRequestInfo);
            
            for (final Entry<String, List<String>> paramEntry : this.portalParameters.entrySet()) {
                paramEntry.setValue(Collections.unmodifiableList(paramEntry.getValue()));
            }
            this.portalParameters = Collections.unmodifiableMap(this.portalParameters);
            
            readOnly = true;
        }
    }
    
    private void checkReadOnly() {
        if (readOnly) {
            throw new UnsupportedOperationException("makeReadOnly has been called, this object is in read-only mode");
        }
    }

    @Override
    public String getTargetedLayoutNodeId() {
        return this.targetedLayoutNodeId;
    }
    public void setTargetedLayoutNodeId(String targetedLayoutNodeId) {
        this.checkReadOnly();
        this.targetedLayoutNodeId = targetedLayoutNodeId;
    }

    @Override
    public IPortletWindowId getTargetedPortletWindowId() {
        return this.targetedPortletWindowId;
    }
    public void setTargetedPortletWindowId(IPortletWindowId targetedPortletWindowId) {
        this.checkReadOnly();
        this.targetedPortletWindowId = targetedPortletWindowId;
    }
    
    @Override
    public IPortletRequestInfo getTargetedPortletRequestInfo() {
        if (this.targetedPortletWindowId == null) {
            return null;
        }
        
        if (this.readOnly) {
            //Could in theory return null?
            return this.portletRequestInfo.get(this.targetedPortletWindowId);
        }
        
        return this.getPortletRequestInfo(this.targetedPortletWindowId);
    }

    @Override
    public Map<IPortletWindowId, PortletRequestInfoImpl> getPortletRequestInfoMap() {
        return this.portletRequestInfo;
    }
    @Override
    public PortletRequestInfoImpl getPortletRequestInfo(IPortletWindowId portletWindowId) {
        PortletRequestInfoImpl portletRequestInfo = this.portletRequestInfo.get(portletWindowId);
        if (readOnly || portletRequestInfo != null) {
            //If read only return null is ok
            return portletRequestInfo;
        }
        
        portletRequestInfo = new PortletRequestInfoImpl(portletWindowId, this);
        this.portletRequestInfo.put(portletWindowId, portletRequestInfo);
        return portletRequestInfo;
    }

    @Override
    public UrlState getUrlState() {
        return this.urlState;
    }
    public void setUrlState(UrlState urlState) {
        this.checkReadOnly();
        Preconditions.checkNotNull(urlState, "Cannot set a null UrlState");
        this.urlState = urlState;
    }

    @Override
    public UrlType getUrlType() {
        return this.urlType;
    }
    public void setUrlType(UrlType urlType) {
        this.checkReadOnly();
        Preconditions.checkNotNull(urlType, "Cannot set a null UrlType");
        this.urlType = urlType;
    }

    @Override
    public Map<String, List<String>> getPortalParameters() {
        return this.portalParameters;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.targetedLayoutNodeId == null) ? 0 : this.targetedLayoutNodeId.hashCode());
        result = prime * result
                + ((this.targetedPortletWindowId == null) ? 0 : this.targetedPortletWindowId.hashCode());
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
        PortalRequestInfoImpl other = (PortalRequestInfoImpl) obj;
        if (this.targetedLayoutNodeId == null) {
            if (other.targetedLayoutNodeId != null)
                return false;
        }
        else if (!this.targetedLayoutNodeId.equals(other.targetedLayoutNodeId))
            return false;
        if (this.targetedPortletWindowId == null) {
            if (other.targetedPortletWindowId != null)
                return false;
        }
        else if (!this.targetedPortletWindowId.equals(other.targetedPortletWindowId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortalRequestInfoImpl [targetedLayoutNodeId=" + this.targetedLayoutNodeId
                + ", targetedPortletWindowId=" + this.targetedPortletWindowId + ", urlState=" + this.urlState
                + ", urlType=" + this.urlType + ", portalParameters=" + this.portalParameters + "]";
    }
}
