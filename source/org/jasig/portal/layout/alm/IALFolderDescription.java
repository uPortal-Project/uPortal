/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.alm;

import org.jasig.portal.layout.node.IUserLayoutFolderDescription;

/**
 * An interface describing an Aggregated Layout folder descriptions.
 * 
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.
 * It was moved to its present package to reflect that it is part of Aggregated
 * Layouts.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.0 $Revision$ $Date$
 */
public interface IALFolderDescription extends IUserLayoutFolderDescription, IALNodeDescription {
    // lost folder id
    public static final String LOST_FOLDER_ID="lost_folder";
    // root folder id
    public static final String ROOT_FOLDER_ID="userLayoutRootNode";
}
