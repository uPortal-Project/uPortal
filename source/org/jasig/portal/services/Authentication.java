/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.services;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.UserIdentityStoreFactory;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IAdditionalDescriptor;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPrincipal;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.security.provider.ChainingSecurityContext;
import org.jasig.portal.services.persondir.support.IPersonAttributeDao;

/**
 * Attempts to authenticate a user and retrieve attributes
 * associated with the user.
 * @author Ken Weiner, kweiner@unicon.net
 * @author Don Fracapane (df7@columbia.edu)
 * Added properties in the security properties file that hold the tokens used to
 * represent the principal and credential for each security context. This version
 * differs in the way the principal and credentials are set (all contexts are set
 * up front after evaluating the tokens). See setContextParameters() also.
 * @version $Revision$
 * Changes put in to allow credentials and principals to be defined and held by each
 * context.
 */
public class Authentication {
    
    private static final Log log = LogFactory.getLog(Authentication.class);
    
   private final static String BASE_CONTEXT_NAME = "root";
   
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
      
      //Set the principals and credentials for the security context chain
      this.configureSecurityContextChain(principals, credentials, person, securityContext, BASE_CONTEXT_NAME);
      
      // NOTE: The LoginServlet looks in the security.properties file to
      // determine what tokens to look for that represent the principals and
      // credentials for each context. It then retrieves the values from the request
      // and stores the values in the principals and credentials HashMaps that are
      // passed to the Authentication service.

      // Attempt to authenticate the user
      securityContext.authenticate();
      // Check to see if the user was authenticated
      if (securityContext.isAuthenticated()) {
         // Add the authenticated username to the person object
         // the login name may have been provided or reset by the security provider
         // so this needs to be done after authentication.
         person.setAttribute(IPerson.USERNAME, securityContext.getPrincipal().getUID());
         // Retrieve the additional descriptor from the security context
         IAdditionalDescriptor addInfo = person.getSecurityContext().getAdditionalDescriptor();
         // Process the additional descriptor if one was created
         if (addInfo != null) {
            // Replace the passed in IPerson with the additional descriptor if the
            // additional descriptor is an IPerson object created by the security context
            // NOTE: This is not the preferred method, creation of IPerson objects should be
            //       handled by the PersonManager.
            if (addInfo instanceof IPerson) {
               IPerson newPerson = (IPerson)addInfo;
               person.setFullName(newPerson.getFullName());
               for (Enumeration e = newPerson.getAttributeNames(); e.hasMoreElements();) {
                  String attributeName = (String)e.nextElement();
                  person.setAttribute(attributeName, newPerson.getAttribute(attributeName));
               }
            }
            // If the additional descriptor is a map then we can
            // simply copy all of these additional attributes into the IPerson
            else if (addInfo instanceof Map) {
               // Cast the additional descriptor as a Map
               Map additionalAttributes = (Map)addInfo;
               // Copy each additional attribute into the person object
               for (Iterator keys = additionalAttributes.keySet().iterator(); keys.hasNext();) {
                  // Get a key
                  String key = (String)keys.next();
                  // Set the attribute
                  person.setAttribute(key, additionalAttributes.get(key));
               }
            }
            else if (addInfo instanceof ChainingSecurityContext.ChainingAdditionalDescriptor) {
                // do nothing
            }
            else {
                if (log.isWarnEnabled())
                    log.warn("Authentication Service recieved " +
                            "unknown additional descriptor [" + addInfo + "]");
            }
         }
         // Populate the person object using the PersonDirectory if applicable
         if (PropertiesManager.getPropertyAsBoolean("org.jasig.portal.services.Authentication.usePersonDirectory")) {
            // Retrieve all of the attributes associated with the person logging in
            IPersonAttributeDao pa = PersonDirectory.getPersonAttributeDao();
            Map attribs = pa.getUserAttributes((String)person.getAttribute(IPerson.USERNAME));
            // Add each of the attributes to the IPerson
            Iterator en = attribs.keySet().iterator();
            while (en.hasNext()) {
               String key = (String)en.next();
               // String value = (String)attribs.get(key);
               // person.setAttribute(key, value);
               person.setAttribute(key, attribs.get(key));
            }
         }
         // Make sure the the user's fullname is set
         if (person.getFullName() == null) {
            // Use portal display name if one exists
            if (person.getAttribute("portalDisplayName") != null) {
               person.setFullName((String)person.getAttribute("portalDisplayName"));
            }
            // If not try the eduPerson displyName
            else if (person.getAttribute("displayName") != null) {
               person.setFullName((String)person.getAttribute("displayName"));
            }
            // If still no FullName use an unrecognized string
            if (person.getFullName() == null) {
               person.setFullName("Unrecognized person: " + person.getAttribute(IPerson.USERNAME));
            }
         }
         // Find the uPortal userid for this user or flunk authentication if not found
         // The template username should actually be derived from directory information.
         // The reference implemenatation sets the uPortalTemplateUserName to the default in
         // the portal.properties file.
         // A more likely template would be staff or faculty or undergraduate.
         boolean autocreate = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.services.Authentication.autoCreateUsers");
         // If we are going to be auto creating accounts then we must find the default template to use
         if (autocreate && person.getAttribute("uPortalTemplateUserName") == null) {
            String defaultTemplateUserName = PropertiesManager.getProperty("org.jasig.portal.services.Authentication.defaultTemplateUserName");
            person.setAttribute("uPortalTemplateUserName", defaultTemplateUserName);
         }
         try {
            // Attempt to retrieve the UID
            int newUID = UserIdentityStoreFactory.getUserIdentityStoreImpl().getPortalUID(person,
                  autocreate);
            person.setID(newUID);
         } catch (AuthorizationException ae) {
            log.error("Exception retrieving ID", ae);
            throw  new PortalSecurityException("Authentication Service: Exception retrieving UID");
         }
         
         //TODO add IPerson cache
         
         // Record the successful authentication
         StatsRecorder.recordLogin(person);
      }
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

   /**
    * Get the principal and credential for a specific context and store them in
    * the context.
    * @param principals
    * @param credentials
    * @param ctxName
    * @param securityContext
    * @param person
    */
   public void setContextParameters (HashMap principals, HashMap credentials, String ctxName,
         ISecurityContext securityContext, IPerson person) {
      String username = (String)principals.get(ctxName);
      String credential = (String)credentials.get(ctxName);
      // If username or credential are null, this indicates that the token was not
      // set in security properties. We will then use the value for root.
      username = (username != null ? username : (String)principals.get(BASE_CONTEXT_NAME));
      credential = (credential != null ? credential : (String)credentials.get(BASE_CONTEXT_NAME));
      if (log.isDebugEnabled())
          log.debug("Authentication::setContextParameters() username: " + username);
      // Retrieve and populate an instance of the principal object
      IPrincipal principalInstance = securityContext.getPrincipalInstance();
      if (username != null && !username.equals("")) {
         principalInstance.setUID(username);
      }
      // Retrieve and populate an instance of the credentials object
      IOpaqueCredentials credentialsInstance = securityContext.getOpaqueCredentialsInstance();
      credentialsInstance.setCredentials(credential);
   }
   
   /**
    * Recureses through the {@link ISecurityContext} chain, setting the credentials
    * for each.
    * TODO This functionality should be moved into the {@link org.jasig.portal.security.provider.ChainingSecurityContext}.
    * 
    * @param principals
    * @param credentials
    * @param person
    * @param securityContext
    * @param baseContextName
    * @throws PortalSecurityException
    */
   private void configureSecurityContextChain(final HashMap principals, final HashMap credentials, final IPerson person, final ISecurityContext securityContext, final String baseContextName) throws PortalSecurityException {
       this.setContextParameters (principals, credentials, baseContextName, securityContext, person);
       
       // load principals and credentials for the subContexts
       for (final Enumeration subCtxNames = securityContext.getSubContextNames(); subCtxNames.hasMoreElements(); ) {
           final String fullSubCtxName = (String)subCtxNames.nextElement();
           
           //Strip off the base of the name
           String localSubCtxName = fullSubCtxName;
           if (fullSubCtxName.startsWith(baseContextName + ".")) {
               localSubCtxName = localSubCtxName.substring(baseContextName.length() + 1);
           }
          
           final ISecurityContext sc = securityContext.getSubContext(localSubCtxName);
           
           this.configureSecurityContextChain(principals, credentials, person, sc, fullSubCtxName);
       }
   }   
}
