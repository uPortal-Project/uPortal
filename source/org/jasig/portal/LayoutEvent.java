package org.jasig.portal;



/** 
 * Signifies an event generated at the Layout level that is passed on to a channel.
 * Current event types are :
 * <ul>
 *  <li> editButtonEvent - occurs when an edit button on the channel frame has been hit </li>
 *  <li> helpButtonEvent - occurs when a help button on the channel frame has been hit </li>
 *  <li> detachButtonEvent - occurs when a detach button on the channel frame has been hit </li>
 *  <li> minimizeButtonEvent - occurs when a minimize button on the channel frame has been hit </li>
 * </ul>
 * @version 0.1
 */

public class LayoutEvent {

    public static final int EDIT_BUTTON_EVENT = 1;
    public static final int HELP_BUTTON_EVENT = 2;
    public static final int DETACH_BUTTON_EVENT = 3;
    public static final int MINIMIZE_BUTTON_EVENT = 4;

    private static final String [] eventName = {
	"editButtonEvent",
	"helpButtonEvent",
	"detachButtonEvent",
	"minimizeButtonEvent"
    };

    private int event;

    public LayoutEvent(int ev) { event=ev; }

    public String getEventName() { return eventName[event]; };
    public int getEventNumber() { return event; }

}
