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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A class describing a folder node fo the user layout structure.
 */
public class UserLayoutFolderDescription extends UserLayoutNodeDescription
        implements IUserLayoutFolderDescription {

    private String folderType = "regular";

    /**
     * Reconstruct folder information from an xml <code>Element</code>
     *
     * @param xmlNode a user layout channel <code>Element</code> value
     * @exception PortalException if xml is malformed
     */
    public UserLayoutFolderDescription(Element xmlNode) throws PortalException {
        super(xmlNode);

        if (!xmlNode.getNodeName().equals("folder")) {
            throw new PortalException("Given XML Element is not a folder!");
        }

        this.folderType = xmlNode.getAttribute("type");
    }

    public UserLayoutFolderDescription() {}

    /**
     * Returns a type of the node, could be FOLDER or CHANNEL integer constant.
     *
     * @return a type
     */
    public LayoutNodeType getType() {
        return LayoutNodeType.FOLDER;
    }

    @Override
    public String getFolderType() {
        return this.folderType;
    }

    @Override
    public void setFolderType(String folderTypeArg) {

        if (null == folderTypeArg) {
            throw new IllegalArgumentException("Folder type cannot be set to null.");
        }

        this.folderType = folderTypeArg;
    }

    /**
     * Creates a <code>org.w3c.dom.Element</code> representation of the current node.
     *
     * @param root a <code>Document</code> for which the <code>Element</code> should be created.
     * @return a <code>Node</code> value
     */
    public Element getXML(Document root) {
        Element node = root.createElement("folder");
        this.addNodeAttributes(node);
        return node;
    }

    public void addNodeAttributes(Element node) {
        super.addNodeAttributes(node);
        node.setAttribute("type", this.folderType);
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("ID", this.id)
                .append("name", this.name)
                .append("channel_or_folder?", this.getType())
                .append("type", this.folderType)
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
