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

package org.jasig.portal.portlet.om;



/**
 * A portlet entity represents what a user subscribes to (adds to their layout) in
 * the portal object model.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletEntity {
    /**
     * @return The unique identifier for this portlet entity.
     */
    public IPortletEntityId getPortletEntityId();
    
    /**
     * @return The ID of the parent portlet defintion
     */
    public IPortletDefinitionId getPortletDefinitionId();
    
    /**
     * @return The subscribe ID for the channel underlying this entity.
     */
    public String getChannelSubscribeId();
    
    /**
     * @return The ID of the user this entity is for. 
     */
    public int getUserId();
    
    /**
     * @return The preferences for this portlet entity, will not be null.
     */
    public IPortletPreferences getPortletPreferences();
    
    /**
     * @param portletPreferences The preferences for this portlet entity.
     * @throws IllegalArgumentException If preferences is null.
     */
    public void setPortletPreferences(IPortletPreferences portletPreferences);
    
}
