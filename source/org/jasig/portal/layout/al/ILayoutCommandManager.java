/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.ILayoutManager;
import org.jasig.portal.layout.al.common.ILayoutManagerCommands;


/**
 * An interface describing layout command manager.
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @author Peter Kharchenko: mvi at immagic.com
 */
public interface ILayoutCommandManager extends ILayoutManagerCommands {
    
    /**
     * Specify the layout manager
     * @param manager - manager for which the commands are issued
     */
    public void setLayoutManager(ILayoutManager manager);
    
    /**
     * Execute a sequence of the recorded commands.
     * @param manager - the manager on which to execute the commands
     */
    public void executeLayoutCommands() throws PortalException;
    
    /**
     * Clear all user modification commands
     */
    public void clearCommands();
}
