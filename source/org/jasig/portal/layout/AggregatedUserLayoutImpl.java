package org.jasig.portal.layout;

/**
 * An implementation of Aggregated User Layout Interface defining common operations on user layout nodes,
 * that is channels and folders
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.0
 */

import java.util.*;




import org.jasig.portal.security.IPerson;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.w3c.dom.Document;
import javax.xml.parsers.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.jasig.portal.*;
import org.apache.xerces.dom.DocumentImpl;
import org.jasig.portal.utils.CommonUtils;




public class AggregatedUserLayoutImpl implements IUserLayoutManager {

  private IAggregatedUserLayoutStore layoutStore;
  private Hashtable layout;
  private int layoutId;
  private String rootNodeId = UserLayoutNodeDescription.ROOT_FOLDER_ID;
  private IPerson person;

  // User Layout restrictions
  private List restrictions = Collections.synchronizedList(new LinkedList());

  // the tag names constants
  private static final String LAYOUT = "layout";
  private static final String LAYOUT_FRAGMENT = "layout_fragment";
  private static final String FOLDER = "folder";
  private static final String CHANNEL = "channel";
  private static final String PARAMETER = "parameter";


    private static final String CHANNEL_PREFIX="n";
    private static final String FOLDER_PREFIX="s";


  public AggregatedUserLayoutImpl( IPerson person, int layoutId) {
    this.person = person;
    this.layoutId = layoutId;
    layout = new Hashtable();
  }

  public AggregatedUserLayoutImpl( IPerson person, int layoutId, IAggregatedUserLayoutStore layoutStore ) {
    this ( person, layoutId );
    this.layoutStore = layoutStore;
  }


  /**
     * Sets the internal representation of the UserLayout.
     * The user layout root node always has ID="root"
     * @param layout a <code>Hashtable</code> object containing the UserLayout data
     * @exception PortalException if an error occurs
     */
  public void setUserLayout(Hashtable layout) throws PortalException {
    this.layout = layout;
  }


  public int getLayoutId() {
    return layoutId;
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
    return getLayoutNode(nodeId).getParentNodeId();
  }


  /**
     * Determine a list of child node Ids for a given node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>List</code> of <code>String</code> child node Ids.
     * @exception PortalException if an error occurs
     */
  public List getChildIds(String nodeId) throws PortalException {
    return getLayoutFolder(nodeId).getChildNodes();
  }


  /**
     * Determine an Id of a previous sibling node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> Id value of a previous sibling node, or <code>null</code> if this is the first sibling.
     * @exception PortalException if an error occurs
     */
  public String getPreviousSiblingId(String nodeId) throws PortalException {
    return getLayoutNode(nodeId).getPreviousNodeId();
  }


  /**
     * Determine an Id of a next sibling node.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> Id value of a next sibling node, or <code>null</code> if this is the last sibling.
     * @exception PortalException if an error occurs
     */
  public String getNextSiblingId(String nodeId) throws PortalException {
    return getLayoutNode(nodeId).getNextNodeId();
  }


   /**
     * Return a cache key, uniqly corresponding to the composition and the structure of the user layout.
     *
     * @return a <code>String</code> value
     * @exception PortalException if an error occurs
     */
  public String getCacheKey() throws PortalException {
      return null;
  }

  public void getUserLayout(ContentHandler contentHandler) throws PortalException {
    getUserLayout(rootNodeId,contentHandler);
  }

  /**
     * Output subtree of a user layout (with appropriate markings) defined by a particular node into
     * a <code>ContentHandler</code>
     *
     * @param nodeId a <code>String</code> a node determining a user layout subtree.
     * @param ch a <code>ContentHandler</code> value
     * @exception PortalException if an error occurs
     */
  public void getUserLayout(String nodeId,ContentHandler contentHandler) throws PortalException {

    UserLayoutFolderDescription folderDescription = null;
    UserLayoutChannelDescription channelDescription = null;

    if ( contentHandler != null && nodeId != null ) {
      try {

         UserLayoutNode node = getLayoutNode(nodeId);
         //System.out.println("layout: " + layout );
         //System.out.println("node: " + node + " node ID: " + nodeId );
         AttributesImpl attributes = new AttributesImpl();

         // If we have a folder
         if ( node.getNodeType().equals(FOLDER) ) {

           // Start document if we have the root node
           if (nodeId.equals(rootNodeId)) contentHandler.startDocument();
           if (nodeId.equals(rootNodeId)) contentHandler.startElement("",LAYOUT,LAYOUT,new AttributesImpl());

             UserLayoutFolder folder = (UserLayoutFolder) node;
             folderDescription = (UserLayoutFolderDescription) folder.getNodeDescription();
             String folderId = folderDescription.getId();
             attributes.addAttribute("","ID","ID","ID",
                        rootNodeId.equals(folderId)?folderId:FOLDER_PREFIX+folderId);
             attributes.addAttribute("","type","type","CDATA",
                        UserLayoutFolderDescription.folderTypeNames[folderDescription.getFolderType()]);
             attributes.addAttribute("","hidden","hidden","CDATA",CommonUtils.boolToStr(folderDescription.isHidden()));
             attributes.addAttribute("","unremovable","unremovable","CDATA",CommonUtils.boolToStr(folderDescription.isUnremovable()));
             attributes.addAttribute("","immutable","immutable","CDATA",CommonUtils.boolToStr(folderDescription.isImmutable()));
             attributes.addAttribute("","name","name","CDATA",folderDescription.getName());

             contentHandler.startElement("",FOLDER,FOLDER,attributes);
             List childNodeIds = folder.getChildNodes();
             if ( childNodeIds != null ) {
               for ( Iterator i = childNodeIds.iterator(); i.hasNext(); ) {
                UserLayoutNode childNode = (UserLayoutNode) layout.get(i.next());
                // !!!!!!!!!!!
                getUserLayout(childNode.getNodeDescription().getId(),contentHandler);
               }
             }
             contentHandler.endElement("",FOLDER,FOLDER);

            // Start document if we have the root node
            if (nodeId.equals(rootNodeId)) contentHandler.endElement("",LAYOUT,LAYOUT);
            if (nodeId.equals(rootNodeId)) contentHandler.endDocument();



          // If we have a channel
         } else {

              channelDescription = (UserLayoutChannelDescription) node.getNodeDescription();

              attributes.addAttribute("","ID","ID","ID",CHANNEL_PREFIX+channelDescription.getId());
              attributes.addAttribute("","typeID","typeID","CDATA",channelDescription.getChannelTypeId());
              attributes.addAttribute("","hidden","hidden","CDATA",CommonUtils.boolToStr(channelDescription.isHidden()));
              attributes.addAttribute("","editable","editable","CDATA",CommonUtils.boolToStr(channelDescription.isEditable()));
              attributes.addAttribute("","unremovable","unremovable","CDATA",CommonUtils.boolToStr(channelDescription.isUnremovable()));
              attributes.addAttribute("","name","name","CDATA",channelDescription.getName());
              attributes.addAttribute("","description","description","CDATA",channelDescription.getDescription());
              attributes.addAttribute("","title","title","CDATA",channelDescription.getTitle());
              attributes.addAttribute("","class","class","CDATA",channelDescription.getClassName());
              attributes.addAttribute("","chanID","chanID","CDATA",channelDescription.getChannelPublishId());
              attributes.addAttribute("","fname","fname","CDATA",channelDescription.getFunctionalName());
              attributes.addAttribute("","timeout","timeout","CDATA",String.valueOf(channelDescription.getTimeout()));
              attributes.addAttribute("","hasHelp","hasHelp","CDATA",CommonUtils.boolToStr(channelDescription.hasHelp()));
              attributes.addAttribute("","hasAbout","hasAbout","CDATA",CommonUtils.boolToStr(channelDescription.hasAbout()));

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

              contentHandler.endElement("",CHANNEL,CHANNEL);

         }

      } catch ( SAXException saxe ) {
         throw new PortalException(saxe.getMessage());
        }

    }




  }


    private UserLayoutNode getLayoutNode(String nodeId) {
     try {
        return (UserLayoutNode)layout.get(nodeId);
     } catch ( Exception e ) {
        return null;
       }
    }

    private UserLayoutFolder getLayoutFolder(String folderId) {
     try {
        return (UserLayoutFolder)layout.get(folderId);
     } catch (Exception e ) {
        return null;
       }
    }

    /**
     * Build the DOM consistent of folders and channels using the internal
     *
     *
     * @param domLayout a <code>Document</code> a user layout document.
     * @param node a <code>Element</code> a node that will be used as a root for the tree construction
     * @param nodeId a <code>String</code> a nodeId from the user layout internal representation
     * @exception PortalException if an error occurs
     */
    private void appendDescendants(Document domLayout,Element node, String nodeId) throws PortalException {
          UserLayoutNode layoutNode = getLayoutNode(nodeId);
          UserLayoutNodeDescription nodeDesc = layoutNode.getNodeDescription();
          Element newNode = domLayout.createElement(layoutNode.getNodeType());
          nodeDesc.addNodeAttributes(newNode);
          node.appendChild(newNode);
          if (layoutNode.getNodeType().equals(FOLDER)) {
           List childIds = getLayoutFolder(nodeId).getChildNodes();
           if ( childIds != null )
            for ( Iterator i = childIds.iterator(); i.hasNext(); )
             appendDescendants(domLayout,newNode,(String)i.next());
          }
    }


    public Document getUserLayoutDOM() throws PortalException {
      try {
        //Document domLayout = org.jasig.portal.utils.DocumentFactory.getNewDocument();
        Document domLayout = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element layoutNode = domLayout.createElement(LAYOUT);
        domLayout.appendChild(layoutNode);
        // Build the DOM
        appendDescendants(domLayout,layoutNode,rootNodeId);
        return domLayout;
      } catch ( Exception e ) {
          throw new PortalException ("Couldn't create the DOM representation: " + e );
        }
    }



    private void setUserLayoutDOM( Node n, String parentNodeId ) throws PortalException {

      Element node = (Element) n;

      NodeList childNodes = node.getChildNodes();

      UserLayoutNodeDescription nodeDesc = UserLayoutNodeDescription.createUserLayoutNodeDescription(node);

      String nodeId = node.getAttribute("ID");

      if ( nodeDesc instanceof UserLayoutFolderDescription )
        nodeId = nodeId.substring(FOLDER_PREFIX.length());
      else if ( nodeDesc instanceof UserLayoutChannelDescription )
        nodeId = nodeId.substring(CHANNEL_PREFIX.length());


      nodeDesc.setId(nodeId);
      nodeDesc.setName(node.getAttribute("name"));
      nodeDesc.setHidden((node.getAttribute("hidden").equalsIgnoreCase("true"))?true:false);
      nodeDesc.setImmutable((node.getAttribute("immutable").equalsIgnoreCase("true"))?true:false);
      nodeDesc.setUnremovable((node.getAttribute("unremovable").equalsIgnoreCase("true"))?true:false);
      nodeDesc.setHidden((node.getAttribute("hidden").equalsIgnoreCase("true"))?true:false);


      UserLayoutNode layoutNode = null;
      if (nodeDesc instanceof UserLayoutChannelDescription) {
        UserLayoutChannelDescription channelDesc = (UserLayoutChannelDescription) nodeDesc;
        channelDesc.setChannelPublishId(node.getAttribute("chanID"));
        channelDesc.setChannelTypeId(node.getAttribute("typeID"));
        channelDesc.setClassName(node.getAttribute("class"));
        channelDesc.setDescription(node.getAttribute("description"));
        channelDesc.setEditable((node.getAttribute("editable").equalsIgnoreCase("true"))?true:false);
        channelDesc.setHasAbout((node.getAttribute("hasAbout").equalsIgnoreCase("true"))?true:false);
        channelDesc.setHasHelp((node.getAttribute("hasHelp").equalsIgnoreCase("true"))?true:false);
        channelDesc.setFunctionalName(node.getAttribute("fname"));
        channelDesc.setTimeout(Long.parseLong(node.getAttribute("timeout")));
        channelDesc.setTitle(node.getAttribute("title"));

        // Getting parameters
        for ( int i = 0; i < childNodes.getLength(); i++ ) {
          Node parameter = childNodes.item(i);
          NamedNodeMap attributes = parameter.getAttributes();

          Node paramNameNode = attributes.getNamedItem("name");

          String paramName = (paramNameNode!=null)?paramNameNode.getNodeValue():null;

          Node paramValueNode = attributes.getNamedItem("value");

          String paramValue = (paramValueNode!=null)?paramValueNode.getNodeValue():null;

          Node overParamNode = attributes.getNamedItem("override");

          String overParam = (overParamNode!=null)?overParamNode.getNodeValue():null;

          if ( paramName != null ) {
            channelDesc.setParameterValue(paramName, paramValue);
            channelDesc.setParameterOverride(paramName, "yes".equalsIgnoreCase(overParam)?true:false);
          }
        }
          // Adding to the layout
          layoutNode = new UserLayoutNode(channelDesc);
      } else {
          layoutNode = new UserLayoutFolder(nodeDesc);
        }

          UserLayoutFolder parentFolder = getLayoutFolder(parentNodeId);
          // Binding the current node to the parent child list and parentNodeId to the current node
          if ( parentFolder != null ) {
           parentFolder.addChildNode(nodeDesc.getId());
           layoutNode.setParentNodeId(parentNodeId);
          }


          Element nextNode = (Element) node.getNextSibling();
          Element prevNode = (Element) node.getPreviousSibling();

          if ( nextNode != null )
            layoutNode.setNextNodeId(nextNode.getAttribute("ID"));
          if ( prevNode != null )
            layoutNode.setPreviousNodeId(prevNode.getAttribute("ID"));

          // Putting the LayoutNode object into the layout
          layout.put(nodeDesc.getId(), layoutNode);

          // Recurrence for all children
          for ( int i = 0; i < childNodes.getLength() && (layoutNode.getNodeType().equals(FOLDER)); i++ )
            setUserLayoutDOM ( childNodes.item(i), nodeDesc.getId() );

    }

    public void setUserLayoutDOM( Document domLayout ) throws PortalException {
      Element rootNode = domLayout.getDocumentElement();
      layout.put(UserLayoutNodeDescription.ROOT_FOLDER_ID,
                 new UserLayoutFolder(UserLayoutNodeDescription.createUserLayoutNodeDescription(rootNode)));
      NodeList childNodes = rootNode.getChildNodes();
      for ( int i = 0; i < childNodes.getLength(); i++ )
       setUserLayoutDOM ( childNodes.item(i), rootNodeId );
    }

    public void setLayoutStore(IUserLayoutStore layoutStore ) {
      this.layoutStore = (IAggregatedUserLayoutStore) layoutStore;
    }

    public void loadUserLayout() throws PortalException {}
    public void saveUserLayout() throws PortalException {}


    public UserLayoutNodeDescription getNode(String nodeId) throws PortalException {
        return getLayoutNode(nodeId).getNodeDescription();
    }


    public boolean moveNode(String nodeId, String parentId,String nextSiblingId) throws PortalException {
     UserLayoutFolder targetFolder = getLayoutFolder(parentId);
     UserLayoutNode node = getLayoutNode(nodeId);
     // Changing the next node id for the previous sibling node
     String prevSiblingId = (getLayoutNode(nextSiblingId)).getPreviousNodeId();
     if ( prevSiblingId != null ) (getLayoutNode(prevSiblingId)).setNextNodeId(nodeId);


     // delete the node from the old parent child list
     UserLayoutFolder sourceFolder = getLayoutFolder(node.getParentNodeId());
     sourceFolder.deleteChildNode(nodeId);


     node.setParentNodeId(parentId);
     node.setNextNodeId(nextSiblingId);
     targetFolder.addChildNode(nodeId);

     // TO UPDATE THE APPROPRIATE INFO IN THE DB
     // TO BE DONE !!!!!!!!!!!

     return true;

    }

    public boolean deleteNode(String nodeId) throws PortalException {
      if ( layout != null ) {
       // Deleting the node from the parent
       UserLayoutFolder parentNode = getLayoutFolder(getLayoutNode(nodeId).getParentNodeId());
       parentNode.deleteChildNode(nodeId);
       UserLayoutNode node = getLayoutNode(nodeId);

       // Changing the next node id for the previous sibling node
       // and the previous node for the next sibling node
       String prevSiblingId = node.getPreviousNodeId();
       String nextSiblingId = node.getNextNodeId();

       if ( prevSiblingId != null ) (getLayoutNode(prevSiblingId)).setNextNodeId(nextSiblingId);
       if ( nextSiblingId != null ) (getLayoutNode(nextSiblingId)).setPreviousNodeId(prevSiblingId);

       // TO UPDATE THE APPROPRIATE INFO IN THE DB
       // TO BE DONE !!!!!!!!!!!

       return (layout.remove(nodeId)!=null);
      }
       return false;

    }

    public UserLayoutNodeDescription addNode(UserLayoutNodeDescription nodeDesc, String parentId,String nextSiblingId) throws PortalException {
        // Getting nodeId from the database
        // TO BE DONE
        String nodeId = "bla-bla";
        nodeDesc.setId(nodeId);
        UserLayoutFolder parentFolder = getLayoutFolder(parentId);
        UserLayoutNode nextNode = getLayoutNode(nextSiblingId);
        UserLayoutNode prevNode = getLayoutNode(nextNode.getPreviousNodeId());

        //Assign the parent, next, prev nodes to the added node
        UserLayoutNode layoutNode;
        if ( nodeDesc instanceof UserLayoutFolderDescription )
          layoutNode = new UserLayoutFolder(nodeDesc);
        else
          layoutNode = new UserLayoutNode(nodeDesc);

        layoutNode.setParentNodeId(parentId);
        layoutNode.setNextNodeId(nextSiblingId);
        layoutNode.setPreviousNodeId(nextNode.getPreviousNodeId());

        // Assign nodeId to the parent, next sibling, previous sibling nodes
        prevNode.setNextNodeId(nodeId);
        nextNode.setPreviousNodeId(nodeId);
        parentFolder.addChildNode(nodeId);

        // update nodes
        layout.put(nodeId,layoutNode);

        // TO UPDATE THE APPROPRIATE INFO IN THE DB
        // TO BE DONE !!!!!!!!!!!

        return nodeDesc;
    }

    public boolean updateNode(UserLayoutNodeDescription node) throws PortalException {
        return false;
    }


    public boolean canAddNode(UserLayoutNodeDescription node, String parentId, String nextSiblingId) throws PortalException {
       return false;
    }

    public boolean canMoveNode(String nodeId, String parentId, String nextSiblingId) throws PortalException { return false ;}
    public boolean canDeleteNode(String nodeId) throws PortalException { return false; }
    public boolean canUpdateNode(String nodeId) throws PortalException { return false; }

    public void markAddTargets(UserLayoutNodeDescription node) {}
    public void markMoveTargets(String nodeId) throws PortalException {}

    
    public boolean addLayoutEventListener(LayoutEventListener l){
        return false;
    }
    public boolean removeLayoutEventListener(LayoutEventListener l){
        return false;
    };

}
