/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.restrictions.IUserLayoutRestriction;
import org.jasig.portal.layout.restrictions.PriorityRestriction;
import org.jasig.portal.layout.restrictions.RestrictionTypes;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.utils.GuidGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The aggregated user layout implementation.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public class AggregatedLayout implements IAggregatedLayout {

    // The hashtable with the layout nodes
    private Hashtable layout = null;

    // The layout ID value
    private String layoutId;

    // The restriction mask
    private int restrictionMask = 0;

    // The layout manager
    private IAggregatedUserLayoutManager layoutManager = null;

    // GUID generator
    private static GuidGenerator guid = null;
    private String cacheKey = null;


  public AggregatedLayout (  String layoutId, IAggregatedUserLayoutManager layoutManager ) throws PortalException {
    this ( layoutId );
    this.layoutManager = layoutManager;
    restrictionMask = layoutManager.getRestrictionMask();
  }

  public AggregatedLayout (  String layoutId ) throws PortalException {
    this.layoutId = layoutId;
    try {
     if ( guid == null )
      guid = new GuidGenerator();
      updateCacheKey();
    } catch ( Exception e ) {
        throw new PortalException(e);
      }
  }

  public void setLayoutManager ( IAggregatedUserLayoutManager layoutManager ) {
    this.layoutManager = layoutManager;
  }
  
  public void setLayoutData ( Hashtable layout ) throws PortalException {
    this.layout = layout;
  }

  public Hashtable getLayoutData() throws PortalException {
    return layout;
  }

  private void updateCacheKey() {
     cacheKey = guid.getNewGuid();
  }

  private void bindRestrictions( IALNodeDescription nodeDesc, ContentHandler contentHandler ) throws SAXException {
      Hashtable restrictions = nodeDesc.getRestrictions();
      for ( Enumeration e = restrictions.keys(); e.hasMoreElements(); ) {
       IUserLayoutRestriction restriction = (IUserLayoutRestriction ) e.nextElement();
       if ( ( restriction.getRestrictionType() & restrictionMask ) > 0 ) {
         AttributesImpl paramAttrs = new AttributesImpl();
         paramAttrs.addAttribute("","path","path","CDATA",restriction.getRestrictionPath());
         // we have to re-scale the priority restriction for the UI
         if ( ( restriction.getRestrictionType() & RestrictionTypes.PRIORITY_RESTRICTION ) > 0 ) {
          PriorityRestriction priorRestriction = (PriorityRestriction) restriction;
          paramAttrs.addAttribute("","value","value","CDATA",((int)priorRestriction.getMinValue()/IAggregatedUserLayoutManager.PRIORITY_COEFF)+"-"+
                                                             ((int)priorRestriction.getMaxValue()/IAggregatedUserLayoutManager.PRIORITY_COEFF));
         } else
          paramAttrs.addAttribute("","value","value","CDATA",restriction.getRestrictionExpression());

         paramAttrs.addAttribute("","type","type","CDATA",restriction.getRestrictionType()+"");
         contentHandler.startElement("",RESTRICTION,RESTRICTION,paramAttrs);
         contentHandler.endElement("",RESTRICTION,RESTRICTION);
       }
      }
  }

  public ALNode getLayoutNode(String nodeId) {
     if ( nodeId != null )
        return (ALNode)layout.get(nodeId);
        return null;
  }

  public ALFolder getLayoutFolder(String folderId) {
     if ( folderId != null )
        return (ALFolder)layout.get(folderId);
        return null;
  }

  public ALNode getLastSiblingNode ( String nodeId ) {
     ALNode node = null;
     for ( String nextId = nodeId; nextId != null; ) {
       node = getLayoutNode(nextId);
       nextId = node.getNextNodeId();
     }
       return node;
  }

  public ALNode getFirstSiblingNode ( String nodeId ) {
     ALNode node = null;
     for ( String prevId = nodeId; prevId != null; ) {
       node = getLayoutNode(prevId);
       prevId = node.getPreviousNodeId();
     }
       return node;
  }

  private void createMarkingLeaf(ContentHandler contentHandler, String leafName, String parentNodeId, String nextNodeId) throws PortalException {
     try {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("","parentID","parentID","CDATA",parentNodeId);
      attributes.addAttribute("","nextID","nextID","CDATA",CommonUtils.nvl(nextNodeId));
      contentHandler.startElement("",leafName,leafName,attributes);
      contentHandler.endElement("",leafName,leafName);
     } catch ( SAXException saxe ) {
         throw new PortalException(saxe.getMessage());
       }
  }


  private void createMarkingLeaf(Document document, String leafName, String parentNodeId, String nextNodeId, Node node) throws PortalException {
     try {
      Element markingLeaf = document.createElement(leafName);
      markingLeaf.setAttribute("parentID",parentNodeId);
      markingLeaf.setAttribute("nextID",nextNodeId);
      node.appendChild(markingLeaf);
     } catch ( Exception saxe ) {
         throw new PortalException(saxe.getMessage());
       }
  }

   /*private void createFragmentList(Document document, Node rootNode) throws PortalException {
     try {
      Element alternateLayouts = document.createElement("fragments");
      if ( fragments != null ) {
       for ( Iterator fragEnum = fragments.keySet().iterator(); fragEnum.hasNext(); ) {
        Element alternate = document.createElement("fragment");
        String key = (String) fragEnum.next();
        alternate.setAttribute("ID",key);
        alternate.setAttribute("desc",(String) fragments.get(key));
        alternateLayouts.appendChild(alternate);
       }
      }
        rootNode.appendChild(alternateLayouts);
     } catch ( Exception saxe ) {
         throw new PortalException(saxe.getMessage());
       }
   }

   private void createFragmentList(ContentHandler contentHandler) throws PortalException {
     try {
       contentHandler.startElement("","fragments","fragments",new AttributesImpl());
      if ( fragments != null ) {
       for ( Iterator fragEnum = fragments.keySet().iterator(); fragEnum.hasNext(); ) {
        AttributesImpl attributes = new AttributesImpl();
        String key = (String) fragEnum.next();
        attributes.addAttribute("","ID","ID","CDATA",key);
        attributes.addAttribute("","desc","desc","CDATA",(String) fragments.get(key));
        contentHandler.startElement("","fragment","fragment",attributes);
        contentHandler.endElement("","fragment","fragment");
       }
      }
       contentHandler.endElement("","fragments","fragments");
     } catch ( SAXException saxe ) {
         throw new PortalException(saxe.getMessage());
       }
   }*/

   /**
     * Build the DOM consistent of folders and channels using the internal representation
     * @param domLayout a <code>Document</code> a user layout document.
     * @param node a <code>Element</code> a node that will be used as a root for the tree construction
     * @param nodeId a <code>String</code> a nodeId from the user layout internal representation
     * @exception PortalException if an error occurs
     */
   private void appendDescendants(Document domLayout,Node node, String nodeId) throws PortalException {
          ALNode layoutNode = getLayoutNode(nodeId);
          IALNodeDescription nodeDesc = layoutNode.getNodeDescription();
          Element markingMoveLeaf = null, markingAddLeaf = null;

          Element newNode = domLayout.createElement((layoutNode.getNodeType()==IUserLayoutNodeDescription.FOLDER)?FOLDER:CHANNEL);

          layoutNode.addNodeAttributes(newNode);

          String parentId = layoutNode.getParentNodeId();
          String nextId = layoutNode.getNextNodeId();


            if ( layoutManager != null && parentId != null && layoutNode.getPreviousNodeId() == null ) {
             if ( !nodeDesc.isHidden() && !getLayoutNode(parentId).getNodeDescription().isHidden() ) {
              IALNodeDescription moveTargetsNodeDesc = layoutManager.getNodeBeingMoved();
              String moveTargetsNodeId = ( moveTargetsNodeDesc != null ) ? moveTargetsNodeDesc.getId() : null;
              IALNodeDescription addTargetsNodeDesc = layoutManager.getNodeBeingAdded();
              if ( addTargetsNodeDesc != null && layoutManager.canAddNode(addTargetsNodeDesc,parentId,nodeId) )
               createMarkingLeaf(domLayout,ADD_TARGET,parentId,nodeId,node);

              if ( moveTargetsNodeId != null && layoutManager.canMoveNode(moveTargetsNodeId,parentId,nodeId) )
               createMarkingLeaf(domLayout,MOVE_TARGET,parentId,nodeId,node);
             }
            }

          // Appending a new node
          node.appendChild(newNode);

          if ( parentId != null ) {

            boolean isNodeMarkable = false;
            if ( nextId != null && !getLayoutNode(nextId).getNodeDescription().isHidden() )
              isNodeMarkable = true;
            else if ( nextId == null )
              isNodeMarkable = true;

            if ( layoutManager != null && isNodeMarkable && !getLayoutNode(parentId).getNodeDescription().isHidden() ) {
              IALNodeDescription moveTargetsNodeDesc = layoutManager.getNodeBeingMoved();
              String moveTargetsNodeId = ( moveTargetsNodeDesc != null ) ? moveTargetsNodeDesc.getId() : null;
              IALNodeDescription addTargetsNodeDesc = layoutManager.getNodeBeingAdded();
             if ( addTargetsNodeDesc != null && layoutManager.canAddNode(addTargetsNodeDesc,parentId,nextId) )
               createMarkingLeaf(domLayout,ADD_TARGET,parentId,nextId,node);
             if ( moveTargetsNodeId != null && !moveTargetsNodeId.equals(nextId) &&
                  layoutManager.canMoveNode(moveTargetsNodeId,parentId,nextId) )
               createMarkingLeaf(domLayout,MOVE_TARGET,parentId,nextId,node);

            }

          }


          // Adding restrictions to the node
          nodeDesc.addRestrictionChildren(newNode,domLayout);
          if ( layoutNode.getNodeType() == IUserLayoutNodeDescription.FOLDER ) {
           // Loop for all children
           String firstChildId = ((ALFolder)layoutNode).getFirstChildNodeId();
            for ( String nextNodeId = firstChildId; nextNodeId != null; ) {
             // !!!!!!!!!!!
             appendDescendants(domLayout,newNode,nextNodeId);
             nextNodeId = getLayoutNode(nextNodeId).getNextNodeId();
            }
          } else if ( layoutNode.getNodeType() == IUserLayoutNodeDescription.CHANNEL ) {
              ALChannelDescription channelDesc = (ALChannelDescription) nodeDesc;
              // Adding channel parameters
              channelDesc.addParameterChildren(newNode,domLayout);
            }
    }


     /**
     * Returns a list of fragment Ids existing in the layout.
     *
     * @return a <code>Set</code> of <code>String</code> fragment Ids.
     * @exception PortalException if an error occurs
     */
    public Set getFragmentIds() throws PortalException {
      Set fragmentIds = new HashSet();
      for ( Enumeration nodes = layout.elements(); nodes.hasMoreElements();) {
         ALNode node = (ALNode) nodes.nextElement();
         String fragmentId = node.getFragmentId();
         if ( fragmentId != null && !fragmentIds.contains(fragmentId))
          fragmentIds.add(fragmentId);
      } 	
      return null;
    }

    /**
     * Returns an fragment Id for a given node.
     * Returns null if the node is not part of any fragments.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> fragment Id
     * @exception PortalException if an error occurs
     */
    public String getFragmentId(String nodeId) throws PortalException {
      return getNode(nodeId).getFragmentId();
    }

    /**
     * Returns an fragment root Id for a given fragment.
     *
     * @param fragmentId a <code>String</code> value
     * @return a <code>String</code> fragment root Id
     * @exception PortalException if an error occurs
     */
    public String getFragmentRootId(String fragmentId) throws PortalException {
	   ILayoutFragment fragment = layoutManager.getFragment(fragmentId);	
	   if ( fragment != null && (fragment instanceof ALFragment)) {
		 ALFolder rootFolder = (ALFolder) ((ALFragment)fragment).getNode(fragment.getRootId());
		 return rootFolder.getFirstChildNodeId();	
	   }
	     throw new PortalException("Check that the fragment with ID="+fragmentId+" has "+ALFragment.class.getName()+" type and is not NULL!");
    }


    /**
     * Writes user layout content (with appropriate markings) into
     * a <code>ContentHandler</code>
     *
     * @param ch a <code>ContentHandler</code> value
     * @exception PortalException if an error occurs
     */
    public void writeTo(ContentHandler ch) throws PortalException {
       writeTo ( getRootId(), ch );
    }

    /**
     * Writes subtree of a user layout (with appropriate markings) defined by a particular node into
     * a <code>ContentHandler</code>
     *
     * @param nodeId a <code>String</code> a node determining a user layout subtree.
     * @param contentHandler a <code>ContentHandler</code> value
     * @exception PortalException if an error occurs
     */
  public void writeTo(String nodeId, ContentHandler contentHandler ) throws PortalException {

    IALFolderDescription folderDescription = null;
    IALChannelDescription channelDescription = null;

    if ( contentHandler != null && nodeId != null ) {
      try {

         ALNode node = getLayoutNode(nodeId);
         AttributesImpl attributes = new AttributesImpl();

         // If we have a folder
         if ( node.getNodeType() == IUserLayoutNodeDescription.FOLDER ) {

           // Start document if we have the root node
           if (nodeId.equals(getRootId())) contentHandler.startDocument();

           if (nodeId.equals(getRootId())) {
              contentHandler.startElement("",LAYOUT,LAYOUT,new AttributesImpl());
              // Create a fragment list that the user owns
              //createFragmentList(contentHandler);
           }

             ALFolder folder = (ALFolder) node;
             folderDescription = (IALFolderDescription) node.getNodeDescription();
             attributes.addAttribute("","ID","ID","ID",nodeId);
             attributes.addAttribute("","type","type","CDATA",
                        IUserLayoutFolderDescription.folderTypeNames[folderDescription.getFolderType()]);
             attributes.addAttribute("","hidden","hidden","CDATA",CommonUtils.boolToStr(folderDescription.isHidden()));
             attributes.addAttribute("","unremovable","unremovable","CDATA",CommonUtils.boolToStr(folderDescription.isUnremovable()));
             attributes.addAttribute("","immutable","immutable","CDATA",CommonUtils.boolToStr(folderDescription.isImmutable()));
             attributes.addAttribute("","name","name","CDATA",folderDescription.getName());

             contentHandler.startElement("",FOLDER,FOLDER,attributes);


             // Loop for all children
             String firstChildId = folder.getFirstChildNodeId();
               for ( String nextNodeId = firstChildId; nextNodeId != null; ) {
               	
                 // if necessary we add marking nodes
                 if ( layoutManager != null ) {	
                  if ( !node.getNodeDescription().isHidden() && !getLayoutNode(nextNodeId).getNodeDescription().isHidden() ) {
                    IALNodeDescription nodeDesc = layoutManager.getNodeBeingMoved();
                    String moveTargetsNodeId = ( nodeDesc != null ) ? nodeDesc.getId() : null;
                    IALNodeDescription addTargetsNodeDesc = layoutManager.getNodeBeingAdded();
                    if ( addTargetsNodeDesc != null && layoutManager.canAddNode(addTargetsNodeDesc,nodeId,nextNodeId) )
                     createMarkingLeaf(contentHandler,ADD_TARGET,nodeId,nextNodeId);
                    if ( moveTargetsNodeId != null && !moveTargetsNodeId.equals(nextNodeId) && layoutManager.canMoveNode(moveTargetsNodeId,nodeId,nextNodeId) )
                     createMarkingLeaf(contentHandler,MOVE_TARGET,nodeId,nextNodeId);
                  }
                 }

                // Recurrence
                writeTo (nextNodeId,contentHandler);
                nextNodeId = getLayoutNode(nextNodeId).getNextNodeId();
               }

                  // if necessary we add marking nodes to the end of the sibling line
                  if ( layoutManager != null && !node.getNodeDescription().isHidden() ) {
                   IALNodeDescription nodeDesc = layoutManager.getNodeBeingMoved();
                   String moveTargetsNodeId = ( nodeDesc != null ) ? nodeDesc.getId() : null;
                   IALNodeDescription addTargetsNodeDesc = layoutManager.getNodeBeingAdded();
                   if ( addTargetsNodeDesc != null && layoutManager.canAddNode(addTargetsNodeDesc,nodeId,null) )
                    createMarkingLeaf(contentHandler,ADD_TARGET,nodeId,null);
                   if ( moveTargetsNodeId != null && layoutManager.canMoveNode(moveTargetsNodeId,nodeId,null) )
                    createMarkingLeaf(contentHandler,MOVE_TARGET,nodeId,null);
                  }

             // Putting restrictions to the content handler
              if ( restrictionMask > 0 )
                bindRestrictions(folderDescription,contentHandler);

             contentHandler.endElement("",FOLDER,FOLDER);

            // Start document if we have the root node
            if (nodeId.equals(getRootId())) contentHandler.endElement("",LAYOUT,LAYOUT);
            if (nodeId.equals(getRootId())) contentHandler.endDocument();



          // If we have a channel
         } else {

              channelDescription = (IALChannelDescription) node.getNodeDescription();

              attributes.addAttribute("","ID","ID","ID",nodeId);
              attributes.addAttribute("","typeID","typeID","CDATA",channelDescription.getChannelTypeId());
              attributes.addAttribute("","hidden","hidden","CDATA",CommonUtils.boolToStr(channelDescription.isHidden()));
              attributes.addAttribute("","editable","editable","CDATA",CommonUtils.boolToStr(channelDescription.isEditable()));
              attributes.addAttribute("","unremovable","unremovable","CDATA",CommonUtils.boolToStr(channelDescription.isUnremovable()));
              attributes.addAttribute("","immutable","immutable","CDATA",CommonUtils.boolToStr(channelDescription.isImmutable()));
              attributes.addAttribute("","name","name","CDATA",channelDescription.getName());
              attributes.addAttribute("","description","description","CDATA",channelDescription.getDescription());
              attributes.addAttribute("","title","title","CDATA",channelDescription.getTitle());
              attributes.addAttribute("","class","class","CDATA",channelDescription.getClassName());
              attributes.addAttribute("","chanID","chanID","CDATA",channelDescription.getChannelPublishId());
              attributes.addAttribute("","fname","fname","CDATA",channelDescription.getFunctionalName());
              attributes.addAttribute("","timeout","timeout","CDATA",String.valueOf(channelDescription.getTimeout()));
              attributes.addAttribute("","hasHelp","hasHelp","CDATA",CommonUtils.boolToStr(channelDescription.hasHelp()));
              attributes.addAttribute("","hasAbout","hasAbout","CDATA",CommonUtils.boolToStr(channelDescription.hasAbout()));
              attributes.addAttribute("","secure","secure","CDATA",CommonUtils.boolToStr(channelDescription.isSecure()));

              contentHandler.startElement("",CHANNEL,CHANNEL,attributes);

              if ( channelDescription.hasParameters() ) {
                Enumeration paramNames = channelDescription.getParameterNames();
                while ( paramNames.hasMoreElements() ) {
                  String name = (String) paramNames.nextElement();
                  String value = channelDescription.getParameterValue(name);
                  AttributesImpl paramAttrs = new AttributesImpl();
                  paramAttrs.addAttribute("","name","name","CDATA",name);
                  paramAttrs.addAttribute("","value","value","CDATA",value);
                  paramAttrs.addAttribute("","override","override","CDATA",
                             channelDescription.canOverrideParameter(name)?"yes":"no");
                  contentHandler.startElement("",PARAMETER,PARAMETER,paramAttrs);
                  contentHandler.endElement("",PARAMETER,PARAMETER);
                }

              }

              // Putting restrictions to the content handler
              if ( restrictionMask > 0 )
                bindRestrictions(channelDescription,contentHandler);

              contentHandler.endElement("",CHANNEL,CHANNEL);

         }

      } catch ( SAXException saxe ) {
         throw new PortalException(saxe.getMessage());
        }

     }

   }

    /**
     * Writes user layout content (with appropriate markings) into
     * a <code>Document</code> object
     *
     * @param document a <code>Document</code> value
     * @exception PortalException if an error occurs
     */
    public void writeTo(Document document) throws PortalException {
      writeTo ( getRootId(), document );
    }

    /**
     * Writes subtree of a user layout (with appropriate markings) defined by a particular node into
     * a <code>Document</code>
     *
     * @param nodeId a <code>String</code> a node determining a user layout subtree.
     * @param document a <code>Document</code> object
     * @exception PortalException if an error occurs
     */
    public void writeTo(String nodeId, Document document) throws PortalException {
      try {
        Element layoutNode = document.createElement((nodeId.equals(getRootId()))?LAYOUT:FRAGMENT);
        document.appendChild(layoutNode);
        // Create a fragment list which the user owns
        /*if (nodeId.equals(getRootId()))
         createFragmentList(document,layoutNode);*/
        // Build the DOM
        appendDescendants(document,layoutNode,nodeId);
      } catch ( Exception e ) {
          e.printStackTrace();
          throw new PortalException ("Couldn't create the DOM representation: " + e );
        }
    }

    /**
     * Obtain a description of a node (channel or a folder) in a given user layout.
     *
     * @param nodeId a <code>String</code> channel subscribe id or folder id.
     * @return an <code>UserLayoutNodeDescription</code> value
     * @exception PortalException if an error occurs
     */
    public IUserLayoutNodeDescription getNodeDescription(String nodeId) throws PortalException {
      ALNode node = getLayoutNode(nodeId);
      if ( node != null )
       return node.getNodeDescription();
      throw new PortalException ( "The node with nodeID="+nodeId+" does not exist in the layout!" );
    }

     /**
     * Returns a node specified by a node ID.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>ALNode</code> object
     * @exception PortalException if an error occurs
     */
    public ALNode getNode( String nodeId) throws PortalException {
       return getLayoutNode(nodeId);
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
      ALNode node = getLayoutNode(nodeId);
      if ( node != null )
       return node.getParentNodeId();
      throw new PortalException ( "The node with nodeID="+nodeId+" does not exist in the layout!" );
    }

    /**
     * Returns a list of child node Ids for a given node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>Enumeration</code> of <code>String</code> child node Ids.
     * @exception PortalException if an error occurs
     */
    public Enumeration getChildIds(String nodeId) throws PortalException {
      Vector childIds = new Vector();
      String firstChildId = getLayoutFolder(nodeId).getFirstChildNodeId();
      for ( String nextNodeId = firstChildId; nextNodeId != null; ) {
       childIds.add(nextNodeId);
       nextNodeId = getLayoutNode(nextNodeId).getNextNodeId();
      }
      return childIds.elements();
    }

    /**
     * Determine an Id of a next sibling node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> Id value of a next sibling node, or <code>null</code> if this is the last sibling.
     * @exception PortalException if an error occurs
     */
    public String getNextSiblingId(String nodeId) throws PortalException {
     ALNode node = getLayoutNode(nodeId);
     if ( node != null )
      return node.getNextNodeId();
     throw new PortalException ( "The node with nodeID="+nodeId+" does not exist in the layout!" );
    }


    /**
     * Determine an Id of a previous sibling node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> Id value of a previous sibling node, or <code>null</code> if this is the first sibling.
     * @exception PortalException if an error occurs
     */
    public String getPreviousSiblingId(String nodeId) throws PortalException {
     ALNode node = getLayoutNode(nodeId);
     if ( node != null )
      return node.getPreviousNodeId();
       throw new PortalException ( "The node with nodeID="+nodeId+" does not exist in the layout!" );
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
     * Register a layout event listener
     *
     * @param l a <code>LayoutEventListener</code> object
     * @return a <code>boolean</code> success status
     */
    public boolean addLayoutEventListener(LayoutEventListener l) {
      // TO IMPLEMENT
      return false;
    }


    /**
     * Remove a registered layout event listener.
     *
     * @param l a <code>LayoutEventListener</code> object
     * @return a <code>boolean</code> success status
     */
    public boolean removeLayoutEventListener(LayoutEventListener l) {
     // TO IMPLEMENT
     return false;
    }

    /**
     * Returns a layout Id associated with this manager/
     *
     * @return an <code>String</code> layout Id value;
     */
    public String getId() {
      return layoutId;
    }

    /**
     * Returns a node id associated with the supplied functional name.
     *
     * @param fname  the functional name to lookup
     * @return a <code>String</code> subscription id
     * @exception PortalException if an error occurs
     */
    public String getNodeId(String fname) throws PortalException {
       for ( Enumeration nodeIds = layout.keys(); nodeIds.hasMoreElements() ;) {
          String nodeId = nodeIds.nextElement().toString();
          ALNode node  = getLayoutNode(nodeId);
          if ( node.getNodeType() == IUserLayoutNodeDescription.CHANNEL ) {
              ALChannelDescription channelDesc = (ALChannelDescription) node.getNodeDescription();
              if ( fname.equals(channelDesc.getFunctionalName()) )
                return node.getId();
          }
        }
                return null;
    }

     /**
     * Returns a list of node Ids in the layout.
     *
     * @return a <code>Enumeration</code> of node Ids
     * @exception PortalException if an error occurs
     */
    public Enumeration getNodeIds() throws PortalException {
      if ( layout == null )
        throw new PortalException ( "The layout is NULL!" );
      return layout.keys();
    }

    /**
     * Returns an id of the root node.
     *
     * @return a <code>String</code> value
     */
    public String getRootId() {
      return IALFolderDescription.ROOT_FOLDER_ID;
    }

}
