/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;



import java.util.Collection;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.node.ILayoutNode;
import org.jasig.portal.layout.al.common.node.INodeDescription;
import org.jasig.portal.layout.al.common.node.INodeId;
import org.jasig.portal.layout.al.common.node.INode;
import org.jasig.portal.layout.al.common.node.NodeType;
import org.jasig.portal.layout.al.common.restrictions.IUserLayoutRestriction;
import org.jasig.portal.layout.al.common.restrictions.RestrictionPath;
import org.jasig.portal.layout.al.common.restrictions.RestrictionType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * An abstract base implementation of the aggregated layout node
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public abstract class ALNode implements IALNode {
    
    
    protected IALNodeDescription nodeDescription;
    protected ALNode parentNode;
    protected ALNode nextSiblingNode;
    protected ALNode previousSiblingNode;
    protected ALNode firstChildNode;
    protected INodeId id;
    
    protected int priority = 0;
    
    public ALNode(IALNodeDescription nd) {
        nodeDescription = nd;
    }
    /**
     * @return Returns the firstChildNode.
     */
    public ILayoutNode getFirstChildNode() {
        return firstChildNode;
    }
    /**
     * @param firstChildNode The firstChildNode to set.
     */
    public void setFirstChildNode(ALNode firstChildNode) {
        this.firstChildNode = firstChildNode;
    }
    /**
     * @return Returns the nextSiblingNode.
     */
    public INode getNextSiblingNode() {
        return nextSiblingNode;
    }
    /**
     * @param nextSiblingNode The nextSiblingNode to set.
     */
    public void setNextSiblingNode(ALNode nextSiblingNode) {
        this.nextSiblingNode = nextSiblingNode;
    }
    /**
     * @return Returns the parentNode.
     */
    public INode getParentNode() {
        return parentNode;
    }
    /**
     * @param parentNode The parentNode to set.
     */
    public void setParentNode(ALNode parentNode) {
        this.parentNode = parentNode;
    }
    /**
     * @return Returns the previousSiblingNode.
     */
    public INode getPreviousSiblingNode() {
        return previousSiblingNode;
    }
    /**
     * @param previousSiblingNode The previousSiblingNode to set.
     */
    public void setPreviousSiblingNode(ALNode previousSiblingNode) {
        this.previousSiblingNode = previousSiblingNode;
    }
    public INodeId getId() {
        return id;
    }
    public IFragmentId getFragmentId() {
        return nodeDescription.getFragmentId();
    }
    public IFragmentLocalNodeId getFragmentNodeId() {
        return nodeDescription.getFragmentNodeId();
    }
    /**
     * Gets the node type
     * @return a node type
     */
    public abstract NodeType getNodeType();
    
    public void setNodeDescription(IALNodeDescription nd) {
        nodeDescription = nd;
    }
    public IALNodeDescription getNodeDescription() {
        return nodeDescription;
    }
    /**
     * Sets the priority for this node.
     * @param priority a <code>int</code> priority value
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
    /**
     * Gets the priority value for this node.
     */
    public int getPriority() {
        return priority;
    }
    /**
     * Gets a restriction by the given name and path.
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @param restrictionPath a <code>RestrictionPath</code>  restriction path
     * @return a IUserLayoutRestriction
     */
    public IUserLayoutRestriction getRestriction(RestrictionType restrictionType,RestrictionPath restrictionPath) {
            return nodeDescription.getRestriction(restrictionType,restrictionPath);
    }
    /**
     * Gets a restriction by the type.
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @return a IUserLayoutRestriction
     */
    public IUserLayoutRestriction getLocalRestriction(RestrictionType restrictionType) {
            return nodeDescription.getLocalRestriction(restrictionType);
    }
    /**
     * Gets a restrictions list by a restriction path.
     * @param restrictionPath a <code>RestrictionPath</code> restriction path
     * @return a IUserLayoutRestriction
     */
    public Collection getRestrictionsByPath(RestrictionPath restrictionPath) {
            return nodeDescription.getRestrictionsByPath(restrictionPath);
    }
    /**
     * Add all of common node attributes to the <code>Element</code>.
     * @param node an <code>Element</code> value
     */
    public void addNodeAttributes(Element node) {
        nodeDescription.addNodeAttributes(node);
        node.setAttribute("priority", priority + "");
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
        String nodeName = xmlNode.getNodeName();
        if (nodeName.equals("channel")) {
            return new ALChannelDescription(xmlNode);
        } else if (nodeName.equals("folder")) {
            return new ALFolderDescription(xmlNode);
        } else {
            throw new PortalException("Given XML element is neither folder nor channel");
        }
    }
    public static ALNode createALNode(INodeDescription nodeDescription) throws PortalException {
        if (nodeDescription instanceof IALFolderDescription) {
            // should be a folder
            return new ALFolder(new ALFolderDescription((IALFolderDescription) nodeDescription));
        } else if (nodeDescription instanceof IALChannelDescription) {
            return new ALChannel(new ALChannelDescription((IALChannelDescription) nodeDescription));
        } else {
            throw new PortalException("ALNode::createALNode() : The node description supplied is neither a folder nor a channel! Can't make the ALNode");
        }
    }
    // delegate methods to node description
    /**
     * @param restriction
     */
    public void addRestriction(IUserLayoutRestriction restriction) {
        nodeDescription.addRestriction(restriction);
    }
    /**
     * @param node
     * @param root
     */
    public void addRestrictionChildren(Element node, Document root) {
        nodeDescription.addRestrictionChildren(node, root);
    }
    /**
     * @return
     */
    public String getGroup() {
        return nodeDescription.getGroup();
    }
    /**
     * @return
     */
    public String getName() {
        return nodeDescription.getName();
    }
    /**
     * @return
     */
    public Collection getRestrictions() {
        return nodeDescription.getRestrictions();
    }
    /**
     * @return
     */
    public NodeType getType() {
        return nodeDescription.getType();
    }
    /**
     * @param root
     * @return
     */
    public Element getXML(Document root) {
        return nodeDescription.getXML(root);
    }
    /**
     * @return
     */
    public boolean isFragmentRoot() {
        return nodeDescription.isFragmentRoot();
    }
    /**
     * @return
     */
    public boolean isHidden() {
        return nodeDescription.isHidden();
    }
    /**
     * @return
     */
    public boolean isImmutable() {
        return nodeDescription.isImmutable();
    }
    /**
     * @return
     */
    public boolean isUnremovable() {
        return nodeDescription.isUnremovable();
    }
    /**
     * @param fragmentId
     */
    public void setFragmentId(IFragmentId fragmentId) {
        nodeDescription.setFragmentId(fragmentId);
    }
    /**
     * @param fragmentNodeId
     */
    public void setFragmentNodeId(IFragmentLocalNodeId fragmentNodeId) {
        nodeDescription.setFragmentNodeId(fragmentNodeId);
    }
    /**
     * @param value
     */
    public void setFragmentRoot(boolean value) {
        nodeDescription.setFragmentRoot(value);
    }
    /**
     * @param group
     */
    public void setGroup(String group) {
        nodeDescription.setGroup(group);
    }
    /**
     * @param setting
     */
    public void setHidden(boolean setting) {
        nodeDescription.setHidden(setting);
    }
    /**
     * @param id
     */
    public void setId(INodeId id) {
       this.id = id;
    }
    /**
     * @param setting
     */
    public void setImmutable(boolean setting) {
        nodeDescription.setImmutable(setting);
    }
    /**
     * @param name
     */
    public void setName(String name) {
        nodeDescription.setName(name);
    }
    /**
     * @param restrictions
     */
    public void setRestrictions(Collection restrictions) {
        nodeDescription.setRestrictions(restrictions);
    }
    /**
     * @param setting
     */
    public void setUnremovable(boolean setting) {
        nodeDescription.setUnremovable(setting);
    }
}