/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.concurrency.caching;

import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityCachingService;
import org.jasig.portal.concurrency.IEntityCachingServiceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates an instance of <code>IEntityCachingService</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class ReferenceEntityCachingServiceFactory implements IEntityCachingServiceFactory {
    private static final Log log = LogFactory.getLog(ReferenceEntityCachingServiceFactory.class);
/**
 * ReferenceEntityLockServiceFactory constructor.
 */
public ReferenceEntityCachingServiceFactory() {
    super();
}
/**
 * Return an instance of the service implementation.
 * @return org.jasig.portal.concurrency.caching.IEntityCachingService
 * @exception CachingException
 */
public IEntityCachingService newCachingService() throws CachingException
{
    try
        { return ReferenceEntityCachingService.singleton(); }
    catch ( CachingException ce )
    {
        log.error( "ReferenceEntityLockServiceFactory.newCachingService(): " + ce);
        throw ce;
    }
}
}
