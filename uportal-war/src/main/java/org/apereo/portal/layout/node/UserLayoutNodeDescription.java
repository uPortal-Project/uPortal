/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout.node;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.portal.PortalException;
import org.apereo.portal.layout.dlm.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A class describing common features of user layout nodes that is channels and folders
 */
public abstract class UserLayoutNodeDescription implements IUserLayoutNodeDescription {
    protected String id = null;
    protected String name = null;
    protected boolean immutable = false;
    protected boolean unremovable = false;
    protected boolean hidden = false;
    protected boolean deleteAllowed = true; // used in DLM
    protected boolean editAllowed = true; // used in DLM
    protected boolean moveAllowed = true; // used in DLM
    protected boolean addChildAllowed = true; // used in DLM
    protected double precedence = 0.0; // used in DLM

    public UserLayoutNodeDescription() {};

    UserLayoutNodeDescription(Element xmlNode) throws PortalException {

        // standard Node attributes
        this.setId(xmlNode.getAttribute("ID"));
        this.setName(xmlNode.getAttribute("name"));
        this.setUnremovable((new Boolean(xmlNode.getAttribute("unremovable"))).booleanValue());
        this.setImmutable((new Boolean(xmlNode.getAttribute("immutable"))).booleanValue());

        if (xmlNode.getAttribute(Constants.ATT_DELETE_ALLOWED).equals("false"))
            this.setDeleteAllowed(false);

        if (xmlNode.getAttribute(Constants.ATT_MOVE_ALLOWED).equals("false"))
            this.setMoveAllowed(false);

        if (xmlNode.getAttribute(Constants.ATT_EDIT_ALLOWED).equals("false"))
            this.setEditAllowed(false);

        if (xmlNode.getAttribute(Constants.ATT_ADD_CHILD_ALLOWED).equals("false"))
            this.setAddChildAllowed(false);

        String precedence = xmlNode.getAttribute(Constants.ATT_PRECEDENCE);

        if (!precedence.equals("")) {
            try {
                this.setPrecedence(Double.parseDouble(precedence));
            } catch (NumberFormatException nfe) {
                // if format is invalid leave it as default
            }
        }
    }

    /**
     * Returns the precedence value for this node. The precedence is 0.0 for a user owned node and
     * the value of the node's owning fragment's precedence for a node incorporated from another
     * fragment. Added by SCT for DLM.
     */
    public double getPrecedence() {
        return this.precedence;
    }

    /**
     * Set the precedence of a node. See getPrecedence for more information. Added by SCT for DLM.
     */
    public void setPrecedence(double setting) {
        this.precedence = setting;
    }

    /** Returns true if the node can be moved. Added by SCT for DLM. */
    public boolean isMoveAllowed() {
        return this.moveAllowed;
    }

    /** Set whether a node can be moved or not. Added by SCT for DLM. */
    public void setMoveAllowed(boolean setting) {
        this.moveAllowed = setting;
    }

    /** Returns true if the node can be deleted. Added by SCT for DLM. */
    public boolean isDeleteAllowed() {
        return this.deleteAllowed;
    }

    /** Set whether a node can be deleted or not. Added by SCT for DLM. */
    public void setDeleteAllowed(boolean setting) {
        this.deleteAllowed = setting;
    }

    /** Returns true if the node can be edited. Added by SCT for DLM. */
    public boolean isEditAllowed() {
        return this.editAllowed;
    }

    /** Set whether a node can be edited or not. Added by SCT for DLM. */
    public void setEditAllowed(boolean setting) {
        this.editAllowed = setting;
    }

    /** Returns true if a child node may be added to the node. Added by SCT for DLM. */
    public boolean isAddChildAllowed() {
        return this.addChildAllowed;
    }

    /** Set whether or not child nodes can be added to this node. Added by SCT for DLM. */
    public void setAddChildAllowed(boolean setting) {
        this.addChildAllowed = setting;
    }

    /**
     * Returns a node Id. The Id has to be unique in the entire user layout document.
     *
     * @return a <code>String</code> value
     */
    public String getId() {
        return this.id;
    }

    /** Set a new node Id. The Id has to be unique in the entire user layout document. */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Determine a name associated with this node.
     *
     * @return a folder/channel name.
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUnremovable() {
        return this.unremovable;
    }

    public void setUnremovable(boolean setting) {
        this.unremovable = setting;
    }

    public boolean isImmutable() {
        return this.immutable;
    }

    public void setImmutable(boolean setting) {
        this.immutable = setting;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean setting) {
        this.hidden = setting;
    }

    /**
     * Returns a type of the node, could be FOLDER or CHANNEL integer constant.
     *
     * @return a type
     */
    public abstract LayoutNodeType getType();

    /**
     * Creates a <code>org.w3c.dom.Element</code> representation of the current node.
     *
     * @param root a <code>Document</code> for which the <code>Element</code> should be created.
     * @return a <code>Element</code> value
     */
    public abstract Element getXML(Document root);

    /**
     * Add all of common node attributes to the <code>Element</code>.
     *
     * @param node an <code>Element</code> value
     */
    public void addNodeAttributes(Element node) {
        node.setAttribute("ID", this.getId());
        node.setAttribute("name", this.getName());
        node.setAttribute("unremovable", (new Boolean(this.isUnremovable())).toString());
        node.setAttribute("immutable", (new Boolean(this.isImmutable())).toString());
        node.setAttribute("hidden", (new Boolean(this.isHidden())).toString());

        if (!this.isDeleteAllowed())
            node.setAttributeNS(Constants.NS_URI, Constants.ATT_DELETE_ALLOWED, "false");
        if (!this.isMoveAllowed())
            node.setAttributeNS(Constants.NS_URI, Constants.ATT_MOVE_ALLOWED, "false");
        if (!this.isEditAllowed())
            node.setAttributeNS(Constants.NS_URI, Constants.ATT_EDIT_ALLOWED, "false");
        if (!this.isAddChildAllowed())
            node.setAttributeNS(Constants.NS_URI, Constants.ATT_ADD_CHILD_ALLOWED, "false");
        if (this.getPrecedence() != 0.0)
            node.setAttributeNS(
                    Constants.NS_URI,
                    Constants.ATT_PRECEDENCE,
                    Double.toString(this.getPrecedence()));
    }

    /**
     * A factory method to create a <code>UserLayoutNodeDescription</code> instance, based on the
     * information provided in the user layout <code>Element</code>.
     *
     * @param xmlNode a user layout DTD folder/channel <code>Element</code> value
     * @return an <code>UserLayoutNodeDescription</code> value
     * @exception PortalException if the xml passed is somehow invalid.
     */
    public static UserLayoutNodeDescription createUserLayoutNodeDescription(Element xmlNode)
            throws PortalException {
        // is this a folder or a channel ?
        String nodeName = xmlNode.getNodeName();
        if (nodeName.equals("channel")) {
            return new UserLayoutChannelDescription(xmlNode);
        } else if (nodeName.equals("folder")) {
            return new UserLayoutFolderDescription(xmlNode);
        } else {
            throw new PortalException(
                    "Given XML element '" + nodeName + "' is neither folder nor channel");
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ID", this.id)
                .append("name", this.name)
                .append("channel_or_folder?", this.getType())
                .append("precedence", this.precedence)
                .append("moveAllowed", this.moveAllowed)
                .append("removable", !this.unremovable)
                .append("deleteAllowed", this.deleteAllowed)
                .append("immutable", this.immutable)
                .append("editAllowed", this.editAllowed)
                .append("precedence", this.precedence)
                .toString();
    }
}
