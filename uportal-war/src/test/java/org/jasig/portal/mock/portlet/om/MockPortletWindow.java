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

package org.jasig.portal.mock.portlet.om;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.PortletWindowID;
import org.apache.pluto.container.om.portlet.PortletDefinition;
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
public class MockPortletWindow implements IPortletWindow, PortletWindow {
    private static final long serialVersionUID = 1L;

    private IPortletEntity portletEntity;
    private IPortletWindowId portletWindowId;
    private String contextPath;
    private String portletName;
    private IPortletWindowId delegationParent;
    private PortletDefinition portletDefinition;
    
    private Map<String, String[]> previousPrivateRenderParameters = new ParameterMap();
    private Map<String, String[]> previousPublicRenderParameters = new ParameterMap();
    private transient PortletMode portletMode = PortletMode.VIEW;
    private transient WindowState windowState = WindowState.NORMAL;
    private Integer expirationCache = null;
    
    public MockPortletWindow() {
        this.portletWindowId = null;
        this.portletEntity = null;
        this.contextPath = null;
        this.portletName = null;
        this.delegationParent = null;
    }
    
    /**
     * Creates a new PortletWindow with the default settings
     */
    public MockPortletWindow(IPortletWindowId portletWindowId, IPortletEntity portletEntity, String contextPath, String portletName, IPortletWindowId delegateParent, PortletDefinition portletDefinition) {
        this.portletWindowId = portletWindowId;
        this.portletEntity = portletEntity;
        this.contextPath = contextPath;
        this.portletName = portletName;
        this.delegationParent = delegateParent;
        this.portletDefinition = portletDefinition;
    }
    /**
     * Creates a new PortletWindow with the default settings
     */
    public MockPortletWindow(IPortletWindowId portletWindowId, IPortletEntity portletEntity, String contextPath, String portletName, IPortletWindowId delegateParent) {
        this.portletWindowId = portletWindowId;
        this.portletEntity = portletEntity;
        this.contextPath = contextPath;
        this.portletName = portletName;
        this.delegationParent = delegateParent;
    }
    
    /**
     * Creates a new PortletWindow with the default settings
     */
    public MockPortletWindow(IPortletWindowId portletWindowId, IPortletEntity portletEntity, String contextPath, String portletName) {
        this(portletWindowId, portletEntity, contextPath, portletName, null);
    }
    
    /**
     * Creates a new PortletWindow cloned from the passed IPortletWindow
     */
    public MockPortletWindow(IPortletWindowId portletWindowId, IPortletWindow portletWindow) {
        this.portletWindowId = portletWindowId;
        this.portletEntity = portletWindow.getPortletEntity();
        //this.contextPath = portletWindow.getContextPath();
        //this.portletName = portletWindow.getPortletName();
        this.portletMode = portletWindow.getPortletMode();
        this.windowState = portletWindow.getWindowState();
    }

    @Override
    public IPortletEntity getPortletEntity() {
        return this.portletEntity;
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
    
    /* (non-Javadoc)
     * @see org.apache.pluto.PortletWindow#getContextPath()
     */
    public String getContextPath() {
        return this.contextPath;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletWindow#getPortletName()
     */
    public String getPortletName() {
        return this.portletName;
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
    public Map<String, String[]> getRequestParameers() {
        return this.previousPrivateRenderParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#setRequestParameters(java.util.Map)
     */
    @Override
    public void setRenderParameters(Map<String, String[]> requestParameters) {
        if (requestParameters == null) {
            this.previousPrivateRenderParameters = null;
        }
        else {
            this.previousPrivateRenderParameters = new ParameterMap(requestParameters);
        }
    }
    
    
    @Override
    public Map<String, String[]> getPublicRenderParameters() {
        return this.previousPublicRenderParameters;
    }

    @Override
    public void setPublicRenderParameters(Map<String, String[]> requestParameters) {
        if (requestParameters == null) {
            this.previousPublicRenderParameters = null;
        }
        else {
            this.previousPublicRenderParameters = new ParameterMap(requestParameters);
        }
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

    /**
     * @return the requestParameters
     */
    @Override
    public Map<String, String[]> getRenderParameters() {
        return previousPrivateRenderParameters;
    }

    /**
     * @param portletEntity the portletEntity to set
     */
    public void setPortletEntity(IPortletEntity portletEntity) {
        this.portletEntity = portletEntity;
    }

    /**
     * @param portletWindowId the portletWindowId to set
     */
    public void setPortletWindowId(IPortletWindowId portletWindowId) {
        this.portletWindowId = portletWindowId;
    }

    /**
     * @param contextPath the contextPath to set
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * @param portletName the portletName to set
     */
    public void setPortletName(String portletName) {
        this.portletName = portletName;
    }
    
    public void setDelegationParent(IPortletWindowId delegationParent) {
        this.delegationParent = delegationParent;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getDelegationParent()
     */
    @Override
    public IPortletWindowId getDelegationParentId() {
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
        if (this.contextPath == null) {
            throw new InvalidObjectException("contextPath can not be null");
        }
        if (this.portletName == null) {
            throw new InvalidObjectException("portletName can not be null");
        }
        if (this.previousPrivateRenderParameters == null) {
            throw new InvalidObjectException("requestParameters can not be null");
        }
        
        //Read & validate transient fields
        final String portletModeStr = (String)ois.readObject();
        if (portletModeStr == null) {
            throw new InvalidObjectException("portletMode can not be null");
        }
        this.portletMode = new PortletMode(portletModeStr);
        
        final String windowStateStr = (String)ois.readObject();
        if (windowStateStr == null) {
            throw new InvalidObjectException("windowState can not be null");
        }
        this.windowState = new WindowState(windowStateStr);
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IPortletWindow)) {
            return false;
        }
        MockPortletWindow rhs = (MockPortletWindow) object;
        return new EqualsBuilder()
            .append(this.portletWindowId, rhs.getId())
            .append(this.contextPath, rhs.getContextPath())
            .append(this.portletName, rhs.getPortletName())
            .append(this.windowState, rhs.getWindowState())
            .append(this.portletMode, rhs.getPortletMode())
            .append(this.expirationCache, rhs.getExpirationCache())
            .append(this.previousPrivateRenderParameters, rhs.getRenderParameters())
            .append(this.previousPublicRenderParameters, rhs.getPublicRenderParameters())
            .append(this.delegationParent, rhs.getDelegationParentId())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(1445247369, -1009176817)
            .append(this.portletWindowId)
            .append(this.contextPath)
            .append(this.portletName)
            .append(this.windowState)
            .append(this.portletMode)
            .append(this.expirationCache)
            .append(this.previousPrivateRenderParameters)
            .append(this.previousPublicRenderParameters)
            .append(this.delegationParent)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("portletWindowId", this.portletWindowId)
            .append("contextPath", this.contextPath)
            .append("portletName", this.portletName)
            .append("windowState", this.windowState)
            .append("portletMode", this.portletMode)
            .append("expirationCache", this.expirationCache)
            .append("previousPrivateRenderParameters", this.previousPrivateRenderParameters)
            .append("previousPublicRenderParameters", this.previousPublicRenderParameters)
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
