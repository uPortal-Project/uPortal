/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlet.om;

import java.util.List;
import java.util.Map;
import javax.portlet.WindowState;
import org.apereo.portal.layout.om.IStylesheetDescriptor;

/**
 * A portlet entity represents what a user subscribes to (adds to their layout) in the portal object
 * model.
 *
 */
public interface IPortletEntity extends IPortletEntityDescriptor {
    /** @return The unique identifier for this portlet entity. */
    @Override
    public IPortletEntityId getPortletEntityId();

    /** @return The parent portlet defintion */
    public IPortletDefinition getPortletDefinition();

    /** @return The ID of the layout node this entity represents. */
    @Override
    public String getLayoutNodeId();

    /** @return The ID of the user this entity is for. */
    @Override
    public int getUserId();

    /**
     * @return A read-only Map of the WindowStates that have been set. The key is the {@link
     *     IStylesheetDescriptor#getId()} value.
     */
    public Map<Long, WindowState> getWindowStates();

    /** @return The persisted window state for the this portlet and stylesheet */
    public WindowState getWindowState(IStylesheetDescriptor stylesheetDescriptor);

    /** @param state The persisted window state for this portlet and stylesheet */
    public void setWindowState(IStylesheetDescriptor stylesheetDescriptor, WindowState state);

    /** @return The List of PortletPreferences, will not be null */
    public List<IPortletPreference> getPortletPreferences();

    /**
     * @param portletPreferences The List of PortletPreferences, null clears the preferences but
     *     actually sets an empty list
     * @return true if the portlet preferences changed
     */
    public boolean setPortletPreferences(List<IPortletPreference> portletPreferences);

    /**
     * @return A hash code created based on the userId, layoutId and parent {@link
     *     IPortletDefinition}
     */
    @Override
    public int hashCode();

    /**
     * Must compare correctly with any other {@link IPortletEntity} on {@link #getUserId()}, {@link
     * #getLayoutNodeId()}, and {@link #getPortletDefinition()}
     */
    @Override
    public boolean equals(Object o);
}
