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

package org.jasig.portal.portlet;

import java.util.Locale;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.jasig.portal.portlet.rendering.IPortletRenderer;

/**
 * Utilities for portlets
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class PortletUtils {
    private static final PortletMode[] PORTLET_MODES = {
        PortletMode.VIEW, PortletMode.EDIT, PortletMode.HELP, 
        IPortletRenderer.ABOUT, IPortletRenderer.CONFIG};
    
    private static final WindowState[] WINDOW_STATES = {
        WindowState.NORMAL, WindowState.MAXIMIZED, WindowState.MINIMIZED, 
        IPortletRenderer.DETACHED, IPortletRenderer.EXCLUSIVE};
    
    private PortletUtils() {
    }
    
    /**
     * Convert a String into a {@link PortletMode}
     */
    public static PortletMode getPortletMode(String mode) {
        if (mode == null) {
            return null;
        }
        
        //Same thing new PortletMode(String) does internally 
        mode = mode.toLowerCase(Locale.ENGLISH);
        
        for (final PortletMode portletMode : PORTLET_MODES) {
            if (portletMode.toString().equals(mode)) {
                return portletMode;
            }
        }
        
        return new PortletMode(mode);
    }
    
    /**
     * Convert a String into a {@link WindowState}
     */
    public static WindowState getWindowState(String state) {
        if (state == null) {
            return null;
        }
        
        //Same thing new WindowState(String) does internally 
        state = state.toLowerCase(Locale.ENGLISH);
        
        for (final WindowState windowState : WINDOW_STATES) {
            if (windowState.toString().equals(state)) {
                return windowState;
            }
        }
        
        return new WindowState(state);
    }
}
