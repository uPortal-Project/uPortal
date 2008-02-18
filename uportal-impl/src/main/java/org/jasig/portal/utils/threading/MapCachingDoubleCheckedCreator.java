/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.threading;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.commons.collections15.map.ReferenceMap;
import org.apache.commons.lang.Validate;

/**
 * Implementation of DoubleCheckedCreator that stores what it creates in a backing Map. Subclasses need to implement
 * {@link #getKey(Object...)} to provide a Map key to use for the arguments and {@link #create(Object...)} to create
 * new instances of the objects.
 * <br/>
 * The default constructor uses a {@link ReferenceMap} with hard references to the keys 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class MapCachingDoubleCheckedCreator<K, T> extends DoubleCheckedCreator<T> {
    private final Map<K, T> objectCache;
    
    public MapCachingDoubleCheckedCreator() {
        this.objectCache = new ReferenceMap<K, T>(ReferenceMap.HARD, ReferenceMap.SOFT);
    }
    
    /**
     * @param cache The Map to store created isntances in
     * @param readWriteLock the ReadWriteLock to use for the double checked locking
     */
    public MapCachingDoubleCheckedCreator(Map<K, T> cache, ReadWriteLock readWriteLock) {
        super(readWriteLock);

        Validate.notNull(cache, "cache can not be null");
        this.objectCache = cache;
    }
    
    /**
     * @return A read-only view of the underlying Map used to cache created objects
     */
    public final Map<K, T> getCacheMap() {
        return Collections.unmodifiableMap(this.objectCache);
    }
    
    /**
     * @param args Optional arguments passed to {@link #get(Object...)} used to create a key
     * @return The key for the specified arguments
     */
    protected abstract K getKey(Object... args);
    
    /* (non-Javadoc)
     * @see org.jasig.portal.utils.threading.DoubleCheckedCreator#retrieve(java.lang.Object[])
     */
    @Override
    protected final T retrieve(Object... args) {
        final K key = this.getKey(args);
        return objectCache.get(key);
    }
}
