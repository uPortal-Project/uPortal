/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.node;

import org.jasig.portal.PortalException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A class describing a folder node fo the user layout structure.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
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
    public int getType() {
      return FOLDER;
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
