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


package  org.jasig.portal.services;

import  java.util.Vector;
import  java.io.File;
import  java.io.FileInputStream;
import  java.io.IOException;
import  java.util.Properties;
import  org.jasig.portal.GenericPortalBean;
import  org.jasig.portal.security.IRole;
import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.security.IAuthorization;
import  org.jasig.portal.security.IAuthorizationFactory;
import  org.jasig.portal.security.PortalSecurityException;


/**
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$
 */
public class Authorization {
  protected IAuthorization m_authorization = null;
  protected static String s_factoryName = null;
  protected static IAuthorizationFactory m_Factory = null;
  static {
    // Get the security properties file
    File secprops = new File(GenericPortalBean.getPortalBaseDir() + "properties" + File.separator + "security.properties");
    // Get the properties from the security properties file
    Properties pr = new Properties();
    try {
      pr.load(new FileInputStream(secprops));
      // Look for our authorization factory and instantiate an instance of it or die trying.
      if ((s_factoryName = pr.getProperty("authorizationProvider")) == null) {
        LogService.instance().log(LogService.ERROR, new PortalSecurityException("AuthorizationProvider not specified or incorrect in security.properties"));
      }
      else {
        try {
          m_Factory = (IAuthorizationFactory)Class.forName(s_factoryName).newInstance();
        } catch (Exception e) {
          LogService.instance().log(LogService.ERROR, new PortalSecurityException("Failed to instantiate " + s_factoryName));
        }
      }
    } catch (IOException e) {
      LogService.instance().log(LogService.ERROR, new PortalSecurityException(e.getMessage()));
    }
  }

  /**
   * put your documentation comment here
   */
  public Authorization () {
    // From our factory get an actual authorization instance
    m_authorization = m_Factory.getAuthorization();
  }

  /**
   * put your documentation comment here
   * @param person
   * @param role
   * @return
   */
  public boolean isUserInRole (IPerson person, IRole role) {
    return  (m_authorization.isUserInRole(person, role));
  }

  /**
   * put your documentation comment here
   * @return
   */
  public Vector getAllRoles () {
    return  (m_authorization.getAllRoles());
  }

  /**
   * put your documentation comment here
   * @param channelID
   * @param roles
   * @return
   */
  public int setChannelRoles (int channelID, Vector roles) {
    return  (m_authorization.setChannelRoles(channelID, roles));
  }

  /**
   * put your documentation comment here
   * @param person
   * @return
   */
  public boolean canUserPublish (IPerson person) {
    return  (m_authorization.canUserPublish(person));
  }

  // For the subscribe mechanism to use
  public Vector getAuthorizedChannels (IPerson person) {
    return  (m_authorization.getAuthorizedChannels(person));
  }

  /**
   * put your documentation comment here
   * @param person
   * @param channelID
   * @return
   */
  public boolean canUserSubscribe (IPerson person, int channelID) {
    return  (m_authorization.canUserSubscribe(person, channelID));
  }

  /**
   * put your documentation comment here
   * @param person
   * @param channelID
   * @return
   */
  public boolean canUserRender (IPerson person, int channelID) {
    return  (m_authorization.canUserRender(person, channelID));
  }

  /**
   * put your documentation comment here
   * @param person
   * @param channelID
   * @return
   */
  public Vector getChannelRoles (int channelID) {
    return  (m_authorization.getChannelRoles(channelID));
  }

  /**
   * put your documentation comment here
   * @param person
   * @return
   */
  public Vector getUserRoles (IPerson person) {
    return  (m_authorization.getUserRoles(person));
  }

  /**
   * put your documentation comment here
   * @param person
   * @param roles
   */
  public void addUserRoles (IPerson person, Vector roles) {
    m_authorization.addUserRoles(person, roles);
  }

  /**
   * put your documentation comment here
   * @param person
   * @param roles
   */
  public void removeUserRoles (IPerson person, Vector roles) {
    m_authorization.removeUserRoles(person, roles);
  }
}



