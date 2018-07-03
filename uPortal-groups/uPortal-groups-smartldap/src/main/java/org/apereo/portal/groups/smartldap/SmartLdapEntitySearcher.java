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

import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntityGroupStore;
import org.apereo.portal.groups.IEntitySearcher;
import org.apereo.portal.groups.IEntitySearcherFactory;
import org.apereo.portal.groups.IGroupConstants;

public class SmartLdapEntitySearcher implements IEntitySearcher {

    // Instance Members.
    private final IEntityGroupStore store;

    /*
     * Public API.
     */

    public static final class Factory implements IEntitySearcherFactory {

        /*
         * Public API.
         */

        @Override
        public IEntitySearcher newEntitySearcher() throws GroupsException {
            return new SmartLdapEntitySearcher(new SmartLdapGroupStore.Factory().newGroupStore());
        }
    }

    @Override
    public EntityIdentifier[] searchForEntities(
            String query, IGroupConstants.SearchMethod method, Class type) throws GroupsException {
        return store.searchForGroups(query, method, type);
    }

    /*
     * Implementation.
     */

    private SmartLdapEntitySearcher(IEntityGroupStore store) {

        // Instance Members.
        this.store = store;
    }
}
