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


/**
 * UserLayoutFolder summary sentence goes here.
 * <p>
 * Company: Instructional Media &amp; Magic
 * 
 * @author Michael Ivanov mailto:mvi@immagic.com
 * @version $Revision$
 */


public class ALFolder extends ALNode {

    protected String firstChildNodeId;

    public ALFolder() {
        super();
    }

    public ALFolder ( IALFolderDescription nd ) {
        super (nd);
    }

    /**
     * Sets the first child node ID
     */
    public void setFirstChildNodeId( String firstChildNodeId ) {
        this.firstChildNodeId = firstChildNodeId;
    }

    /**
     * Gets the first child node ID
     * @return a first child node ID
     */
    public String getFirstChildNodeId() {
        return firstChildNodeId;
    }

	/*
	 * @see org.jasig.portal.layout.ALNode#getNodeType()
	 */
    public int getNodeType() {
        return FOLDER;
    }

    public static ALFolder createLostFolder() {
        ALFolder lostFolder = new ALFolder();
        ALFolderDescription folderDesc = new ALFolderDescription();
        folderDesc.setId(IALFolderDescription.LOST_FOLDER_ID);
        folderDesc.setHidden(false);
        folderDesc.setImmutable(false);
        folderDesc.setUnremovable(true);
        folderDesc.setFolderType(IUserLayoutFolderDescription.REGULAR_TYPE);
        lostFolder.setNodeDescription(folderDesc);
        lostFolder.setParentNodeId(AggregatedUserLayoutImpl.ROOT_FOLDER_ID);
        return lostFolder;
    }

}
