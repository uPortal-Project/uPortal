/* Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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
    try
        { return getGroupStore(); }
    catch ( Exception ex )
    {
        log.error( "FileSystemGroupStoreFactory.newEntityStore(): " + ex);
        throw new GroupsException(ex.getMessage());
    }
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
    try
    {
        FileSystemGroupStore fsGroupStore = (FileSystemGroupStore)getGroupStore();
        String groupsRoot = (String)svcDescriptor.get("groupsRoot");
        if ( groupsRoot != null )
            { fsGroupStore.setGroupsRootPath(groupsRoot); }
        return fsGroupStore;
    }
    catch ( Exception ex )
    {
        log.error( "FileSystemGroupStoreFactory.newGroupStore(): " + ex);
        throw new GroupsException(ex.getMessage());
    }
}
}
