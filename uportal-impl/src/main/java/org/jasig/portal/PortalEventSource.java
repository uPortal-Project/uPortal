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

package org.jasig.portal;

/**
 * Represents the source of the PortalEvent.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 * @deprecated IChannel rendering code will be replaced with portlet specific rendering code in a future release
 */
@Deprecated
public class PortalEventSource {

    /**
     * The user layout as source of events, for example control actuation events 
     * (button presses) and channel window manipulation events 
     * (minimize and maximize).
     */
    public static final PortalEventSource LAYOUT_GENERATED = 
        new PortalEventSource("layout");

    /**
     * Framework-generated events, such as sessions ending and unsubscription
     * from channels.
     */
    public static final PortalEventSource FRAMEWORK_GENERATED = 
        new PortalEventSource("framework");

    /** String representation of the type of event source. */
    private final String typeName;

    private PortalEventSource(String name) {
        this.typeName = name;
    }

    public String toString() {
        return this.typeName;
    }
    
    /**
     * Two PortalEventSources are equal if their typeNames are equal.
     * @param other an object for comparison
     * @return true if other is a PortalEventSource with the same typeName.
     */
    public boolean equals(Object other) {
        
        if (other == null)
            return false;
        
        if (! (other instanceof PortalEventSource))
            return false;
        
        PortalEventSource otherSource = (PortalEventSource) other;
        
        return this.typeName.equals(otherSource.typeName);
        
    }
}