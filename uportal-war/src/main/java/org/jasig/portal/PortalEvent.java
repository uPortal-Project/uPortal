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
 * Events which may be arguments to the IChannel receiveEvent() method.
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class PortalEvent {
    
  
    // framework-generated events
    
    public static final int SESSION_DONE = 1;
    public static final int UNSUBSCRIBE = 2;

    // layout-generated events
    public static final int EDIT_BUTTON_EVENT = 3;
    public static final int HELP_BUTTON_EVENT = 4;
    public static final int ABOUT_BUTTON_EVENT = 5;
    public static final int DETACH_BUTTON_EVENT = 6;
    public static final int MINIMIZE_EVENT = 7;
    public static final int MAXIMIZE_EVENT = 8;
    public static final int NORMAL_EVENT = 9;
    
    
  /**
   * The framework-generated event that is broadcast to
   * channels that were used for a user session which is now ending.
   * Typical usage is to trigger state cleanup in channels that are not
   * user-session-scoped.
   */
  public static final PortalEvent SESSION_DONE_EVENT = 
      new PortalEvent(SESSION_DONE, "sessionDone", 
              PortalEventSource.FRAMEWORK_GENERATED);

  /**
   * The framework-generated event that is sent to
   * a channel when the user unsubscribes from that channel.
   */
  public static final PortalEvent UNSUBSCRIBE_EVENT = 
      new PortalEvent(UNSUBSCRIBE, "unsubscribe", 
              PortalEventSource.FRAMEWORK_GENERATED);

  /**
   * The layout-generated event that is sent to
   * a channel when the user actuates its edit control.
   */
  public static final PortalEvent EDIT_BUTTON = 
      new PortalEvent(EDIT_BUTTON_EVENT, "editButtonEvent", 
              PortalEventSource.LAYOUT_GENERATED);
  
  /**
   * The layout-generated event that is sent to
   * a channel when the user actuates its help control.
   */
  public static final PortalEvent HELP_BUTTON = 
      new PortalEvent(HELP_BUTTON_EVENT, "helpButtonEvent", 
              PortalEventSource.LAYOUT_GENERATED);
  
  /**
   * The layout-generated event that is sent to
   * a channel when the user actuates its about control.
   */
  public static final PortalEvent ABOUT_BUTTON = 
      new PortalEvent(ABOUT_BUTTON_EVENT, "aboutButtonEvent", 
              PortalEventSource.LAYOUT_GENERATED);
  
  /**
   * The layout-generated event that is sent to
   * a channel when the user actuates its detach control.
   */
  public static final PortalEvent DETACH_BUTTON = 
      new PortalEvent(DETACH_BUTTON_EVENT, "detachButtonEvent", 
              PortalEventSource.LAYOUT_GENERATED);
  
  /**
   * The layout-generated event that is sent to
   * a channel when the user actuates its minimize control.
   */
  public static final PortalEvent MINIMIZE = 
      new PortalEvent(MINIMIZE_EVENT, "minimizeEvent", 
              PortalEventSource.LAYOUT_GENERATED);
  
  /**
   * The layout-generated event that is sent to
   * a channel when the user actuates its maximize control.
   */
  public static final PortalEvent NORMAL = 
      new PortalEvent(NORMAL_EVENT, "normalEvent", 
              PortalEventSource.LAYOUT_GENERATED);
  
  /**
   * The layout-generated event that is sent to
   * a channel when the user actuates its maximize control.
   */
  public static final PortalEvent MAXIMIZE = 
      new PortalEvent(MAXIMIZE_EVENT, "maximizeEvent", 
              PortalEventSource.LAYOUT_GENERATED);
  
  /**
   * Integer representation of an event.  Must be one of the static integers
   * declared in this class.
   */
  private final int eventNumber;

  /**
   * String representation of the event.
   */
  private final String eventName;
  
  /**
   * Source of the event -- curently either FRAMEWORK or LEGACY_LAYOUT_QNAME.
   */
  private final PortalEventSource source;
  
  /**
   * Construct a PortalEvent instance from parameters.
   * @param eventNumber - integer representation of event type
   * @param eventName - String name of event
   * @param source - source type of event
   */
  private PortalEvent(int eventNumber, String eventName, PortalEventSource source) {
      this.eventNumber = eventNumber;
      this.eventName = eventName;
      this.source = source;
  }
  
  /**
   * Get a String representing this event.
   * @return a String representing this event.
   */
  public String getEventName() {
    return this.eventName;
  }

  /**
   * Get an integer representing this event.
   * @return an integer representing this event.
   */
  public int getEventNumber() {
    return this.eventNumber;
  }
  
  /**
   * Get the source type of this event.
   * @return the source type of this event.
   */
  public PortalEventSource getSource() {
      return this.source;
  }
  
  /**
   * Two PortalEvents are equal if they have the same eventNumber.
 * @param other - another object
 * @return true if other is a PortalEvent with the same eventNumber,
 * false otherwise
   */
  @Override
public boolean equals(Object other) {
      if (other == null)
          return false;
      if (! (other instanceof PortalEvent))
          return false;
      PortalEvent otherEvent = (PortalEvent) other;
      
      if (otherEvent.eventNumber == this.eventNumber)
          return true;
      
      return false;
  }
  
  @Override
public String toString() {
      return this.eventName;
  }
  
}
