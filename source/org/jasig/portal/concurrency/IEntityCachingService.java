/**
 * Copyright (c) 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.concurrency;

import org.jasig.portal.IBasicEntity;

/**
  * Defines an api for a caching service that caches and retrieves
  * <code>IBasicEntities</code>.  Cached entities of a given type are
  * stored in an <code>IEntityCache</code>.  The service manages
  * access to these caches and is respon sible for initiating any
  * cache clean up or invalidation.
  * <p>
  * The actual caching api is minimal:
  * <p>
  * <code>
  *      void add(IBasicEntity entity);<br>
  *      IBasicEntity get(Class type, String key);<br>
  *      void remove(Class type, String key);<br>
  *      void update(IBasicEntity entity);<br>
  * </code>
  * <p>
  *
  * @author Dan Ellentuck
  * @version $Revision$
  *
  * @see org.jasig.portal.IBasicEntity
  * @see org.jasig.portal.concurrency.IEntityCache
  *
*/
public interface IEntityCachingService {

/**
 * Adds the entity to the cache.
 * @param ent org.jasig.portal.concurrency.IBasicEntity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public void add(IBasicEntity ent) throws CachingException;
/**
 * Returns the cached entity identified by type and key.
 * @param type Class
 * @param key String
 * @return IBasicEntity entity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public IBasicEntity get(Class type, String key) throws CachingException;
/**
 * Removes the cached entity identified by type and key from the cache
 * and notifies peer caches.
 * @param type Class
 * @param key String
 * @exception CachingException
 */
public void remove(Class type, String key) throws CachingException;
/**
 * Updates the entity in the cache and notifies peer caches.
 * @param ent org.jasig.portal.concurrency.IBasicEntity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public void update(IBasicEntity ent) throws CachingException;
}
