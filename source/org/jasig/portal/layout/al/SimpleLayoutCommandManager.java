/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import java.util.Iterator;

/**
 * A naive implementation of the command manager that
 * appends commands to a list without any reduction procedures.
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @author Michael Ivanov: mvi at immagic.com
 */
public class SimpleLayoutCommandManager extends AbstractLayoutCommandManager {
    
    public SimpleLayoutCommandManager() {
    	super();
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#executeLayoutCommands(org.jasig.portal.layout.al.ILayoutManager)
     */
    public void executeLayoutCommands() {
        for (Iterator iter = commands.iterator(); iter.hasNext();) {
            ILayoutCommand command = (ILayoutCommand) iter.next();
            command.execute(this.layoutManager);
        }
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#recordAddCommand(org.jasig.portal.layout.node.IUserLayoutNodeDescription, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
     */
    public void recordLayoutCommand(ILayoutCommand layoutCommand) {
        commands.add(layoutCommand);
    }
    
}
