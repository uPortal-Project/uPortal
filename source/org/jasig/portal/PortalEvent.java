/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * Signifies an event generated at the Layout level that is passed on to a channel.
 * Current event types are :
 * <ul>
 *  <li> editButtonEvent - occurs when an edit button on the channel frame has been hit </li>
 *  <li> helpButtonEvent - occurs when a help button on the channel frame has been hit </li>
 *  <li> aboutButtonEvent - occurs when an about button on the channel frame has been hit </li>
 *  <li> detachButtonEvent - occurs when a detach button on the channel frame has been hit </li>
 *  <li> renderingDone - occurs at the end of each rendering cycle </li>
 *  <li> sessionDone - signlas channel that current session is beign terminated </li>
 *  <li> unsubscribe - signlas channel that the user has unsubscribed the channel </li>
 * </ul>
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class PortalEvent
{
    // framework-generated events
  public static final int RENDERING_DONE = 0;
  public static final int SESSION_DONE = 1;
  public static final int UNSUBSCRIBE = 2;

    // layout-generated events
  public static final int EDIT_BUTTON_EVENT = 3;
  public static final int HELP_BUTTON_EVENT = 4;
  public static final int ABOUT_BUTTON_EVENT = 5;
  public static final int DETACH_BUTTON_EVENT = 6;
  public static final int MINIMIZE_EVENT = 7;
  public static final int MAXIMIZE_EVENT = 8;

  private static final String [] eventName =
  {
      "renderingDone",
      "sessionDone",
      "unsubscribe",
      "editButtonEvent",
      "helpButtonEvent",
      "aboutButtonEvent",
      "detachButtonEvent",
      "minimizeEvent",
      "maximizeEvent",
  };

  private int event;

  public PortalEvent(int ev) {
    event = ev;
  }

  public String getEventName() {
    return eventName[event];
  }

  public int getEventNumber() {
    return event;
  }
}
