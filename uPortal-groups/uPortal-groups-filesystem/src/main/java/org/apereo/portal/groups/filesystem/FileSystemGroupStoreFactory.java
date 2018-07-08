/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.groups.filesystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.groups.ComponentGroupServiceDescriptor;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntityGroupStore;
import org.apereo.portal.groups.IEntityGroupStoreFactory;
import org.apereo.portal.groups.IEntityStore;
import org.apereo.portal.groups.IEntityStoreFactory;

/**
 * Returns <code>IEntityGroupStore</code> and <code>IEntityStore</code> implementations for the file
 * system group service.
 */
public class FileSystemGroupStoreFactory implements IEntityGroupStoreFactory, IEntityStoreFactory {
    private static final Log log = LogFactory.getLog(FileSystemGroupStoreFactory.class);
    /** ReferenceGroupServiceFactory constructor. */
    public FileSystemGroupStoreFactory() {
        super();
    }
    /** @return org.apereo.portal.groups.filesystem.FileSystemGroupStore */
    protected static FileSystemGroupStore getGroupStore() throws GroupsException {
        return new FileSystemGroupStore();
    }
    /**
     * Return an instance of the entity store implementation.
     *
     * @return IEntityStore
     * @exception GroupsException
     */
    @Override
    public IEntityStore newEntityStore() throws GroupsException {
        return getGroupStore();
    }

    /**
     * Needs to be fixed, don't call!
     *
     * @return IEntityGroupStore
     * @exception GroupsException
     */
    @Override
    public IEntityGroupStore newGroupStore() throws GroupsException {
        throw new UnsupportedOperationException();
    }

    /**
     * Return an instance of the entity group store implementation.
     *
     * @return IEntityGroupStore
     * @exception GroupsException
     */
    @Override
    public IEntityGroupStore newGroupStore(ComponentGroupServiceDescriptor svcDescriptor)
            throws GroupsException {
        FileSystemGroupStore fsGroupStore = (FileSystemGroupStore) getGroupStore();
        String groupsRoot = (String) svcDescriptor.get("groupsRoot");
        if (groupsRoot != null) {
            fsGroupStore.setGroupsRootPath(groupsRoot);
        }
        return fsGroupStore;
    }
}
