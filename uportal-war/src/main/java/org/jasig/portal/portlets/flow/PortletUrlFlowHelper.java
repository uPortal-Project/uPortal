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

package org.jasig.portal.portlets.flow;

import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import org.jasig.portal.portlet.PortletUtils;
import org.springframework.webflow.context.ExternalContext;

/**
 * Helper methods to allow setting window states and portlet modes during flow exections
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletUrlFlowHelper {
    public void setWindowState(ExternalContext externalContext, String windowState) {
        final ActionResponse actionResponse;
        try {
            actionResponse = (ActionResponse)externalContext.getNativeResponse();
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException("setWindowState can only be called during an action request", e);
        }
        
        try {
            actionResponse.setWindowState(PortletUtils.getWindowState(windowState));
        }
        catch (WindowStateException e) {
            throw new IllegalArgumentException("The specified WindowState '" + windowState + "' is not valid", e);
        }
    }

    public void setPortletMode(ExternalContext externalContext, String portletMode) {
        final ActionResponse actionResponse;
        try {
            actionResponse = (ActionResponse)externalContext.getNativeResponse();
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException("setPortletMode can only be called during an action request", e);
        }
        
        try {
            actionResponse.setPortletMode(PortletUtils.getPortletMode(portletMode));
        }
        catch (PortletModeException e) {
            throw new IllegalArgumentException("The specified PortletMode '" + portletMode + "' is not valid", e);
        }
    }
}
