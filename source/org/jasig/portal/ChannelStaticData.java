package org.jasig.portal;

import  java.util.Map;
import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.security.ISecurityContext;
import  org.jasig.portal.security.IAuthorizationPrincipal;
import  org.jasig.portal.services.AuthorizationService;
// import  org.jasig.portal.security.PermissionManager;
import  org.jasig.portal.services.LogService;
import  javax.naming.Context;
import  java.util.Hashtable;


/**
 * Used to store channel configuration items and parameters.
 * @author Ken Weiner, Peter Kharchenko
 * @version $Revision$
 * @author Peter Kharchenko
 */
public class ChannelStaticData extends Hashtable {
  private long m_timeout = java.lang.Long.MAX_VALUE;
  // Cache a reference to the portal's JNDI context
  private Context m_portalContext = null;
  // This is the ID that globally identifies the channel
  private String m_channelGlobalID = null;
  // This is the ID that locally identifies the channel in the user's layout
  private String m_channelInstanceID = null;
  // Cache the IPerson
  private IPerson m_person = null;
  // Cache the security context
  private ISecurityContext m_securityContext = null;
  // Cache the PermissionManager for this channel
  //  private PermissionManager m_permissionManager = null;

  /**
   * Similar to the {@link #getParameter(String)}, but can be used to pass things other then strings.
   * @param key
   * @return
   */
  public synchronized Object get (Object key) {
    return  super.get(key);
  }
  /**
   * Returns an instance of the IAuthorizationPrincipal for the IPerson
   * @return
   */
  public IAuthorizationPrincipal getAuthorizationPrincipal () {
    String key = "" + getPerson().getID();
    Class type = org.jasig.portal.security.IPerson.class;
    IAuthorizationPrincipal ap = null;
    try
    {
      ap = AuthorizationService.instance().newPrincipal(key, type);
    }
    catch (AuthorizationException ae)
    {
        LogService.instance().log(LogService.ERROR, "Could not get authorization service: " + ae);
    }
    return ap;
  }
  /**
   * put your documentation comment here
   */
  public String getChannelGlobalID () {
    return  (m_channelGlobalID);
  }
  /**
   * Gets the channel ID
   * @return the channel's ID
   */
  public String getChannelID () {
    return  (m_channelInstanceID);
  }
  /**
   * Obtain a channel JNDI context
   * @return JNDI context
   */
    public Context getJNDIContext() {
        return m_portalContext;
    }
  /**
   * Get information contained in a particular <param> element
   * @param key param name
   * @return param value
   */
  public synchronized String getParameter (String key) {
    return  (String)super.get(key);
  }
  /**
   * put your documentation comment here
   * @return
   */
  public IPerson getPerson () {
    return  (m_person);
  }
  /**
   * put your documentation comment here
   * @return
   */
  public long getTimeout () {
    return  (m_timeout);
  }
  /**
   * If you need to pass Objects, use this
   * Similar to the {@link #setParameter(String,String)}, but can be used to pass things other then strings.
   * @param key
   * @param value
   * @return
   */
  public synchronized Object put (Object key, Object value) {
    return  super.put(key, value);
  }
  /**
   * put your documentation comment here
   * @param   String channelGlobalID
   */
  public void setChannelGlobalID (String channelGlobalID) {
    m_channelGlobalID = channelGlobalID;
  }
  /**
   * Sets the channel instance ID
   * @param sChannelID the unique channelID
   */
  public void setChannelID (String sChID) {
    m_channelInstanceID = sChID;
  }
    /**
     * Set channel JNDI context.
     *
     * @param c a <code>Context</code> value
     */
    public void setJNDIContext(Context c) {
        m_portalContext=c;
    }
  /**
   * Set information contained in a channel <param> element
   * Parameters are strings!
   * @param key param name
   * @param value param value
   */
  public synchronized String setParameter (String key, String value) {
    return  (String)super.put(key, value);
  }
  /**
   * Copy parameter list from a Map
   * @param params a map of params
   */
  public void setParameters (Map params) {
    // Copy the map
    putAll(params);
  }
  /**
   * put your documentation comment here
   * @param per
   */
  public void setPerson (IPerson person) {
    m_person = person;
  }
  /**
   * put your documentation comment here
   * @param value
   */
  public void setTimeout (long value) {
    m_timeout = value;
  }
}
