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
package org.apereo.portal.groups.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.groups.ComponentGroupServiceDescriptor;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntityGroupStore;
import org.apereo.portal.groups.IEntityGroupStoreFactory;

/** Returns an instance of the ldap <code>IEntityGroupStore</code>. */
public class LDAPGroupStoreFactory implements IEntityGroupStoreFactory {
    private static final Log log = LogFactory.getLog(LDAPGroupStoreFactory.class);
    protected static LDAPGroupStore groupStore;
    /** ReferenceGroupServiceFactory constructor. */
    public LDAPGroupStoreFactory() {
        super();
    }
    /** @return org.apereo.portal.groups.ldap.LDAPGroupStore */
    protected static synchronized LDAPGroupStore getGroupStore() {
        if (groupStore == null) {
            groupStore = new LDAPGroupStore();
        }
        return groupStore;
    }
    /**
     * Return an instance of the group store implementation.
     *
     * @return IEntityGroupStore
     * @exception GroupsException
     */
    @Override
    public IEntityGroupStore newGroupStore() throws GroupsException {
        return newGroupStore(null);
    }
    /**
     * Return an instance of the group store implementation.
     *
     * @return IEntityGroupStore
     * @exception GroupsException
     */
    @Override
    public IEntityGroupStore newGroupStore(ComponentGroupServiceDescriptor svcDescriptor)
            throws GroupsException {
        return newInstance();
    }
    /**
     * Return an instance of the group store implementation.
     *
     * @return IEntityGroupStore
     * @exception GroupsException
     */
    public IEntityGroupStore newInstance() throws GroupsException {
        return getGroupStore();
    }
}
