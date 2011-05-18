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

package org.jasig.portal.api.portlet;

import java.util.Collections;
import java.util.Map;

import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.jasig.portal.url.ParameterMap;

/**
 * The resulting state of the delegated action request
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DelegationActionResponse extends DelegationResponse {
    private final String redirectLocation;
    private final String renderUrlParamName;
    private final PortletMode portletMode;
    private final WindowState windowState;
    private final Map<String, String[]> renderParameters;

    
    public DelegationActionResponse(DelegateState delegateState, PortletMode portletMode, WindowState windowState,
            Map<String, String[]> renderParameters) {
        super(delegateState);
        
        this.portletMode = portletMode;
        this.windowState = windowState;
        if (renderParameters != null) {
            this.renderParameters = new ParameterMap(renderParameters);
        }
        else {
            this.renderParameters =  Collections.emptyMap();
        }
        
        this.redirectLocation = null;
        this.renderUrlParamName = null;
    }
    
    public DelegationActionResponse(DelegateState delegateState, String redirectLocation, String renderUrlParamName) {
        super(delegateState);
        this.redirectLocation = redirectLocation;
        this.renderUrlParamName = renderUrlParamName;

        this.portletMode = null;
        this.windowState = null;
        this.renderParameters = Collections.emptyMap();
    }

    /**
     * @return The render url parameter name specified in {@link ActionResponse#sendRedirect(String, String)}, null if no redirect was sent
     */
    public String getRenderUrlParamName() {
        return renderUrlParamName;
    }

    /**
     * @return The url specified in {@link ActionResponse#sendRedirect(String)}, null if no redirect was sent
     */
    public String getRedirectLocation() {
        return this.redirectLocation;
    }
    
    public PortletMode getPortletMode() {
        return portletMode;
    }

    public WindowState getWindowState() {
        return windowState;
    }

    public Map<String, String[]> getRenderParameters() {
        //Have to clone to make sure the value arrays don't get modified
        return new ParameterMap(this.renderParameters);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((portletMode == null) ? 0 : portletMode.hashCode());
        result = prime * result + ((redirectLocation == null) ? 0 : redirectLocation.hashCode());
        result = prime * result + ((renderParameters == null) ? 0 : renderParameters.hashCode());
        result = prime * result + ((renderUrlParamName == null) ? 0 : renderUrlParamName.hashCode());
        result = prime * result + ((windowState == null) ? 0 : windowState.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DelegationActionResponse other = (DelegationActionResponse) obj;
        if (portletMode == null) {
            if (other.portletMode != null)
                return false;
        }
        else if (!portletMode.equals(other.portletMode))
            return false;
        if (redirectLocation == null) {
            if (other.redirectLocation != null)
                return false;
        }
        else if (!redirectLocation.equals(other.redirectLocation))
            return false;
        if (renderParameters == null) {
            if (other.renderParameters != null)
                return false;
        }
        else if (!renderParameters.equals(other.renderParameters))
            return false;
        if (renderUrlParamName == null) {
            if (other.renderUrlParamName != null)
                return false;
        }
        else if (!renderUrlParamName.equals(other.renderUrlParamName))
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
        return "DelegationActionResponse [redirectLocation=" + redirectLocation + ", renderUrlParamName="
                + renderUrlParamName + ", portletMode=" + portletMode + ", windowState=" + windowState
                + ", renderParameters=" + renderParameters + ", getDelegateState()=" + getDelegateState() + "]";
    }
}
