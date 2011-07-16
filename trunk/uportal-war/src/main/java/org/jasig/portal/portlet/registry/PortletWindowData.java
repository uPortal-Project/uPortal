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

package org.jasig.portal.portlet.registry;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindowDescriptor;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Serializable persistent portlet window data. This class MUST be thread safe.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class PortletWindowData implements IPortletWindowDescriptor, Serializable {
    private static final long serialVersionUID = 1L;
    
    private final IPortletEntityId portletEntityId;
    private final IPortletWindowId portletWindowId;
    private final IPortletWindowId delegationParentId;
    private volatile Map<String, String[]> renderParameters = Collections.emptyMap();
    private volatile Map<String, String[]> publicRenderParameters = Collections.emptyMap();
    private volatile transient PortletMode portletMode = PortletMode.VIEW;
    private volatile transient WindowState windowState = WindowState.NORMAL;
    private volatile Integer expirationCache = null;
    
    /**
     * @param portletWindowId The unique identifier for this PortletWindow
     * @param portletEntityId The unique identifier of the parent IPortletEntity
     * @param delegationParentId The delegation parent of this portlet, null allowed
     * @throws IllegalArgumentException if portletWindowId, or portletEntityId are null
     */
    public PortletWindowData(IPortletWindowId portletWindowId, IPortletEntityId portletEntityId, IPortletWindowId delegationParentId) {
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        this.portletWindowId = portletWindowId;
        this.portletEntityId = portletEntityId;
        this.delegationParentId = delegationParentId;
    }
    
    /**
     * @param portletWindowId The unique identifier for this PortletWindow
     * @param portletEntityId The unique identifier of the parent IPortletEntity
     * @throws IllegalArgumentException if portletWindowId, or portletEntityId are null
     */
    public PortletWindowData(IPortletWindowId portletWindowId, IPortletEntityId portletEntityId) {
        this(portletWindowId, portletEntityId, null);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindowData#getRenderParameters()
     */
    public Map<String, String[]> getRenderParameters() {
        return this.renderParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindowData#setRenderParameters(java.util.Map)
     */
    public void setRenderParameters(Map<String, String[]> renderParameters) {
        Validate.notNull(renderParameters, "renderParameters can not be null");
        this.renderParameters = renderParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindowData#getPublicRenderParameters()
     */
    public Map<String, String[]> getPublicRenderParameters() {
        return this.publicRenderParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindowData#setPublicRenderParameters(java.util.Map)
     */
    public void setPublicRenderParameters(Map<String, String[]> publicRenderParameters) {
        Validate.notNull(publicRenderParameters, "publicRenderParameters can not be null");
        this.publicRenderParameters = publicRenderParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindowData#getPortletMode()
     */
    public PortletMode getPortletMode() {
        return this.portletMode;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindowData#setPortletMode(javax.portlet.PortletMode)
     */
    public void setPortletMode(PortletMode portletMode) {
        Validate.notNull(portletMode, "PortletMode can not be null");
        this.portletMode = portletMode;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindowData#getWindowState()
     */
    public WindowState getWindowState() {
        return this.windowState;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindowData#setWindowState(javax.portlet.WindowState)
     */
    public void setWindowState(WindowState windowState) {
        Validate.notNull(windowState, "WindowState can not be null");
        this.windowState = windowState;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindowData#getExpirationCache()
     */
    public Integer getExpirationCache() {
        return this.expirationCache;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindowData#setExpirationCache(java.lang.Integer)
     */
    public void setExpirationCache(Integer expirationCache) {
        this.expirationCache = expirationCache;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindowData#getPortletEntityId()
     */
    @Override
    public IPortletEntityId getPortletEntityId() {
        return this.portletEntityId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindowData#getPortletWindowId()
     */
    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindowId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindowData#getDelegationParentId()
     */
    public IPortletWindowId getDelegationParentId() {
        return this.delegationParentId;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.delegationParentId == null) ? 0 : this.delegationParentId.hashCode());
        result = prime * result + ((this.portletEntityId == null) ? 0 : this.portletEntityId.hashCode());
        result = prime * result + ((this.portletWindowId == null) ? 0 : this.portletWindowId.hashCode());
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
        PortletWindowData other = (PortletWindowData) obj;
        if (this.delegationParentId == null) {
            if (other.delegationParentId != null)
                return false;
        }
        else if (!this.delegationParentId.equals(other.delegationParentId))
            return false;
        if (this.portletEntityId == null) {
            if (other.portletEntityId != null)
                return false;
        }
        else if (!this.portletEntityId.equals(other.portletEntityId))
            return false;
        if (this.portletWindowId == null) {
            if (other.portletWindowId != null)
                return false;
        }
        else if (!this.portletWindowId.equals(other.portletWindowId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletWindow [" +
                "portletWindowId=" + this.portletWindowId + ", " +
                "delegationParentId=" + this.delegationParentId + ", " +
                "portletMode=" + this.portletMode + ", " +
                "windowState=" + this.windowState + ", " +
                "expirationCache=" + this.expirationCache + ", " +
                "renderParameters=" + this.renderParameters + ", " +
                "publicRenderParameters=" + this.publicRenderParameters + ", " +
                "portletEntityId=" + this.portletEntityId + "]";
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(this.portletMode.toString());
        oos.writeObject(this.windowState.toString());
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        //Read & validate non-transient fields
        ois.defaultReadObject();
        
        if (this.portletWindowId == null) {
            throw new InvalidObjectException("portletWindowId can not be null");
        }
        if (this.portletEntityId == null) {
            throw new InvalidObjectException("portletEntityId can not be null");
        }
        
        //Read & validate transient fields
        final String portletModeStr = (String)ois.readObject();
        if (portletModeStr == null) {
            throw new InvalidObjectException("portletMode can not be null");
        }
        this.portletMode = PortletUtils.getPortletMode(portletModeStr);
        
        final String windowStateStr = (String)ois.readObject();
        if (windowStateStr == null) {
            throw new InvalidObjectException("windowState can not be null");
        }
        this.windowState = PortletUtils.getWindowState(windowStateStr);
    }
}
