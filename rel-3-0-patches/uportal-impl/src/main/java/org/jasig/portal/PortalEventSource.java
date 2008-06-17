/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * Represents the source of the PortalEvent.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
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