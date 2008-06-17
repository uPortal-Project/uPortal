/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups.filesystem;

import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntitySearcher;
import org.jasig.portal.groups.IEntitySearcherFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Returns <code>IEntityGroupStore</code> and <code>IEntityStore</code>
 * implementations for the file system group service.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class FileSystemEntitySearcherFactory implements IEntitySearcherFactory {
    private static final Log log = LogFactory.getLog(FileSystemEntitySearcherFactory.class);
/**
 * FileSytemEntitySearcherFactory constructor.
 */
public FileSystemEntitySearcherFactory() {
    super();
}
/**
 * @return org.jasig.portal.groups.filesystem.FileSystemGroupStore
 */
protected static FileSystemGroupStore getGroupStore() throws GroupsException
{
    return new FileSystemGroupStore();
}
public IEntitySearcher newEntitySearcher() throws GroupsException
{
    return getGroupStore();
}
}
