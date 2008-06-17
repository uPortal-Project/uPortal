/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 * @deprecated See deprecation note for {@link ReferenceEntityCache}
 */
@Deprecated
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
