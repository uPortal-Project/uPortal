/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import org.jasig.portal.layout.al.common.node.ILayoutNode;

/**
 * An extension of the layout node interface that includes
 * aggregation and restriction information.
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @author Michael Ivanov: mvi at immagic.com
 * @version 1.0
 */
public interface IALNode extends ILayoutNode, IALNodeDescription {

    /**
     * Gets the priority value for this node.
     */
    public int getPriority();
    
}
