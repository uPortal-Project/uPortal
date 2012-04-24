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

import java.util.Map;
import java.util.Set;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.jasig.portal.portlet.rendering.IPortletRenderer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;

/**
 * Utilities for portlets
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class PortletUtils {
    private static final Map<String, PortletMode> PORTLET_MODES = 
            ImmutableSortedMap.<String, PortletMode>orderedBy(String.CASE_INSENSITIVE_ORDER)
                .put(PortletMode.VIEW.toString(), PortletMode.VIEW)
                .put(PortletMode.EDIT.toString(), PortletMode.EDIT)
                .put(PortletMode.HELP.toString(), PortletMode.HELP)
                .put(IPortletRenderer.ABOUT.toString(), IPortletRenderer.ABOUT)
                .put(IPortletRenderer.CONFIG.toString(), IPortletRenderer.CONFIG)
                .build();
    
    private static final Map<String, WindowState> WINDOW_STATES =
            ImmutableSortedMap.<String, WindowState>orderedBy(String.CASE_INSENSITIVE_ORDER)
                .put(WindowState.NORMAL.toString(), WindowState.NORMAL)
                .put(WindowState.MAXIMIZED.toString(), WindowState.MAXIMIZED)
                .put(WindowState.MINIMIZED.toString(), WindowState.MINIMIZED)
                .put(IPortletRenderer.DASHBOARD.toString(), IPortletRenderer.DASHBOARD)
                .put(IPortletRenderer.DETACHED.toString(), IPortletRenderer.DETACHED)
                .put(IPortletRenderer.EXCLUSIVE.toString(), IPortletRenderer.EXCLUSIVE)
                .build();
    
    private static final Set<WindowState> TARGETED_WINDOW_STATES =
            ImmutableSet.<WindowState>builder()
            .add(WindowState.MAXIMIZED)
            .add(IPortletRenderer.DETACHED)
            .add(IPortletRenderer.EXCLUSIVE)
            .build();
                    
    private PortletUtils() {
    }
    
    /**
     * Checks if the specified window state is for a specifically targeted portlet
     * 
     * @param state The WindowState to check
     * @return true if the window state is targeted
     */
    public static boolean isTargetedWindowState(WindowState state) {
        return TARGETED_WINDOW_STATES.contains(state);
    }
    
    /**
     * Convert a String into a {@link PortletMode}
     */
    public static PortletMode getPortletMode(String mode) {
        if (mode == null) {
            return null;
        }
        
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
        
        final WindowState windowState = WINDOW_STATES.get(state);
        if (windowState != null) {
            return windowState;
        }
        
        return new WindowState(state);
    }
}
