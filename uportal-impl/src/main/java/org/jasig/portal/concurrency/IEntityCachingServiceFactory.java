/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.concurrency;

/**
 * Factory interface for creating an <code>IEntityCachingService</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public interface IEntityCachingServiceFactory {
/**
 * Return an instance of IEntityCachingService.
 * @return org.jasig.portal.concurrency.IEntityCachingService
 * @exception org.jasig.portal.concurrency.CachingException
 */
public IEntityCachingService newCachingService() throws CachingException;
}
