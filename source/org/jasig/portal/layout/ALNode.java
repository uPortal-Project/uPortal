/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;



import java.util.Collection;
import java.util.Vector;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.restrictions.IUserLayoutRestriction;
import org.w3c.dom.Element;


/**
 * IALNode summary sentence goes here.
 * <p>
 * Company: Instructional Media &amp; Magic
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public abstract class ALNode implements ILayoutNode {

     protected String parentNodeId;
     protected String nextNodeId;
     protected String previousNodeId;
     protected IALNodeDescription nodeDescription;
     protected int priority = 0;

     public ALNode() {}

     public ALNode ( IALNodeDescription nd ) {
       nodeDescription = nd;
     }

     public String getId() {
        return nodeDescription.getId();
     }

     public String getFragmentId() {
        return nodeDescription.getFragmentId();
     }

     public String getFragmentNodeId() {
        return nodeDescription.getFragmentNodeId();
     }

     /**
     * Gets the node type
     * @return a node type
     */
     public abstract int getNodeType();

     public void setNodeDescription ( IALNodeDescription nd ) {
       nodeDescription = nd;
     }

     public IUserLayoutNodeDescription getNodeDescription() {
       return nodeDescription;
     }

     public void setParentNodeId ( String parentNodeId ) {
      this.parentNodeId = parentNodeId;
     }

     public String getParentNodeId() {
       return parentNodeId;
     }

     public void setNextNodeId ( String nextNodeId ) {
      this.nextNodeId = nextNodeId;
     }

     public String getNextNodeId() {
       return nextNodeId;
     }

     public void setPreviousNodeId ( String previousNodeId ) {
      this.previousNodeId = previousNodeId;
     }

     public String getPreviousNodeId() {
       return previousNodeId;
     }

    /**
     * Sets the priority for this node.
     * @param priority a <code>int</code> priority value
     */
     public void setPriority ( int priority ) {
       this.priority = priority;
     }

     /**
     * Gets the priority value for this node.
     */
     public int getPriority() {
       return priority;
     }

     /**
     * Gets a restriction by the type.
     * @param restrictionName a <code>String</code>  name of the restriction
     * @return a IUserLayoutRestriction
     */
     public IUserLayoutRestriction getRestriction( String restrictionName ) {
      if ( nodeDescription != null )
       return nodeDescription.getRestriction(restrictionName);
       return null;
     }

     /**
     * Gets a restrictions list by a restriction path.
     * @param restrictionPath a <code>String</code> restriction path
     * @return a IUserLayoutRestriction
     */
     public Collection getRestrictionsByPath( String restrictionPath ) {
      if ( nodeDescription != null )
        return nodeDescription.getRestrictionsByPath(restrictionPath);
        return new Vector();
     }

     /**
     * Add all of common node attributes to the <code>Element</code>.
     * @param node an <code>Element</code> value
     */
    protected void addNodeAttributes(Element node) {
        nodeDescription.addNodeAttributes(node);
        node.setAttribute("priority",priority+"");
    }


    /**
     * A factory method to create a <code>IALNodeDescription</code> instance,
     * based on the information provided in the user layout <code>Element</code>.
     *
     * @param xmlNode a user layout DTD folder/channel <code>Element</code> value
     * @return an <code>IALNodeDescription</code> value
     * @exception PortalException if the xml passed is somehow invalid.
     */
    public static IALNodeDescription createUserLayoutNodeDescription(Element xmlNode) throws PortalException {
        // is this a folder or a channel ?
        String nodeName=xmlNode.getNodeName();
        if(nodeName.equals("channel")) {
            return new ALChannelDescription(xmlNode);
        } else if(nodeName.equals("folder")) {
            return new ALFolderDescription(xmlNode);
        } else {
            throw new PortalException("Given XML element is neither folder nor channel");
        }
    }

    public static ALNode createALNode(IUserLayoutNodeDescription nodeDescription) throws PortalException {
        if(nodeDescription instanceof IALFolderDescription) {
            // should be a folder
            return new ALFolder(new ALFolderDescription((IALFolderDescription)nodeDescription));
        } else if(nodeDescription instanceof IALChannelDescription) {
            return new ALChannel(new ALChannelDescription((IALChannelDescription)nodeDescription));
        } else {
            throw new PortalException("ALNode::createALNode() : The node description supplied is neither a folder nor a channel! Can't make the ALNode");
        }
    }

  }
