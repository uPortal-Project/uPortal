/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import org.jasig.portal.layout.node.IUserLayoutFolderDescription;

/**
 * An interface describing an Aggregated Layout folder descriptions
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.0
 */
public interface IALFolderDescription extends IUserLayoutFolderDescription, IALNodeDescription {
    // lost folder id
    public static final String LOST_FOLDER_ID="lost_folder";
    // root folder id
    public static final String ROOT_FOLDER_ID="userLayoutRootNode";
}
