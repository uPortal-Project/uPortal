/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
