package org.jasig.portal;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;

import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Used to store channel configuration items and parameters.
 * @author Ken Weiner, Peter Kharchenko
 * @version $Revision$
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 */
public class ChannelStaticData extends Hashtable {

    private static final Log log = LogFactory.getLog(ChannelStaticData.class);
    
  private long m_timeout = java.lang.Long.MAX_VALUE;
  // Cache a reference to the portal's JNDI context
  private Context m_portalContext = null;
  // This is the ID that globally identifies the channel as
  // it's defined during the publish time.
  private String m_channelPublishId = null;
  // This is the ID that locally identifies the channel in the user's layout
  // The id is determined at the subscribe time.
  private String m_channelSubscribeId = null;
  // Cache the IPerson
  private IPerson m_person = null;
  private ICCRegistry iccr=null;

  // Cache the PermissionManager for this channel
  //  private PermissionManager m_permissionManager = null;


  /**
   * Returns an instance of the IAuthorizationPrincipal for the IPerson
   * @return instance of the IAuthorizationPrincipal for the IPerson
   */
  public IAuthorizationPrincipal getAuthorizationPrincipal() {
    return getAuthorizationPrincipal( getPerson() );
  }

  /**
   * Returns an instance of the IAuthorizationPrincipal for the IPerson
   * @param person a IPerson instance
   * @return instance of the IAuthorizationPrincipal for the IPerson
   */
  public static IAuthorizationPrincipal getAuthorizationPrincipal( IPerson person ) {
    EntityIdentifier pid = person.getEntityIdentifier();
    IAuthorizationPrincipal ap = null;
    try
    {
      ap = AuthorizationService.instance().newPrincipal(pid.getKey(), pid.getType());
    }
    catch (AuthorizationException ae)
    {
        log.error( "Could not get authorization service: " + ae);
    }
    return ap;
  }

    /**
     * Determine channel publish Id.
     *
     * @return channel's publish Id (defined at publish-time)
     */
    public String getChannelPublishId() {
        return  (m_channelPublishId);
    }

  /**
   * Gets the channel subscribe Id
   * @return the channel's Id (defined at subscribe-time)
   */
  public String getChannelSubscribeId() {
    return  (m_channelSubscribeId);
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
  public synchronized String getParameter(String key) {
    return  (String)super.get(key);
  }

  /**
   * Provide information on the user the channel is serving
   * @return <code>IPerons</code> object.
   */
  public IPerson getPerson() {
    return  (m_person);
  }

  /**
   * Maximum time the channel will be allowed to spend in the rendering cycle.
   * @return timeout (in milliseconds) after which the channel thread will be killed.
   * Ideally, channels should monitor for this timeout and abort internal execution
   * if the rendering cycle takes too long.
   */
  public long getTimeout() {
    return  (m_timeout);
  }


  /**
   * Setter method for channel publish Id
   * @param channelPublishId channel publish Id (defined at a publish-time)
   */
    public void setChannelPublishId(String channelPublishId) {
        m_channelPublishId = channelPublishId;
    }

    /**
     * Sets the channel subscribe Id
     * @param channelSubscribeId the channel subscribe Id
     */
    public void setChannelSubscribeId(String channelSubscribeId) {
        m_channelSubscribeId = channelSubscribeId;
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
    public String setParameter(String key, String value) {
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
     * Setter method for the user being served by the channel
     * @param person an <code>IPerson<code> value.
     */
    public void setPerson(IPerson person) {
        m_person = person;
    }

    /**
     * Setter method for channel timeout.
     * @param value
     */
    public void setTimeout(long value) {
        m_timeout = value;
    }

    /**
     * Obtain inter-channel communication registry object
     *
     * @return an <code>ICCRegistry</code> value
     */
    public ICCRegistry getICCRegistry() {
        return this.iccr;
    }

    /**
     * Set inter-channel communication registry object
     *
     * @param registry an <code>ICCRegistry</code> value
     */
    public void setICCRegistry(ICCRegistry registry) {
        this.iccr=registry;
    }
}
