/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.threading;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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
        this.objectCache = new HashMap<K, T>();
    }
    
    /**
     * @param cache The Map to store created isntances in
     */
    public MapCachingDoubleCheckedCreator(Map<K, T> cache) {
        Validate.notNull(cache, "cache can not be null");
        this.objectCache = cache;
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
     * Allows the object cache map to be cleared of entries
     */
    public final void clear() {
        this.writeLock.lock();
        try {
            this.objectCache.clear();
        }
        finally {
            this.writeLock.unlock();
        }
    }
    
    /**
     * @param args Optional arguments passed to {@link #get(Object...)} used to create a key
     * @return The key for the specified arguments
     */
    protected abstract K getKey(Object... args);
    
    /**
     * @param key The key to create the object for
     * @param args The object retrieval arguments
     * @return A new object instance for the key and args
     */
    protected abstract T createInternal(K key, Object... args);

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.threading.DoubleCheckedCreator#create(java.lang.Object[])
     */
    @Override
    protected final T create(Object... args) {
        final K key = this.getKey(args);
        final T value = this.createInternal(key, args);
        this.objectCache.put(key, value);
        return value;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.threading.DoubleCheckedCreator#retrieve(java.lang.Object[])
     */
    @Override
    protected final T retrieve(Object... args) {
        final K key = this.getKey(args);
        return this.objectCache.get(key);
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("objectCache", this.objectCache)
                .toString();
    }
}
