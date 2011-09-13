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
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.jasig.portal.portlet.rendering.IPortletRenderer;

import com.google.common.collect.ImmutableMap;

/**
 * Utilities for portlets
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class PortletUtils {
    private static final Map<String, PortletMode> PORTLET_MODES = 
            ImmutableMap.of(
                    PortletMode.VIEW.toString(), PortletMode.VIEW,
                    PortletMode.EDIT.toString(), PortletMode.EDIT,
                    PortletMode.HELP.toString(), PortletMode.HELP,
                    IPortletRenderer.ABOUT.toString(), IPortletRenderer.ABOUT,
                    IPortletRenderer.CONFIG.toString(), IPortletRenderer.CONFIG);
    
    private static final Map<String, WindowState> WINDOW_STATES = 
            ImmutableMap.of(
                    WindowState.NORMAL.toString(), WindowState.NORMAL,
                    WindowState.MAXIMIZED.toString(), WindowState.MAXIMIZED,
                    WindowState.MINIMIZED.toString(), WindowState.MINIMIZED,
                    IPortletRenderer.DETACHED.toString(), IPortletRenderer.DETACHED,
                    IPortletRenderer.EXCLUSIVE.toString(), IPortletRenderer.EXCLUSIVE);
                    
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
        
        final PortletMode portletMode = PORTLET_MODES.get(mode);
        if (portletMode != null) {
            return portletMode;
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

        final WindowState windowState = WINDOW_STATES.get(state);
        if (windowState != null) {
            return windowState;
        }
        
        return new WindowState(state);
    }
}
