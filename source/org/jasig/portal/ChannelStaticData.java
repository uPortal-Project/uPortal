package org.jasig.portal;

import java.util.Map;

/**
 * Used to store channel configuration items and parameters.
 * @author Ken Weiner, Peter Kharchenko
 * @version 0.2
 * @author Peter Kharchenko
 */

public class ChannelStaticData extends java.util.Hashtable
{
    private String sChannelID = null;
  
    // Parameters are strings !
    /**
     * Set information contained in a channel <param> element
     * @param key param name
     * @param value param value
     */
    public synchronized String setParameter (String key, String value) {return (String) super.put (key, value);}
    /**
     * Get information contained in a particular <param> element
     * @param key param name
     * @return param value
     */
    public synchronized String getParameter (String key) {return (String) super.get (key);}


    // if you need to pass Objects, use this
    /**
     * Similar to the {@link #setParameter(String,String)}, but can be used to pass things other then strings.
     */
    public synchronized Object put (Object key, Object value) {return super.put (key, value);}
    /**
     * Similar to the {@link #getParameter(String)}, but can be used to pass things other then strings.
     */
    public synchronized Object get (Object key) {return super.get (key);}
    

    /**
     * Copy parameter list from a Map
     * @param params a map of params
     */
    public void setParameters(Map params) {
	// copy a Map 
	this.putAll(params);
    };
    
  
    /**
     * Sets the channel ID
     * @param sChannelID the unique channelID
     */
    public void setChannelID (String sChID) {this.sChannelID = sChID;}
    
    /**
     * Gets the channel ID
     * @return the channel's ID
     */
    public String getChannelID () {return sChannelID;}  
}
    
    
