package org.jasig.portal;

import java.util.*;

/**
 * The SmartCache class is used to store objects in memory for
 * a specified amount of time.
 * @author Ken Weiner
 * @version $Revision$
 */
public class SmartCache extends java.util.Hashtable
{
  protected int iSweepInterval = 3600000;   // default to 1 hour

  public SmartCache (int iSweepInterval)
  {
    super ();
    this.iSweepInterval = iSweepInterval * 1000;
  }

  public SmartCache ()
  {
    super ();
  }

  public synchronized Object put (Object key, Object value)
  {
    ValueWrapper valueWrapper = new ValueWrapper (value);
    return super.put (key, valueWrapper);
  }

  public synchronized Object put (Object key, Object value, long lCacheInterval)
  {
    ValueWrapper valueWrapper = new ValueWrapper (value, lCacheInterval);
    return super.put (key, valueWrapper);
  }

  public synchronized Object get (Object key)
  {
    ValueWrapper valueWrapper = (ValueWrapper) super.get (key);

    if (valueWrapper != null)
    {
      // Check if value has expired
      if (valueWrapper.getCreationTime () + valueWrapper.getCacheInterval () < System.currentTimeMillis())
      {
        remove (key);
        return null;
      }
      return valueWrapper.getValue ();
    }
    else
      return null;
  }

  /**
   * Removes from the cache values which have expired
   */
  protected void sweepCache ()
  {
    for (Enumeration enum = keys ();enum.hasMoreElements ();)
    {
      Object key = enum.nextElement ();
      ValueWrapper valueWrapper = (ValueWrapper) super.get (key);

      if (valueWrapper.getCreationTime () + valueWrapper.getCacheInterval () < System.currentTimeMillis())
      {
        remove (key);
      }
    }
  }

  private class ValueWrapper
  {
    private long lCreationTime = System.currentTimeMillis ();
    private long lCacheInterval = 3600000;
    private Object oValue;

    protected ValueWrapper (Object oValue)
    {
      this.oValue = oValue;
    }

    protected ValueWrapper (Object oValue, long lCacheInterval)
    {
      this.oValue = oValue;
      this.lCacheInterval = lCacheInterval;
    }

    protected Object getValue ()
    {
      return oValue;
    }

    protected void setValue (Object oValue)
    {
      this.oValue = oValue;
    }

    protected long getCreationTime ()
    {
      return lCreationTime;
    }

    protected void setCreationTime (long lCreationTime)
    {
      this.lCreationTime = lCreationTime;
    }

    protected long getCacheInterval ()
    {
      return lCacheInterval;
    }

    protected void setCacheInterval (long lCacheInterval)
    {
      this.lCacheInterval = lCacheInterval;
    }
  }
}
