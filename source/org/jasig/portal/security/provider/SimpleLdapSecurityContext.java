/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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

import org.jasig.portal.ldap.LdapServices;
import org.jasig.portal.ldap.ILdapServer;
import org.jasig.portal.security.IConfigurableSecurityContext;
import org.jasig.portal.security.PortalSecurityException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p>This is an implementation of a SecurityContext that checks a user's
 * credentials against an LDAP directory.  It expects to be able to bind
 * to the LDAP directory as the user so that it can authenticate the
 * user.</p>
 * <p>
 * By implementing the {@link org.jasig.portal.security.IConfigurableSecurityContext}
 * interface this context may have properties set on it. The one property
 * the <code>SimpleLdapSecurityContext</code> looks for is defined by
 * the String {@link #LDAP_PROPERTIES_CONNECTION_NAME} "connection".
 * This property allows a specific, named, LDAP connection to be used by
 * the context. If no "connection" property is specified the default
 * LDAP connection returned by {@link org.jasig.portal.ldap.LdapServices} is
 * used. 
 * </p>
 *
 * @author Russell Tokuyama (University of Hawaii)
 * @version $Revision$
 */
public class SimpleLdapSecurityContext extends ChainingSecurityContext
    implements IConfigurableSecurityContext {
    
    private static final Log log = LogFactory.getLog(SimpleLdapSecurityContext.class);
    
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
  
  public static final String LDAP_PROPERTIES_CONNECTION_NAME = "connection";
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
    ILdapServer ldapConn;
    
    String propFile = ctxProperties.getProperty(LDAP_PROPERTIES_CONNECTION_NAME);
    if(propFile != null && propFile.length() > 0)
        ldapConn = LdapServices.getLdapServer(propFile);
    else
        ldapConn = LdapServices.getDefaultLdapServer();    
    
    String creds = new String(this.myOpaqueCredentials.credentialstring);
    if (this.myPrincipal.UID != null && !this.myPrincipal.UID.trim().equals("") && this.myOpaqueCredentials.credentialstring
        != null && !creds.trim().equals("")) {
      DirContext conn = null;
      NamingEnumeration results = null;
      StringBuffer user = new StringBuffer("(");
      String first_name = null;
      String last_name = null;
      
      user.append(ldapConn.getUidAttribute()).append("=");
      user.append(this.myPrincipal.UID).append(")");
      if (log.isDebugEnabled())
          log.debug(
                     "SimpleLdapSecurityContext: Looking for " +
                     user.toString());
      
      try {
          conn = ldapConn.getConnection();
          
          // set up search controls
          SearchControls searchCtls = new SearchControls();
          searchCtls.setReturningAttributes(attributes);
          searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
          
          // do lookup
          if (conn != null) {
              try {
                results = conn.search(ldapConn.getBaseDN(), user.toString(), searchCtls);
                if (results != null) {
                  if (!results.hasMore())
                    log.error(
                                   "SimpleLdapSecurityContext: user not found , " +
                                   this.myPrincipal.UID);
                  while (results != null && results.hasMore()) {
                    SearchResult entry = (SearchResult)results.next();
                    StringBuffer dnBuffer = new StringBuffer();
                    dnBuffer.append(entry.getName()).append(", ");
                    dnBuffer.append(ldapConn.getBaseDN());
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
    
                    String attrSearch = "(" + ldapConn.getUidAttribute() + "=*)";
                    log.debug(
                                   "SimpleLdapSecurityContext: Looking in " +
                                   dnBuffer.toString() + " for " + attrSearch);
                    conn.search(dnBuffer.toString(), attrSearch, searchCtls);
    
                    this.isauth = true;
                    this.myPrincipal.FullName = first_name + " " + last_name;
                    log.debug(
                                   "SimpleLdapSecurityContext: User " +
                                   this.myPrincipal.UID + " (" +
                                   this.myPrincipal.FullName + ") is authenticated");
    
                    // Since LDAP is case-insensitive with respect to uid, force
                    // user name to lower case for use by the portal
                    this.myPrincipal.UID = this.myPrincipal.UID.toLowerCase();
                  } // while (results != null && results.hasMore())
                }
                else {
                  log.error(
                                 "SimpleLdapSecurityContext: No such user: " +
                                 this.myPrincipal.UID);
                }
              } catch (AuthenticationException ae) {
                log.info("SimpleLdapSecurityContext: Password invalid for user: " + this.myPrincipal.UID);
              } catch (Exception e) {
                log.error(
                               "SimpleLdapSecurityContext: LDAP Error with user: " +
                               this.myPrincipal.UID + "; ", e);
                throw new PortalSecurityException("SimpleLdapSecurityContext: LDAP Error" + e + " with user: " + this.myPrincipal.UID);
              } finally {
                ldapConn.releaseConnection(conn);
              }
          }
          else {
            log.error("LDAP Server Connection unavalable");
          }
      }
      catch (final NamingException ne) {
          log.error("Error geting connection to LDAP server.", ne);
      }
    }
    else {
      log.error( "Principal or OpaqueCredentials not initialized prior to authenticate");
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
   * @param attrs LDAP query results
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



