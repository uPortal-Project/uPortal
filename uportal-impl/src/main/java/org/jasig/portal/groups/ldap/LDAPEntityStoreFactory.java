/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups.ldap;

import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IEntityStoreFactory;

/**
 * Returns an instance of the ldap <code>IEntityStore</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class LDAPEntityStoreFactory implements IEntityStoreFactory {
/**
 * ReferenceGroupServiceFactory constructor.
 */
public LDAPEntityStoreFactory() {
    super();
}
/**
 * Return an instance of the entity store implementation.
 * @return IEntityStore
 * @exception GroupsException
 */
public IEntityStore newEntityStore() throws GroupsException
{
    return newInstance();
}
/**
 * Return an instance of the entity store implementation.
 * @return IEntityStore
 * @exception GroupsException
 */
public IEntityStore newInstance() throws GroupsException
{
   return (IEntityStore) new LDAPGroupStoreFactory().newGroupStore();
}
}
