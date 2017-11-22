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

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a state change in a portlet's lifecycle. Lifecycle states are hierarchical, starting
 * at the bottom with <code>CREATED</code> and ending with <code>MAINTENANCE</code> at the top.
 * Setting a portlet's current lifecycle state clears all entries at or above the specified state,
 * and creates a new entry -- with the current user's Id and the current data -- at the specified
 * state.
 */
public interface IPortletLifecycleEntry extends Comparable<IPortletLifecycleEntry>, Serializable {

    int getUserId();

    PortletLifecycleState getLifecycleState();

    Date getDate();
}
