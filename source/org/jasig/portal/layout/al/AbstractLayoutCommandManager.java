/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import java.util.List;
import java.util.ArrayList;

/**
 * An abstract command manager that
 * can be used as a basic class in all implementations.
 * 
 * @author Michael Ivanov: mvi at immagic.com
 */
public abstract class AbstractLayoutCommandManager implements ILayoutCommandManager {
	
    protected List commands;
    protected ILayoutManager layoutManager;
    
    public AbstractLayoutCommandManager() {
    	commands = new ArrayList();
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#executeLayoutCommands(org.jasig.portal.layout.al.ILayoutManager)
     */
    public abstract void executeLayoutCommands();
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#recordAddCommand(org.jasig.portal.layout.node.IUserLayoutNodeDescription, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
     */
    public abstract void recordLayoutCommand(ILayoutCommand layoutCommand);
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#clearCommands()
     */
    public void clearCommands() {
        commands.clear();
    }
    /**
     * @return Returns the layoutManager.
     */
    public ILayoutManager getLayoutManager() {
        return layoutManager;
    }
    /**
     * @param layoutManager The layoutManager to set.
     */
    public void setLayoutManager(ILayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }
}
