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

import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.lang.Validate;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.PortletWindowID;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Implementation of the {@link IPortletWindow} interface that tracks the current
 * state of the portlet.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class StatelessPortletWindowImpl implements IPortletWindow, PortletWindow {
    private final PortletDefinition portletDefinition;
    private final IPortletEntity portletEntity;
    private final PortletWindowData portletWindowData;
    
    /**
     * Creates a new PortletWindow with the default settings
     * 
     * @param portletWindowData The persistent portlet window data
     * @param portletEntity The parent IPortletEntity
     * @param portletDefinition The pluto portlet descriptor level object (pluto calls it a definition)
     * @throws IllegalArgumentException if any parameters are null
     */
    public StatelessPortletWindowImpl(PortletWindowData portletWindowData, IPortletEntity portletEntity, PortletDefinition portletDefinition) {
        Validate.notNull(portletWindowData, "portletWindowData can not be null");
        Validate.notNull(portletEntity, "portletEntity can not be null");
        Validate.notNull(portletDefinition, "portletDefinition can not be null");
        
        this.portletWindowData = portletWindowData;
        this.portletEntity = portletEntity;
        this.portletDefinition = portletDefinition;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletWindow#getId()
     */
    @Override
    public PortletWindowID getId() {
        return this.portletWindowData.getPortletWindowId();
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getPortletWindowId()
     */
    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindowData.getPortletWindowId();
    }
    
    @Override
    public IPortletEntityId getPortletEntityId() {
        return this.portletEntity.getPortletEntityId();
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
        return this.portletWindowData.getPortletMode();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletWindow#getWindowState()
     */
    @Override
    public WindowState getWindowState() {
        return this.portletWindowData.getWindowState();
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#setPortletMode(javax.portlet.PortletMode)
     */
    @Override
    public void setPortletMode(PortletMode mode){
        this.portletWindowData.setPortletMode(mode);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#setWindowState(javax.portlet.WindowState)
     */
    @Override
    public void setWindowState(WindowState state) {
        this.portletWindowData.setWindowState(state);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getRequestParameers()
     */
    @Override
    public Map<String, String[]> getRenderParameters() {
        return this.portletWindowData.getRenderParameters();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#setRequestParameters(java.util.Map)
     */
    @Override
    public void setRenderParameters(Map<String, String[]> renderParameters) {
        this.portletWindowData.setRenderParameters(renderParameters);
    }
    
    @Override
    public Map<String, String[]> getPublicRenderParameters() {
        return this.portletWindowData.getPublicRenderParameters();
    }

    @Override
    public void setPublicRenderParameters(Map<String, String[]> publicRenderParameters) {
        this.portletWindowData.setPublicRenderParameters(publicRenderParameters);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getExpirationCache()
     */
    @Override
    public Integer getExpirationCache() {
        return this.portletWindowData.getExpirationCache();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#setExpirationCache(java.lang.Integer)
     */
    @Override
    public void setExpirationCache(Integer expirationCache) {
        this.portletWindowData.setExpirationCache(expirationCache);
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getDelegationParent()
     */
    @Override
    public IPortletWindowId getDelegationParentId() {
        return this.portletWindowData.getDelegationParentId();
    }

    @Override
    public PortletWindow getPlutoPortletWindow() {
        return this;
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletWindow#getPortletDefinition()
     */
	@Override
	public PortletDefinition getPortletDefinition() {
		return this.portletDefinition;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.portletEntity == null) ? 0 : this.portletEntity.hashCode());
        result = prime * result + ((this.portletWindowData.getPortletWindowId() == null) ? 0 : this.portletWindowData.getPortletWindowId().hashCode());
        result = prime * result + ((this.portletWindowData.getDelegationParentId() == null) ? 0 : this.portletWindowData.getDelegationParentId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!IPortletWindow.class.isAssignableFrom(obj.getClass()))
            return false;
        final IPortletWindow other = (IPortletWindow) obj;
        if (this.portletEntity == null) {
            if (other.getPortletEntity() != null)
                return false;
        }
        else if (!this.portletEntity.equals(other.getPortletEntity()))
            return false;
        if (this.portletWindowData.getDelegationParentId() == null) {
            if (other.getDelegationParentId() != null)
                return false;
        }
        else if (!this.portletWindowData.getDelegationParentId().equals(other.getDelegationParentId()))
            return false;
        if (this.portletWindowData.getPortletWindowId() == null) {
            if (other.getPortletWindowId() != null)
                return false;
        }
        else if (!this.portletWindowData.getPortletWindowId().equals(other.getPortletWindowId()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletWindow [" +
        		"portletWindowId=" + this.portletWindowData.getPortletWindowId() + ", " +
        		"delegationParentId=" + this.portletWindowData.getDelegationParentId() + ", " +
        		"portletMode=" + this.portletWindowData.getPortletMode() + ", " +
        		"windowState=" + this.portletWindowData.getWindowState() + ", " +
        		"expirationCache=" + this.portletWindowData.getExpirationCache() + ", " +
        		"renderParameters=" + this.portletWindowData.getRenderParameters() + ", " +
        		"publicRenderParameters=" + this.portletWindowData.getPublicRenderParameters() + ", " +
				"portletEntity=" + this.portletEntity + "]";
    }
}
