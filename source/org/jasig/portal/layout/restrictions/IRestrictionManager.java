/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.node.ILayoutNode;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;


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
     * @param nodeId a <code>String</code> node ID
     * @param restrictionName a restriction name
     * @param restrictionPath a <code>String</code> restriction path
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(String nodeId, String restrictionName, String restrictionPath, String propertyValue) throws PortalException;

  /**
     * Checks the local restriction specified by the parameters below.
     * @param nodeId a <code>String</code> node ID
     * @param restrictionName a restriction name
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(String nodeId, String restrictionName, String propertyValue ) throws PortalException;
  
  /**
   * Checks the necessary restrictions while adding a new node
   * @param node a <code>ILayoutNode</code> a new node to be added
   * @param parentId a <code>String</code> parent node ID
   * @param nextSiblingId a <code>String</code> next sibling node ID
   * @return a boolean value
   * @exception PortalException if an error occurs
   */
  public boolean checkAddRestrictions( ILayoutNode node, String parentId, String nextSiblingId ) throws PortalException;

  /**
     * Checks the necessary restrictions while moving a node.
     * @param nodeId a <code>String</code> node ID of a node to be moved
     * @param newParentId a <code>String</code> new parent node ID
     * @param nextSiblingId a <code>String</code> next sibling node ID
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkMoveRestrictions( String nodeId, String newParentId, String nextSiblingId ) throws PortalException;

  /**
     * Checks the necessary restrictions while deleting a node.
     * @param nodeId a <code>String</code> node ID of a node to be deleted
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkDeleteRestrictions( String nodeId ) throws PortalException;


  /**
     * Recursively checks the depth restrictions beginning with a given node.
     * @param nodeId a <code>String</code> node ID
     * @param newParentId a <code>String</code> new parent node ID
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkDepthRestrictions(String nodeId,String newParentId) throws PortalException;

  /**
     * Recursively checks the depth restrictions beginning with a given node.
     * @param nodeId a <code>String</code> node ID
     * @param depth a depth on which the node is going to be attached
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkDepthRestrictions( String nodeId, int depth ) throws PortalException;
  

  /**
   * Checks the necessary restrictions while updating a node.
   * @param nodeDescription a <code>IUserLayoutNodeDescription</code> node description of a node to be updated
   * @return a boolean value
   * @exception PortalException if an error occurs
   */
  public boolean checkUpdateRestrictions(IUserLayoutNodeDescription nodeDescription) throws PortalException;
  
}
