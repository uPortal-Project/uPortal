/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.restrictions;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.IAggregatedLayout;
import org.jasig.portal.layout.al.IALNode;
import org.jasig.portal.layout.al.common.node.ILayoutNode;
import org.jasig.portal.layout.al.common.node.INodeId;

/**
 * The generic aggregated layout restriction class.
 *
 * @author Michael Ivanov
 * @version $Revision$
 */

public abstract class ALRestriction extends LayoutRestriction {
	
  public ALRestriction() {
	 super();
  }	

  public ALRestriction( String name ) {
    super(name);
  }

  public ALRestriction( String name, RestrictionPath nodePath ) {
    super(name,nodePath);
  }

  
  /**
   * Checks the restriction on a node for a given user layout and node ID
   * @param layout a <code>IAggregatedLayout</code> layout
   * @param nodeId a <code>INodeId</code> node ID
   * @return a boolean value
   * @exception PortalException
   */
  public boolean checkRestriction ( IAggregatedLayout layout, INodeId nodeId ) throws PortalException {
  	IALNode node = (IALNode) layout.getNode(nodeId);
  	if ( node == null )
  	  throw new PortalException ( "The aggregated layout does not contain the node specified by ID = "+nodeId);	
  	return checkRestriction(node);
  }
  
  /**
   * Checks the restriction on a given node
   * @param node a <code>ILayoutNode</code> node
   * @return a boolean value
   * @exception PortalException
   */
  public boolean checkRestriction ( ILayoutNode node ) throws PortalException {
  	return false;
  }
  
}
