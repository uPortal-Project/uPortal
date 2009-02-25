/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates an instance of the reference <code>IEntityGroupStore</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class ReferenceEntityGroupStoreFactory implements IEntityGroupStoreFactory {
    
    private static final Log log = LogFactory.getLog(ReferenceEntityGroupStoreFactory.class);
    
/**
 * ReferenceGroupServiceFactory constructor.
 */
public ReferenceEntityGroupStoreFactory() {
    super();
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
    try
        { return new RDBMEntityGroupStore(); }
    catch ( Exception ex )
    {
        log.error( "ReferenceEntityGroupStoreFactory.newInstance(): " + ex);
        throw new GroupsException(ex);
    }
}
}
