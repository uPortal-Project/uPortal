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


/**
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class Authentication {
  protected org.jasig.portal.security.IPerson m_Person = null;
  protected ISecurityContext ic = null;

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
        // Set the user's GlobalUID and Userid (also known as username)
        // GlobalUID is the integer key to to user data in uPortal reference implementation
        // username is a string also called uid in the eduPerson 1.0 specification.
        // These two attributes generally would come from the principal created for
        // the current security context.
        m_Person.setID(me.getGlobalUID());
        m_Person.setAttribute("username", me.getUID());
        try {
          // Directory information to be filled in for the user would usually come from a
          // directory service such as LDAP.  In the reference implementation we retrieve these
          // attributes from the database.
          String directoryInfo[] = GenericPortalBean.getUserLayoutStore().getUserDirectoryInformation(me.getUID());
          // Set the user's full name
          m_Person.setFullName(directoryInfo[0] + " " + directoryInfo[1]);
          // And set the email address
          m_Person.setAttribute("Email", directoryInfo[2]);
        } catch (Exception e) {
        // nothing do do if no directory info
        }
      } 
      else {
        // Set the IPerson to be the AdditionalDescriptor object
        m_Person = (IPerson)addInfo;
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



