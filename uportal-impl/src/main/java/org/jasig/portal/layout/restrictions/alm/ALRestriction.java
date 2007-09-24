/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions.alm;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.alm.ALNode;
import org.jasig.portal.layout.alm.IAggregatedLayout;
import org.jasig.portal.layout.restrictions.UserLayoutRestriction;

/**
 * The generic aggregated layout restriction class.
 *
 * @author Michael Ivanov
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public abstract class ALRestriction extends UserLayoutRestriction {
	
  public ALRestriction() {
	 super();
  }	

  public ALRestriction( String name ) {
    super(name);
  }

  public ALRestriction( String name, String nodePath ) {
    super(name,nodePath);
  }

  
  /**
   * Checks the restriction on a node for a given user layout and node ID
   * @param layout a <code>IAggregatedLayout</code> layout
   * @param nodeId a <code>String</code> node ID
   * @return a boolean value
   * @exception PortalException
   */
  public boolean checkRestriction ( IAggregatedLayout layout, String nodeId ) throws PortalException {
  	ALNode node = layout.getLayoutNode(nodeId);
  	if ( node == null )
  	  throw new PortalException ( "The aggregated layout does not contain the node specified by ID = "+nodeId);	
  	return checkRestriction(node);
  }
  
  
}
