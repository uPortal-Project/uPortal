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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.pluto.container.PortletWindowID;
import org.apache.pluto.container.om.portlet.PortletDefinition;
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
class PortletWindowImpl implements IPortletWindow {
    private static final long serialVersionUID = 1L;

    private final PortletDefinition portletDefinition;
    private final IPortletEntityId portletEntityId;
    private final IPortletWindowId portletWindowId;
    private final IPortletWindowId delegationParent;
    
    private Map<String, List<String>> requestParameters = new HashMap<String, List<String>>();
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
    public PortletWindowImpl(IPortletWindowId portletWindowId, IPortletEntityId portletEntityId, PortletDefinition portletDefinition, IPortletWindowId delegationParent) {
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        Validate.notNull(portletDefinition, "portletDefinition can not be null");
        
        this.portletWindowId = portletWindowId;
        this.portletEntityId = portletEntityId;
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
    public PortletWindowImpl(IPortletWindowId portletWindowId, IPortletEntityId portletEntityId, PortletDefinition portletDefinition) {
        this(portletWindowId, portletEntityId, portletDefinition, null);
    }
    
    /**
     * Creates a new PortletWindow cloned from the passed IPortletWindow
     * 
     * @param portletWindowId The unique identifier for this PortletWindow
     * @param portletWindow The PortletWindow to clone settings from
     * @throws IllegalArgumentException if portletWindowId, or portletWindow are null
     */
    public PortletWindowImpl(IPortletWindowId portletWindowId, IPortletWindow portletWindow) {
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        Validate.notNull(portletWindow, "portletWindow can not be null");
        
        this.portletWindowId = portletWindowId;
        this.portletEntityId = portletWindow.getPortletEntityId();
        this.portletMode = portletWindow.getPortletMode();
        this.windowState = portletWindow.getWindowState();
        this.portletDefinition = portletWindow.getPortletDefinition();;
        this.delegationParent = portletWindow.getDelegationParent();
        
        Validate.notNull(this.portletEntityId, "portletWindow.parentPortletEntityId can not be null");
        Validate.notNull(this.portletMode, "portletWindow.portletMode can not be null");
        Validate.notNull(this.windowState, "portletWindow.windowState can not be null");
        Validate.notNull(this.portletDefinition, "portletWindow.portletDefinition can not be null");
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletWindow#getId()
     */
    public PortletWindowID getId() {
        return this.portletWindowId;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getPortletWindowId()
     */
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindowId;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getParentPortletEntityId()
     */
    public IPortletEntityId getPortletEntityId() {
        return this.portletEntityId;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletWindow#getPortletMode()
     */
    public PortletMode getPortletMode() {
        return this.portletMode;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.PortletWindow#getWindowState()
     */
    public WindowState getWindowState() {
        return this.windowState;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#setPortletMode(javax.portlet.PortletMode)
     */
    public void setPortletMode(PortletMode mode){
        Validate.notNull(mode, "mode can not be null");
        this.portletMode = mode;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#setWindowState(javax.portlet.WindowState)
     */
    public void setWindowState(WindowState state) {
        Validate.notNull(state, "state can not be null");
        this.windowState = state;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getRequestParameers()
     */
    public Map<String, List<String>> getRequestParameters() {
        return this.requestParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#setRequestParameters(java.util.Map)
     */
    public void setRequestParameters(Map<String, List<String>> requestParameters) {
        this.requestParameters = requestParameters;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getExpirationCache()
     */
    public Integer getExpirationCache() {
        return this.expirationCache;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#setExpirationCache(java.lang.Integer)
     */
    public void setExpirationCache(Integer expirationCache) {
        this.expirationCache = expirationCache;
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletWindow#getDelegationParent()
     */
    public IPortletWindowId getDelegationParent() {
        return this.delegationParent;
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
        /*
        if (this.contextPath == null) {
            throw new InvalidObjectException("contextPath can not be null");
        }
        if (this.portletName == null) {
            throw new InvalidObjectException("portletName can not be null");
        }
        */
        
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
        IPortletWindow rhs = (IPortletWindow) object;
        return new EqualsBuilder()
            .append(this.portletWindowId, rhs.getId())
            /*.append(this.contextPath, rhs.getContextPath())
            .append(this.portletName, rhs.getPortletName()) */
            .append(this.windowState, rhs.getWindowState())
            .append(this.portletMode, rhs.getPortletMode())
            .append(this.expirationCache, rhs.getExpirationCache())
            .append(this.requestParameters, rhs.getRequestParameters())
            .append(this.delegationParent, rhs.getDelegationParent())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(1445247369, -1009176817)
            .append(this.portletWindowId)
           /* .append(this.contextPath)
            .append(this.portletName)*/
            .append(this.windowState)
            .append(this.portletMode)
            .append(this.expirationCache)
            .append(this.requestParameters)
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
            /*.append("contextPath", this.contextPath)
            .append("portletName", this.portletName)*/
            .append("windowState", this.windowState)
            .append("portletMode", this.portletMode)
            .append("expirationCache", this.expirationCache)
            .append("requestParameters", this.requestParameters)
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
