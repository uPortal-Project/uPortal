/**
 * Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
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
 */

package org.jasig.portal.security.provider;

import org.jasig.portal.LdapServices;
import org.jasig.portal.security.*;
import org.jasig.portal.Logger;
import org.jasig.portal.RdbmServices;
import java.util.*;
import java.security.MessageDigest;

import javax.naming.*;
import javax.naming.directory.*;

import jsbook.chapter11.crypt.JCrypt;

/**
 * <p>This is an implementation of a SecurityContext that checks a user's
 * credentials against a UNIX encrypted password entry from an LDAP
 * directory.  It expects to be able to query an LDAP directory for
 * a user and get back that user's credentials stored in the userPassword
 * attribute in this form, <b>"{crypt}xdfi9jadkjasjk"</b>, where
 * <b>"{crypt}"</b> indicates that the UNIX crypt function is used to
 * encrypt the cleartext password.</p>
 *
 * @author Russell Tokuyama (University of Hawaii)
 * @version $Revision$
 */

public class SimpleLdapSecurityContext extends ChainingSecurityContext
  implements SecurityContext {
  
  // Attributes that we're interested in.
  public static final int ATTR_UID         = 0;
  public static final int ATTR_CREDENTIALS = ATTR_UID + 1;
  public static final int ATTR_FIRSTNAME   = ATTR_CREDENTIALS + 1;
  public static final int ATTR_LASTNAME    = ATTR_FIRSTNAME + 1;

  private final int SIMPLE_LDAP_SECURITYAUTHTYPE = 0xFF03;
  private static final String[] attributes = {
    "uid",            // user ID
    "userPassword",   // credentials
    "givenName",      // first name
    "sn"              // last name
  };

  
  SimpleLdapSecurityContext() {
    super();
  }

  /**
   * Returns the type of authentication this class provides.
   * @return authorization type
   */
  public int getAuthType() {
    /*
     * What is this for?  No one would know what to do with the
     * value returned.  Subclasses might know but our getAuthType()
     * doesn't return anything easily useful.
     */
    return this.SIMPLE_LDAP_SECURITYAUTHTYPE;
  }

  /**
   * Authenticates the user.
   */
  public synchronized void authenticate() {
    this.isauth = false;
    
    LdapServices ldapservices = new LdapServices();
    if (this.myPrincipal.UID != null &&
        this.myOpaqueCredentials.credentialstring != null) {
      
      DirContext conn = null;
      Attributes results = null;

      String baseDN = null;
      String user = null;
      String passwd = null;
      String first_name = null;
      String last_name = null;
      
      user = "uid=" + this.myPrincipal.UID + "," + ldapservices.getBaseDN();
      passwd = new String(this.myOpaqueCredentials.credentialstring);
      Logger.log(Logger.DEBUG, "Looking for " + user);
      
      try {
        conn = ldapservices.getConnection();
        results = conn.getAttributes(user, attributes);
        if (results != null) {
          if (! (this.isauth = isAuth(results, passwd)))
            return;
          
          first_name = getAttributeValue(results, ATTR_FIRSTNAME);
          last_name  = getAttributeValue(results, ATTR_LASTNAME);
          this.myPrincipal.FullName = first_name + " " + last_name;
          Logger.log(Logger.DEBUG, "User " + this.myPrincipal.UID +
                     " is authenticated");
        }
        else {
          Logger.log(Logger.ERROR, "No such user: " + this.myPrincipal.UID);
        }
      }
      catch (Exception e) {
        Logger.log(Logger.ERROR, new PortalSecurityException
                   ("LDAP Error" + e));
      }
      finally {
        ldapservices.releaseConnection(conn);
      }
    }
    else {
      Logger.log(Logger.ERROR,
                 "Principal or OpaqueCredentials not initialized prior to authenticate");
    }
    
    // Ok...we are now ready to authenticate all of our subcontexts.
    super.authenticate();
    return;
  }
  
  
  /*--------------------- Helper methods ---------------------*/

  /**
   * Determines if a user has supplied the correct credentials.  In
   * this implementation, the UNIX crypt encryption method is used.
   * The user's credentials are expected to be stored in the userPassword
   * attribute in this form, <b>"{crypt}xdfi9jadkjasjk"</b>, where
   * <b>"{crypt}"</b> indicates that the UNIX crypt function is used to
   * encrypt the cleartext password.</p>
   *
   * <p>We use the encrypted portion of the userPassword attribute value
   * to encrypt the supplied user password and then compare the two.
   * If they are the same, the user is authenticated.</p>
   */
  private boolean isAuth(Attributes results, String passwd)
    throws NamingException {
    
    NamingEnumeration passwords = null;
    String aPassword = null;
    String encryptedPassword = null;
    Object o = null;
    Attribute attrib = results.get(attributes[ATTR_CREDENTIALS]);
    
    if (attrib != null) {
      // look for the UNIX encrypted password
      for (passwords = attrib.getAll(); passwords.hasMoreElements(); ) {
        o = passwords.nextElement();
        if (o.getClass().getName().trim().equals("[B")) {
          aPassword = new String((byte[]) o);
          Logger.log(Logger.DEBUG, "ldap credentials = " + aPassword);
          if (aPassword != null && aPassword.startsWith("{crypt}")) {
            aPassword = aPassword.trim().substring(7); // encrypted part
            encryptedPassword = JCrypt.crypt(aPassword, passwd);
            if (encryptedPassword.equals(aPassword)) {
              Logger.log(Logger.INFO, "ldap credentials matched");
              return true;
            }
          }
        }
      } // for (passwords...
    } // if (attrib != null)
    else {            
      Logger.log(Logger.ERROR, "Password not found for " +
                 this.myPrincipal.UID);
      return false;  // user not authenticated
    }
    Logger.log(Logger.DEBUG, "SimpleLdapSecurityContext.isAuth() returning false");
    return false;
  }
  
  /**
   * <p>Return a single value of an attribute from possibly multiple values,
   * grossly ignoring anything else.  If there are no values, then
   * return an empty string.</p>
   *
   * @param results LDAP query results
   * @param attribute LDAP attribute we are interested in
   * @return a single value of the attribute
   */
  private String getAttributeValue(Attributes results, int attribute)
    throws NamingException {
    NamingEnumeration values = null;
    String aValue = "";
    
    if (!isAttribute(attribute))
      return aValue;
    
    Attribute attrib = results.get(attributes[attribute]);
    if (attrib != null) {
      for (values = attrib.getAll(); values.hasMoreElements(); ) {
        aValue = (String) values.nextElement();
        break;  // take only the first attribute value
      }
    }
    return aValue;
  }

  /**
   * Is this a value attribute that's been requested?
   *
   * @param attribute in question
   */
  private boolean isAttribute(int attribute) {
    if (attribute < ATTR_UID || attribute > ATTR_LASTNAME) {
      return false;
    }
    return true;
  }

} // SimpleLdapSecurityContext

// eof: SimpleLdapSecurityContext.java
