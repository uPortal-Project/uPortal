/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.concurrency;

import org.jasig.portal.IBasicEntity;

/**
  * Defines the api for a cache that caches <code>IBasicEntities</code>
  * of a single type.
  *
  * @author Dan Ellentuck
  * @version $Revision$
  */
public interface IEntityCache {
    
    /**
     * @param entity - the entity to be cached.
     */
    public void add(IBasicEntity entity) throws CachingException;
    
    /**
     * Purge stale entries from the cache.
     */
    public void cleanupCache() throws CachingException;
    
    /**
     * Remove all entries from the cache.
     */
    public void clearCache() throws CachingException;
    
    /**
     * @param key the key of the entity.
     * @return org.jasig.portal.concurrency.IBasicEntity
     */
    public IBasicEntity get(String key);
    
    /**
     * @see org.jasig.portal.EntityTypes for known types.
     * @return java.lang.Class
     */
    public Class getEntityType();
    
    /**
     * @param entityKey - the key of the entity to be un-cached.
     */
    public void remove(String entityKey) throws CachingException;
    
    /**
     * Answers the number of entries in the cache.
     */
    public int size();
    
    /**
     * @param entity - the entity to be updated in the cache.
     */
    public void update(IBasicEntity entity) throws CachingException;

}
