/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.utils;

import java.util.HashMap;
import java.util.Iterator;

/**
 * The SmartCache class is used to store objects in memory for
 * a specified amount of time.  The time should be specified in seconds.
 * If the time is specified as a negative value, it will be cahced indefinitely.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class SmartCache extends HashMap {
  protected int iExpirationTimeout = 3600000;                   // default to 1 hour

  /**
   * Instantiate a new SmartCache.  Usually instances of SmartCache are
   * declared as static. When retrieving a value from SmartCache, it will
   * be null if the value has expired.  It is up to the client to then
   * retrieve the value and put it in the cache again.
   * Example:
   * <code>
   * import org.jasig.portal.utils.SmartCache;
   *
   * public class CacheClient {
   *   private static SmartCache cache = new SmartCache(3600); // This cache's values will expire in one hour
   *
   *   public static void main (String[] args) {
   *     // Try to get a value from the cache
   *     String aKey = "exampleKey";
   *     String aValue = (String)cache.get(aKey);
   *
   *     if (aValue == null) {
   *       // If we are here, the value has either expired or not in the cache
   *       // so we will get the value and stuff it in the cache
   *       String freshValue = someMethodWhichReturnsAString();
   *
   *       // Make sure it isn't null before putting it into the cache
   *       if (freshValue != null) {
   *         cache.put(aKey, freshValue);
   *         aValue = freshValue;
   *       }
   *     }
   *
   *     System.out.println ("Got the value: " + aValue);
   *   }
   * }
   * </code>
   * @param iExpirationTimeout specified in seconds
   */
  public SmartCache(int iExpirationTimeout) {
    super();
    this.iExpirationTimeout = iExpirationTimeout*1000;
  }

  /**
   * Instantiate SmartCache with a default expiration timeout of one hour.
   */
  public SmartCache() {
    super();
  }

  /**
   * Add a new value to the cache.  The value will expire in accordance with the
   * cache's expiration timeout value which was set when the cache was created.
   * @param key the key, typically a String
   * @param value the value
   * @return the previous value of the specified key in this hashtable, or null if it did not have one.
   */
  public synchronized Object put(Object key, Object value) {
    ValueWrapper valueWrapper = new ValueWrapper(value);
    return super.put(key, valueWrapper);
  }

  /**
   * Add a new value to the cache
   * @param key the key, typically a String
   * @param value the value
   * @param lCacheInterval an expiration timeout value, in seconds, which will  
   * override the default cache value just for this item. If a negative timeout
   * value is specified, the value will be cached indefinitely.                     
   * @return the cached object
   */
  public synchronized Object put(Object key, Object value, long lCacheInterval) {
    ValueWrapper valueWrapper = new ValueWrapper(value, lCacheInterval);
    return super.put(key, valueWrapper);
  }

  /**
   * Get an object from the cache.
   * @param key the key, typically a String
   * @return the value to which the key is mapped in this cache; null if the key is not mapped to any value in this cache.
   */
  public synchronized Object get(Object key) {
    ValueWrapper valueWrapper = (ValueWrapper)super.get(key);
    if (valueWrapper != null) {
      // Check if value has expired
      long creationTime = valueWrapper.getCreationTime();
      long cacheInterval = valueWrapper.getCacheInterval();
      long currentTime = System.currentTimeMillis();
      if (cacheInterval >= 0 && creationTime + cacheInterval < currentTime) {
        remove(key);
        return null;
      }
      return valueWrapper.getValue();
    }
    else
      return null;
  }

  /**
   * Removes from the cache values which have expired.
   */
  protected void sweepCache() {
    for (Iterator keyIterator = keySet().iterator(); keyIterator.hasNext();) {
      Object key = keyIterator.next();
      ValueWrapper valueWrapper = (ValueWrapper)super.get(key);
      long creationTime = valueWrapper.getCreationTime();
      long cacheInterval = valueWrapper.getCacheInterval();
      long currentTime = System.currentTimeMillis();
      if (cacheInterval >= 0 && creationTime + cacheInterval < currentTime) {
        remove(key);
      }
    }
  }

  private class ValueWrapper {
    private long lCreationTime = System.currentTimeMillis();
    private long lCacheInterval = iExpirationTimeout;
    private Object oValue;

    protected ValueWrapper(Object oValue) {
      this.oValue = oValue;
    }

    protected ValueWrapper(Object oValue, long lCacheInterval) {
      this.oValue = oValue;
      this.lCacheInterval = lCacheInterval*1000;
    }

    protected Object getValue() {
      return  oValue;
    }

    protected void setValue(Object oValue) {
      this.oValue = oValue;
    }

    protected long getCreationTime() {
      return  lCreationTime;
    }

    protected void setCreationTime(long lCreationTime) {
      this.lCreationTime = lCreationTime;
    }

    protected long getCacheInterval() {
      return  lCacheInterval;
    }

    protected void setCacheInterval(long lCacheInterval) {
      this.lCacheInterval = lCacheInterval;
    }
  }
}



