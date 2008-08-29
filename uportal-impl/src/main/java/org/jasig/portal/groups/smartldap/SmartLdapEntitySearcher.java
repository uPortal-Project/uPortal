/**
 * Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
