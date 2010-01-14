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

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Extended version of {@link IPortletWindowRegistry} that handles transient portlet windows that only exist in object
 * form for the duration of a request.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @deprecated use {@link IPortletWindowRegistry} directly
 */
@Deprecated
public interface ITransientPortletWindowRegistry extends IPortletWindowRegistry {

    /**
     * Creates an IPortletWindowId for the specified string identifier
     * 
     * @param portletWindowId The string represenation of the portlet window ID.
     * @return The IPortletWindowId for the string
     * @throws IllegalArgumentException If portletWindowId is null
     */
    public IPortletWindowId createTransientPortletWindowId(HttpServletRequest request, IPortletWindowId sourcePortletWindowId);
    
    /**
     * @param request The current portal request
     * @param portletWindowId The window ID to check
     * @return true if the window id is for a transient window.
     */
    public boolean isTransient(HttpServletRequest request, IPortletWindowId portletWindowId);
}
