/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups.filesystem;

import org.jasig.portal.groups.ComponentGroupServiceDescriptor;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntityGroupStoreFactory;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IEntityStoreFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Returns <code>IEntityGroupStore</code> and <code>IEntityStore</code>
 * implementations for the file system group service.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class FileSystemGroupStoreFactory implements IEntityGroupStoreFactory,
IEntityStoreFactory {
    private static final Log log = LogFactory.getLog(FileSystemGroupStoreFactory.class);
/**
 * ReferenceGroupServiceFactory constructor.
 */
public FileSystemGroupStoreFactory() {
    super();
}
/**
 * @return org.jasig.portal.groups.filesystem.FileSystemGroupStore
 */
protected static FileSystemGroupStore getGroupStore() throws GroupsException
{
    return new FileSystemGroupStore();
}
/**
 * Return an instance of the entity store implementation.
 * @return IEntityStore
 * @exception GroupsException
 */
public IEntityStore newEntityStore() throws GroupsException
{
    return getGroupStore();
}
/**
 * Return an instance of the entity group store implementation.
 * @return IEntityGroupStore
 * @exception GroupsException
 */
public IEntityGroupStore newGroupStore() throws GroupsException
{
    return newGroupStore(null);
}
/**
 * Return an instance of the entity group store implementation.
 * @return IEntityGroupStore
 * @exception GroupsException
 */
public IEntityGroupStore newGroupStore(ComponentGroupServiceDescriptor svcDescriptor)
throws GroupsException
{
    FileSystemGroupStore fsGroupStore = (FileSystemGroupStore)getGroupStore();
    String groupsRoot = (String)svcDescriptor.get("groupsRoot");
    if ( groupsRoot != null )
        { fsGroupStore.setGroupsRootPath(groupsRoot); }
        return fsGroupStore;
}
}
