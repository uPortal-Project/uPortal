/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
 *  <li> sessionDone - signlas channel that current session is beign terminated </li>
 *  <li> unsubscribe - signlas channel that the user has unsubscribed the channel </li>
 * </ul>
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class PortalEvent
{
    // framework-generated events
    
  /**
   * Warning: the framework never sends this event.  Listening for it is pointless.
   * @deprecated this event will be removed in uPortal 2.5
   */
  public static final int RENDERING_DONE = 0;
  public static final int SESSION_DONE = 1;
  public static final int UNSUBSCRIBE = 2;

    // layout-generated events
  public static final int EDIT_BUTTON_EVENT = 3;
  public static final int HELP_BUTTON_EVENT = 4;
  public static final int ABOUT_BUTTON_EVENT = 5;
  public static final int DETACH_BUTTON_EVENT = 6;

  private static final String [] eventName =
  {
      "renderingDone",
      "sessionDone",
      "unsubscribe",
      "editButtonEvent",
      "helpButtonEvent",
      "aboutButtonEvent",
      "detachButtonEvent",
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
