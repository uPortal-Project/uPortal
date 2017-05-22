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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An interface describing common features of user layout nodes, that is channels and folders
 */
public interface IUserLayoutNodeDescription {
    public enum LayoutNodeType {
        PORTLET,
        FOLDER;
    }

    /**
     * Returns a node Id. The Id has to be unique in the entire user layout document.
     *
     * @return a <code>String</code> value
     */
    public String getId();

    /** Set a new node Id. The Id has to be unique in the entire user layout document. */
    public void setId(String id);

    /**
     * Determine a name associated with this node.
     *
     * @return a folder/channel name.
     */
    public String getName();

    /**
     * Returns a type of the node, could be FOLDER or CHANNEL integer constant.
     *
     * @return a type
     */
    public LayoutNodeType getType();

    public void setName(String name);

    public boolean isUnremovable();

    public void setUnremovable(boolean setting);

    public boolean isImmutable();

    public void setImmutable(boolean setting);

    public boolean isHidden();

    public void setHidden(boolean setting);

    /**
     * Creates a <code>org.w3c.dom.Element</code> representation of the current node.
     *
     * @param root a <code>Document</code> for which the <code>Element</code> should be created.
     * @return a <code>Element</code> value
     */
    public Element getXML(Document root);

    public void addNodeAttributes(Element node);

    /** Returns true if child nodes can be added to the node. Added by SCT for DLM. */
    public boolean isAddChildAllowed();

    /** Set whether or not child nodes can be added to this node. Added by SCT for DLM. */
    public void setAddChildAllowed(boolean setting);

    /** Returns true if the node's attributes can be edited. Added by SCT for DLM. */
    public boolean isEditAllowed();

    /** Set whether a node's attributes can be edited or not. Added by SCT for DLM. */
    public void setEditAllowed(boolean setting);

    /**
     * Returns the precedence value for this node. The precedence is 0.0 for a user owned node and
     * the value of the node's owning fragment's precedence for a node incorporated from another
     * fragment. Added by SCT for DLM.
     */
    public double getPrecedence();

    /**
     * Set the precedence of a node. See getPrecedence for more information. Added by SCT for DLM.
     */
    public void setPrecedence(double setting);

    /** Returns true if the node can be moved. Added by SCT for DLM. */
    public boolean isMoveAllowed();

    /** Set whether a node can be moved or not. Added by SCT for DLM. */
    public void setMoveAllowed(boolean setting);

    /** Returns true if the node can be deleted. Added by SCT for DLM. */
    public boolean isDeleteAllowed();

    /** Set whether a node can be deleted or not. Added by SCT for DLM. */
    public void setDeleteAllowed(boolean setting);
}
