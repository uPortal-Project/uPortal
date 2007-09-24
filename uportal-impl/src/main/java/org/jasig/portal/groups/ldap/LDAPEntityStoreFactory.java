/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
