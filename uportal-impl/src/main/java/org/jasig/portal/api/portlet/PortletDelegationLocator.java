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

import javax.portlet.PortletRequest;

import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Creates {@link PortletDelegationDispatcher} instances that can be used to execute another portlet as a child. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface PortletDelegationLocator {
    /**
     * The PortletDelegationLocator instance will be available in the request under this key
     */
    public static final String PORTLET_DELECATION_LOCATOR_ATTR = PortletDelegationLocator.class.getName();
    
    /**
     * Creates a new portlet delegation dispatcher and corresponding portlet window
     * 
     * @param request The current request
     * @param fName The functional name of the portlet to create a dispatcher for
     * @return Null if no portlet exists for the specified fname
     */
    public PortletDelegationDispatcher createRequestDispatcher(PortletRequest request, String fName);
    
    /**
     * Creates a new portlet delegation dispatcher and corresponding portlet window
     * 
     * @param request The current request
     * @param portletDefinitionId The portlet definition id of the portlet to create a dispatcher for
     * @return The dispatcher
     */
    public PortletDelegationDispatcher createRequestDispatcher(PortletRequest request, IPortletDefinitionId portletDefinitionId);
    
    /**
     * Get an existing delegation dispatcher for an existing portlet window.
     * 
     * @param request The current request
     * @param portletWindowId The IPortletWindowId from the previously created dispatcher
     * @return The previously created dispatcher, null if no dispatcher exists for the ID
     */
    public PortletDelegationDispatcher getRequestDispatcher(PortletRequest request, IPortletWindowId portletWindowId);
}
