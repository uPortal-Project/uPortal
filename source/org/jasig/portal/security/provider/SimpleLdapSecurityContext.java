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


package  org.jasig.portal.security.provider;

import java.util.Properties;
import java.util.Vector;

import javax.naming.AuthenticationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.jasig.portal.LdapServices;
import org.jasig.portal.security.IConfigurableSecurityContext;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.services.LogService;


/**
 * <p>This is an implementation of a SecurityContext that checks a user's
 * credentials against an LDAP directory.  It expects to be able to bind
 * to the LDAP directory as the user so that it can authenticate the
 * user.</p>
 *
 * @author Russell Tokuyama (University of Hawaii)
 * @version $Revision$
 */
public class SimpleLdapSecurityContext extends ChainingSecurityContext
    implements IConfigurableSecurityContext {
  // Attributes that we're interested in.
  public static final int ATTR_UID = 0;
  public static final int ATTR_FIRSTNAME = ATTR_UID + 1;
  public static final int ATTR_LASTNAME = ATTR_FIRSTNAME + 1;
  private final int SIMPLE_LDAP_SECURITYAUTHTYPE = 0xFF04;
  private static final String[] attributes =  {
    "uid",      // user ID
    "givenName",                // first name
    "sn"        // last name
  };
  
  private static final String LDAP_PROPERTIES_FILE_PROP_NAME = "properties";
  private Properties ctxProperties;

  SimpleLdapSecurityContext() {
    super();
    ctxProperties = new Properties();
  }
  
  /**
   * Sets the properties to use for this security context.
   * 
   * @see org.jasig.portal.security.IConfigurableSecurityContext#setProperties(java.util.Properties)
   */
  public void setProperties(Properties props)
  {
      ctxProperties = props;
  }  

  /**
   * Returns the type of authentication this class provides.
   * @return authorization type
   */
  public int getAuthType () {
    /*
     * What is this for?  No one would know what to do with the
     * value returned.  Subclasses might know but our getAuthType()
     * doesn't return anything easily useful.
     */
    return  this.SIMPLE_LDAP_SECURITYAUTHTYPE;
  }

  /**
   * Authenticates the user.
   */
  public synchronized void authenticate () throws PortalSecurityException {
    this.isauth = false;
    LdapServices ldapservices;
    
    String propFile = ctxProperties.getProperty(LDAP_PROPERTIES_FILE_PROP_NAME);
    if(propFile != null && propFile.length() > 0)
        ldapservices = new LdapServices(propFile);
    else
        ldapservices = new LdapServices();    
    
    String creds = new String(this.myOpaqueCredentials.credentialstring);
    if (this.myPrincipal.UID != null && !this.myPrincipal.UID.trim().equals("") && this.myOpaqueCredentials.credentialstring
        != null && !creds.trim().equals("")) {
      DirContext conn = null;
      NamingEnumeration results = null;
      String baseDN = null;
      StringBuffer user = new StringBuffer("(");
      String passwd = null;
      String first_name = null;
      String last_name = null;
      
      user.append(ldapservices.getUidAttribute()).append("=");
      user.append(this.myPrincipal.UID).append(")");
      LogService.log(LogService.DEBUG,
                     "SimpleLdapSecurityContext: Looking for " +
                     user.toString());
      conn = ldapservices.getConnection();
      
      // set up search controls
      SearchControls searchCtls = new SearchControls();
      searchCtls.setReturningAttributes(attributes);
      searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      
      // do lookup
      try {
        results = conn.search(ldapservices.getBaseDN(), user.toString(), searchCtls);
        if (results != null) {
          if (!results.hasMore())
            LogService.log(LogService.ERROR,
                           "SimpleLdapSecurityContext: user not found , " +
                           this.myPrincipal.UID);
          Vector entries = new Vector();
          while (results != null && results.hasMore()) {
            SearchResult entry = (SearchResult)results.next();
            StringBuffer dnBuffer = new StringBuffer();
            dnBuffer.append(entry.getName()).append(", ");
            dnBuffer.append(ldapservices.getBaseDN());
            Attributes attrs = entry.getAttributes();
            first_name = getAttributeValue(attrs, ATTR_FIRSTNAME);
            last_name = getAttributeValue(attrs, ATTR_LASTNAME);
            // re-bind as user
            conn.removeFromEnvironment(javax.naming.Context.SECURITY_PRINCIPAL);
            conn.removeFromEnvironment(javax.naming.Context.SECURITY_CREDENTIALS);
            conn.addToEnvironment(javax.naming.Context.SECURITY_PRINCIPAL, dnBuffer.toString());
            conn.addToEnvironment(javax.naming.Context.SECURITY_CREDENTIALS, this.myOpaqueCredentials.credentialstring);
            searchCtls = new SearchControls();
            searchCtls.setReturningAttributes(new String[0]);
            searchCtls.setSearchScope(SearchControls.OBJECT_SCOPE);
            
            String attrSearch = "(" + ldapservices.getUidAttribute() + "=*)";
            LogService.log(LogService.DEBUG,
                           "SimpleLdapSecurityContext: Looking in " +
                           dnBuffer.toString() + " for " + attrSearch);
            conn.search(dnBuffer.toString(), attrSearch, searchCtls);
 
            this.isauth = true;
            this.myPrincipal.FullName = first_name + " " + last_name;
            LogService.log(LogService.DEBUG,
                           "SimpleLdapSecurityContext: User " +
                           this.myPrincipal.UID + " (" +
                           this.myPrincipal.FullName + ") is authenticated");

            // Since LDAP is case-insensitive with respect to uid, force
            // user name to lower case for use by the portal
            this.myPrincipal.UID = this.myPrincipal.UID.toLowerCase();
          } // while (results != null && results.hasMore())
        }
        else {
          LogService.log(LogService.ERROR,
                         "SimpleLdapSecurityContext: No such user: " +
                         this.myPrincipal.UID);
        }
      } catch (AuthenticationException ae) {
        LogService.log(LogService.INFO,"SimpleLdapSecurityContext: Password invalid for user: " + this.myPrincipal.UID);
      } catch (Exception e) {
        LogService.log(LogService.ERROR,
                       "SimpleLdapSecurityContext: LDAP Error with user: " +
                       this.myPrincipal.UID + "; " + e);
        throw new PortalSecurityException("SimpleLdapSecurityContext: LDAP Error" + e + " with user: " + this.myPrincipal.UID);
      } finally {
        ldapservices.releaseConnection(conn);
      }
    }
    else {
      LogService.log(LogService.ERROR, "Principal or OpaqueCredentials not initialized prior to authenticate");
    }
    // Ok...we are now ready to authenticate all of our subcontexts.
    super.authenticate();
    return;
  }

  /*--------------------- Helper methods ---------------------*/
  /**
   * <p>Return a single value of an attribute from possibly multiple values,
   * grossly ignoring anything else.  If there are no values, then
   * return an empty string.</p>
   *
   * @param results LDAP query results
   * @param attribute LDAP attribute we are interested in
   * @return a single value of the attribute
   */
  private String getAttributeValue (Attributes attrs, int attribute) throws NamingException {
    NamingEnumeration values = null;
    String aValue = "";
    if (!isAttribute(attribute))
      return  aValue;
    Attribute attrib = attrs.get(attributes[attribute]);
    if (attrib != null) {
      for (values = attrib.getAll(); values.hasMoreElements();) {
        aValue = (String)values.nextElement();
        break;                  // take only the first attribute value
      }
    }
    return  aValue;
  }

  /**
   * Is this a value attribute that's been requested?
   *
   * @param attribute in question
   */
  private boolean isAttribute (int attribute) {
    if (attribute < ATTR_UID || attribute > ATTR_LASTNAME) {
      return  false;
    }
    return  true;
  }
}



