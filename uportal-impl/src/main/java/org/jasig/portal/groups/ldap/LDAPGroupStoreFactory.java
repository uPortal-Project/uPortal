/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups.ldap;

import org.jasig.portal.groups.ComponentGroupServiceDescriptor;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntityGroupStoreFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Returns an instance of the ldap <code>IEntityGroupStore</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class LDAPGroupStoreFactory implements IEntityGroupStoreFactory {
    private static final Log log = LogFactory.getLog(LDAPGroupStoreFactory.class);
    protected static LDAPGroupStore groupStore;
/**
 * ReferenceGroupServiceFactory constructor.
 */
public LDAPGroupStoreFactory() {
    super();
}
/**
 * @return org.jasig.portal.groups.ldap.LDAPGroupStore
 */
protected static synchronized LDAPGroupStore getGroupStore()
{
    if ( groupStore == null )
        { groupStore = new LDAPGroupStore(); }
    return groupStore;
}
/**
 * Return an instance of the group store implementation.
 * @return IEntityGroupStore
 * @exception GroupsException
 */
public IEntityGroupStore newGroupStore() throws GroupsException
{
    return newGroupStore(null);
}
/**
 * Return an instance of the group store implementation.
 * @return IEntityGroupStore
 * @exception GroupsException
 */
public IEntityGroupStore newGroupStore(ComponentGroupServiceDescriptor svcDescriptor)
throws GroupsException
{
    return newInstance();
}
/**
 * Return an instance of the group store implementation.
 * @return IEntityGroupStore
 * @exception GroupsException
 */
public IEntityGroupStore newInstance() throws GroupsException
{
    return getGroupStore();
}
}
