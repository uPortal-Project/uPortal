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

import  java.util.Map;
import org.jasig.portal.security.PortalSecurityException;
import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.security.SecurityContext;
import  javax.naming.InitialContext;
import  javax.naming.Context;
import  java.util.Hashtable;


/**
 * Used to store channel configuration items and parameters.
 * @author Ken Weiner, Peter Kharchenko
 * @version $Revision$
 * @author Peter Kharchenko
 */
public class ChannelStaticData extends java.util.Hashtable {
  private InitialContext m_portalContext = null;
  private String m_channelGlobalID = null;
  private String m_channelInstanceID = null;
  private long m_timeout = java.lang.Long.MAX_VALUE;
  private IPerson m_person = null;
  private SecurityContext m_securityContext = null;

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
   * Get information contained in a particular <param> element
   * @param key param name
   * @return param value
   */
  public synchronized String getParameter (String key) {
    return  (String)super.get(key);
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
   * Similar to the {@link #getParameter(String)}, but can be used to pass things other then strings.
   * @param key
   * @return
   */
  public synchronized Object get (Object key) {
    return  super.get(key);
  }

  /**
   * Copy parameter list from a Map
   * @param params a map of params
   */
  public void setParameters (Map params) {
    // copy a Map
    this.putAll(params);
  }

  /**
   * Sets the channel instance ID
   * @param sChannelID the unique channelID
   */
  public void setChannelID (String sChID) {
    m_channelInstanceID = sChID;
  }

  /**
   * Gets the channel ID
   * @return the channel's ID
   */
  public String getChannelID () {
    return  (m_channelInstanceID);
  }

  /**
   * put your documentation comment here
   * @param   String channelGlobalID
   */
  public void setChannelGlobalID (String channelGlobalID) {
    m_channelGlobalID = channelGlobalID;
  }

  /**
   * put your documentation comment here
   */
  public String getChannelGlobalID () {
    return  (m_channelGlobalID);
  }

  /**
   * put your documentation comment here
   * @param value
   */
  public void setTimeout (long value) {
    m_timeout = value;
  }

  /**
   * put your documentation comment here
   * @return
   */
  public long getTimeout () {
    return  (m_timeout);
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
   * @param per
   */
  public void setPerson (IPerson person) {
    m_person = person;
  }

  /**
   * put your documentation comment here
   * @return
   */
  public SecurityContext getSecurityContext () {
    return  (m_securityContext);
  }

  /**
   * put your documentation comment here
   * @param sc
   */
  public void setSecurityContext (SecurityContext securitContext) {
    m_securityContext = securitContext;
  }

  /**
   * This method will fail in uPortal 1.x until someone implements the JNDI context!
   * @return
   */
  public InitialContext getPortalContext () {
    if (m_portalContext != null) {
      return  (m_portalContext);
    }
    else {
      Hashtable environment = new Hashtable(1);
      // Set up the path
      environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jasig.portal.jndi.PortalInitialContextFactory");
      try {
        InitialContext ctx = new InitialContext(environment);
        return  (ctx);
      } catch (Exception e) {
        e.printStackTrace(System.err);
        return  (null);
      }
    }
  }
}



