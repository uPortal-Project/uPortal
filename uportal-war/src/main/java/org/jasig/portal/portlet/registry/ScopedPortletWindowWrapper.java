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
import org.apache.pluto.container.PortletWindowID;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Wrapper around another {@link IPortletWindow}. All setter methods write through directly to
 * the wrapped window. For the portletMode and windowState writes are stored in this class as well.
 * Getters read-through to the wrapped window except for the listed properties, those read from the
 * local reference if it has been set.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ScopedPortletWindowWrapper implements IPortletWindow {
    private static final long serialVersionUID = 1L;
    
    private final IPortletWindow portletWindow;
    
    private transient PortletMode portletMode;
    private transient WindowState windowState;
    
    public ScopedPortletWindowWrapper(IPortletWindow portletWindow) {
        Validate.notNull(portletWindow, "portletWindow should not be null");
        this.portletWindow = portletWindow;
    }

    @Override
    public WindowState getWindowState() {
        if (this.windowState != null) {
            return this.windowState;
        }
        return this.portletWindow.getWindowState();
    }

    @Override
    public void setWindowState(WindowState state) {
        this.portletWindow.setWindowState(state);
        this.windowState = state;
    }

    @Override
    public PortletMode getPortletMode() {
        if (this.portletMode != null) {
            return this.portletMode;
        }
        return this.portletWindow.getPortletMode();
    }

    @Override
    public void setPortletMode(PortletMode mode) {
        this.portletWindow.setPortletMode(mode);
        this.portletMode = mode;
    }
    
    

    @Override
    public Map<String, String[]> getPreviousPrivateRenderParameters() {
        return this.portletWindow.getPreviousPrivateRenderParameters();
    }

    @Override
    public void setPreviousPrivateRenderParameters(Map<String, String[]> requestParameters) {
        this.portletWindow.setPreviousPrivateRenderParameters(requestParameters);
    }

    @Override
    public Map<String, String[]> getPreviousPublicRenderParameters() {
        return this.portletWindow.getPreviousPublicRenderParameters();
    }

    @Override
    public void setPreviousPublicRenderParameters(Map<String, String[]> requestParameters) {
        this.portletWindow.setPreviousPublicRenderParameters(requestParameters);
    }

    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindow.getPortletWindowId();
    }

    @Override
    public PortletWindowID getId() {
        return this.portletWindow.getId();
    }

    @Override
    public IPortletEntityId getPortletEntityId() {
        return this.portletWindow.getPortletEntityId();
    }

    @Override
    public PortletDefinition getPortletDefinition() {
        return this.portletWindow.getPortletDefinition();
    }

    @Override
    public void setExpirationCache(Integer expirationCache) {
        this.portletWindow.setExpirationCache(expirationCache);
    }

    @Override
    public Integer getExpirationCache() {
        return this.portletWindow.getExpirationCache();
    }

    @Override
    public IPortletWindowId getDelegationParent() {
        return this.portletWindow.getDelegationParent();
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
}
