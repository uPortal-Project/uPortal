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
package org.apereo.portal.groups.smartldap;

import org.apereo.portal.groups.EntityImpl;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntity;
import org.apereo.portal.groups.IEntityStore;
import org.apereo.portal.groups.IEntityStoreFactory;

public class SmartLdapEntityStore implements IEntityStore {

    /*
     * Public API.
     */

    public static final class Factory implements IEntityStoreFactory {

        /*
         * Public API.
         */

        @Override
        public IEntityStore newEntityStore() throws GroupsException {
            return new SmartLdapEntityStore();
        }
    }

    @Override
    public IEntity newInstance(String key, Class type) throws GroupsException {
        return new EntityImpl(key, type);
    }

    /*
     * Implementation.
     */

    private SmartLdapEntityStore() {}
}
