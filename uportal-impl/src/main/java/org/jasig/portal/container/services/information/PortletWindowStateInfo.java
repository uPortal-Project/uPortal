/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.container.services.information;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;


/**
 * Class for tracking the current and previous WindowState and PortletMode for
 * a PortletWindow. The previous mode and state fields are automaticly updated
 * when the current mode and state are updated. The getters/setters are
 * synchronized since setting the previous and current is not an atomic
 * operation.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Id$
 */
public class PortletWindowStateInfo implements Serializable {
    private final Lock stateLock = new Lock();
    private final Lock modeLock = new Lock();
    
    private transient WindowState currentState = WindowState.NORMAL;
    private transient WindowState previousState = WindowState.NORMAL;
    private transient PortletMode currentMode = PortletMode.VIEW;
    private transient PortletMode previousMode = PortletMode.VIEW;
    
    
    /**
     * @return Returns the currentMode.
     */
    public PortletMode getCurrentMode() {
        synchronized (this.modeLock) {
            return this.currentMode;
        }
    }
    /**
     * @return Returns the currentState.
     */
    public WindowState getCurrentState() {
        synchronized (this.stateLock) {
            return this.currentState;
        }
    }
    /**
     * @return Returns the previousMode.
     */
    public PortletMode getPreviousMode() {
        synchronized (this.modeLock) {
            return this.previousMode;
        }
    }
    /**
     * @return Returns the previousState.
     */
    public WindowState getPreviousState() {
        synchronized (this.stateLock) {
            return this.previousState;
        }
    }
    /**
     * @param currentMode The currentMode to set.
     */
    public void setCurrentMode(PortletMode currentMode) {
        if (currentMode == null)
            throw new IllegalArgumentException("currentMode cannot be null");

        synchronized (this.modeLock) {
            this.previousMode = this.currentMode;
            this.currentMode = currentMode;
        }
    }
    /**
     * @param currentState The currentState to set.
     */
    public void setCurrentState(WindowState currentState) {
        if (currentState == null)
            throw new IllegalArgumentException("currentState cannot be null");

        synchronized (this.stateLock) {
            this.previousState = this.currentState;
            this.currentState = currentState;
        }
    }
    
    
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuffer buff = new StringBuffer();
        
        buff.append("[");
        buff.append("currentMode='").append(this.currentMode).append("', ");
        buff.append("previousMode='").append(this.previousMode).append("', ");
        buff.append("currentState='").append(this.currentState).append("', ");
        buff.append("previousState='").append(this.previousState).append("'");
        buff.append("]");
        
        return buff.toString();
    }
    
    
    /**
     * Used by the Java serialization system to write this object out.
     * 
     * @see Serializable
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        //Write out non-transient fields
        out.defaultWriteObject();
        
        //Write modes and states as strings since they aren't serializable
        out.writeObject(this.currentMode.toString());
        out.writeObject(this.previousMode.toString());
        out.writeObject(this.currentState.toString());
        out.writeObject(this.previousState.toString());
    }

    /**
     * Used by the Java serialization system to re-create this object
     * 
     * @see Serializable
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        //Read in non-transient fields
        in.defaultReadObject();
        
        //Read in mode and state strings
        final String currentModeStr = (String)in.readObject();
        final String previousModeStr = (String)in.readObject();
        final String currentStateStr = (String)in.readObject();
        final String previousStateStr = (String)in.readObject();
        
        //Convert mode and state strings to real objects
        this.currentMode = this.stringToMode(currentModeStr);
        this.previousMode = this.stringToMode(previousModeStr);
        this.currentState = this.stringToState(currentStateStr);
        this.previousState = this.stringToState(previousStateStr);
    }
    
    /**
     * Utility to converting PortletMode string names to objects. Tries
     * to re-use standard objects before resorting to creating a new object. 
     * 
     * @param modeName The name of the mode.
     * @return The PorltetMode representing the name
     */
    private PortletMode stringToMode(String modeName) {
        if (PortletMode.VIEW.toString().equalsIgnoreCase(modeName))
            return PortletMode.VIEW;
        else if (PortletMode.EDIT.toString().equalsIgnoreCase(modeName))
            return PortletMode.EDIT;
        else if (PortletMode.HELP.toString().equalsIgnoreCase(modeName))
            return PortletMode.HELP;
        else
            return new PortletMode(modeName);
    }
    
    /**
     * Utility to converting WindowState string names to objects. Tries
     * to re-use standard objects before resorting to creating a new object. 
     * 
     * @param stateName The name of the state.
     * @return The WindowState representing the name
     */
    private WindowState stringToState(String stateName) {
        if (WindowState.NORMAL.toString().equalsIgnoreCase(stateName))
            return WindowState.NORMAL;
        else if (WindowState.MINIMIZED.toString().equalsIgnoreCase(stateName))
            return WindowState.MINIMIZED;
        else if (WindowState.MAXIMIZED.toString().equalsIgnoreCase(stateName))
            return WindowState.MAXIMIZED;
        else
            return new WindowState(stateName);
    }
    
    /**
     * Utility class that is used for synchornization. Keeping the two
     * lock fields final is important to ensure the synchronization works
     * correctly. Had to create a custom class instead of using Object since
     * Object is not serializable. 
     */
    private class Lock implements Serializable {
    }
}
