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


package  org.jasig.portal;

import  java.util.*;


/**
 * The SmartCache class is used to store objects in memory for
 * a specified amount of time.
 * @author Ken Weiner
 * @version $Revision$
 * @deprecated Use org.jasig.portal.utils.SmartCache
 */
public class SmartCache extends HashMap {
  protected int iSweepInterval = 3600000;       // default to 1 hour

  /**
   * put your documentation comment here
   * @param   int iSweepInterval
   */
  public SmartCache (int iSweepInterval) {
    super();
    this.iSweepInterval = iSweepInterval*1000;
  }

  /**
   * put your documentation comment here
   */
  public SmartCache () {
    super();
  }

  /**
   * put your documentation comment here
   * @param key
   * @param value
   * @return 
   */
  public synchronized Object put (Object key, Object value) {
    ValueWrapper valueWrapper = new ValueWrapper(value);
    return  super.put(key, valueWrapper);
  }

  /**
   * put your documentation comment here
   * @param key
   * @param value
   * @param lCacheInterval
   * @return 
   */
  public synchronized Object put (Object key, Object value, long lCacheInterval) {
    ValueWrapper valueWrapper = new ValueWrapper(value, lCacheInterval);
    return  super.put(key, valueWrapper);
  }

  /**
   * put your documentation comment here
   * @param key
   * @return 
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
   * Removes from the cache values which have expired
   */
  protected void sweepCache () {
    for (Iterator keyIterator = keySet().iterator(); keyIterator.hasNext();)
    {
      Object key = keyIterator.next();
      ValueWrapper valueWrapper = (ValueWrapper)super.get(key);
      if (valueWrapper.getCreationTime() + valueWrapper.getCacheInterval() < System.currentTimeMillis())
      {
        remove(key);
      }
    }
  }

  private class ValueWrapper {
    private long lCreationTime = System.currentTimeMillis();
    private long lCacheInterval = 3600000;
    private Object oValue;

    /**
     * put your documentation comment here
     * @param     Object oValue
     */
    protected ValueWrapper (Object oValue) {
      this.oValue = oValue;
    }

    /**
     * put your documentation comment here
     * @param     Object oValue
     * @param     long lCacheInterval
     */
    protected ValueWrapper (Object oValue, long lCacheInterval) {
      this.oValue = oValue;
      this.lCacheInterval = lCacheInterval;
    }

    /**
     * put your documentation comment here
     * @return 
     */
    protected Object getValue () {
      return  oValue;
    }

    /**
     * put your documentation comment here
     * @param oValue
     */
    protected void setValue (Object oValue) {
      this.oValue = oValue;
    }

    /**
     * put your documentation comment here
     * @return 
     */
    protected long getCreationTime () {
      return  lCreationTime;
    }

    /**
     * put your documentation comment here
     * @param lCreationTime
     */
    protected void setCreationTime (long lCreationTime) {
      this.lCreationTime = lCreationTime;
    }

    /**
     * put your documentation comment here
     * @return 
     */
    protected long getCacheInterval () {
      return  lCacheInterval;
    }

    /**
     * put your documentation comment here
     * @param lCacheInterval
     */
    protected void setCacheInterval (long lCacheInterval) {
      this.lCacheInterval = lCacheInterval;
    }
  }
}



