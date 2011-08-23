/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.layout.node;

import org.jasig.portal.PortalException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A class describing a folder node fo the user layout structure.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version 1.0
 */
public class UserLayoutFolderDescription extends UserLayoutNodeDescription implements IUserLayoutFolderDescription {
    public static final int REGULAR_TYPE=0;
    public static final int HEADER_TYPE=1;
    public static final int FOOTER_TYPE=2;

    public static final String[] folderTypeNames= {"regular","header","footer"};

    protected int folderType=REGULAR_TYPE;

    /**
     * Reconstruct folder information from an xml <code>Element</code>
     *
     * @param xmlNode a user layout channel <code>Element</code> value
     * @exception PortalException if xml is malformed
     */
    public UserLayoutFolderDescription(Element xmlNode) throws PortalException {
        super( xmlNode );
        
        if(!xmlNode.getNodeName().equals("folder")) {
            throw new PortalException("Given XML Element is not a folder!");
        }

        // folder-specific attributes
        String typeName=xmlNode.getAttribute("type");
        // default to regular
        int int_folderType=REGULAR_TYPE;
        if(typeName!=null) {
            // search for a match
            for(int i=0;i<folderTypeNames.length;i++) {
                if(typeName.equals(folderTypeNames[i])) {
                    int_folderType=i;
                }
            }
        }
        this.setFolderType(int_folderType);
    }

    public UserLayoutFolderDescription() {
    }

    public UserLayoutFolderDescription(IUserLayoutFolderDescription d) {
        super(d);
        setFolderType(d.getFolderType());
    }

    /**
     * Returns a type of the node, could be FOLDER or CHANNEL integer constant.
     *
     * @return a type
     */
    public LayoutNodeType getType() {
      return LayoutNodeType.FOLDER;
    }

    /**
     * Returns folder type.
     *
     * @return an <code>int</code> value corresponding
     * to one of the valid folder types.
     */
    public int getFolderType() {
        return this.folderType;
    }

    /**
     * Assign a type to a folder.
     *
     * @param folderType an <code>int</code> value corresponding
     * to one of the valid folder types.
     */
    public void setFolderType(int folderType) {
        this.folderType=folderType;
    }

    /**
     * Creates a <code>org.w3c.dom.Element</code> representation of the current node.
     *
     * @param root a <code>Document</code> for which the <code>Element</code> should be created.
     * @return a <code>Node</code> value
     */
    public Element getXML(Document root) {
        Element node=root.createElement("folder");
        this.addNodeAttributes(node);
        return node;
    }

    public void addNodeAttributes(Element node) {
        super.addNodeAttributes(node);
        node.setAttribute("type",folderTypeNames[this.getFolderType()]);
    }
}
