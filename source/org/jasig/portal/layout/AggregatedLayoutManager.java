/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.layout;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.restrictions.IUserLayoutRestriction;
import org.jasig.portal.layout.restrictions.PriorityRestriction;
import org.jasig.portal.layout.restrictions.RestrictionTypes;
import org.jasig.portal.layout.restrictions.UserLayoutRestriction;
import org.jasig.portal.layout.restrictions.UserLayoutRestrictionFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.GuidGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;



/**
 * An implementation of Aggregated User Layout Manager Interface defining common operations on user layout nodes,
 * that is channels and folders
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public class AggregatedLayoutManager implements IAggregatedUserLayoutManager {

  private AggregatedUserLayoutStore layoutStore;
  private AggregatedLayout layout;
  private UserProfile userProfile;
  private static final String lostFolderId = IALFolderDescription.LOST_FOLDER_ID;
  private IPerson person;

  // Boolean flags for marking nodes
  //private boolean addTargetsAllowed = false;
  //private boolean moveTargetsAllowed = false;

  private IALNodeDescription addTargetsNodeDesc;
  private String moveTargetsNodeId;
  private int restrictionMask = 0;
  private boolean autoCommit = false;

  // The ID of the current loaded fragment
  private String fragmentId;

  // The IDs and names of the fragments which a user is owner of
  private Hashtable fragments;

  // GUID generator
  private static GuidGenerator guid = null;
  private String cacheKey = null;



  public AggregatedLayoutManager( IPerson person, UserProfile userProfile ) throws Exception {
    this.person = person;
    this.userProfile = userProfile;
    layout = new AggregatedLayout ( String.valueOf(getLayoutId()), this );
    autoCommit = false;
    if ( guid == null )
      guid = new GuidGenerator();
      updateCacheKey();
  }

  public AggregatedLayoutManager( IPerson person, UserProfile userProfile, IUserLayoutStore layoutStore ) throws Exception {
    this ( person, userProfile );
    this.layoutStore = (AggregatedUserLayoutStore) layoutStore;
    this.loadUserLayout();
  }

  private void updateCacheKey() {
     cacheKey = guid.getNewGuid();
  }

  public IUserLayout getUserLayout() throws PortalException {
      return layout;
  }

  public void setUserLayout(IUserLayout userLayout) throws PortalException {
   if ( !(layout instanceof AggregatedLayout) )
    throw new PortalException ( "The user layout instance must have AggregatedLayout type!" );
    this.layout = (AggregatedLayout) layout;
    updateCacheKey();
  }

  /**
     * A factory method to create an empty <code>IUserLayoutNodeDescription</code> instance
     *
     * @param nodeType a node type value
     * @return an <code>IUserLayoutNodeDescription</code> instance
     * @exception PortalException if the error occurs.
     */
    public IUserLayoutNodeDescription createNodeDescription( int nodeType ) throws PortalException {
            switch ( nodeType ) {
              case IUserLayoutNodeDescription.FOLDER:
                return new ALFolderDescription();
              case IUserLayoutNodeDescription.CHANNEL:
                return new ALChannelDescription();
              default:
                return null;
            }
    }

    /**
     * Sets a layout manager to auto-commit mode that allows to update the database immediately
     * @param autoCommit a boolean value
     */
    public void setAutoCommit (boolean  autoCommit) {
      this.autoCommit = autoCommit;
    }


    /**
     * Returns an Id of the current user layout.
     *
     * @return a <code>int</code> value
     */
     public int getLayoutId() {
       return userProfile.getLayoutId();
     }


  /**
     * Returns an Id of a parent user layout node.
     * The user layout root node always has ID="root"
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> value
     * @exception PortalException if an error occurs
     */
  public String getParentId(String nodeId) throws PortalException {
    return layout.getParentId(nodeId);
  }


    /**
     * Returns a list of child node Ids for a given node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>Enumeration</code> of <code>String</code> child node Ids.
     * @exception PortalException if an error occurs
     */
    public Enumeration getChildIds(String nodeId) throws PortalException {
      return layout.getChildIds(nodeId);
    }


  /**
     * Determine an Id of a previous sibling node.
     *
     * @param nodeId a <code>String</code> node ID
     * @return a <code>String</code> Id value of a previous sibling node, or <code>null</code> if this is the first sibling.
     * @exception PortalException if an error occurs
     */
  public String getPreviousSiblingId(String nodeId) throws PortalException {
    return layout.getPreviousSiblingId(nodeId);
  }


  /**
     * Determine an Id of a next sibling node.
     *
     * @param nodeId a <code>String</code> node ID
     * @return a <code>String</code> Id value of a next sibling node, or <code>null</code> if this is the last sibling.
     * @exception PortalException if an error occurs
     */
  public String getNextSiblingId(String nodeId) throws PortalException {
    return layout.getNextSiblingId(nodeId);
  }


   /**
     * Checks the restriction specified by the parameters below
     * @param nodeId a <code>String</code> node ID
     * @param restrictionType a restriction type
     * @param restrictionPath a <code>String</code> restriction path
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  protected boolean checkRestriction(String nodeId, int restrictionType, String restrictionPath, String propertyValue) throws PortalException {
    ALNode node = getLayoutNode(nodeId);
    return (node!=null)?checkRestriction(node,restrictionType,restrictionPath,propertyValue):true;
  }


  /**
     * Checks the local restriction specified by the parameters below
     * @param nodeId a <code>String</code> node ID
     * @param restrictionType a restriction type
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  protected boolean checkRestriction(String nodeId, int restrictionType, String propertyValue ) throws PortalException {
    return (nodeId!=null)?checkRestriction(nodeId, restrictionType, UserLayoutRestriction.LOCAL_RESTRICTION, propertyValue):true;
  }

  /**
     * Checks the restriction specified by the parameters below
     * @param node a <code>ALNode</code> node to be checked
     * @param restrictionType a restriction type
     * @param restrictionPath a <code>String</code> restriction path
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  protected boolean checkRestriction(ALNode node, int restrictionType, String restrictionPath, String propertyValue) throws PortalException {
    IUserLayoutRestriction restriction = node.getRestriction(UserLayoutRestriction.getRestrictionName(restrictionType,restrictionPath));
    if ( restriction != null )
     return restriction.checkRestriction(propertyValue);
     return true;
  }

 private void moveWrongFragmentsToLostFolder() throws PortalException {
  Enumeration nodes = layoutStore.getIncorrectPushedFragmentNodes(person,userProfile);
  while ( nodes.hasMoreElements() ) {
    String nodeId = (String) nodes.nextElement();
    ALNode node = getLayoutNode(nodeId);
    if ( node != null && nodeId != null ) {
     if ( !moveNodeToLostFolder(nodeId) )
      LogService.log(LogService.INFO, "Unable to move the pushed fragment with ID="+node.getFragmentId()+" to the lost folder");
    }
  }
 }


  /**
     * Moves the nodes to the lost folder if they don't satisfy their restrictions
     * @exception PortalException if an error occurs
     */
  protected void moveWrongNodesToLostFolder() throws PortalException {
    moveWrongNodesToLostFolder(getRootFolderId(),0);
  }

   /**
     * Moves the nodes to the lost folder if they don't satisfy their restrictions
     * @param nodeId a <code>String</code> node ID
     * @param depth a depth of the given node
     * @exception PortalException if an error occurs
     */
  private void moveWrongNodesToLostFolder(String nodeId, int depth) throws PortalException {

      ALNode node = getLayoutNode(nodeId);

      // Checking restrictions on the node
      Vector restrictions = node.getRestrictionsByPath(UserLayoutRestriction.LOCAL_RESTRICTION);
      for ( int i = 0; i < restrictions.size(); i++ ) {
         IUserLayoutRestriction restriction = (IUserLayoutRestriction)restrictions.get(i);

         // check other restrictions except priority and depth
         if ( ( restriction.getRestrictionType() & (RestrictionTypes.DEPTH_RESTRICTION | RestrictionTypes.PRIORITY_RESTRICTION )) == 0
                && !restriction.checkRestriction(node) ) {
            moveNodeToLostFolder(nodeId);
            break;
         }
      }

      // Checking the depth restriction
      if ( !checkRestriction(nodeId,RestrictionTypes.DEPTH_RESTRICTION,depth+"") ) {
          moveNodeToLostFolder(nodeId);
      }

      // Checking children related restrictions on the children if they exist
      restrictions = node.getRestrictionsByPath("children");
      boolean isFolder = (node.getNodeType() == IUserLayoutNodeDescription.FOLDER );
      if ( isFolder ) {
       for ( int i = 0; i < restrictions.size(); i++ ) {
         IUserLayoutRestriction restriction = (IUserLayoutRestriction)restrictions.get(i);
          for ( String nextId = ((ALFolder)node).getFirstChildNodeId(); nextId != null; ) {
           ALNode nextNode = getLayoutNode(nextId);
           String tmpNodeId = nextNode.getNextNodeId();
           if ( (restriction.getRestrictionType() & RestrictionTypes.DEPTH_RESTRICTION) == 0 &&
                !restriction.checkRestriction(nextNode) ) {
              moveNodeToLostFolder(nextId);
           }
            nextId = tmpNodeId;
          }
       }
      }

      // Checking parent related restrictions on the parent if it exists
      String parentNodeId = node.getParentNodeId();
      if ( parentNodeId != null ) {
       restrictions = node.getRestrictionsByPath("parent");
       ALNode parentNode = getLayoutNode(parentNodeId);
       for ( int i = 0; i < restrictions.size(); i++ ) {
          IUserLayoutRestriction restriction = (IUserLayoutRestriction)restrictions.get(i);
          if ( (restriction.getRestrictionType() & RestrictionTypes.DEPTH_RESTRICTION) == 0 &&
                !restriction.checkRestriction(parentNode) ) {
              moveNodeToLostFolder(nodeId);
              break;
          }
       }
      }

        if ( isFolder ) {
            ++depth;
            String firstChildId = ((ALFolder)node).getFirstChildNodeId();
            for ( String nextId = firstChildId; nextId != null; ) {
                ALNode tmpNode = getLayoutNode(nextId);
                String tmpNodeId = tmpNode.getNextNodeId();
                if ( nextId.equals(firstChildId) ) {
                    while ( !changeSiblingNodesOrder(firstChildId) )
                     moveNodeToLostFolder(getLastSiblingNode(firstChildId).getId());
                }
                moveWrongNodesToLostFolder(nextId,depth);
                nextId = tmpNodeId;
            }
        }

  }

  // Gets the content of the lost folder
  public String getLostFolderXML() throws PortalException {
    Document document = DocumentFactory.getNewDocument();
    layout.writeTo(ALFolderDescription.LOST_FOLDER_ID,document);
    return org.jasig.portal.utils.XML.serializeNode(document);
  }

  /**
     * Checks the local restriction specified by the parameters below
     * @param node a <code>ALNode</code> node to be checked
     * @param restrictionType a restriction type
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  protected boolean checkRestriction(ALNode node, int restrictionType, String propertyValue ) throws PortalException {
    return checkRestriction(node, restrictionType, UserLayoutRestriction.LOCAL_RESTRICTION, propertyValue);
  }


  /**
     * Checks the necessary restrictions while adding a new node
     * @param nodeDesc a <code>IALNodeDescription</code> node description of a new node to be added
     * @param parentId a <code>String</code> parent node ID
     * @param nextSiblingId a <code>String</code> next sibling node ID
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  private boolean checkAddRestrictions( IALNodeDescription nodeDesc, String parentId, String nextSiblingId ) throws PortalException {
    String newNodeId = nodeDesc.getId();
    ALNode newNode = null;
    if ( newNodeId == null ) {
      if ( nodeDesc instanceof IALChannelDescription )
        newNode = new ALChannel((IALChannelDescription)nodeDesc);
      else
        newNode = new ALFolder((IALFolderDescription)nodeDesc);
    } else
        newNode = getLayoutNode(newNodeId);

    ALNode parentNode = getLayoutNode(parentId);

    if ( !(parentNode.getNodeType()==IUserLayoutNodeDescription.FOLDER ) )
      throw new PortalException ("The target parent node should be a folder!");

    //if ( checkRestriction(parentNode,RestrictionTypes.IMMUTABLE_RESTRICTION,"false") ) {
    if ( !parentNode.getNodeDescription().isImmutable() ) {

     // Checking children related restrictions
     Vector restrictions = parentNode.getRestrictionsByPath("children");
     for ( int i = 0; i < restrictions.size(); i++ ) {
         IUserLayoutRestriction restriction = (IUserLayoutRestriction)restrictions.get(i);
         if ( (restriction.getRestrictionType() & RestrictionTypes.DEPTH_RESTRICTION) == 0 &&
                !restriction.checkRestriction(newNode) )
            return false;
     }

     // Checking parent related restrictions
     restrictions = newNode.getRestrictionsByPath("parent");
     for ( int i = 0; i < restrictions.size(); i++ ) {
          IUserLayoutRestriction restriction = (IUserLayoutRestriction)restrictions.get(i);
          if ( (restriction.getRestrictionType() & RestrictionTypes.DEPTH_RESTRICTION) == 0 &&
                !restriction.checkRestriction(parentNode) )
            return false;
     }

     // Considering two cases if the node is new or it is already in the user layout
     if ( newNodeId != null ) {
      // Checking depth restrictions for the node and all its descendants (if there are any)
      if ( !checkDepthRestrictions(newNodeId,parentId) )
         return false;
     } else
         return checkRestriction(newNode,RestrictionTypes.DEPTH_RESTRICTION,(getDepth(parentId)+1)+"");

     // Checking sibling nodes order
     return changeSiblingNodesPriorities(newNode,parentId,nextSiblingId,false);

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
  private boolean checkMoveRestrictions( String nodeId, String newParentId, String nextSiblingId ) throws PortalException {
    ALNode node = getLayoutNode(nodeId);
    ALNode oldParentNode = getLayoutNode(node.getParentNodeId());
    ALNode newParentNode = getLayoutNode(newParentId);

    /*if ( checkRestriction(oldParentNode,RestrictionTypes.IMMUTABLE_RESTRICTION,"false") &&
         checkRestriction(newParentNode,RestrictionTypes.IMMUTABLE_RESTRICTION,"false") ) {*/
    if ( !oldParentNode.getNodeDescription().isImmutable() && !newParentNode.getNodeDescription().isImmutable() ) {

     if ( !oldParentNode.equals(newParentNode) ) {
      // Checking children related restrictions
      Vector restrictions = newParentNode.getRestrictionsByPath("children");
      for ( int i = 0; i < restrictions.size(); i++ ) {
         IUserLayoutRestriction restriction = (IUserLayoutRestriction)restrictions.get(i);
         if ( (restriction.getRestrictionType() & RestrictionTypes.DEPTH_RESTRICTION) == 0 &&
                !restriction.checkRestriction(node) )
            return false;
      }

      // Checking parent related restrictions
      restrictions = node.getRestrictionsByPath("parent");
      for ( int i = 0; i < restrictions.size(); i++ ) {
          IUserLayoutRestriction restriction = (IUserLayoutRestriction)restrictions.get(i);
          if ( (restriction.getRestrictionType() & RestrictionTypes.DEPTH_RESTRICTION) == 0 &&
                !restriction.checkRestriction(newParentNode) )
            return false;
      }

      // Checking depth restrictions for the node and all its descendants
      if ( !checkDepthRestrictions(nodeId,newParentId) )
            return false;
     }

      // Checking sibling nodes order
      return changeSiblingNodesPriorities(node,newParentId,nextSiblingId,false);

    } else
        return false;
  }


  /**
     * Checks the necessary restrictions while deleting a node
     * @param nodeId a <code>String</code> node ID of a node to be deleted
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  private boolean checkDeleteRestrictions( String nodeId ) throws PortalException {
    ALNode node = getLayoutNode(nodeId);
    if ( nodeId == null || node == null ) return true;
    //if ( checkRestriction(node.getParentNodeId(),RestrictionTypes.IMMUTABLE_RESTRICTION,"false") ) {
    if ( !getLayoutNode(node.getParentNodeId()).getNodeDescription().isImmutable() ) {
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
  private boolean checkDepthRestrictions(String nodeId,String newParentId) throws PortalException {
    if ( nodeId == null ) return true;
    int nodeDepth = getDepth(nodeId);
    int parentDepth = getDepth(newParentId);
    if ( nodeDepth == parentDepth+1 ) return true;
    return checkDepthRestrictions(nodeId,newParentId,nodeDepth);
  }


  /**
     * Recursively checks the depth restrictions beginning with a given node
     * @param nodeId a <code>String</code> node ID
     * @param newParentId a <code>String</code> new parent node ID
     * @param parentDepth a parent depth
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  private boolean checkDepthRestrictions(String nodeId,String newParentId,int parentDepth) throws PortalException {
    ALNode node = getLayoutNode(nodeId);
    // Checking restrictions for the node
    if ( !checkRestriction(nodeId,RestrictionTypes.DEPTH_RESTRICTION,(parentDepth+1)+"") )
            return false;
    if ( node.getNodeType() == IUserLayoutNodeDescription.FOLDER ) {
     for ( String nextId = ((ALFolder)node).getFirstChildNodeId(); nextId != null; nextId = node.getNextNodeId() ) {
      node = getLayoutNode(nextId);
      if ( !checkDepthRestrictions(nextId,node.getParentNodeId(),parentDepth+1) )
            return false;
     }
    }
    return true;
  }


  /**
     * Gets the tree depth for a given node
     * @param nodeId a <code>String</code> node ID
     * @return a depth value
     * @exception PortalException if an error occurs
     */
  public int getDepth(String nodeId) throws PortalException {
	 int depth = 0;
	 for ( String parentId = getParentId(nodeId); parentId != null; parentId = getParentId(parentId), depth++ );
	 return depth;
  }


  /**
     * Moves the node to the lost folder
     * @param nodeId a <code>String</code> node ID
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  private boolean moveNodeToLostFolder(String nodeId) throws PortalException {
   ALFolder lostFolder = getLayoutFolder(lostFolderId);
   if ( lostFolder == null ) {
    lostFolder = ALFolder.createLostFolder();
   }
    // Moving the node to the lost folder
    return moveNode(nodeId,lostFolder.getId(),null);
  }


  /**
     * Gets the restriction specified by the parameters below
     * @param node a <code>ALNode</code> node
     * @param restrictionType a restriction type
     * @param restrictionPath a <code>String</code> restriction path
     * @return a <code>IUserLayoutRestriction</code> instance
     * @exception PortalException if an error occurs
     */
  private static IUserLayoutRestriction getRestriction( ALNode node, int restrictionType, String restrictionPath ) throws PortalException {
     return node.getRestriction(UserLayoutRestriction.getRestrictionName(restrictionType,restrictionPath));
  }

   /**
     * Return a priority restriction for the given node.
     * @return a <code>PriorityRestriction</code> object
     * @exception PortalException if an error occurs
     */
  public static PriorityRestriction getPriorityRestriction( ALNode node ) throws PortalException {
     PriorityRestriction priorRestriction = getPriorityRestriction(node,UserLayoutRestriction.LOCAL_RESTRICTION);
     if ( priorRestriction == null ) {
       priorRestriction = (PriorityRestriction)
         UserLayoutRestrictionFactory.createRestriction(RestrictionTypes.PRIORITY_RESTRICTION,"0-"+java.lang.Integer.MAX_VALUE,UserLayoutRestriction.LOCAL_RESTRICTION);
     }
     return priorRestriction;
  }


  /**
     * Return a priority restriction for the given node.
     * @return a <code>PriorityRestriction</code> object
     * @exception PortalException if an error occurs
     */
  private static PriorityRestriction getPriorityRestriction( ALNode node, String restrictionPath ) throws PortalException {
     return (PriorityRestriction) getRestriction(node,RestrictionTypes.PRIORITY_RESTRICTION,restrictionPath);
  }


  /**
     * Change if it's possible priority values for all the sibling nodes
     * @param node a <code>String</code> a node from the sibling line
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  protected boolean changeSiblingNodesPriorities ( String nodeId ) throws PortalException {
     ALNode firstNode = getFirstSiblingNode(nodeId);
     String parentId = firstNode.getParentNodeId();
     for ( String id = firstNode.getId(); id != null; ) {
       ALNode node = getLayoutNode(id);
       String nextId = node.getNextNodeId();
       if ( !changeSiblingNodesPriorities(node,parentId,nextId,false) )
            return false;
        id = nextId;
     }
        return true;
  }


  /**
     * Change if it's possible priority values for all the sibling nodes after trying to add a new node
     * @param node a <code>String</code> a node to be added
     * @param parentNodeId a <code>String</code> parent node ID
     * @param nextNodeId a <code>String</code> next sibling node ID
     * @param justCheck a boolean
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  protected boolean changeSiblingNodesPriorities(ALNode node, String parentNodeId, String nextNodeId, boolean justCheck ) throws PortalException {

    ALNode firstNode = null, nextNode = null;
    String firstNodeId = null;
    int priority = 0, nextPriority = 0, prevPriority = 0, range[] = null, prevRange[] = null, nextRange[] = null;
    PriorityRestriction priorityRestriction = null;
    String nodeId = node.getId();

    ALFolder parent = getLayoutFolder(parentNodeId);
    if ( parentNodeId != null ) {
        firstNodeId = parent.getFirstChildNodeId();
        // if the node is equal the first node in the sibling line we get the next node
        if ( nodeId.equals(firstNodeId) )
            firstNodeId = node.getNextNodeId();
        if ( firstNodeId == null ) return true;
    } else
         return false;

    firstNode = getLayoutNode(firstNodeId);

    if ( nextNodeId != null ) {
      nextNode = getLayoutNode(nextNodeId);
      nextPriority = nextNode.getPriority();
      priorityRestriction = getPriorityRestriction(nextNode);
      nextRange = priorityRestriction.getRange();
    }


    priority = node.getPriority();
    priorityRestriction = getPriorityRestriction(node);
    range = priorityRestriction.getRange();

    // If we add a new node to the beginning of the sibling line
    if ( firstNodeId.equals(nextNodeId) ) {

      if ( range[1] <= nextRange[0] ) return false;

      if ( priority > nextPriority ) return true;

      if ( range[1] > nextPriority ) {
           if ( !justCheck ) node.setPriority(range[1]);
           return true;
      }

      if ( (nextPriority+1) <= range[1] && (nextPriority+1) >= range[0] ) {
             if ( !justCheck ) node.setPriority(nextPriority+1);
             return true;
      }
    }

    // If we add a new node to the end of the sibling line
    if ( nextNode == null ) {

      // Getting the last node
      ALNode lastNode = getLastSiblingNode(firstNodeId);

      // if the node to be added is equal the last node in the sibling line
      if ( nodeId.equals(lastNode.getId()) )
          lastNode = getLayoutNode(lastNode.getPreviousNodeId());

      int lastPriority = lastNode.getPriority();
      PriorityRestriction lastPriorityRestriction = getPriorityRestriction(lastNode);
      int[] lastRange = lastPriorityRestriction.getRange();


      if ( range[0] >= lastRange[1] ) return false;

      if ( priority < lastPriority ) return true;

      if ( range[0] < lastPriority ) {
           if ( !justCheck ) node.setPriority(range[0]);
           return true;
      }

      if ( (lastPriority-1) <= range[1] && (lastPriority-1) >= range[0] ) {
             if ( !justCheck ) node.setPriority(lastPriority-1);
             return true;
      }

    }



    // If we add a new node in a general case
    if ( nextNode != null && !nextNode.equals(firstNode) && !node.equals(nextNode) ) {

      // Getting the last node
      ALNode prevNode = getLayoutNode(nextNode.getPreviousNodeId());

      prevPriority = prevNode.getPriority();
      PriorityRestriction lastPriorityRestriction = getPriorityRestriction(prevNode);
      prevRange = lastPriorityRestriction.getRange();


      if ( range[1] <= nextRange[0] || range[0] >= prevRange[1] ) return false;

      if ( priority < prevPriority && priority > nextPriority ) return true;

      int maxPossibleLowValue = Math.max(range[0],nextPriority+1);
      int minPossibleHighValue = Math.min(range[1],prevPriority-1);

      if ( minPossibleHighValue >= maxPossibleLowValue ) {
           if ( !justCheck ) node.setPriority(minPossibleHighValue);
           return true;
      }

      // the general case
    }


    //Vector ranges = new Vector();
    Vector priorities = null;
    if ( !justCheck ) priorities = new Vector();
    int tmpPriority = Integer.MAX_VALUE, value;

    // Fill out the vector by priority values
    for ( String nextId = firstNodeId; nextId != null; ) {
      ALNode curNode = getLayoutNode(nextId);
      // if the node to be added/moved is equal to the current node (it's in the same sibling line)
      if ( nextId.equals(node.getId()) ) {
           nextId = curNode.getNextNodeId();
           continue;
      }

      int[] curRange = (nextId.equals(nextNodeId))?nextRange:getPriorityRestriction(curNode).getRange();
      if ( (value = Math.min(curRange[1],tmpPriority-1)) < tmpPriority ) {
        if ( !justCheck ) priorities.add(new Integer(value));
        tmpPriority = value;
      } else
          return false;
      //ranges.add(new int[] { curRange[0], value });
      // This is a strange line: if (!nextNodeId.equals(nextId)) nextId = node.getNextNodeId();
      nextId = curNode.getNextNodeId();
    }

   if ( !justCheck ) {
    // Setting priority values to the sibling nodes
    int i = 0;
    for ( String nextId = firstNodeId; nextId != null; i++ ) {
      ALNode curNode = getLayoutNode(nextId);
      // if the node to be added/moved is equal to the current node (it's in the same sibling line)
      if ( nextId.equals(node.getId()) ) {
           nextId = curNode.getNextNodeId();
           i--;
           continue;
      }
      int prior = ((Integer)priorities.get(i)).intValue();
      if (nextId.equals(nextNodeId))
       nextNode.setPriority(prior);
      else
       curNode.setPriority(prior);

      nextId = curNode.getNextNodeId();
    }
   }

    // ssss
    return true;
  }



  /**
     * Change the sibling nodes order depending on their priority values
     * @param firstNodeId a <code>String</code> first node ID in the sibling line
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  protected boolean changeSiblingNodesOrder(String firstNodeId) throws PortalException {
    if ( firstNodeId == null )
      throw new PortalException ( "The first node ID in the sibling line cannot be NULL!" );
    ALNode firstNode = getLayoutNode(firstNodeId);
    String parentNodeId = firstNode.getParentNodeId();
    boolean rightOrder = true;
    ALNode node = null;
    for ( String nextNodeId = firstNodeId; nextNodeId != null; ) {
      node = getLayoutNode(nextNodeId);
      nextNodeId = node.getNextNodeId();
      if ( nextNodeId != null ) {
       ALNode nextNode = getLayoutNode(nextNodeId);
       if ( node.getPriority() <= nextNode.getPriority() ) {
           rightOrder = false;
           break;
       }
      }
    }

    if ( rightOrder ) return true;

    // Check if the current order is right
    if ( changeSiblingNodesPriorities(firstNodeId) ) return true;

    // Choosing more suitable order of the nodes in the sibling line
    ALNode lastNode = getLastSiblingNode(node.getId());
    String lastNodeId = lastNode.getId();
    for ( String prevNodeId = lastNodeId; prevNodeId != null; ) {
      for ( String nodeId = lastNodeId; nodeId != null; ) {
       if ( !nodeId.equals(prevNodeId) ) {
        if ( moveNode(prevNodeId,parentNodeId,nodeId) )
          if ( changeSiblingNodesPriorities(firstNodeId) )
            return true;
       }
        nodeId = getLayoutNode(nodeId).getPreviousNodeId();
      }
        // Checking the last place in the sibling line
        if ( moveNode(prevNodeId,parentNodeId,null) )
          if ( changeSiblingNodesPriorities(firstNodeId) )
            return true;

        prevNodeId = getLayoutNode(prevNodeId).getPreviousNodeId();
    }
        return false;
  }


   /**
     * Return a cache key, uniqly corresponding to the composition and the structure of the user layout.
     *
     * @return a <code>String</code> value
     * @exception PortalException if an error occurs
     */
  public String getCacheKey() throws PortalException {
      return cacheKey;
  }

  /**
     * Output a tree of a user layout (with appropriate markings) defined by a particular node into
     * a <code>ContentHandler</code>
     * @param contentHandler a <code>ContentHandler</code> value
     * @exception PortalException if an error occurs
     */
  public void getUserLayout(ContentHandler contentHandler) throws PortalException {
    layout.writeTo(contentHandler);
  }



  /**
     * Output subtree of a user layout (with appropriate markings) defined by a particular node into
     * a <code>ContentHandler</code>
     *
     * @param nodeId a <code>String</code> a node determining a user layout subtree.
     * @param contentHandler a <code>ContentHandler</code> value
     * @exception PortalException if an error occurs
     */
  public void getUserLayout(String nodeId, ContentHandler contentHandler) throws PortalException {
     layout.writeTo(nodeId,contentHandler);
  }


    private ALNode getLayoutNode(String nodeId) {
     return layout.getLayoutNode(nodeId);
    }

    private ALFolder getLayoutFolder(String folderId) {
     return layout.getLayoutFolder(folderId);
    }

    private ALNode getLastSiblingNode ( String nodeId ) {
     return layout.getLastSiblingNode(nodeId);
    }

    private ALNode getFirstSiblingNode ( String nodeId ) {
     return layout.getFirstSiblingNode(nodeId);
    }




    public Document getUserLayoutDOM() throws PortalException {
      Document document = DocumentFactory.getNewDocument();
      layout.writeTo(document);
      return document;
    }

    private void setUserLayoutDOM( Node n, String parentNodeId, Hashtable layoutData ) throws PortalException {

      Element node = (Element) n;

      NodeList childNodes = node.getChildNodes();

      IALNodeDescription nodeDesc = ALNode.createUserLayoutNodeDescription(node);

      String nodeId = node.getAttribute("ID");

      nodeDesc.setId(nodeId);
      nodeDesc.setName(node.getAttribute("name"));
      nodeDesc.setFragmentId(node.getAttribute("fragmentID"));
      nodeDesc.setHidden(CommonUtils.strToBool(node.getAttribute("hidden")));
      nodeDesc.setImmutable(CommonUtils.strToBool(node.getAttribute("immutable")));
      nodeDesc.setUnremovable(CommonUtils.strToBool(node.getAttribute("unremovable")));
      nodeDesc.setHidden(CommonUtils.strToBool(node.getAttribute("hidden")));


      ALNode layoutNode = null;
      IALChannelDescription channelDesc = null;

      if (nodeDesc instanceof IALChannelDescription)
          channelDesc = (IALChannelDescription) nodeDesc;

        // Getting parameters and restrictions
        for ( int i = 0; i < childNodes.getLength(); i++ ) {
          Node childNode = childNodes.item(i);
          String nodeName = childNode.getNodeName();
          NamedNodeMap attributes = childNode.getAttributes();
          if ( IAggregatedLayout.PARAMETER.equals(nodeName) && channelDesc != null ) {
           Node paramNameNode = attributes.getNamedItem("name");
           String paramName = (paramNameNode!=null)?paramNameNode.getFirstChild().getNodeValue():null;
           Node paramValueNode = attributes.getNamedItem("value");
           String paramValue = (paramValueNode!=null)?paramValueNode.getFirstChild().getNodeValue():null;
           Node overParamNode = attributes.getNamedItem("override");
           String overParam = (overParamNode!=null)?overParamNode.getFirstChild().getNodeValue():null;

           if ( paramName != null ) {
            channelDesc.setParameterValue(paramName, paramValue);
            channelDesc.setParameterOverride(paramName, "yes".equalsIgnoreCase(overParam)?true:false);
           }
          } else if ( IAggregatedLayout.RESTRICTION.equals(nodeName) ) {
             Node restrPathNode = attributes.getNamedItem("path");
             String restrPath = (restrPathNode!=null)?restrPathNode.getFirstChild().getNodeValue():null;
             Node restrValueNode = attributes.getNamedItem("value");
             String restrValue = (restrValueNode!=null)?restrValueNode.getFirstChild().getNodeValue():null;
             Node restrTypeNode = attributes.getNamedItem("type");
             String restrType = (restrTypeNode!=null)?restrTypeNode.getFirstChild().getNodeValue():"0";

             if ( restrValue != null ) {
              IUserLayoutRestriction restriction = UserLayoutRestrictionFactory.createRestriction(CommonUtils.parseInt(restrType),restrValue,restrPath);
              nodeDesc.addRestriction(restriction);
             }
            }
        }


      if ( channelDesc != null ) {
        channelDesc.setChannelPublishId(node.getAttribute("chanID"));
        channelDesc.setChannelTypeId(node.getAttribute("typeID"));
        channelDesc.setClassName(node.getAttribute("class"));
        channelDesc.setDescription(node.getAttribute("description"));
        channelDesc.setEditable(CommonUtils.strToBool(node.getAttribute("editable")));
        channelDesc.setHasAbout(CommonUtils.strToBool(node.getAttribute("hasAbout")));
        channelDesc.setHasHelp(CommonUtils.strToBool(node.getAttribute("hasHelp")));
        channelDesc.setIsSecure(CommonUtils.strToBool(node.getAttribute("secure")));
        channelDesc.setFunctionalName(node.getAttribute("fname"));
        channelDesc.setTimeout(Long.parseLong(node.getAttribute("timeout")));
        channelDesc.setTitle(node.getAttribute("title"));

          // Adding to the layout
          layoutNode = new ALChannel(channelDesc);
      } else {
          layoutNode = new ALFolder((IALFolderDescription)nodeDesc);
        }
          // Setting priority value
          layoutNode.setPriority(CommonUtils.parseInt(node.getAttribute("priority"),0));

          ALFolder parentFolder = getLayoutFolder(parentNodeId);
          // Binding the current node to the parent child list and parentNodeId to the current node
          if ( parentFolder != null ) {
           //parentFolder.addChildNode(nodeDesc.getId());
           layoutNode.setParentNodeId(parentNodeId);
          }


          Element nextNode = (Element) node.getNextSibling();
          Element prevNode = (Element) node.getPreviousSibling();

          if ( nextNode != null && isNodeFolderOrChannel(nextNode) )
             layoutNode.setNextNodeId(nextNode.getAttribute("ID"));
          if ( prevNode != null && isNodeFolderOrChannel(prevNode) )
             layoutNode.setPreviousNodeId(prevNode.getAttribute("ID") );

          // Setting the first child node ID
          if ( layoutNode.getNodeType() == IUserLayoutNodeDescription.FOLDER ) {
            String id = ((Element)node.getFirstChild()).getAttribute("ID");
            if ( id != null && id.length() > 0 )
             ((ALFolder)layoutNode).setFirstChildNodeId(id);
          }

          // Putting the LayoutNode object into the layout
          layoutData.put(nodeDesc.getId(), layoutNode);

          // Recurrence for all children
          for ( int i = 0; i < childNodes.getLength() && (layoutNode.getNodeType()==IUserLayoutNodeDescription.FOLDER); i++ ) {
            Element childNode = (Element) childNodes.item(i);
            if ( isNodeFolderOrChannel ( childNode ) )
             setUserLayoutDOM ( childNode, nodeDesc.getId(), layoutData );
          }

    }

    public void setUserLayoutDOM( Document domLayout ) throws PortalException {
      Hashtable layoutData = new Hashtable();
      String rootId = getRootFolderId();
      Element rootNode = (Element) domLayout.getDocumentElement().getFirstChild();
      ALFolder rootFolder = new ALFolder((IALFolderDescription)ALNode.createUserLayoutNodeDescription(rootNode));
      rootFolder.setFirstChildNodeId(((Element)rootNode.getFirstChild()).getAttribute("ID"));
      layoutData.put(rootId,rootFolder);
      NodeList childNodes = rootNode.getChildNodes();
      for ( int i = 0; i < childNodes.getLength(); i++ )
       setUserLayoutDOM ( childNodes.item(i), rootId, layoutData );
      layout.setLayoutData(layoutData);
      updateCacheKey();
    }

    private boolean isNodeFolderOrChannel ( Element node ) {
      String nodeName = node.getNodeName();
      return ( IAggregatedLayout.FOLDER.equals(nodeName) || IAggregatedLayout.CHANNEL.equals(nodeName) );
    }

    public void setLayoutStore(IUserLayoutStore layoutStore ) {
      this.layoutStore = (AggregatedUserLayoutStore) layoutStore;
    }


    public void loadUserLayout() throws PortalException {
     try {
      if ( layoutStore != null ) {
       fragmentId = null;
       layout = (AggregatedLayout) layoutStore.getAggregatedLayout(person,userProfile);
       layout.setLayoutManager(this);
       fragments = (Hashtable) layoutStore.getFragments(person);
       if ( fragments != null && fragments.size() > 0 ) 
         layout.setFragments(fragments);
       // Setting the first child node id for the root node to NULL if it does not exist in the layout
       ALFolder rootFolder = getLayoutFolder(getRootFolderId());
       String firstChildId = rootFolder.getFirstChildNodeId();
       if ( firstChildId != null && getLayoutNode(firstChildId) == null )
        rootFolder.setFirstChildNodeId(null);
       // Moving the wrong pushed fragments to the lost folder
       moveWrongFragmentsToLostFolder();
       // Checking restrictions and move "wrong" nodes to the lost folder
       moveWrongNodesToLostFolder();
       updateCacheKey();
      }
     } catch ( Exception e ) {
         e.printStackTrace();
         throw new PortalException(e.getMessage());
       }
    }

    public void saveUserLayout() throws PortalException {
      try {
        if ( !isLayoutFragment() )
          layoutStore.setAggregatedLayout(person,userProfile,layout);
        else
          saveFragment();
          
         updateCacheKey();
      } catch ( Exception e ) {
        throw new PortalException(e.getMessage());
      }
    }
    
	public String createFragment( String fragmentName, String fragmentDesc ) throws PortalException {
	  try {
			 // Creating an empty layout with a root folder
			 String newFragmentId = layoutStore.getNextFragmentId();
			 ALFragment fragment = new ALFragment (newFragmentId,this);
			 ALNode rootFolder = ALFolder.createRootFolder();
			 Hashtable layoutData = new Hashtable();
			 layoutData.put(getRootFolderId(),rootFolder);
			 fragment.setLayoutData(layoutData);
			 fragment.setName(fragmentName+"_"+newFragmentId);
			 fragment.setDescription(fragmentDesc+" (id="+newFragmentId+")");
		
		     // Getting the list of the fragments	
			 fragments = (Hashtable) layoutStore.getFragments(person);
		     if ( fragments != null && fragments.size() > 0 ) 
			  fragment.setFragments(fragments);
			  
			 this.fragmentId = newFragmentId;
			
			 // Setting the layout to the new fragment
			 layout = fragment;
			
			 updateCacheKey();
			 
			 // Return a new fragment ID
			 return fragmentId;
			 
		  } catch ( Exception e ) {
			throw new PortalException(e.getMessage());
		  }
	}

    public void loadFragment( String fragmentId ) throws PortalException {
      try {
      
        layout = (ALFragment) layoutStore.getFragment(person,fragmentId);
        layout.setLayoutManager(this);
        fragments = (Hashtable) layoutStore.getFragments(person);
		if ( fragments != null && fragments.size() > 0 ) 
		  layout.setFragments(fragments);
        this.fragmentId = fragmentId;
        // Checking restrictions and move "wrong" nodes to the lost folder
        //moveWrongNodesToLostFolder();
        updateCacheKey();
      } catch ( Exception e ) {
        throw new PortalException(e.getMessage());
      }
    }

    public void saveFragment() throws PortalException {
      try {
       if ( isLayoutFragment() ) {
        layoutStore.setFragment(person,(ILayoutFragment)layout);
		fragments = (Hashtable) layoutStore.getFragments(person);
		if ( fragments != null && fragments.size() > 0 ) 
		  layout.setFragments(fragments);
       }
        updateCacheKey();
      } catch ( Exception e ) {
        throw new PortalException(e.getMessage());
      }
    }
    
	/**
		 * Deletes the current fragment if the layout is a fragment
		 * @exception PortalException if an error occurs
		 */
	public void deleteFragment() throws PortalException {
		try {
		  if ( isLayoutFragment() ) {
			layoutStore.deleteFragment(person,fragmentId);
		  }
		    loadUserLayout();
			updateCacheKey();
	    } catch ( Exception e ) {
			throw new PortalException(e.getMessage());
		  }		
	}

    private boolean isLayoutFragment() {
      return ( fragmentId != null );
    }

    public IUserLayoutNodeDescription getNode(String nodeId) throws PortalException {
      return layout.getNodeDescription(nodeId);
    }


    public synchronized boolean moveNode(String nodeId, String parentId,String nextSiblingId) throws PortalException {

     // Checking restrictions if the parent is not the lost folder
     if ( !parentId.equals(IALFolderDescription.LOST_FOLDER_ID) )
      if ( !canMoveNode(nodeId,parentId,nextSiblingId) )
            return false;

     ALNode node = getLayoutNode(nodeId);
     ALFolder targetFolder = getLayoutFolder(parentId);
     ALFolder sourceFolder = getLayoutFolder(node.getParentNodeId());
     String sourcePrevNodeId = node.getPreviousNodeId();
     String sourceNextNodeId = node.getNextNodeId();
     ALNode targetNextNode = getLayoutNode(nextSiblingId);
     ALNode targetPrevNode = null, sourcePrevNode = null, sourceNextNode = null;
     String prevSiblingId = null;

      // If the nextNode != null we calculate the prev node from it otherwise we have to run to the last node in the sibling line

      if ( targetNextNode != null )
        targetPrevNode = getLayoutNode(targetNextNode.getPreviousNodeId());
      else
        targetPrevNode = getLastSiblingNode(targetFolder.getFirstChildNodeId());

      if ( targetPrevNode != null ) {
        targetPrevNode.setNextNodeId(nodeId);
        prevSiblingId = targetPrevNode.getId();
      }

      // Changing the previous node id for the new next sibling node
      if ( targetNextNode != null )
        targetNextNode.setPreviousNodeId(nodeId);


     if ( nodeId.equals(sourceFolder.getFirstChildNodeId()) ) {
      // Set the new first child node ID to the source folder
      sourceFolder.setFirstChildNodeId(node.getNextNodeId());
     }

     String firstChildId = targetFolder.getFirstChildNodeId();
     if ( firstChildId == null || firstChildId.equals(nextSiblingId) ) {
      // Set the new first child node ID to the target folder
      targetFolder.setFirstChildNodeId(nodeId);
     }

     // Set the new next node ID for the source previous node
     if ( sourcePrevNodeId != null ) {
       sourcePrevNode =  getLayoutNode(sourcePrevNodeId);
       sourcePrevNode.setNextNodeId(sourceNextNodeId);
     }

     // Set the new previous node ID for the source next node
     if ( sourceNextNodeId != null ) {
       sourceNextNode = getLayoutNode(sourceNextNodeId);
       sourceNextNode.setPreviousNodeId(sourcePrevNodeId);
     }


     node.setParentNodeId(parentId);
     node.setNextNodeId(nextSiblingId);
     node.setPreviousNodeId(prevSiblingId);


     // TO UPDATE THE APPROPRIATE INFO IN THE DB
     // TO BE DONE !!!!!!!!!!!
     boolean result = true;
     if ( autoCommit ) {
      if ( sourcePrevNode != null ) result &= layoutStore.updateUserLayoutNode(person,userProfile,sourcePrevNode);
      if ( sourceNextNode != null ) result &= layoutStore.updateUserLayoutNode(person,userProfile,sourceNextNode);
      if ( targetPrevNode != null ) result &= layoutStore.updateUserLayoutNode(person,userProfile,targetPrevNode);
      if ( targetNextNode != null ) result &= layoutStore.updateUserLayoutNode(person,userProfile,targetNextNode);
      result &= layoutStore.updateUserLayoutNode(person,userProfile,targetFolder);
      result &= layoutStore.updateUserLayoutNode(person,userProfile,sourceFolder);
      // Changing the node being moved
      result &= layoutStore.updateUserLayoutNode(person,userProfile,node);
     }

      updateCacheKey();
      return result;

    }

    public synchronized boolean deleteNode(String nodeId) throws PortalException {

       if ( nodeId == null ) return false;

       ALNode node = getLayoutNode(nodeId);

       if ( node == null ) return false;

       // Checking restrictions
       if ( !canDeleteNode(nodeId) ) {
           LogService.log(LogService.DEBUG, "The node with ID = '" + nodeId + "' cannot be deleted");
           return false;
       }



       // Deleting the node from the parent
       ALFolder parentFolder = getLayoutFolder(node.getParentNodeId());

       boolean result = false;
       //parentNode.deleteChildNode(nodeId);
       if ( nodeId.equals(parentFolder.getFirstChildNodeId()) ) {
         // Set the new first child node ID to the source folder
         parentFolder.setFirstChildNodeId(node.getNextNodeId());
         // Update it in the database
         if ( autoCommit ) result = layoutStore.updateUserLayoutNode(person,userProfile,parentFolder);
       }

       // Changing the next node id for the previous sibling node
       // and the previous node for the next sibling node
       String prevSiblingId = node.getPreviousNodeId();
       String nextSiblingId = node.getNextNodeId();

       if ( prevSiblingId != null ) {
        ALNode prevNode = getLayoutNode(prevSiblingId);
        prevNode.setNextNodeId(nextSiblingId);
        if ( autoCommit ) result = layoutStore.updateUserLayoutNode(person,userProfile,prevNode);
       }

       if ( nextSiblingId != null ) {
        ALNode nextNode = getLayoutNode(nextSiblingId);
        nextNode.setPreviousNodeId(prevSiblingId);
        if ( autoCommit ) result = layoutStore.updateUserLayoutNode(person,userProfile,nextNode);
       }

       // DELETE THE NODE FROM THE DB
       if ( autoCommit )
         result = layoutStore.deleteUserLayoutNode(person,userProfile,node);

       // Deleting the nodefrom the hashtable and returning the result value
       result = (layout.getLayoutData().remove(nodeId)!=null) && ((autoCommit)?result:true);

       updateCacheKey();
       return result;

    }

    public synchronized IUserLayoutNodeDescription addNode(IUserLayoutNodeDescription nodeDesc, String parentId,String nextSiblingId) throws PortalException {

        // Checking restrictions
        if ( !canAddNode(nodeDesc,parentId,nextSiblingId) )
               return null;

        ALFolder parentFolder = getLayoutFolder(parentId);
        ALNode nextNode = getLayoutNode(nextSiblingId);
        ALNode prevNode =  null;

        // If the nextNode != null we calculate the prev node from it otherwise we have to run to the last node in the sibling line
        if ( nextNode != null )
             prevNode = getLayoutNode(nextNode.getPreviousNodeId());
        else
             prevNode = getLastSiblingNode( parentFolder.getFirstChildNodeId());



        ALNode layoutNode=ALNode.createALNode(nodeDesc);


        // Setting the parent node ID
        layoutNode.setParentNodeId(parentId);


        if ( prevNode != null ) layoutNode.setPreviousNodeId(prevNode.getId());
        if ( nextNode != null ) layoutNode.setNextNodeId(nextSiblingId);


        // Add the new node to the database and get the node with a new node ID
        layoutNode = layoutStore.addUserLayoutNode(person,userProfile,layoutNode);
        String nodeId = layoutNode.getId();

        // Putting the new node into the hashtable
        layout.getLayoutData().put(nodeId,layoutNode);


        if ( prevNode != null ) prevNode.setNextNodeId(nodeId);


        if ( nextNode != null ) nextNode.setPreviousNodeId(nodeId);

        // Setting new child node ID to the parent node
        if ( prevNode == null )
          parentFolder.setFirstChildNodeId(nodeId);

        // TO UPDATE ALL THE NEIGHBOR NODES IN THE DATABASE
        if ( nextNode != null )
         layoutStore.updateUserLayoutNode(person,userProfile,nextNode);

        if ( prevNode != null )
         layoutStore.updateUserLayoutNode(person,userProfile,prevNode);

        // Update the parent node
        layoutStore.updateUserLayoutNode(person,userProfile,parentFolder);


        updateCacheKey();
        return layoutNode.getNodeDescription();
    }

    private void changeDescendantsBooleanProperties (IALNodeDescription nodeDesc,IALNodeDescription oldNodeDesc, String nodeId) throws PortalException {
      changeDescendantsBooleanProperties(nodeDesc.isHidden()==oldNodeDesc.isHidden(),nodeDesc.isImmutable()==oldNodeDesc.isImmutable(),
                                         nodeDesc.isUnremovable()==oldNodeDesc.isUnremovable(),nodeDesc,nodeId);
      updateCacheKey();
    }

    private void changeDescendantsBooleanProperties (boolean hiddenValuesMatch, boolean immutableValuesMatch, boolean unremovableValuesMatch,
                                                                                IALNodeDescription nodeDesc, String nodeId) throws PortalException {

          ALNode node = getLayoutNode(nodeId);

          if ( node.getNodeType() == IUserLayoutNodeDescription.FOLDER ) {
           // Loop for all children
            String firstChildId = ((ALFolder)node).getFirstChildNodeId();
            for ( String nextNodeId = firstChildId; nextNodeId != null; ) {

             ALNode currentNode = getLayoutNode(nextNodeId);

             // Checking the hidden property if it's changed
             if ( !hiddenValuesMatch ) {
                 // Checking the hidden node restriction
                 boolean canChange = checkRestriction(currentNode,RestrictionTypes.HIDDEN_RESTRICTION,CommonUtils.boolToStr(nodeDesc.isHidden()));
                 // Checking the hidden parent node related restriction
                 canChange &= checkRestriction(currentNode.getParentNodeId(),RestrictionTypes.HIDDEN_RESTRICTION,"children",CommonUtils.boolToStr(nodeDesc.isHidden()));
              // Checking the hidden children node related restrictions
              if ( currentNode.getNodeType() == IUserLayoutNodeDescription.FOLDER ) {
               ALFolder folder = (ALFolder) node;
               //Loop for all children
               for ( String nextId = folder.getFirstChildNodeId(); nextId != null; nextId = getLayoutNode(nextId).getNextNodeId() )
                canChange &= checkRestriction(nextId,RestrictionTypes.HIDDEN_RESTRICTION,"parent",CommonUtils.boolToStr(nodeDesc.isHidden()));
              }
                // Changing the hidden value if canChange is true
                if ( canChange )
                 currentNode.getNodeDescription().setHidden(nodeDesc.isHidden());
             }

             // Checking the immutable property if it's changed
             if ( !immutableValuesMatch ) {
                 // Checking the immutable node restriction
                 boolean canChange = checkRestriction(currentNode,RestrictionTypes.IMMUTABLE_RESTRICTION,CommonUtils.boolToStr(nodeDesc.isImmutable()));
                 // Checking the immutable parent node related restriction
                 canChange &= checkRestriction(currentNode.getParentNodeId(),RestrictionTypes.IMMUTABLE_RESTRICTION,"children",CommonUtils.boolToStr(nodeDesc.isImmutable()));
              // Checking the immutable children node related restrictions
              if ( currentNode.getNodeType() == IUserLayoutNodeDescription.FOLDER ) {
               ALFolder folder = (ALFolder) node;
               //Loop for all children
               for ( String nextId = folder.getFirstChildNodeId(); nextId != null; nextId = getLayoutNode(nextId).getNextNodeId() )
                canChange &= checkRestriction(nextId,RestrictionTypes.IMMUTABLE_RESTRICTION,"parent",CommonUtils.boolToStr(nodeDesc.isImmutable()));
              }
                // Changing the immutable value if canChange is true
                if ( canChange )
                 currentNode.getNodeDescription().setImmutable(nodeDesc.isImmutable());
             }

             // Checking the unremovable property if it's changed
             if ( !unremovableValuesMatch ) {
                 // Checking the unremovable node restriction
                 boolean canChange = checkRestriction(currentNode,RestrictionTypes.UNREMOVABLE_RESTRICTION,CommonUtils.boolToStr(nodeDesc.isUnremovable()));
                 // Checking the unremovable parent node related restriction
                 canChange &= checkRestriction(currentNode.getParentNodeId(),RestrictionTypes.UNREMOVABLE_RESTRICTION,"children",CommonUtils.boolToStr(nodeDesc.isUnremovable()));
              // Checking the unremovable children node related restrictions
              if ( currentNode.getNodeType() == IUserLayoutNodeDescription.FOLDER ) {
               ALFolder folder = (ALFolder) node;
               //Loop for all children
               for ( String nextId = folder.getFirstChildNodeId(); nextId != null; nextId = getLayoutNode(nextId).getNextNodeId() )
                canChange &= checkRestriction(nextId,RestrictionTypes.UNREMOVABLE_RESTRICTION,"parent",CommonUtils.boolToStr(nodeDesc.isImmutable()));
              }
                // Changing the unremovable value if canChange is true
                if ( canChange )
                 currentNode.getNodeDescription().setUnremovable(nodeDesc.isUnremovable());
             }
             /// Recurrence
             changeDescendantsBooleanProperties(hiddenValuesMatch,immutableValuesMatch,unremovableValuesMatch,nodeDesc,nextNodeId);
             nextNodeId = currentNode.getNextNodeId();
            }
          }

    }

    public synchronized boolean updateNode(IUserLayoutNodeDescription nodeDesc) throws PortalException {

        // Checking restrictions
        if ( !canUpdateNode(nodeDesc) )
              return false;

          ALNode node = getLayoutNode(nodeDesc.getId());
          IALNodeDescription oldNodeDesc = node.getNodeDescription();

          // We have to change all the boolean properties on descendants
          changeDescendantsBooleanProperties((IALNodeDescription)nodeDesc,oldNodeDesc,nodeDesc.getId());
          node.setNodeDescription((IALNodeDescription)nodeDesc);

          updateCacheKey();

          // Update the node into the database
          if ( autoCommit )
           return layoutStore.updateUserLayoutNode(person,userProfile,node);
          else
           return true;
    }


    public boolean canAddNode(IUserLayoutNodeDescription nodeDesc, String parentId, String nextSiblingId) throws PortalException {
       return checkAddRestrictions((IALNodeDescription)nodeDesc,parentId,nextSiblingId);
    }

    public boolean canMoveNode(String nodeId, String parentId, String nextSiblingId) throws PortalException {
       return checkMoveRestrictions(nodeId,parentId,nextSiblingId);
    }

    public boolean canDeleteNode(String nodeId) throws PortalException {
      return checkDeleteRestrictions(nodeId);
    }


    public boolean canUpdateNode(IUserLayoutNodeDescription nodeDescription) throws PortalException {
        IALNodeDescription nodeDesc=(IALNodeDescription)nodeDescription;
        String nodeId = nodeDesc.getId();

        if ( nodeId == null ) return false;
        ALNode node = getLayoutNode(nodeId);
        IALNodeDescription currentNodeDesc = node.getNodeDescription();
        // If the node Ids do no match to each other then return false
        if ( !nodeId.equals(currentNodeDesc.getId()) ) return false;

        // Checking the immutable node restriction
        //if ( checkRestriction(node,RestrictionTypes.IMMUTABLE_RESTRICTION,"true") )
        if ( currentNodeDesc.isImmutable() )
            return false;

        // Checking the immutable parent node related restriction
        if ( getRestriction(getLayoutNode(node.getParentNodeId()),RestrictionTypes.IMMUTABLE_RESTRICTION,"children") != null &&
             checkRestriction(node.getParentNodeId(),RestrictionTypes.IMMUTABLE_RESTRICTION,"children","true") )
            return false;

        // Checking the immutable children node related restrictions
        if ( node.getNodeType() == IUserLayoutNodeDescription.FOLDER ) {
            ALFolder folder = (ALFolder) node;
            //Loop for all children
            for ( String nextId = folder.getFirstChildNodeId(); nextId != null; nextId = getLayoutNode(nextId).getNextNodeId() )
             if ( getRestriction(getLayoutNode(nextId),RestrictionTypes.IMMUTABLE_RESTRICTION,"parent") != null &&
                  checkRestriction(nextId,RestrictionTypes.IMMUTABLE_RESTRICTION,"parent","true") )
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
        Vector restrictions = getLayoutNode(node.getParentNodeId()).getRestrictionsByPath("children");
        for ( int i = 0; i < restrictions.size(); i++ ) {
         IUserLayoutRestriction restriction = (IUserLayoutRestriction)restrictions.get(i);
         if ( !restriction.checkRestriction(node) ) {
            node.setNodeDescription(currentNodeDesc);
            return false;
         }
        }


        // Checking child related restrictions for the parent
        if ( node.getNodeType() == IUserLayoutNodeDescription.FOLDER ) {
         for ( String nextId = ((ALFolder)node).getFirstChildNodeId(); nextId != null; ) {
          ALNode child = getLayoutNode(nextId);
          restrictions = child.getRestrictionsByPath("parent");
          for ( int i = 0; i < restrictions.size(); i++ ) {
           IUserLayoutRestriction restriction = (IUserLayoutRestriction)restrictions.get(i);
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

    public void markAddTargets(IUserLayoutNodeDescription nodeDesc) throws PortalException {
     if ( nodeDesc != null && !( nodeDesc instanceof IALNodeDescription ) )
       throw new PortalException ( "The given argument is not a node description!" );
     addTargetsNodeDesc = (IALNodeDescription)nodeDesc;
     updateCacheKey();
    }

    public void markMoveTargets(String nodeId) throws PortalException {
     if ( nodeId != null && getLayoutNode(nodeId) == null )
       throw new PortalException ( "The node with nodeID="+nodeId+" does not exist in the layout!" );
     moveTargetsNodeId = nodeId;
     updateCacheKey();
    }

    /**
     * Returns the description of the node currently being added to the layout
     *
     * @return node an <code>IALNodeDescription</code> object
     * @exception PortalException if an error occurs
     */
    public IALNodeDescription getNodeBeingAdded() throws PortalException {
      return addTargetsNodeDesc;
    }


    /**
     * Returns the description of the node currently being moved in the layout
     *
     * @return node an <code>IALNodeDescription</code> object
     * @exception PortalException if an error occurs
     */
    public IALNodeDescription getNodeBeingMoved() throws PortalException {
     if ( moveTargetsNodeId != null ) {
      ALNode node = getLayoutNode(moveTargetsNodeId);
      return (node != null ) ? node.getNodeDescription() : null;
     }
      return null;
    }


    /**
     * Sets a restriction mask that logically multiplies one of the types from <code>RestrictionTypes</code> and
     * is responsible for filtering restrictions for the layout output to ContentHandler or DOM
     * @param restrictionMask a restriction mask
     */
    public void setRestrictionMask (int restrictionMask) {
      this.restrictionMask = restrictionMask;
    }

    /**
     * Gets a restriction mask that logically multiplies one of the types from <code>RestrictionTypes</code> and
     * is responsible for filtering restrictions for the layout output to ContentHandler or DOM
     * @return a restriction mask
     */
    public int getRestrictionMask () {
      return restrictionMask;
    }


    public boolean addLayoutEventListener(LayoutEventListener l){
        return false;
    }
    public boolean removeLayoutEventListener(LayoutEventListener l){
        return false;
    }


    /**
     * Returns an id of the root folder.
     *
     * @return a <code>String</code> value
     */
    public String getRootFolderId() {
        return layout.getRootId();
    }

    /**
     * Returns a subscription id given a functional name.
     * @param fname  the functional name to lookup.
     * @return a <code>String</code> subscription id.
     */
    public String getSubscribeId (String fname) throws PortalException {
      return layout.getNodeId(fname);
    }


}
