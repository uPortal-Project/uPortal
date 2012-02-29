/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.services;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IAuthorizationServiceFactory;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IUpdatingPermissionManager;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;

/**
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class AuthorizationService
{
    
    private static final Log log = LogFactory.getLog(AuthorizationService.class);
    
    private final static SingletonDoubleCheckedCreator<AuthorizationService> authorizationServiceInstance = new SingletonDoubleCheckedCreator<AuthorizationService>() {
        @Override
        protected AuthorizationService createSingleton(Object... args) {
            return new AuthorizationService();
        }
    };
    
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
		secprops.close();
      // Look for our authorization factory and instantiate an instance of it or die trying.
      if ((s_factoryName = pr.getProperty("authorizationProvider")) == null) {
        log.error("AuthorizationProvider not specified or incorrect in security.properties", new PortalSecurityException("AuthorizationProvider not specified or incorrect in security.properties"));
      }
      else {
        try {
          m_Factory = (IAuthorizationServiceFactory)Class.forName(s_factoryName).newInstance();
        } catch (Exception e) {
          log.error("Failed to instantiate AuthorizationProvider " + s_factoryName,  new PortalSecurityException("Failed to instantiate AuthorizationProvider " + s_factoryName));
        }
        
        if (m_Factory == null) {
            log.error("AuthorizationProvider not specified or incorrect in security.properties", new PortalSecurityException("AuthorizationProvider not specified or incorrect in security.properties"));
        }
      }
    } catch (IOException e) {
      log.error("Error loading security properties", e);
    } finally {
			try {
				if (secprops != null)
					secprops.close();
			} catch (IOException ioe) {
				log.error("Error closing security properties file.", ioe);
			}
		}
  }

  /**
   *  
   */
  private AuthorizationService () throws AuthorizationException
  {
      // From our factory get an actual authorization instance
      m_authorization = m_Factory.getAuthorization();
  }
  
 /**
   * @return org.jasig.portal.groups.IGroupMember
   * @param principal IAuthorizationPrincipal
   * @exception org.jasig.portal.groups.GroupsException
   */
  public IGroupMember getGroupMember(IAuthorizationPrincipal principal)
         throws GroupsException
   {
       return m_authorization.getGroupMember(principal);
   }
   
  /**
   * @return Authorization
   */
  public final static AuthorizationService instance() throws AuthorizationException
  {
      return authorizationServiceInstance.get();
  }

  /**
   * @param owner java.lang.String
   * @return org.jasig.portal.security.IPermissionManager
   * @exception org.jasig.portal.AuthorizationException
   */
  public IPermissionManager newPermissionManager(String owner)
         throws AuthorizationException
  {
       return m_authorization.newPermissionManager(owner);
  }

  /**
   * @param key java.lang.String
   * @param type java.lang.Class
   * @return org.jasig.portal.security.IAuthorizationPrincipal
   * @exception org.jasig.portal.AuthorizationException
   */
  public IAuthorizationPrincipal newPrincipal(String key, Class type)
         throws AuthorizationException
  {
       return m_authorization.newPrincipal(key, type);
  }
  
  /**
   * @param groupMember
   * @return org.jasig.portal.security.IAuthorizationPrincipal
   * @exception org.jasig.portal.groups.GroupsException
   */
   public IAuthorizationPrincipal newPrincipal(IGroupMember groupMember)
          throws GroupsException
   {
       return m_authorization.newPrincipal(groupMember);
   }
   
  /**
   * @param permission
   * @return org.jasig.portal.security.IAuthorizationPrincipal
   * @exception org.jasig.portal.AuthorizationException
   */
  public IAuthorizationPrincipal newPrincipal(IPermission permission)
         throws AuthorizationException
  {
       return m_authorization.getPrincipal(permission);
  }

  /**
   * @param owner java.lang.String
   * @return org.jasig.portal.security.IUpdatingPermissionManager
   * @exception org.jasig.portal.AuthorizationException
   */
  public IUpdatingPermissionManager newUpdatingPermissionManager(String owner)
         throws AuthorizationException
  {
       return m_authorization.newUpdatingPermissionManager(owner);
  }
}
