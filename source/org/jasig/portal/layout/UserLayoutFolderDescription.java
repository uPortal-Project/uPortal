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
        if(!xmlNode.getNodeName().equals("folder")) {
            throw new PortalException("Given XML Element is not a folder!");
        }

        // could do some validation here, but this code will probably go away anyhow

        // standard Node attributes
        this.setId(xmlNode.getAttribute("ID"));
        this.setName(xmlNode.getAttribute("name"));
        this.setUnremovable((new Boolean(xmlNode.getAttribute("unremovable"))).booleanValue());
        this.setImmutable((new Boolean(xmlNode.getAttribute("immutable"))).booleanValue());

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
    
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("UserLayoutFolderDescription id=[");
    	sb.append(this.id);
    	sb.append("] name=[");
    	sb.append(this.name);
    	sb.append("] immutable=[");
    	sb.append(this.immutable);
    	sb.append("] unremovable=[");
    	sb.append(this.unremovable);
    	sb.append("] hidden=[");
    	sb.append(this.hidden);
    	sb.append("] folderType=[");
    	sb.append(this.folderType);
    	sb.append("]");
    	return sb.toString();
    }
}
