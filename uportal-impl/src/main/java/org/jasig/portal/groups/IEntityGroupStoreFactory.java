/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups;

/**
 * Factory interface for creating an <code>IEntityGroupStore</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface IEntityGroupStoreFactory {
/**
 * @return IEntityGroupStore
 * @throws GroupsException
 */
public IEntityGroupStore newGroupStore() throws GroupsException;
/**
 * Factory method takes a service descriptor parm, which lets the factory
 * customize the store.
 *
 * @param svcDescriptor
 * @return IEntityGroupStore
 * @throws GroupsException
 */
public IEntityGroupStore newGroupStore(ComponentGroupServiceDescriptor svcDescriptor)
throws GroupsException;
}
