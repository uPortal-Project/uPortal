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
 * @deprecated needs to be fixed, don't call
 */
@Deprecated()
public IEntityGroupStore newGroupStore() throws GroupsException
{
	throw new UnsupportedOperationException("unimplemented method called");
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
