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

package org.jasig.portal.concurrency.caching;

import java.util.HashMap;

/**
 * A rewrite of SmartCache that uses a moderate LRU algorithm:  entries
 * are purged from the cache via periodic sweeps rather than in response to
 * specific cache additions.  Note that sweeps have to be kicked off
 * externally, e.g.,
 * <p>
 * <code>
 *   int MAX_CACHE_SIZE = 1000;<br>
 *   int MAX_UNUSED_TIME_MILLIS = 30*60*1000;<br>
 *   LRUCache cache = new LRUCache(MAX_CACHE_SIZE, MAX_UNUSED_TIME_MILLIS);<br>
 *   // ... put stuff in ...<br>
 *   cache.sweepCache()<br>
 *   // ... put more stuff in ...<br>
 * </code>
 * <p>
 * At the end of the sweep, the cache will have no more (and possibly less)
 * than <code>maxSize</code> entries, though the sweep may have to reduce
 * <code>maxUnusedTimeMillis</code> in order to get there.
 * <p>
 * @author Ken Weiner
 * @author Dan Ellentuck
 * @version $Revision$
 * @see org.jasig.portal.utils.SmartCache
 */
public class LRUCache extends HashMap
{
    // Maximum size of cache, after sweep.  Defaults to 1000.
    protected static int DEFAULT_MAX_SIZE = 1000;
    protected int maxSize;

    // Maximum unused time for cache entries, used only when size
    // exceeds maxSize.  Defaults to 30 minutes.
    protected static int DEFAULT_MAX_UNUSED_TIME_MILLIS = 30*60*1000;
    protected int maxUnusedTimeMillis;

    // Wrapper adds last used timestamp.
    private class ValueWrapper {
        private long lastReferenceTime = System.currentTimeMillis();
        private Object oValue;

        protected ValueWrapper (Object oValue) {
            this.oValue = oValue;
        }
        protected Object getValue () {
            return  oValue;
        }
        protected void setValue (Object oValue) {
            this.oValue = oValue;
        }
        protected long getLastReferenceTime () {
            return  lastReferenceTime;
        }
        protected void resetLastReferenceTime () {
            this.lastReferenceTime = System.currentTimeMillis();
        }
    }
/**
 */
public LRUCache()
{
    this(DEFAULT_MAX_SIZE, DEFAULT_MAX_UNUSED_TIME_MILLIS);
}
/**
 */
public LRUCache(int size)
{
    this(size, DEFAULT_MAX_UNUSED_TIME_MILLIS);
}
/**
 * @param size int
 * @param maxUnusedAge int
 */
public LRUCache(int size, int maxUnusedAge)
{
    super();
    maxSize = size;
    maxUnusedTimeMillis = maxUnusedAge;
}
  /**
   * Synchronizes removal of ALL entries from the cache.
   */
  public synchronized void clear() {
    super.clear();
  }
  /**
   * Get the object from the cache and reset the timestamp.
   * @param key the key, typically a String
   * @return the value to which the key is mapped in this cache;
   * null if the key is not mapped to any value in this cache.
   */
  public synchronized Object get (Object key) {
    ValueWrapper valueWrapper = (ValueWrapper)super.get(key);
    if (valueWrapper != null) {
        // Update timestamp
        valueWrapper.resetLastReferenceTime();
        return valueWrapper.getValue();
    }
    else
      return  null;
  }
  /**
   * Add a new value to the cache.
   * @param key the key, typically a String
   * @param value the value
   * @return the previous value of the specified key in this hashtable, or null if it did not have one.
   */
  public synchronized Object put (Object key, Object value) {
    ValueWrapper valueWrapper = new ValueWrapper(value);
    return  super.put(key, valueWrapper);
  }
  /**
   * Synchronizes removal of an entry from the cache.
   * @param key the key, typically a String
   * @return the previous value of the specified key in this hashtable,
   * or null if it did not have one.
   */
  public synchronized Object remove(Object key) {
    return super.remove(key);
  }

  /**
   * Sweep the cache until it gets back under <code>maxSize</code>.
   */
  public void sweepCache() {
    long maxAge = maxUnusedTimeMillis;
    while ( size() > maxSize )
    {
      long cutOff = System.currentTimeMillis() - maxAge;
      Object[] keys = getKeySetArray();
      for (int i=0; i<keys.length; i++)
      {
        ValueWrapper valueWrapper = (ValueWrapper)super.get(keys[i]);
        if ( valueWrapper != null )
        {
          if (valueWrapper.getLastReferenceTime() < cutOff )
          {
            remove(keys[i]);
          }
        }
      }
      maxAge = maxAge * 3 / 4;
    }
  }

  private synchronized Object[] getKeySetArray() 
  { 
    return keySet().toArray(new Object[size()]); 
  } 
}
