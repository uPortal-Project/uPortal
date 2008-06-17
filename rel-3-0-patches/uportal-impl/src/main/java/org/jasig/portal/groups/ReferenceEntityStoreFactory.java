/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates an instance of the reference <code>IEntityStore</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class ReferenceEntityStoreFactory implements IEntityStoreFactory {
    
    private static final Log log = LogFactory.getLog(ReferenceEntityStoreFactory.class);
    
/**
 * ReferenceGroupServiceFactory constructor.
 */
public ReferenceEntityStoreFactory() {
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
    try
        { return new RDBMEntityStore(); }
    catch ( Exception ex )
    {
        log.error( "ReferenceEntityStoreFactory.newInstance(): " + ex);
        throw new GroupsException(ex);
    }
}
}
