/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering;

import java.util.Iterator;

import org.apache.commons.lang.Validate;
import org.jasig.portal.utils.cache.CacheKey;

/**
 * Generic {@link CacheableEventReader} implementation, Takes a reference to the eventReader and
 * the corresponding {@link CacheKey}.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CacheableEventReaderImpl<R, E> implements CacheableEventReader<R, E> {
    private CacheKey cacheKey;
    private final R eventReader;
    
    public CacheableEventReaderImpl(CacheKey cacheKey, R eventReader) {
        Validate.notNull(cacheKey, "CacheKey cannot be null");
        this.cacheKey = cacheKey;
        this.eventReader = eventReader;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.CacheableEventReader#getCacheKey()
     */
    @Override
    public CacheKey getCacheKey() {
        return this.cacheKey;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.CacheableEventReader#getEventReader()
     */
    @Override
    public R getEventReader() {
        return this.eventReader;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<E> iterator() {
        return (Iterator<E>)this.eventReader;
    }

    @Override
    public int hashCode() {
        return this.cacheKey.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CacheableEventReaderImpl<?, ?> other = (CacheableEventReaderImpl<?, ?>) obj;
        if (this.cacheKey == null) {
            if (other.cacheKey != null)
                return false;
        }
        else if (!this.cacheKey.equals(other.cacheKey))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CacheableEventReaderImpl [cacheKey=" + this.cacheKey + ", eventReader=" + this.eventReader + "]";
    }
}
