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

package org.jasig.portal.groups.smartldap;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntitySearcher;
import org.jasig.portal.groups.IEntitySearcherFactory;

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

        public IEntitySearcher newEntitySearcher() throws GroupsException {
            return new SmartLdapEntitySearcher(new SmartLdapGroupStore.Factory().newGroupStore());
        }
    
    }

    public EntityIdentifier[] searchForEntities(String query, int method, Class type) throws GroupsException {
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
