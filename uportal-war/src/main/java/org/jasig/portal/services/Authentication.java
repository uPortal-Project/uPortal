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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.events.IPortalEventFactory;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IAdditionalDescriptor;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPrincipal;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.security.provider.ChainingSecurityContext;
import org.jasig.portal.utils.MovingAverage;
import org.jasig.portal.utils.MovingAverageSample;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
@Service
public class Authentication {
    private static final Log log = LogFactory.getLog(Authentication.class);

    private final static String BASE_CONTEXT_NAME = "root";

    // Metric counters
    private static final MovingAverage authenticationTimes = new MovingAverage();
    public static MovingAverageSample lastAuthentication = new MovingAverageSample();

    private IUserIdentityStore userIdentityStore;
    private IPortalEventFactory portalEventFactory;
    private IPersonAttributeDao personAttributeDao;

    @Autowired
    public void setPersonAttributeDao(@Qualifier("personAttributeDao") IPersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }

    @Autowired
    public void setUserIdentityStore(IUserIdentityStore userIdentityStore) {
        this.userIdentityStore = userIdentityStore;
    }

    @Autowired
    public void setPortalEventFactory(IPortalEventFactory portalEventFactory) {
        this.portalEventFactory = portalEventFactory;
    }

    /**
     * Attempts to authenticate a given IPerson based on a set of principals and credentials
     * @param principals
     * @param credentials
     * @param person
     * @exception PortalSecurityException
     */
    public void authenticate(HttpServletRequest request, Map<String, String> principals, Map<String, String> credentials, IPerson person) throws PortalSecurityException {

        // Retrieve the security context for the user
        final ISecurityContext securityContext = person.getSecurityContext();

        //Set the principals and credentials for the security context chain
        this.configureSecurityContextChain(principals, credentials, person, securityContext, BASE_CONTEXT_NAME);

        // NOTE: The LoginServlet looks in the security.properties file to
        // determine what tokens to look for that represent the principals and
        // credentials for each context. It then retrieves the values from the request
        // and stores the values in the principals and credentials HashMaps that are
        // passed to the Authentication service.

        // Attempt to authenticate the user
        final long start = System.currentTimeMillis();
        securityContext.authenticate();
        final long elapsed = System.currentTimeMillis() - start;
        // Check to see if the user was authenticated
        if (securityContext.isAuthenticated()) {
            lastAuthentication = authenticationTimes.add(elapsed); // metric
            // Add the authenticated username to the person object
            // the login name may have been provided or reset by the security provider
            // so this needs to be done after authentication.
            person.setAttribute(IPerson.USERNAME, securityContext.getPrincipal().getUID());
            // Retrieve the additional descriptor from the security context
            final IAdditionalDescriptor addInfo = person.getSecurityContext().getAdditionalDescriptor();
            // Process the additional descriptor if one was created
            if (addInfo != null) {
                // Replace the passed in IPerson with the additional descriptor if the
                // additional descriptor is an IPerson object created by the security context
                // NOTE: This is not the preferred method, creation of IPerson objects should be
                //       handled by the PersonManager.
                if (addInfo instanceof IPerson) {
                    final IPerson newPerson = (IPerson) addInfo;
                    person.setFullName(newPerson.getFullName());
                    for (final Enumeration<String> e = newPerson.getAttributeNames(); e.hasMoreElements();) {
                        final String attributeName = e.nextElement();
                        person.setAttribute(attributeName, newPerson.getAttribute(attributeName));
                    }
                    this.resetEntityIdentifier(person, newPerson);
                }
                // If the additional descriptor is a map then we can
                // simply copy all of these additional attributes into the IPerson
                else if (addInfo instanceof Map) {
                    // Cast the additional descriptor as a Map
                    final Map<?, ?> additionalAttributes = (Map<?, ?>) addInfo;
                    // Copy each additional attribute into the person object
                    for (final Iterator<?> keys = additionalAttributes.keySet().iterator(); keys.hasNext();) {
                        // Get a key
                        final String key = (String) keys.next();
                        // Set the attribute
                        person.setAttribute(key, additionalAttributes.get(key));
                    }
                }
                else if (addInfo instanceof ChainingSecurityContext.ChainingAdditionalDescriptor) {
                    // do nothing
                }
                else {
                    if (log.isWarnEnabled()) {
                        log.warn("Authentication Service recieved " + "unknown additional descriptor [" + addInfo + "]");
                    }
                }
            }
            // Populate the person object using the PersonDirectory if applicable
            if (PropertiesManager.getPropertyAsBoolean("org.jasig.portal.services.Authentication.usePersonDirectory")) {
                // Retrieve all of the attributes associated with the person logging in
                final String username = this.getUsername(person);
                final IPersonAttributes personAttributes = this.personAttributeDao.getPerson(username);

                if (personAttributes != null) {
                    // attribs may be null.  IPersonAttributeDao returns null when it does not recognize a user at all, as
                    // distinguished from returning an empty Map of attributes when it recognizes a user has having no
                    // attributes.

                    person.setAttributes(personAttributes.getAttributes());
                }

            }
            // Make sure the the user's fullname is set
            if (person.getFullName() == null) {
                // Use portal display name if one exists
                if (person.getAttribute("portalDisplayName") != null) {
                    person.setFullName((String) person.getAttribute("portalDisplayName"));
                }
                // If not try the eduPerson displyName
                else if (person.getAttribute("displayName") != null) {
                    person.setFullName((String) person.getAttribute("displayName"));
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
            final boolean autocreate = PropertiesManager
                    .getPropertyAsBoolean("org.jasig.portal.services.Authentication.autoCreateUsers");
            // If we are going to be auto creating accounts then we must find the default template to use
            if (autocreate && person.getAttribute("uPortalTemplateUserName") == null) {
                final String defaultTemplateUserName = PropertiesManager
                        .getProperty("org.jasig.portal.services.Authentication.defaultTemplateUserName");
                person.setAttribute("uPortalTemplateUserName", defaultTemplateUserName);
            }
            try {
                // Attempt to retrieve the UID
                final int newUID = this.userIdentityStore.getPortalUID(person, autocreate);
                person.setID(newUID);
            }
            catch (final AuthorizationException ae) {
                log.error("Exception retrieving ID", ae);
                throw new PortalSecurityException("Authentication Service: Exception retrieving UID");
            }
        }
        
        this.portalEventFactory.publishLoginEvent(request, this, person);
    }

    /**
     * Return the username to be used for authorization (exit hook)
     * @param person
     * @return usernmae
     */
    protected String getUsername(final IPerson person) {
        return (String) person.getAttribute(IPerson.USERNAME);
    }

    /**
     * Reset the entity identifier in the final person object (exit hook)
     * @param person
     * @param newPerson
     */
    protected void resetEntityIdentifier(final IPerson person, final IPerson newPerson) {
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
    public void setContextParameters(Map<String, String> principals, Map<String, String> credentials, String ctxName,
            ISecurityContext securityContext, IPerson person) {

        if (log.isDebugEnabled()) {
            final StringBuilder msg = new StringBuilder();
            msg.append("Preparing to authenticate;  setting parameters for context name '").append(ctxName)
                    .append("', context class '").append(securityContext.getClass().getName()).append("'");
            // Display principalTokens...
            msg.append("\n\t Available Principal Tokens");
            for (final Object o : principals.entrySet()) {
                final Map.Entry<?, ?> y = (Map.Entry<?, ?>) o;
                msg.append("\n\t\t").append(y.getKey()).append("=").append(y.getValue());
            }
            // Keep credentialTokens secret, but indicate whether they were provided...
            msg.append("\n\t Available Credential Tokens");
            for (final Object o : credentials.entrySet()) {
                final Map.Entry<?, ?> y = (Map.Entry<?, ?>) o;
                final String val = (String) y.getValue();
                String valWasSpecified = null;
                if (val != null) {
                    valWasSpecified = val.trim().length() == 0 ? "empty" : "provided";
                }
                msg.append("\n\t\t").append(y.getKey()).append(" was ").append(valWasSpecified);
            }
            log.debug(msg.toString());
        }

        String username = principals.get(ctxName);
        String credential = credentials.get(ctxName);
        // If username or credential are null, this indicates that the token was not
        // set in security properties. We will then use the value for root.
        username = username != null ? username : (String) principals.get(BASE_CONTEXT_NAME);
        credential = credential != null ? credential : (String) credentials.get(BASE_CONTEXT_NAME);
        if (log.isDebugEnabled()) {
            log.debug("Authentication::setContextParameters() username: " + username);
        }
        // Retrieve and populate an instance of the principal object
        final IPrincipal principalInstance = securityContext.getPrincipalInstance();
        if (username != null && !username.equals("")) {
            principalInstance.setUID(username);
        }
        // Retrieve and populate an instance of the credentials object
        final IOpaqueCredentials credentialsInstance = securityContext.getOpaqueCredentialsInstance();
        if (credentialsInstance != null) {
            credentialsInstance.setCredentials(credential);
        }
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
    private void configureSecurityContextChain(final Map<String, String> principals,
            final Map<String, String> credentials, final IPerson person, final ISecurityContext securityContext,
            final String baseContextName) throws PortalSecurityException {
        this.setContextParameters(principals, credentials, baseContextName, securityContext, person);

        // load principals and credentials for the subContexts
        for (final Enumeration<String> subCtxNames = securityContext.getSubContextNames(); subCtxNames
                .hasMoreElements();) {
            final String fullSubCtxName = subCtxNames.nextElement();

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
