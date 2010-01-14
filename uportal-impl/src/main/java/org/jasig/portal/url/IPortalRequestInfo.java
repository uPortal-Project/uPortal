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

package org.jasig.portal.url;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Provides information about the portal request.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalRequestInfo {
    /**
     * @return The state rendered by the URL
     */
    public UrlState getUrlState();
    
    /**
     * @return The layout node being targeted by the request. If the request isn't targeting a particular layout node null is returned.
     */
    public String getTargetedLayoutNodeId();
    
    /**
     * @return The channel being targeted by the request. If the request isn't targeting a particular channel null is returned.
     */
    public String getTargetedChannelSubscribeId();
    
    /**
     * @return The portlet window being targeted by the request, If the request isn't targeting a particular channel null is returned.
     */
    public IPortletWindowId getTargetedPortletWindowId();
    
    /**
     * @return true if the request represents an action, false if it represents a render.
     */
    public boolean isAction();
}
