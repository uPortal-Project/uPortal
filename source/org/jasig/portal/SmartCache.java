/*
 * put your module comment here
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal;

import  java.util.*;


/**
 * The SmartCache class is used to store objects in memory for
 * a specified amount of time.
 * @author Ken Weiner
 * @version $Revision$
 * @deprecated Use org.jasig.portal.utils.SmartCache instead
 */
public class SmartCache extends java.util.Hashtable {
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
    for (Enumeration enum = keys(); enum.hasMoreElements();) {
      Object key = enum.nextElement();
      ValueWrapper valueWrapper = (ValueWrapper)super.get(key);
      if (valueWrapper.getCreationTime() + valueWrapper.getCacheInterval() < System.currentTimeMillis()) {
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



