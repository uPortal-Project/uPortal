/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A layout command manager that optimizies command recording
 * by reducing successive commands on the same node, and by
 * removing commands that can no longer be performed.
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @author Michael Ivanov: mvi at immagic.com
 */
public class ReducingLayoutCommandManager extends AbstractLayoutCommandManager {
    
    public ReducingLayoutCommandManager() {
       super();
    }
  
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#executeLayoutCommands(org.jasig.portal.layout.al.ILayoutManager)
     */
    public void executeLayoutCommands() {
        List successfulCommands=new ArrayList();
        for (Iterator iter = this.commands.iterator(); iter.hasNext();) {
            ILayoutCommand command = (ILayoutCommand) iter.next();
            if(command.execute(this.layoutManager)) {
                successfulCommands.add(command);
            }
        }
        // replace existing command list with the set of successfull commands
        this.commands=successfulCommands;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.ILayoutCommandManager#recordAttachCommand(org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId, org.jasig.portal.layout.node.INodeId)
     */
    public void recordLayoutCommand(ILayoutCommand layoutCommand) {
        // TODO
    }
   
}
