package org.jasig.portal.services;

import  java.util.Vector;
import  java.io.File;
import  java.io.FileInputStream;
import  java.io.IOException;
import  java.util.Properties;
import  org.jasig.portal.PortalSessionManager;
import  org.jasig.portal.security.*;
import  org.jasig.portal.AuthorizationException;

/**
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class AuthorizationService 
{
  private static AuthorizationService m_instance;  
  protected IAuthorizationService m_authorization = null;
  protected static String s_factoryName = null;
  protected static IAuthorizationServiceFactory m_Factory = null;
  static {
    // Get the security properties file
    java.io.InputStream secprops = AuthorizationService.class.getResourceAsStream("/properties/security.properties");
    // Get the properties from the security properties file
    Properties pr = new Properties();
    try {
      pr.load(secprops);
      // Look for our authorization factory and instantiate an instance of it or die trying.
      if ((s_factoryName = pr.getProperty("authorizationProvider")) == null) {
        LogService.instance().log(LogService.ERROR, new PortalSecurityException("AuthorizationProvider not specified or incorrect in security.properties"));
      }
      else {
        try {
          m_Factory = (IAuthorizationServiceFactory)Class.forName(s_factoryName).newInstance();
        } catch (Exception e) {
          LogService.instance().log(LogService.ERROR, new PortalSecurityException("Failed to instantiate " + s_factoryName));
        }
      }
    } catch (IOException e) {
      LogService.instance().log(LogService.ERROR, new PortalSecurityException(e.getMessage()));
    }
  }

/**
 *
 */
public AuthorizationService () throws AuthorizationException
{
    // From our factory get an actual authorization instance
    m_authorization = m_Factory.getAuthorization();
}
/**
 * @return Authorization
 */
public final static synchronized AuthorizationService instance() throws AuthorizationException 
{
	if ( m_instance == null )
		{ m_instance = new AuthorizationService(); }
	return m_instance;
}
/**
 * @return org.jasig.portal.security.IPermissionManager
 * @param owner java.lang.String
 * @exception org.jasig.portal.AuthorizationException
 */
public IPermissionManager newPermissionManager(String owner) 
       throws AuthorizationException 
{
     return m_authorization.newPermissionManager(owner);
}
/**
 * @return org.jasig.portal.security.IAuthorizationPrincipal
 * @param key java.lang.String
 * @param type java.lang.Class
 * @exception org.jasig.portal.AuthorizationException
 */
public IAuthorizationPrincipal newPrincipal(String key, Class type) 
       throws AuthorizationException 
{
     return m_authorization.newPrincipal(key, type);
}
/**
 * @return org.jasig.portal.security.IUpdatingPermissionManager
 * @param owner java.lang.String
 * @exception org.jasig.portal.AuthorizationException
 */
public IUpdatingPermissionManager newUpdatingPermissionManager(String owner) 
       throws AuthorizationException 
{
     return m_authorization.newUpdatingPermissionManager(owner);
}
}
