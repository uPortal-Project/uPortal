/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;

import org.jasig.portal.layout.al.common.node.IFolderDescription;
import org.jasig.portal.layout.al.common.node.NodeType;
import org.jasig.portal.layout.al.common.node.INodeDescription;
import org.jasig.portal.layout.al.common.node.INode;


/**
 * UserLayoutFolder summary sentence goes here.
 * <p>
 * Company: Instructional Media &amp; Magic
 *
 * @author Michael Ivanov mailto:mvi@immagic.com
 * @version $Revision$
 */


public class ALFolder extends ALNode implements IALFolder {

    protected INode firstChildNode;

    public ALFolder ( INodeDescription nd ) {
        super (nd);
    }

    public void setNodeDescription(INodeDescription nd) {
    	if ( !(nd instanceof IFolderDescription) )
      	  throw new RuntimeException("The node description object must implement IFolderDescription interface!");	
        super.setNodeDescription(nd);
    }
    
    /**
     * Gets the node type
     * @return a node type
     */
     public NodeType getNodeType() {
       return NodeType.FOLDER;
     }

     
     /** Returns folder type.
     *
     * @return an <code>int</code> value corresponding
     * to one of the valid folder types.
     */
     public int getFolderType() {
        return ((IFolderDescription)nodeDescription).getFolderType();     
	 }
     
     /**
      * Assign a type to a folder.
      *
      * @param folderType an <code>int</code> value corresponding
      * to one of the valid folder types.
      */
     public void setFolderType(int folderType) {
     	((IFolderDescription)nodeDescription).setFolderType(folderType);
     }
     
     
    /**
     * Sets the first child node
     */
    public void setFirstChildNode( INode firstChildNode) {
        this.firstChildNode = firstChildNode;
    }

    /**
     * Gets the first child node
     * @return a first child node
     */
    public INode getFirstChildNode() {
        return firstChildNode;
    }


    public static ALFolder createLostFolder() {
    	ALFolderDescription folderDesc = new ALFolderDescription();
        ALFolder lostFolder = new ALFolder(folderDesc);
        lostFolder.setId(IALFolder.LOST_FOLDER_ID);
        folderDesc.setHidden(true);
        folderDesc.setImmutable(false);
        folderDesc.setUnremovable(true);
        folderDesc.setFolderType(IFolderDescription.REGULAR_TYPE);
        lostFolder.setNodeDescription(folderDesc);
        // TODO: how do we assign a reference to a root node ?
        // lostFolder.setParentNodeId(IALFolderDescription.ROOT_FOLDER_ID);
        return lostFolder;
    }

    public static ALFolder createRootFolder() {
    	ALFolderDescription folderDesc = new ALFolderDescription();
        ALFolder rootFolder = new ALFolder(folderDesc);
        rootFolder.setId(IALFolder.ROOT_FOLDER_ID);
        folderDesc.setHidden(false);
        folderDesc.setImmutable(false);
        folderDesc.setUnremovable(true);
        folderDesc.setName("Root folder");
        folderDesc.setFolderType(IFolderDescription.REGULAR_TYPE);
        rootFolder.setNodeDescription(folderDesc);
        return rootFolder;
    }

}
