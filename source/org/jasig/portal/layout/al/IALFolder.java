/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

import org.jasig.portal.layout.al.common.node.ILayoutFolder;
import org.jasig.portal.layout.al.common.node.INodeId;
import org.jasig.portal.layout.al.common.node.NodeIdFactory;

/**
 * An extension of the layout folder interface that includes
 * aggregation and restriction information.
 * 
 * @author Michael Ivanov: mvi at immagic.com
 * @version 1.0
 */
public interface IALFolder extends IALNode, ILayoutFolder {
	
	 // lost folder id
    public static final INodeId LOST_FOLDER_ID=NodeIdFactory.createNodeId("lost_folder");
    // root folder id
    public static final INodeId ROOT_FOLDER_ID=NodeIdFactory.createNodeId("userLayoutRootNode");
    
}
