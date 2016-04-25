/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.rendering;

import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetParameterDescriptor;
import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.springframework.beans.factory.annotation.Required;

import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;


/**
 * Custom attribute source that can override the window state.
 *
 * This is intended for the JSON layout theme where it puts all the portlets
 * in minimized mode to reduce render cost.   It previously tried to set the
 * windowState on the PortletWindow object and then used a flag to disable
 * writing the window state to the DB.  Because the PortletWindow is
 * cached in the session, it would still break the layout for
 * each subsequent render in that session though.  New approach is to not
 * modify the PortletWindow and just customize the channel XML tag attributes
 * without ever updating the PortletWindow or persisting the windowState
 * change.
 *
 * See UP-4364
 *
 * @since 4.2
 *
 * @author Josh Helmer, jhelmer.unicon.net
 */
public class WindowStateAttributeSource extends PortletWindowAttributeSource {
    private WindowState windowState;


    public void setWindowState(final WindowState windowState) {
        this.windowState = windowState;
    }


    @Override
    protected WindowState getWindowState(HttpServletRequest request, IPortletWindow window) {
        if (windowState != null) {
            return windowState;
        }

        return super.getWindowState(request, window);
    }
}
