/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.ALFolder;
import org.jasig.portal.layout.ALNode;
import org.jasig.portal.layout.ILayoutNode;
import org.jasig.portal.layout.IALNodeDescription;
import org.jasig.portal.layout.IAggregatedLayout;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutNodeDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



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

  public void setUserLayout(IUserLayout layout) throws PortalException {
   if ( !(layout instanceof IAggregatedLayout) )
    throw new PortalException ( "The user layout instance must have IAggregatedLayout type!" );
    this.layout = (IAggregatedLayout) layout;
  }

   /**
     * Checks the restriction specified by the parameters below
     * @param nodeId a <code>String</code> node ID
     * @param restrictionName a restriction name
     * @param restrictionPath a <code>String</code> restriction path
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(String nodeId, String restrictionName, String restrictionPath, String propertyValue) throws PortalException {
    ALNode node = layout.getLayoutNode(nodeId);
    return (node!=null)?checkRestriction(node,restrictionName,restrictionPath,propertyValue):true;
  }


  /**
     * Checks the local restriction specified by the parameters below
     * @param nodeId a <code>String</code> node ID
     * @param restrictionName a restriction name
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(String nodeId, String restrictionName, String propertyValue ) throws PortalException {
    return (nodeId!=null)?checkRestriction(nodeId, restrictionName, IUserLayoutRestriction.LOCAL_RESTRICTION_PATH, propertyValue):true;
  }

  /**
     * Checks the restriction specified by the parameters below
     * @param node a <code>ALNode</code> node to be checked
     * @param restrictionName a restriction name
     * @param restrictionPath a <code>String</code> restriction path
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(ALNode node, String restrictionName, String restrictionPath, String propertyValue) throws PortalException {
    IUserLayoutRestriction restriction = node.getRestriction(UserLayoutRestriction.getUniqueKey(restrictionName,restrictionPath));
    if ( restriction != null )
     return restriction.checkRestriction(propertyValue);
     return true;
  }


  /**
     * Checks the local restriction specified by the parameters below
     * @param node a <code>ALNode</code> node to be checked
     * @param restrictionName a restriction name
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(ALNode node, String restrictionName, String propertyValue ) throws PortalException {
    return checkRestriction(node, restrictionName, IUserLayoutRestriction.LOCAL_RESTRICTION_PATH, propertyValue);
  }


  /**
     * Checks the necessary restrictions while adding a new node
     * @param node a <code>ILayoutNode</code> a new node to be added
     * @param parentId a <code>String</code> parent node ID
     * @param nextSiblingId a <code>String</code> next sibling node ID
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkAddRestrictions( ILayoutNode node, String parentId, String nextSiblingId ) throws PortalException {
  	
  	if ( !(node instanceof ALNode) )
       throw new PortalException ("The node must be ALNode type!");
  	
  	ALNode newNode = (ALNode) node;
    String newNodeId = newNode.getId();
    ALNode parentNode = layout.getLayoutNode(parentId);

    if ( !(parentNode.getNodeType()==IUserLayoutNodeDescription.FOLDER ) )
      throw new PortalException ("The target parent node should be a folder!");

    //if ( checkRestriction(parentNode,RestrictionTypes.IMMUTABLE_RESTRICTION,"false") ) {
    if ( !parentNode.getNodeDescription().isImmutable() ) {

     // Checking children related restrictions
     Collection restrictions = parentNode.getRestrictionsByPath(IUserLayoutRestriction.CHILDREN_RESTRICTION_PATH);
     for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
         IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
         if (   !restriction.getName().equals(RestrictionTypes.DEPTH_RESTRICTION) &&
                !restriction.checkRestriction(newNode) )
            return false;
     }

     // Checking parent related restrictions
     restrictions = newNode.getRestrictionsByPath(IUserLayoutRestriction.PARENT_RESTRICTION_PATH);
     for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
          IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
          if (  !restriction.getName().equals(RestrictionTypes.DEPTH_RESTRICTION) &&
                !restriction.checkRestriction(parentNode) )
            return false;
     }

     // Considering two cases if the node is new or it is already in the user layout
     if ( newNodeId != null ) {
      // Checking depth restrictions for the node and all its descendants (if there are any)
      if ( !checkDepthRestrictions(newNodeId,parentId) )
         return false;
     } else
         return checkRestriction(newNode,RestrictionTypes.DEPTH_RESTRICTION,(layout.getDepth(parentId)+1)+"");

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
  public boolean checkMoveRestrictions( String nodeId, String newParentId, String nextSiblingId ) throws PortalException {
  	
    ALNode node = layout.getLayoutNode(nodeId);
    ALNode oldParentNode = layout.getLayoutNode(node.getParentNodeId());
    ALFolder newParentNode = layout.getLayoutFolder(newParentId);

    /*if ( checkRestriction(oldParentNode,RestrictionTypes.IMMUTABLE_RESTRICTION,"false") &&
         checkRestriction(newParentNode,RestrictionTypes.IMMUTABLE_RESTRICTION,"false") ) {*/
    if ( !oldParentNode.getNodeDescription().isImmutable() && !newParentNode.getNodeDescription().isImmutable() ) {

     if ( !oldParentNode.equals(newParentNode) ) {
      // Checking children related restrictions
      Collection restrictions = newParentNode.getRestrictionsByPath(IUserLayoutRestriction.CHILDREN_RESTRICTION_PATH);
      for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
         IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
         if (   !restriction.getName().equals(RestrictionTypes.DEPTH_RESTRICTION) &&
                !restriction.checkRestriction(node) )
            return false;
      }

      // Checking parent related restrictions
      restrictions = node.getRestrictionsByPath(IUserLayoutRestriction.PARENT_RESTRICTION_PATH);
      for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
          IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
          if (  !restriction.getName().equals(RestrictionTypes.DEPTH_RESTRICTION) &&
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
     * @param nodeId a <code>String</code> node ID of a node to be deleted
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkDeleteRestrictions( String nodeId ) throws PortalException {
    ALNode node = layout.getLayoutNode(nodeId);
    if ( nodeId == null || node == null ) return true;
    //if ( checkRestriction(node.getParentNodeId(),RestrictionTypes.IMMUTABLE_RESTRICTION,"false") ) {
    if ( !layout.getLayoutNode(node.getParentNodeId()).getNodeDescription().isImmutable() ) {
         // Checking the unremovable restriction on the node to be deleted
         //return checkRestriction(nodeId,RestrictionTypes.UNREMOVABLE_RESTRICTION,"false");
         return !node.getNodeDescription().isUnremovable();
    } else
         return false;
  }


  /**
     * Recursively checks the depth restrictions beginning with a given node
     * @param nodeId a <code>String</code> node ID
     * @param newParentId a <code>String</code> new parent node ID
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkDepthRestrictions(String nodeId,String newParentId) throws PortalException {
    if ( nodeId == null ) return true;
    int nodeDepth = layout.getDepth(nodeId);
    int parentDepth = layout.getDepth(newParentId);
    if ( nodeDepth == parentDepth+1 ) return true;
    return checkDepthRestrictions(nodeId,parentDepth+1);
  }


  /**
     * Recursively checks the depth restrictions beginning with a given node
     * @param nodeId a <code>String</code> node ID
     * @param depth a depth on which the node is going to be attached
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkDepthRestrictions( String nodeId, int depth ) throws PortalException {
    ALNode node = layout.getLayoutNode(nodeId);
    // Checking restrictions for the node
    if ( !checkRestriction(nodeId,RestrictionTypes.DEPTH_RESTRICTION,depth+"") )
            return false;
    if ( node.getNodeType() == IUserLayoutNodeDescription.FOLDER ) {
     for ( String nextId = ((ALFolder)node).getFirstChildNodeId(); nextId != null; nextId = node.getNextNodeId() ) {
      node = layout.getLayoutNode(nextId);
      if ( !checkDepthRestrictions(nextId,depth+1) )
            return false;
     }
    }
    return true;
  }


  /**
     * Gets the restriction specified by the parameters below
     * @param node a <code>ALNode</code> node
     * @param restrictionName a restriction name
     * @param restrictionPath a <code>String</code> restriction path
     * @return a <code>IUserLayoutRestriction</code> instance
     * @exception PortalException if an error occurs
     */
  public static IUserLayoutRestriction getRestriction( ALNode node, String restrictionName, String restrictionPath ) throws PortalException {
     return node.getRestriction(UserLayoutRestriction.getUniqueKey(restrictionName,restrictionPath));
  }

   /**
     * Return a priority restriction for the given node.
     * @return a <code>PriorityRestriction</code> object
     * @exception PortalException if an error occurs
     */
  public static PriorityRestriction getPriorityRestriction( ALNode node ) throws PortalException {
     PriorityRestriction priorRestriction = getPriorityRestriction(node,IUserLayoutRestriction.LOCAL_RESTRICTION_PATH);
     if ( priorRestriction == null ) {
       priorRestriction = (PriorityRestriction)
         UserLayoutRestrictionFactory.createRestriction(RestrictionTypes.PRIORITY_RESTRICTION,"0-"+java.lang.Integer.MAX_VALUE);
     }
     return priorRestriction;
  }


  /**
     * Return a priority restriction for the given node.
     * @return a <code>PriorityRestriction</code> object
     * @exception PortalException if an error occurs
     */
  public static PriorityRestriction getPriorityRestriction( ALNode node, String restrictionPath ) throws PortalException {
     return (PriorityRestriction) getRestriction(node,RestrictionTypes.PRIORITY_RESTRICTION,restrictionPath);
  }



    

  public boolean checkUpdateRestrictions(IUserLayoutNodeDescription nodeDescription) throws PortalException {
        IALNodeDescription nodeDesc=(IALNodeDescription)nodeDescription;
        String nodeId = nodeDesc.getId();

        if ( nodeId == null ) return false;
        ALNode node = layout.getLayoutNode(nodeId);
        IALNodeDescription currentNodeDesc = (IALNodeDescription) node.getNodeDescription();
        // If the node Ids do no match to each other then return false
        if ( !nodeId.equals(currentNodeDesc.getId()) ) return false;

        // Checking the immutable node restriction
        //if ( checkRestriction(node,RestrictionTypes.IMMUTABLE_RESTRICTION,"true") )
        if ( currentNodeDesc.isImmutable() )
            return false;

        // Checking the immutable parent node related restriction
        if ( getRestriction(layout.getLayoutNode(node.getParentNodeId()),RestrictionTypes.IMMUTABLE_RESTRICTION,IUserLayoutRestriction.CHILDREN_RESTRICTION_PATH) != null &&
             checkRestriction(node.getParentNodeId(),RestrictionTypes.IMMUTABLE_RESTRICTION,IUserLayoutRestriction.CHILDREN_RESTRICTION_PATH,"true") )
            return false;

        // Checking the immutable children node related restrictions
        if ( node.getNodeType() == IUserLayoutNodeDescription.FOLDER ) {
            ALFolder folder = (ALFolder) node;
            //Loop for all children
            for ( String nextId = folder.getFirstChildNodeId(); nextId != null; nextId = layout.getLayoutNode(nextId).getNextNodeId() )
             if ( getRestriction(layout.getLayoutNode(nextId),RestrictionTypes.IMMUTABLE_RESTRICTION,IUserLayoutRestriction.PARENT_RESTRICTION_PATH) != null &&
                  checkRestriction(nextId,RestrictionTypes.IMMUTABLE_RESTRICTION,IUserLayoutRestriction.PARENT_RESTRICTION_PATH,"true") )
                  return false;
        }

       // if a new node description doesn't contain any restrictions the old restrictions will be used
        if ( nodeDesc.getRestrictions() == null )
          nodeDesc.setRestrictions(currentNodeDesc.getRestrictions());
        Hashtable rhash = nodeDesc.getRestrictions();
        // Setting the new node description to the node
        node.setNodeDescription(nodeDesc);

        // Checking restrictions for the node
        if ( rhash != null ) {
           for ( Enumeration enum = rhash.elements(); enum.hasMoreElements(); )
             if ( !((IUserLayoutRestriction)enum.nextElement()).checkRestriction(node) ) {
                  node.setNodeDescription(currentNodeDesc);
                  return false;
             }
        }


        // Checking parent related restrictions for the children
        Collection restrictions = layout.getLayoutNode(node.getParentNodeId()).getRestrictionsByPath(IUserLayoutRestriction.CHILDREN_RESTRICTION_PATH);
        for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
         IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
         if ( !restriction.checkRestriction(node) ) {
            node.setNodeDescription(currentNodeDesc);
            return false;
         }
        }


        // Checking child related restrictions for the parent
        if ( node.getNodeType() == IUserLayoutNodeDescription.FOLDER ) {
         for ( String nextId = ((ALFolder)node).getFirstChildNodeId(); nextId != null; ) {
          ALNode child = layout.getLayoutNode(nextId);
          restrictions = child.getRestrictionsByPath(IUserLayoutRestriction.PARENT_RESTRICTION_PATH);
          for ( Iterator i = restrictions.iterator(); i.hasNext(); ) {
           IUserLayoutRestriction restriction = (IUserLayoutRestriction) i.next();
           if ( !restriction.checkRestriction(node) ) {
            node.setNodeDescription(currentNodeDesc);
            return false;
           }
          }
            nextId = child.getNextNodeId();
         }
        }

        return true;
    }

}
