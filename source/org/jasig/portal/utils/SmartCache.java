/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.utils;

import java.util.HashMap;
import java.util.Iterator;


/**
 * The SmartCache class is used to store objects in memory for
 * a specified amount of time.  The time should be specified in seconds.
 * @author Ken Weiner, kweiner@interactivebusiness.com
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
  public SmartCache (int iExpirationTimeout) {
    super();
    this.iExpirationTimeout = iExpirationTimeout*1000;
  }

  /**
   * Instantiate SmartCache with a default expiration timeout of one hour.
   */
  public SmartCache () {
    super();
  }

  /**
   * Add a new value to the cache.  The value will expire in accordance with the
   * cache's expiration timeout value which was set when the cache was created.
   * @param key the key, typically a String
   * @param value the value
   * @return the previous value of the specified key in this hashtable, or null if it did not have one.
   */
  public synchronized Object put (Object key, Object value) {
    ValueWrapper valueWrapper = new ValueWrapper(value);
    return  super.put(key, valueWrapper);
  }

  /**
   * Add a new value to the cache
   * @param key the key, typically a String
   * @param value the value
   * @param lCacheInterval an expiration timeout value which will override the default cache value just for this item
   * @return Object
   */
  public synchronized Object put (Object key, Object value, long lCacheInterval) {
    ValueWrapper valueWrapper = new ValueWrapper(value, lCacheInterval);
    return  super.put(key, valueWrapper);
  }

  /**
   * Get an object from the cache.
   * @param key the key, typically a String
   * @return the value to which the key is mapped in this cache; null if the key is not mapped to any value in this cache.
   */
  public synchronized Object get (Object key) {
    ValueWrapper valueWrapper = (ValueWrapper)super.get(key);
    if (valueWrapper != null) {
      // Check if value has expired
      if (valueWrapper.getCreationTime() + valueWrapper.getCacheInterval() < System.currentTimeMillis()) {
        remove(key);
        return  null;
      }
      return  valueWrapper.getValue();
    }
    else
      return  null;
  }

  /**
   * Removes from the cache values which have expired.
   */
  protected void sweepCache () {
    for (Iterator keyIterator = keySet().iterator(); keyIterator.hasNext();) {
      Object key = keyIterator.next();
      ValueWrapper valueWrapper = (ValueWrapper)super.get(key);
      if (valueWrapper.getCreationTime() + valueWrapper.getCacheInterval() < System.currentTimeMillis()) {
        remove(key);
      }
    }
  }

  private class ValueWrapper {
    private long lCreationTime = System.currentTimeMillis();
    private long lCacheInterval = iExpirationTimeout;
    private Object oValue;

    protected ValueWrapper (Object oValue) {
      this.oValue = oValue;
    }

    protected ValueWrapper (Object oValue, long lCacheInterval) {
      this.oValue = oValue;
      this.lCacheInterval = lCacheInterval*1000;
    }

    protected Object getValue () {
      return  oValue;
    }

    protected void setValue (Object oValue) {
      this.oValue = oValue;
    }

    protected long getCreationTime () {
      return  lCreationTime;
    }

    protected void setCreationTime (long lCreationTime) {
      this.lCreationTime = lCreationTime;
    }

    protected long getCacheInterval () {
      return  lCacheInterval;
    }

    protected void setCacheInterval (long lCacheInterval) {
      this.lCacheInterval = lCacheInterval;
    }
  }
}



