/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.restrictions;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.IUserLayout;
import org.jasig.portal.layout.al.common.node.ILayoutNode;
import org.jasig.portal.layout.al.common.node.INodeDescription;
import org.jasig.portal.layout.al.common.node.INodeId;


/**
 * The Restriction Manager Interface
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public interface IRestrictionManager {
	
  /**
     * Sets the user layout.
     * @param layout a <code>IUserLayout</code> user layout to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public void setUserLayout(IUserLayout layout) throws PortalException;

   /**
     * Checks the restriction specified by the parameters below.
     * @param nodeId a node id
     * @param restrictionName a restriction name
     * @param restrictionPath a <code>RestrictionPath</code> restriction path
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(INodeId nodeId, String restrictionName, RestrictionPath restrictionPath, String propertyValue) throws PortalException;

  /**
     * Checks the local restriction specified by the parameters below.
     * @param nodeId a node id
     * @param restrictionName a restriction name
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(INodeId nodeId, String restrictionName, String propertyValue ) throws PortalException;
  
  /**
   * Checks the necessary restrictions while adding a new node
   * @param node a <code>ILayoutNode</code> a new node to be added
   * @param parentId a node parent id
   * @param nextSiblingId a next sibling node id
   * @return a boolean value
   * @exception PortalException if an error occurs
   */
  public boolean checkAddRestrictions( ILayoutNode node, INodeId parentId, INodeId nextSiblingId ) throws PortalException;

  /**
     * Checks the necessary restrictions while moving a node.
     * @param nodeId an id of a node to be moved
     * @param newParentId anew parent node id
     * @param nextSiblingId a next sibling node id
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkMoveRestrictions( INodeId nodeId, INodeId newParentId, INodeId nextSiblingId ) throws PortalException;

  /**
     * Checks the necessary restrictions while deleting a node.
     * @param nodeId an id of a node to be deleted
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkDeleteRestrictions( INodeId nodeId ) throws PortalException;


  /**
     * Recursively checks the depth restrictions beginning with a given node.
     * @param nodeId a node id
     * @param newParentId a new parent node id
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkDepthRestrictions(INodeId nodeId,INodeId newParentId) throws PortalException;

  /**
     * Recursively checks the depth restrictions beginning with a given node.
     * @param nodeId a node id
     * @param depth a depth on which the node is going to be attached
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkDepthRestrictions( INodeId nodeId, int depth ) throws PortalException;
  

  /**
   * Checks the necessary restrictions while updating a node.
   * @param nodeDescription a <code>IUserLayoutNodeDescription</code> node description of a node to be updated
   * @param nodeId a node id
   * @return a boolean value
   * @exception PortalException if an error occurs
   */
  public boolean checkUpdateRestrictions(INodeDescription nodeDescription, INodeId nodeId) throws PortalException;
  
}
