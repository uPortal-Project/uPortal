/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.concurrency.locking;

import org.jasig.portal.concurrency.IEntityLockService;
import org.jasig.portal.concurrency.IEntityLockServiceFactory;
import org.jasig.portal.concurrency.LockingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates the reference implemetation of <code>IEntityLockService</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class ReferenceEntityLockServiceFactory implements IEntityLockServiceFactory {
    private static final Log log = LogFactory.getLog(ReferenceEntityLockServiceFactory.class);
/**
 * ReferenceEntityLockServiceFactory constructor.
 */
public ReferenceEntityLockServiceFactory() {
        super();
}
/**
 * Return an instance of the service implementation.
 * @return org.jasig.portal.concurrency.locking.IEntityLockService
 * @exception LockingException
 */
public IEntityLockService newLockService() throws LockingException
{
    try
        { return ReferenceEntityLockService.singleton(); }
    catch ( LockingException le )
    {
        log.error( "ReferenceEntityLockServiceFactory.newLockService(): " + le);
        throw new LockingException(le);
    }
}
}
