/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;

import org.jasig.portal.layout.al.common.node.IFolderDescription;
import org.jasig.portal.layout.al.common.node.INodeId;
import org.jasig.portal.layout.al.common.node.NodeIdFactory;

/**
 * An interface describing an Aggregated Layout folder descriptions
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.0
 */
public interface IALFolderDescription extends IFolderDescription, IALNodeDescription {
    // lost folder id
    public static final INodeId LOST_FOLDER_ID=NodeIdFactory.createNodeId("lost_folder");
    // root folder id
    public static final INodeId ROOT_FOLDER_ID=NodeIdFactory.createNodeId("userLayoutRootNode");
}
