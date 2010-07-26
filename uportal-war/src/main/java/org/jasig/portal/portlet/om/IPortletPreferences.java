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

import java.util.List;

/**
 * Holder (join) class for a List of portlet preferences
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletPreferences {
    /**
     * @return The List of PortletPreferences, will not be null
     */
    public List<IPortletPreference> getPortletPreferences();
    
    /**
     * @param portletPreferences The List of PortletPreferences.
     * @throws IllegalArgumentException If portletPreferences is null.
     */
    public void setPortletPreferences(List<IPortletPreference> portletPreferences);
}
