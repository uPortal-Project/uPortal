package org.jasig.portal;

import java.net.URLEncoder;

/**
 * Used to store channel configuration items and parameters.
 * @author Ken Weiner
 * @version $Revision$
 */
public class ChannelConfig extends java.util.Hashtable
{  
  private String sChannelID = null;
  
  public synchronized Object setParameter (Object key, Object value) {return super.put (key, value);}
  public synchronized Object getParameter (Object key) {return super.get (key);}
  
  // Include these for legacy channels
  // Possibly delete them for 1.0 release if we can clean up all the channels
  public synchronized Object put (Object key, Object value) {return super.put (key, value);}
  public synchronized Object get (Object key) {return super.get (key);}
  
  /**
   * Sets the channel ID
   */
  public void setChannelID (String sChannelID) {this.sChannelID = sChannelID;}
  
  /**
   * Gets the channel ID
   * @return the channel's ID
   */
  public String getChannelID () {return sChannelID;}  
}
