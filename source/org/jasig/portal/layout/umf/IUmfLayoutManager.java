/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.umf;

import org.jasig.portal.layout.node.INodeId;
import org.jasig.portal.layout.node.ILayoutNode;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;

/**
 * User Modifiable Fragment Layout Manager interface.
 * @author Michael Ivanov <a href="mailto:">mvi@immagic.com</a>
 * @version $Revision$
 */
public interface IUmfLayoutManager extends ILayoutManager {
	
	public ILayoutNode addNode ( IUserLayoutNodeDescription nodeDesc, IFragmentNodeId parentId, INodeId nextId );
	public ILayoutNode addNode ( IUserLayoutNodeDescription nodeDesc, ILayoutNodeId parentId, INodeId nextId );
	
	public boolean moveNode ( ILayoutNodeId nodeId, ILayoutNodeId parentId, INodeId nextId );
	public boolean moveNode ( ILayoutNodeId nodeId, IFragmentNodeId parentId, INodeId nextId );
	public boolean moveNode ( IFragmentNodeId nodeId, ILayoutNodeId parentId, INodeId nextId );
	public boolean moveNode ( IFragmentNodeId nodeId, IFragmentNodeId parentId, INodeId nextId );
	
	public boolean deleteNode ( IFragmentNodeId nodeId );
	
}
