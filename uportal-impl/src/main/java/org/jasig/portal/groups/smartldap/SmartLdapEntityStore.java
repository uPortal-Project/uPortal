/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups.smartldap;

import org.jasig.portal.groups.EntityImpl;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IEntityStoreFactory;

public class SmartLdapEntityStore implements IEntityStore {

    /*
     * Public API.
     */

    public static final class Factory implements IEntityStoreFactory {
        
        /*
         * Public API.
         */

        public IEntityStore newEntityStore() throws GroupsException {
            return new SmartLdapEntityStore();
        }
    
    }
    
    public IEntity newInstance(String key) throws GroupsException {
    	return newInstance(key, null);
    }

    public IEntity newInstance(String key, Class type) throws GroupsException {
    	return new EntityImpl(key, type);
    }
    
    /*
     * Implementation.
     */

    private SmartLdapEntityStore() {}
    
}
