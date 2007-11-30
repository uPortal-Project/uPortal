/* Copyright 2000 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.Context;

import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.springframework.web.context.WebApplicationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Used to store channel configuration items and parameters.
 * @author Ken Weiner, Peter Kharchenko
 * @version $Revision$
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
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
  // reference to layout manager for persisting parameter changes
  private IUserLayoutManager ulm;
  
  // reference to the Spring application context the portal is running in
  private WebApplicationContext webApplicationContext;
  
  private String serializerName;

private IUserLayoutChannelDescription layoutChannelDescription = null;

  // Cache the PermissionManager for this channel
  //  private PermissionManager m_permissionManager = null;

  public ChannelStaticData()
  {
      this(null, null);
  }

  /**
   * If support is being provided to update channel parameters then a Layout
   * Manager instance is required and the initial values of parameters must be
   * set here to forgo override checks. If a LayoutManager is had for
   * ChannelStaticData then setParameter() and setParameters() restrict
   * changing parameters that are not overrideable and hence can not be used to
   * inject the initial parameter values.
   *
   * @param parameters
   * @param ulm
   */
  public ChannelStaticData(Map parameters, IUserLayoutManager ulm)
  {
      if (parameters != null)
          this.putAll(parameters);
      this.ulm = ulm;
  }
  
    /**
     * @return the webApplicationContext
     */
    public WebApplicationContext getWebApplicationContext() {
        return webApplicationContext;
    }
    /**
     * @param webApplicationContext the webApplicationContext to set
     */
    public void setWebApplicationContext(WebApplicationContext webApplicationContext) {
        this.webApplicationContext = webApplicationContext;
    }

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
     * Set information contained in a channel <param>element Parameters are
     * strings!
     *
     * @param key
     *            param name
     * @param value
     *            param value
     * @throws IllegalChannelParameterOverrideException
     *             if key is not configured to be overrideable.
     */
    public String setParameter(String key, String value) {
        if (ulm == null) // nothing can be persisted so drop back to old way
            return (String) super.put(key, value);

        try
        {
            if (getChannelDescription().canOverrideParameter(key))
                return (String) super.put(key, value);
            throw new IllegalChannelParameterOverrideException(key, value);
        }
        catch(PortalException pe)
        {
            // can't get channel description so fall back to old way
        }
        return (String) super.put(key, value);
    }

    /**
     * Returns true if the indicated parameter can be altered. Ad-hoc added
     * parameters will always be allowed to change. Parameters locked during
     * publishing or via the plugged-in layout manager will return false. If no
     * layout manager is available then this method always returns true.
     *
     * @param key
     * @return boolean
     */
    public boolean canSetParameter(String key) throws PortalException
    {
        if (ulm == null) // can't tell so everything is modifiable
            return true;

        if (getChannelDescription().canOverrideParameter(key))
            return true;
        return false;
    }

    private IUserLayoutChannelDescription getChannelDescription()
            throws PortalException
    {
        if (layoutChannelDescription == null)
        {
            layoutChannelDescription = (IUserLayoutChannelDescription) ulm
                    .getNode(getChannelSubscribeId());
        }
        return layoutChannelDescription;
    }

    /**
     * Resets the value of this parameter. If this is an overrideable parameter
     * then a user's override will be removed and the original value restored.
     * If this is a non-overrideable parameter this call will have no effect. If
     * this is an ad-hoc parameter then the parameter will be removed. Ad-hoc
     * parameters are ones added by a channel instance beyond the set of
     * parameters specified during channel publishing.
     *
     * @param key
     *            param name
     */
    public void resetParameter(String key) {
        if (ulm == null) // nothing can be persisted so follow old approach
        {
            super.remove(key);
            return;
        }

        try
        {
            getChannelDescription().resetParameter(key);
            String value = getChannelDescription().getParameterValue(key);
            if (value == null) // ad-hoc parm so delete
                super.remove(key);
            else // not ad-hoc so value was replaced with channel def value
                super.put(key, value);
        }
        catch(PortalException pe)
        {
            // can't get channel description so fall back to old way of just
            // changing parameters as needed.
            super.remove(key);
        }
    }

    /**
     * Writes all string valued parameters to the database as part of the user's
     * layout.
     */
    public void store() throws PortalException
    {
        if (ulm == null)
            return;


        IUserLayoutChannelDescription cd = getChannelDescription();

        for (Iterator itr = this.entrySet().iterator(); itr.hasNext();)
        {
            Map.Entry parm = (Entry) itr.next();
            cd.setParameterValue((String) parm.getKey(), (String) parm
                    .getValue());
        }
        ulm.updateNode(cd);
        ulm.saveUserLayout();
        layoutChannelDescription = null; // force a reload next time
    }

    /**
     * Copy parameter list from a Map
     *
     * @param params
     *            a map of params
     * @throws IllegalChannelParameterOverrideException
     *             if key is not configured to be overrideable.
     */
    public void setParameters (Map params) {
        if (ulm == null) // nothing can be persisted so drop back to old way
            putAll(params);

        try
        {
            IUserLayoutChannelDescription cd = getChannelDescription();
            for(Iterator itr = params.entrySet().iterator(); itr.hasNext();)
            {
                Map.Entry e = (Entry) itr.next();
                if (! cd.canOverrideParameter((String)e.getKey()))
                    throw new IllegalChannelParameterOverrideException(
                        (String)e.getKey(), (String) e.getValue());
            }
        }
        catch(PortalException pe)
        {
            // if an exception occurs and we can't get channel description
            // or all parameters are overrideable then accept all params
        }
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

    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append("ChannelStaticData: ");
        sb.append("Channel Publish ID = [").append(this.m_channelPublishId).append("] ");
        sb.append("Channel Subscribe ID = [").append(this.m_channelSubscribeId).append("] ");
        sb.append("person= [").append(this.m_person).append("] ");
        return sb.toString();
    }
    
    /**
     * Sets the serializer name.
     * @return serializerName
     */
    public String getSerializerName() {
        return serializerName;
    }
    
    /**
     * Setter method for the serializer name.
     * @param serializerName
     */
    public void setSerializerName(String serializerName) {
        this.serializerName = serializerName;
    }

}
