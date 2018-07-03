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
package org.apereo.portal.groups.grouper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.groups.ComponentGroupServiceDescriptor;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntityGroupStore;
import org.apereo.portal.groups.IEntityGroupStoreFactory;

/** Returns an instance of the Grouper entity group store. */
public class GrouperEntityGroupStoreFactory implements IEntityGroupStoreFactory {

    /** Logger. */
    private static final Log LOGGER = LogFactory.getLog(GrouperEntityGroupStoreFactory.class);

    private static IEntityGroupStore groupStore;

    /**
     * returns the instance of GrouperEntityGroupStore.
     *
     * @return The instance.
     * @throws GroupsException if there is an error
     * @see IEntityGroupStoreFactory #newGroupStore()
     */
    public static synchronized IEntityGroupStore getGroupStore() {
        if (groupStore == null) {
            groupStore = new GrouperEntityGroupStore();
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("returning IEntityGroupStore: " + groupStore);
        }
        return groupStore;
    }

    /**
     * returns the instance of GrouperEntityGroupStore.
     *
     * @return The instance.
     * @throws GroupsException if there is an error
     * @see IEntityGroupStoreFactory #newGroupStore()
     */
    @Override
    public IEntityGroupStore newGroupStore() throws GroupsException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating New Grouper IEntityGroupStore");
        }
        return getGroupStore();
    }

    /**
     * Construction with parameters.
     *
     * @param svcDescriptor The parameters.
     * @return The instance.
     * @throws GroupsException if there is an error
     * @see IEntityGroupStoreFactory
     *     #newGroupStore(org.apereo.portal.groups.ComponentGroupServiceDescriptor)
     */
    @Override
    public IEntityGroupStore newGroupStore(ComponentGroupServiceDescriptor svcDescriptor)
            throws GroupsException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating New Grouper IEntityGroupStore");
        }
        return getGroupStore();
    }
}
