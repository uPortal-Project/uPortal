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

import java.util.Collections;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.lang.Validate;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.PortletWindowID;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Implementation of the {@link IPortletWindow} that is backed by a
 * {@link PortletWindowData} object. Tracks changes both locally and
 * in the backing object.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class PortletWindowImpl implements IPortletWindow, PortletWindow {
    private static final long serialVersionUID = 1L;

    private final PortletDefinition portletDefinition;
    private final IPortletEntity portletEntity;
    private final PortletWindowData portletWindowData;
    
    private Map<String, String[]> renderParameters = null;
    private Map<String, String[]> publicRenderParameters = null;
    private PortletMode portletMode = null;
    private WindowState windowState = null;
    private Integer expirationCache = null;
    
    public PortletWindowImpl(
            PortletDefinition portletDefinition, 
            IPortletEntity portletEntity, 
            PortletWindowData portletWindowData) {
        
        Validate.notNull(portletDefinition);
        Validate.notNull(portletEntity);
        Validate.notNull(portletWindowData);
        
        this.portletDefinition = portletDefinition;
        this.portletEntity = portletEntity;
        this.portletWindowData = portletWindowData;
    }

    @Override
    public PortletWindowID getId() {
        return this.portletWindowData.getPortletWindowId();
    }

    @Override
    public PortletDefinition getPortletDefinition() {
        return this.portletDefinition;
    }

    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindowData.getPortletWindowId();
    }

    @Override
    public IPortletEntity getPortletEntity() {
        return this.portletEntity;
    }

    @Override
    public WindowState getWindowState() {
        if (this.windowState != null) {
            return this.windowState;
        }
        return this.portletWindowData.getWindowState();
    }

    @Override
    public void setWindowState(WindowState state) {
        Validate.notNull(state, "state can not be null");
        this.windowState = state;
        this.portletWindowData.setWindowState(state);
    }

    @Override
    public PortletMode getPortletMode() {
        if (this.portletMode != null) {
            return this.portletMode;
        }
        return this.portletWindowData.getPortletMode();
    }

    @Override
    public void setPortletMode(PortletMode mode) {
        Validate.notNull(mode, "mode can not be null");
        this.portletMode = mode;
        this.portletWindowData.setPortletMode(mode);
    }

    @Override
    public void setRenderParameters(Map<String, String[]> requestParameters) {
        requestParameters = Collections.unmodifiableMap(requestParameters);
        this.renderParameters = requestParameters;
        this.portletWindowData.setRenderParameters(requestParameters);
    }

    @Override
    public Map<String, String[]> getRenderParameters() {
        if (this.renderParameters != null) {
            return this.renderParameters;
        }
        return this.portletWindowData.getRenderParameters();
    }

    @Override
    public Map<String, String[]> getPublicRenderParameters() {
        if (this.publicRenderParameters != null) {
            return this.publicRenderParameters;
        }
        return this.portletWindowData.getPublicRenderParameters();
    }

    @Override
    public void setPublicRenderParameters(Map<String, String[]> requestParameters) {
        requestParameters = Collections.unmodifiableMap(requestParameters);
        this.publicRenderParameters = requestParameters;
        this.portletWindowData.setPublicRenderParameters(requestParameters);
        
    }

    @Override
    public void setExpirationCache(Integer expirationCache) {
        this.expirationCache = expirationCache;
        this.portletWindowData.setExpirationCache(expirationCache);
    }

    @Override
    public Integer getExpirationCache() {
        if (this.expirationCache != null) {
            return this.expirationCache;
        }
        return this.portletWindowData.getExpirationCache();
    }

    @Override
    public PortletWindow getPlutoPortletWindow() {
        return this;
    }

    @Override
    public IPortletWindowId getDelegationParent() {
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.portletWindowData == null) ? 0 : this.portletWindowData.hashCode());
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
        PortletWindowImpl other = (PortletWindowImpl) obj;
        if (this.portletWindowData == null) {
            if (other.portletWindowData != null)
                return false;
        }
        else if (!this.portletWindowData.equals(other.portletWindowData))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletWindowImpl [portletDefinition=" + this.portletDefinition + ", portletEntity="
                + this.portletEntity + ", portletWindowData=" + this.portletWindowData + ", renderParameters="
                + this.renderParameters + ", publicRenderParameters=" + this.publicRenderParameters + ", portletMode="
                + this.portletMode + ", windowState=" + this.windowState + ", expirationCache=" + this.expirationCache
                + "]";
    }
}
