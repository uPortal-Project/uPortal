/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.restrictions;

import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.IALFolder;
import org.jasig.portal.layout.al.IALNode;
import org.jasig.portal.layout.al.IALNodeDescription;
import org.jasig.portal.layout.al.IAggregatedLayout;
import org.jasig.portal.layout.al.common.ILayout;
import org.jasig.portal.layout.al.common.node.ILayoutNode;
import org.jasig.portal.layout.al.common.node.INode;
import org.jasig.portal.layout.al.common.node.INodeDescription;
import org.jasig.portal.layout.al.common.node.INodeId;
import org.jasig.portal.layout.al.common.node.NodeType;



/**
 * An implementation of Restriction Manager Interface
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public class ALRestrictionManager implements IALRestrictionManager {

  private static final Log log = LogFactory.getLog(ALRestrictionManager.class);
  private IAggregatedLayout layout;
  
  
  public ALRestrictionManager() throws Exception {}

  public ALRestrictionManager( IAggregatedLayout layout ) throws Exception {
    this.layout = layout;
  }

  public void setUserLayout(ILayout layout) throws PortalException {
   if ( !(layout instanceof IAggregatedLayout) )
    throw new PortalException ( "The user layout instance must have IAggregatedLayout type!" );
    this.layout = (IAggregatedLayout) layout;
  }

   /**
     * Checks the restriction specified by the parameters below
     * @param nodeId a node id
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @param restrictionPath a <code>RestrictionPath</code> restriction path
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(INodeId nodeId, RestrictionType restrictionType, RestrictionPath restrictionPath, String propertyValue) throws PortalException {
    IALNode node = (IALNode) layout.getNode(nodeId);
    return (node!=null)?checkRestriction(node,restrictionType,restrictionPath,propertyValue):true;
  }


  /**
     * Checks the local restriction specified by the parameters below
     * @param nodeId a node id
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(INodeId nodeId, RestrictionType restrictionType, String propertyValue ) throws PortalException {
    return (nodeId!=null)?checkRestriction(nodeId, restrictionType, RestrictionPath.LOCAL_RESTRICTION_PATH, propertyValue):true;
  }

  /**
     * Checks the restriction specified by the parameters below
     * @param node a <code>ALNode</code> node to be checked
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @param restrictionPath a <code>RestrictionPath</code> restriction path
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(IALNode node, RestrictionType restrictionType, RestrictionPath restrictionPath, String propertyValue) throws PortalException {
    IUserLayoutRestriction restriction = node.getRestriction(restrictionType,restrictionPath);
    if ( restriction != null )
     return restriction.checkRestriction(propertyValue);
     return true;
  }


  /**
     * Checks the local restriction specified by the parameters below
     * @param node a <code>ALNode</code> node to be checked
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(IALNode node, RestrictionType restrictionType, String propertyValue ) throws PortalException {
    return checkRestriction(node, restrictionType, RestrictionPath.LOCAL_RESTRICTION_PATH, propertyValue);
  }


  /**
     * Checks the necessary restrictions while adding a new node
     * @param node a <code>ILayoutNode</code> a new node to be added
     * @param parentId a parent node id
     * @param nextSiblingId a <next sibling node id
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkAddRestrictions( INode node, INodeId parentId, INodeId nextSiblingId ) throws PortalException {
  	
  	if ( !(node instanceof IALNode) )
       throw new PortalException ("The node must be IALNode type!");
  	
  	IALNode newNode = (IALNode) node;
    INodeId newNodeId = newNode.getId();
    IALNode parentNode = (IALNode) layout.getNode(parentId);

    if ( !(parentNode.getType()==NodeType.FOLDER ) )
      throw new PortalException ("The target parent node should be a folder!");

    //if ( checkRestriction(parentNode,RestrictionTypes.IMMUTABLE_RESTRICTION,"false") ) {
    if ( !parentNode.isImmutable() ) {

     // Checking children related restrictions
     Collection restrictions = parentNode.getRestrictionsByPath(RestrictionPath.CHILDREN_RESTRICTION_PATH);
     for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
         IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
         if (   !restriction.is(RestrictionType.DEPTH_RESTRICTION) &&
                !restriction.checkRestriction(newNode) )
            return false;
     }

     // Checking parent related restrictions
     restrictions = newNode.getRestrictionsByPath(RestrictionPath.PARENT_RESTRICTION_PATH);
     for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
          IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
          if (  !restriction.is(RestrictionType.DEPTH_RESTRICTION) &&
                !restriction.checkRestriction(parentNode) )
            return false;
     }

     // Considering two cases if the node is new or it is already in the user layout
     if ( newNodeId != null ) {
      // Checking depth restrictions for the node and all its descendants (if there are any)
      if ( !checkDepthRestrictions(newNodeId,parentId) )
         return false;
     } else
         return checkRestriction(newNode,RestrictionType.DEPTH_RESTRICTION,(layout.getDepth(parentId)+1)+"");

     // Checking sibling nodes order
     //return changeSiblingNodesPriorities(newNode,parentId,nextSiblingId);
     return true;

    } else
        return false;
  }


  /**
     * Checks the necessary restrictions while moving a node
     * @param nodeId a <code>String</code> node ID of a node to be moved
     * @param newParentId a <code>String</code> new parent node ID
     * @param nextSiblingId a <code>String</code> next sibling node ID
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkMoveRestrictions( INodeId nodeId, INodeId newParentId, INodeId nextSiblingId ) throws PortalException {
  	
    IALNode node = (IALNode) layout.getNode(nodeId);
    IALNode oldParentNode = (IALNode) node.getParentNode();
    IALFolder newParentNode = (IALFolder) layout.getNode(newParentId);

    /*if ( checkRestriction(oldParentNode,RestrictionTypes.IMMUTABLE_RESTRICTION,"false") &&
         checkRestriction(newParentNode,RestrictionTypes.IMMUTABLE_RESTRICTION,"false") ) {*/
    if ( !oldParentNode.isImmutable() && !newParentNode.isImmutable() ) {

     if ( !oldParentNode.equals(newParentNode) ) {
      // Checking children related restrictions
      Collection restrictions = newParentNode.getRestrictionsByPath(RestrictionPath.CHILDREN_RESTRICTION_PATH);
      for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
         IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
         if (   !restriction.is(RestrictionType.DEPTH_RESTRICTION) &&
                !restriction.checkRestriction(node) )
            return false;
      }

      // Checking parent related restrictions
      restrictions = node.getRestrictionsByPath(RestrictionPath.PARENT_RESTRICTION_PATH);
      for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
          IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
          if (  !restriction.is(RestrictionType.DEPTH_RESTRICTION) &&
                !restriction.checkRestriction(newParentNode) )
            return false;
      }

      // Checking depth restrictions for the node and all its descendants
      if ( !checkDepthRestrictions(nodeId,newParentId) )
            return false;
     }

     return true;

    } else
        return false;
  }


  /**
     * Checks the necessary restrictions while deleting a node
     * @param nodeId an id of a node to be deleted
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkDeleteRestrictions( INodeId nodeId ) throws PortalException {
    IALNode node = (IALNode) layout.getNode(nodeId);
    if ( nodeId == null || node == null ) return true;
    //if ( checkRestriction(node.getParentNodeId(),RestrictionTypes.IMMUTABLE_RESTRICTION,"false") ) {
    if ( !node.getParentNode().isImmutable() ) {
         // Checking the unremovable restriction on the node to be deleted
         //return checkRestriction(nodeId,RestrictionTypes.UNREMOVABLE_RESTRICTION,"false");
         return !node.isUnremovable();
    } else
         return false;
  }


  /**
     * Recursively checks the depth restrictions beginning with a given node
     * @param nodeId a node id
     * @param newParentId an id of the new parent node
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkDepthRestrictions(INodeId nodeId,INodeId newParentId) throws PortalException {
    if ( nodeId == null ) return true;
    int nodeDepth = layout.getDepth(nodeId);
    int parentDepth = layout.getDepth(newParentId);
    if ( nodeDepth == parentDepth+1 ) return true;
    return checkDepthRestrictions(nodeId,parentDepth+1);
  }


  /**
     * Recursively checks the depth restrictions beginning with a given node
     * @param nodeId a node id
     * @param depth a depth on which the node is going to be attached
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkDepthRestrictions( INodeId nodeId, int depth ) throws PortalException {
    IALNode node = (IALNode) layout.getNode(nodeId);
    // Checking restrictions for the node
    if ( !checkRestriction(node.getId(),RestrictionType.DEPTH_RESTRICTION,depth+"") )
            return false;
    if ( node.getType() == NodeType.FOLDER ) {
     IALFolder folder = (IALFolder) node;	
     for ( INode n = folder.getFirstChildNode(); n != null; n = n.getNextSiblingNode() ) {
      if ( !checkDepthRestrictions(((ILayoutNode)n).getId(),depth+1) )
            return false;
     }
    }
    return true;
  }


  /**
     * Gets the restriction specified by the parameters below
     * @param node a <code>ALNode</code> node
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @param restrictionPath a <code>restrictionpath</code> restriction path
     * @return a <code>IUserLayoutRestriction</code> instance
     * @exception PortalException if an error occurs
     */
  public static IUserLayoutRestriction getRestriction( IALNode node, RestrictionType restrictionType, RestrictionPath restrictionPath ) throws PortalException {
     return node.getRestriction(restrictionType,restrictionPath);
  }

   /**
     * Return a priority restriction for the given node.
     * @return a <code>PriorityRestriction</code> object
     * @exception PortalException if an error occurs
     */
  public static PriorityRestriction getPriorityRestriction( IALNode node ) throws PortalException {
     PriorityRestriction priorRestriction = getPriorityRestriction(node,RestrictionPath.LOCAL_RESTRICTION_PATH);
     if ( priorRestriction == null ) {
       priorRestriction = (PriorityRestriction)
         UserLayoutRestrictionFactory.createRestriction(RestrictionType.PRIORITY_RESTRICTION,"0-"+java.lang.Integer.MAX_VALUE);
     }
     return priorRestriction;
  }


  /**
     * Return a priority restriction for the given node.
     * @return a <code>PriorityRestriction</code> object
     * @exception PortalException if an error occurs
     */
  public static PriorityRestriction getPriorityRestriction( IALNode node, RestrictionPath restrictionPath ) throws PortalException {
     return (PriorityRestriction) getRestriction(node,RestrictionType.PRIORITY_RESTRICTION,restrictionPath);
  }


  public boolean checkUpdateRestrictions(INodeDescription nodeDescription, INodeId nodeId ) throws PortalException {
  	   
        IALNodeDescription nodeDesc=(IALNodeDescription) nodeDescription;
        
        if ( nodeId == null ) return false;
        
        IALNode node = (IALNode) layout.getNode(nodeId);
        IALNodeDescription currentNodeDesc = node.getNodeDescription();

        // Checking the immutable node restriction
        //if ( checkRestriction(node,RestrictionTypes.IMMUTABLE_RESTRICTION,"true") )
        if ( node.isImmutable() )
            return false;

        // Checking the immutable parent node related restriction
        if ( getRestriction((IALNode)node.getParentNode(),RestrictionType.IMMUTABLE_RESTRICTION,RestrictionPath.CHILDREN_RESTRICTION_PATH) != null &&
             checkRestriction(((ILayoutNode)node.getParentNode()).getId(),RestrictionType.IMMUTABLE_RESTRICTION,RestrictionPath.CHILDREN_RESTRICTION_PATH,"true") )
            return false;

        // Checking the immutable children node related restrictions
        if ( node.getType().equals(NodeType.FOLDER) ) {
            IALFolder folder = (IALFolder) node;
            //Loop for all children
            for ( node = (IALNode) folder.getFirstChildNode(); node != null; node = (IALNode) node.getNextSiblingNode() )
             if ( getRestriction(node,RestrictionType.IMMUTABLE_RESTRICTION,RestrictionPath.PARENT_RESTRICTION_PATH) != null &&
                  checkRestriction(node.getId(),RestrictionType.IMMUTABLE_RESTRICTION,RestrictionPath.PARENT_RESTRICTION_PATH,"true") )
                  return false;
        }

       // if a new node description doesn't contain any restrictions the old restrictions will be used
        if ( nodeDesc.getRestrictions() == null )
          nodeDesc.setRestrictions(node.getRestrictions());
        
        Collection restrictions = nodeDesc.getRestrictions();
        // Setting the new node description to the node
        node.setNodeDescription(nodeDesc);

        // Checking restrictions for the node
        if ( restrictions != null ) {
           for ( Iterator i = restrictions.iterator(); i.hasNext(); )
             if ( !((IUserLayoutRestriction)i.next()).checkRestriction(node) ) {
                  node.setNodeDescription(currentNodeDesc);
                  return false;
             }
        }


        // Checking parent related restrictions for the children
        restrictions = ((IALNode)node.getParentNode()).getRestrictionsByPath(RestrictionPath.CHILDREN_RESTRICTION_PATH);
        for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
         IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
         if ( !restriction.checkRestriction(node) ) {
            node.setNodeDescription(currentNodeDesc);
            return false;
         }
        }


        // Checking child related restrictions for the parent
        if ( node.getType().equals(NodeType.FOLDER) ) {
         for ( IALNode child = (IALNode) ((IALFolder)node).getFirstChildNode(); child != null; ) {
          restrictions = child.getRestrictionsByPath(RestrictionPath.PARENT_RESTRICTION_PATH);
          for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
           IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
           if ( !restriction.checkRestriction(node) ) {
            node.setNodeDescription(currentNodeDesc);
            return false;
           }
          }
          child=(IALNode) child.getNextSiblingNode();
         }
        }

        return true;
  	
    }  
  	

}
