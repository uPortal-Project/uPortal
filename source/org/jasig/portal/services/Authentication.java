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

import  org.jasig.portal.security.*;
import  org.jasig.portal.security.provider.PersonImpl;
import  org.jasig.portal.GenericPortalBean;
import  org.jasig.portal.RdbmServices;
import  org.jasig.portal.IUserIdentityStore;
import  org.jasig.portal.AuthorizationException;
import  org.jasig.portal.PropertiesManager;
import  java.util.HashMap;
import  java.util.Hashtable;
import  java.util.Enumeration;


/**
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class Authentication {
  protected org.jasig.portal.security.IPerson m_Person = null;
  protected ISecurityContext ic = null;

  /**
   * Attempts to authenticate a given IPerson based on a set of principals and credentials
   * @param principals
   * @param credentials
   * @param person
   * @exception PortalSecurityException
   */
  public void authenticate (HashMap principals, HashMap credentials, IPerson person) throws PortalSecurityException {
    // Retrieve the security context for the user
    ISecurityContext securityContext = person.getSecurityContext();
    // NOTE: At this point the service should be looking in a properties file
    //       to determine what tokens to look for that represent the principals
    //       and credentials. 
    // Retrieve the username and principal
    String username = (String)principals.get("username");
    String password = (String)credentials.get("password");
    // Make sure the principals and credentials are valid
    if (username == null || password == null) {
      return;
    }
    // Retrieve and populate an instance of the principal object
    IPrincipal principalInstance = securityContext.getPrincipalInstance();
    principalInstance.setUID(username);
    // Retrieve and populate an instance of the credentials object
    IOpaqueCredentials credentialsInstance = securityContext.getOpaqueCredentialsInstance();
    credentialsInstance.setCredentials(password);
    // Attempt to authenticate the user
    securityContext.authenticate();
    // Check to see if the user was authenticated
    if (securityContext.isAuthenticated()) {
      // Get the AdditionalDescriptor from the security context
      // This is created by the SecurityContext and should be an
      // IPerson object if present.  This is a likely scenario if the
      // security provider also supplies directory information.
      IAdditionalDescriptor addInfo = person.getSecurityContext().getAdditionalDescriptor();
      // If the IPerson object was not provided by the security context then
      // creating an IPerson object at this point and populating it from
      // directory information is the recommended scenario.
      if (addInfo == null || !(addInfo instanceof PersonImpl)) {
        // Username attribute comes from principal
        // It is either what was typed in or supplied by the security provider
        person.setAttribute("username", username);
        // Retrieve all of the attributes associated with the person logging in
        Hashtable attribs = (new PersonDirectory()).getUserDirectoryInformation(username);
        // Add each of the attributes to the IPerson
        Enumeration en = attribs.keys();
        while (en.hasMoreElements()) {
          String key = (String)en.nextElement();
          String value = (String)attribs.get(key);
          person.setAttribute(key, value);
        }
        // Use portal display name if one exists
        if (attribs.get("portalDisplayName") != null) {
          person.setFullName((String)attribs.get("portalDisplayName"));
        } 
        // If not try the eduPerson displyName
        else if (attribs.get("displayName") != null) {
          person.setFullName((String)attribs.get("displayName"));
        }
        // If still no FullName use an unrecognized string
        if (person.getFullName() == null) {
          person.setFullName("Unrecognized person: " + person.getAttribute("username"));
        }
      } 
      else {
        // Set the IPerson to be the AdditionalDescriptor object
        person = (IPerson)addInfo;
      }
      // Find the uPortal userid for this user or flunk authentication if not found
      // The template username should actually be derived from directory information.
      // The reference implemenatation sets the uPortalTemplateUserName to the default in
      // the portal.properties file.
      // A more likely template would be staff or faculty or undergraduate.
      PropertiesManager pm = new PropertiesManager();
      boolean autocreate = pm.getPropertyAsBoolean("org.jasig.portal.services.Authentication.autoCreateUsers");
      if (autocreate && person.getAttribute("uPortalTemplateUserName") == null) {
        person.setAttribute("uPortalTemplateUserName", pm.getProperty("org.jasig.portal.services.Authentication.defaultTemplateUserName"));
      }
      IUserIdentityStore UIDStore = RdbmServices.getUserIdentityStoreImpl();
      try {
        int newUID = UIDStore.getPortalUID(person, autocreate);
        person.setID(newUID);
      } catch (AuthorizationException ae) {
        LogService.instance().log(LogService.ERROR, ae);
      }
    }
  }

  /**
   * Authenticate a user.
   * @param sUserName User name
   * @param sPassword User password
   * @return true if successful, otherwise false.
   */
  public boolean authenticate (String sUserName, String sPassword) throws PortalSecurityException {
    IPrincipal me;
    IOpaqueCredentials op;
    ic = new InitialSecurityContext("root");
    me = ic.getPrincipalInstance();
    op = ic.getOpaqueCredentialsInstance();
    me.setUID(sUserName);
    op.setCredentials(sPassword);
    ic.authenticate();
    me = ic.getPrincipal();

    /* get the principal which may have changed */
    // Check to see if the user is authenticated
    boolean bAuthenticated = ic.isAuthenticated();
    if (bAuthenticated) {
      // Get the AdditionalDescriptor from the security context
      // This is created by the SecurityContext and should be an
      // IPerson object if present.  This is a likely scenario if the
      // security provider also supplies directory information.
      IAdditionalDescriptor addInfo = ic.getAdditionalDescriptor();
      // If the IPerson object was not provided by the security context then
      // creating an IPerson object at this point and populating it from
      // directory information is the recommended scenario.
      if (addInfo == null || !(addInfo instanceof PersonImpl)) {
        // Create a new IPerson
        m_Person = new PersonImpl();
        // username attribute comes from principal
        // It is either what was typed in or supplied by the security provider
        m_Person.setAttribute("username", me.getUID());
        java.util.Hashtable attribs = (new PersonDirectory()).getUserDirectoryInformation(me.getUID());
        java.util.Enumeration en = attribs.keys();
        while (en.hasMoreElements()) {
          String key = (String)en.nextElement();
          String value = (String)attribs.get(key);
          m_Person.setAttribute(key, value);
        }
        // use portal display name if one exists
        if (attribs.get("portalDisplayName") != null)
          m_Person.setFullName((String)attribs.get("portalDisplayName"));
        // if not try the eduPerson displyName
        else if (attribs.get("displayName") != null)
          m_Person.setFullName((String)attribs.get("displayName"));
        // if still no FullName use an unrecognized string
        if (m_Person.getFullName() == null)
          m_Person.setFullName("Unrecognized person: " + m_Person.getAttribute("username"));
      } 
      else {
        // Set the IPerson to be the AdditionalDescriptor object
        m_Person = (IPerson)addInfo;
      }
      // find the uPortal userid for this user or flunk authentication if not found
      // The template username should actually be derived from directory information.
      // The reference implemenatation sets the uPortalTemplateUserName to the default in
      // the portal.properties file.
      // A more likely template would be staff or faculty or undergraduate.
      PropertiesManager pm = new PropertiesManager();
      boolean autocreate = pm.getPropertyAsBoolean("org.jasig.portal.services.Authentication.autoCreateUsers");
      if (autocreate && m_Person.getAttribute("uPortalTemplateUserName") == null) {
        m_Person.setAttribute("uPortalTemplateUserName", pm.getProperty("org.jasig.portal.services.Authentication.defaultTemplateUserName"));
      }
      IUserIdentityStore UIDStore = RdbmServices.getUserIdentityStoreImpl();
      try {
        int newUID = UIDStore.getPortalUID(m_Person, autocreate);
        m_Person.setID(newUID);
      } catch (AuthorizationException ae) {
        return  (false);
      }
    }
    return  (bAuthenticated);
  }

  /**
   * Returns an IPerson object that can be used to hold site-specific attributes
   * about the logged on user.  This information is established during
   * authentication.
   * @return An object that implements the
   * <code>org.jasig.portal.security.IPerson</code> interface.
   */
  public IPerson getPerson () {
    return  m_Person;
  }

  /**
   * Returns an ISecurityContext object that can be used
   * later. This object is passed as part of the IChannel Interface.
   * The security context may be used to gain authorized access to
   * services.
   * @return An object that implements the
   * <code>org.jasig.portal.security.ISecurityContext</code> interface.
   */
  public ISecurityContext getSecurityContext () {
    return  ic;
  }
}



