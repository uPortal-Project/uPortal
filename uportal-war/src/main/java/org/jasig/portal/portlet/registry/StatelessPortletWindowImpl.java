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
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.PortletWindowID;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.ParameterMap;

/**
 * Implementation of the {@link IPortletWindow} interface that tracks the current
 * state of the portlet.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class StatelessPortletWindowImpl implements IPortletWindow, PortletWindow {
    private static final long serialVersionUID = 1L;

    private final PortletDefinition portletDefinition;
    private final IPortletEntity portletEntity;
    private final IPortletWindowId portletWindowId;
    private final IPortletWindowId delegationParent;
    
    private Map<String, String[]> renderParameters = new ParameterMap();
    private Map<String, String[]> publicRenderParameters = new ParameterMap();
    private transient PortletMode portletMode = PortletMode.VIEW;
    private transient WindowState windowState = WindowState.NORMAL;
    private Integer expirationCache = null;
    
    /**
     * Creates a new PortletWindow with the default settings
     * 
     * @param portletWindowId The unique identifier for this PortletWindow
     * @param portletEntityId The unique identifier of the parent IPortletEntity
     * @param portletDefinition The pluto portlet descriptor level object (pluto calls it a definition)
     * @param delegationParent The ID of the PortletWindow delegating rendering to this portlet
     * @throws IllegalArgumentException if portletWindowId, or portletDefinition are null
     */
    public StatelessPortletWindowImpl(IPortletWindowId portletWindowId, IPortletEntity portletEntity, PortletDefinition portletDefinition, IPortletWindowId delegationParent) {
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        Validate.notNull(portletEntity, "portletEntity can not be null");
        Validate.notNull(portletDefinition, "portletDefinition can not be null");
        
        this.portletWindowId = portletWindowId;
        this.portletEntity = portletEntity;
        this.delegationParent = delegationParent;
        this.portletDefinition = portletDefinition;
    }
    
    /**
     * Creates a new PortletWindow with the default settings
     * 
     * @param portletWindowId The unique identifier for this PortletWindow
     * @param portletEntityId The unique identifier of the parent IPortletEntity
     * @param portletDefinition The pluto portlet descriptor level object (pluto calls it a definition)
     * @throws IllegalArgumentException if portletWindowId, or portletDefinition are null
     */
    public StatelessPortletWindowImpl(IPortletWindowId portletWindowId, IPortletEntity portletEntity, PortletDefinition portletDefinition) {
        this(portletWindowId, portletEntity, portletDefinition, null);
    }
    
    /**
     * Creates a new PortletWindow cloned from the passed IPortletWindow
     * 
     * @param portletWindowId The unique identifier for this PortletWindow
     * @param portletWindow The PortletWindow to clone settings from
     * @throws IllegalArgumentException if portletWindowId, or portletWindow are null
     */
    public StatelessPortletWindowImpl(IPortletWindowId portletWindowId, IPortletWindow portletWindow) {
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        Validate.notNull(portletWindow, "portletWindow can not be null");
        
        this.portletWindowId = portletWindowId;
        this.portletEntity = portletWindow.getPortletEntity();
        this.portletMode = portletWindow.getPortletMode();
        this.windowState = portletWindow.getWindowState();
        this.portletDefinition = portletWindow.getPlutoPortletWindow().getPortletDefinition();;
        this.delegationParent = portletWindow.getDelegationParent();
        
        Validate.notNull(this.portletEntity, "portletWindow.portletEntity can not be null");
        Validate.notNull(this.portletMode, "portletWindow.portletMode can not be null");
        Validate.notNull(this.windowState, "portletWindow.windowState can not be null");
        Validate.notNull(this.portletDefinition, "portletWindow.portletDefinition can not be null");
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletWindow#getId()
     */
    @Override
    public PortletWindowID getId() {
        return this.portletWindowId;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getPortletWindowId()
     */
    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindowId;
    }
    
    @Override
    public IPortletEntity getPortletEntity() {
        return this.portletEntity;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletWindow#getPortletMode()
     */
    @Override
    public PortletMode getPortletMode() {
        return this.portletMode;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletWindow#getWindowState()
     */
    @Override
    public WindowState getWindowState() {
        return this.windowState;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#setPortletMode(javax.portlet.PortletMode)
     */
    @Override
    public void setPortletMode(PortletMode mode){
        Validate.notNull(mode, "mode can not be null");
        this.portletMode = mode;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#setWindowState(javax.portlet.WindowState)
     */
    @Override
    public void setWindowState(WindowState state) {
        Validate.notNull(state, "state can not be null");
        this.windowState = state;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getRequestParameers()
     */
    @Override
    public Map<String, String[]> getRenderParameters() {
        return this.renderParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#setRequestParameters(java.util.Map)
     */
    @Override
    public void setRenderParameters(Map<String, String[]> renderParameters) {
        Validate.notNull(renderParameters, "renderParameters can not be null");
        this.renderParameters = renderParameters;
    }
    
    @Override
    public Map<String, String[]> getPublicRenderParameters() {
        return this.publicRenderParameters;
    }

    @Override
    public void setPublicRenderParameters(Map<String, String[]> publicRenderParameters) {
        Validate.notNull(publicRenderParameters, "publicRenderParameters can not be null");
        this.publicRenderParameters = publicRenderParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getExpirationCache()
     */
    @Override
    public Integer getExpirationCache() {
        return this.expirationCache;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#setExpirationCache(java.lang.Integer)
     */
    @Override
    public void setExpirationCache(Integer expirationCache) {
        this.expirationCache = expirationCache;
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getDelegationParent()
     */
    @Override
    public IPortletWindowId getDelegationParent() {
        return this.delegationParent;
    }

    @Override
    public PortletWindow getPlutoPortletWindow() {
        return this;
    }
    
    //********** Serializable Methods **********//

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.delegationParent == null) ? 0 : this.delegationParent.hashCode());
        result = prime * result + ((this.portletEntity == null) ? 0 : this.portletEntity.hashCode());
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
        StatelessPortletWindowImpl other = (StatelessPortletWindowImpl) obj;
        if (this.delegationParent == null) {
            if (other.delegationParent != null)
                return false;
        }
        else if (!this.delegationParent.equals(other.delegationParent))
            return false;
        if (this.portletEntity == null) {
            if (other.portletEntity != null)
                return false;
        }
        else if (!this.portletEntity.equals(other.portletEntity))
            return false;
        if (this.portletWindowId == null) {
            if (other.portletWindowId != null)
                return false;
        }
        else if (!this.portletWindowId.equals(other.portletWindowId))
            return false;
        return true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("portletWindowId", this.portletWindowId)
            .append("windowState", this.windowState)
            .append("portletMode", this.portletMode)
            .append("expirationCache", this.expirationCache)
            .append("renderParameters", this.renderParameters)
            .append("publicRenderParameters", this.publicRenderParameters)
            .append("delegationParent", this.delegationParent)
            .toString();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletWindow#getPortletDefinition()
     */
	@Override
	public PortletDefinition getPortletDefinition() {
		return this.portletDefinition;
	}
}
