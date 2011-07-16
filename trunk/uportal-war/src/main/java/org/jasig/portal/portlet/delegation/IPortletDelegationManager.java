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

package org.jasig.portal.portlet.delegation;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.api.portlet.DelegationRequest;
import org.jasig.portal.portlet.om.IPortletWindowId;


/**
 * Provides some utility methods for dealing with delegate rendering of portlets
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletDelegationManager {
    /**
     * Set the {@link DelegationRequest} for the specified delegate portlet window. 
     * 
     * @param request The current request
     * @param delegatePortletWindowId The window ID of the delegate
     * @param delegationRequest The delegation request data
     */
    public void setDelegationRequest(HttpServletRequest request, IPortletWindowId delegatePortletWindowId, DelegationRequest delegationRequest);
    
    /**
     * Get the {@link DelegationRequest} for the specified delegate portlet window
     * 
     * @param request The current request
     * @param delegatePortletWindowId The window ID of the delegate
     * @return The delegation request data, null if none exists or none was set
     */
    public DelegationRequest getDelegationRequest(HttpServletRequest request, IPortletWindowId delegatePortletWindowId);
}
