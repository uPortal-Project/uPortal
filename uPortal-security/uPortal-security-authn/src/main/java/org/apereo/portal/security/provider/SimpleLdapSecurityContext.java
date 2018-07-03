/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.security.provider;

import javax.naming.AuthenticationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apereo.portal.ldap.ILdapServer;
import org.apereo.portal.ldap.LdapServices;
import org.apereo.portal.security.PortalSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of a SecurityContext that checks a user's credentials against an LDAP
 * directory. It expects to be able to bind to the LDAP directory as the user so that it can
 * authenticate the user.
 *
 * <p>The default LDAP connection returned by {@link org.apereo.portal.ldap.LdapServices} is used.
 */
public class SimpleLdapSecurityContext extends ChainingSecurityContext {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // Attributes that we're interested in.
    public static final int ATTR_UID = 0;
    public static final int ATTR_FIRSTNAME = ATTR_UID + 1;
    public static final int ATTR_LASTNAME = ATTR_FIRSTNAME + 1;
    private final int SIMPLE_LDAP_SECURITYAUTHTYPE = 0xFF04;
    private static final String[] attributes = {
        "uid", // user ID
        "givenName", // first name
        "sn" // last name
    };

    public static final String LDAP_PROPERTIES_CONNECTION_NAME = "connection";

    /* package-private */ SimpleLdapSecurityContext() {}

    /**
     * Returns the type of authentication this class provides.
     *
     * @return authorization type
     */
    @Override
    public int getAuthType() {
        /*
         * What is this for?  No one would know what to do with the
         * value returned.  Subclasses might know but our getAuthType()
         * doesn't return anything easily useful.
         */
        return this.SIMPLE_LDAP_SECURITYAUTHTYPE;
    }

    /** Authenticates the user. */
    @Override
    public synchronized void authenticate() throws PortalSecurityException {
        this.isauth = false;
        ILdapServer ldapConn;

        ldapConn = LdapServices.getDefaultLdapServer();

        String creds = new String(this.myOpaqueCredentials.credentialstring);
        if (this.myPrincipal.UID != null
                && !this.myPrincipal.UID.trim().equals("")
                && this.myOpaqueCredentials.credentialstring != null
                && !creds.trim().equals("")) {
            DirContext conn = null;
            NamingEnumeration results = null;
            StringBuffer user = new StringBuffer("(");
            String first_name = null;
            String last_name = null;

            user.append(ldapConn.getUidAttribute()).append("=");
            user.append(this.myPrincipal.UID).append(")");
            log.debug("SimpleLdapSecurityContext: Looking for {}", user.toString());

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
                            if (!results.hasMore()) {
                                log.error(
                                        "SimpleLdapSecurityContext: user not found: {}",
                                        this.myPrincipal.UID);
                            }
                            while (results != null && results.hasMore()) {
                                SearchResult entry = (SearchResult) results.next();
                                StringBuffer dnBuffer = new StringBuffer();
                                dnBuffer.append(entry.getName()).append(", ");
                                dnBuffer.append(ldapConn.getBaseDN());
                                Attributes attrs = entry.getAttributes();
                                first_name = getAttributeValue(attrs, ATTR_FIRSTNAME);
                                last_name = getAttributeValue(attrs, ATTR_LASTNAME);
                                // re-bind as user
                                conn.removeFromEnvironment(javax.naming.Context.SECURITY_PRINCIPAL);
                                conn.removeFromEnvironment(
                                        javax.naming.Context.SECURITY_CREDENTIALS);
                                conn.addToEnvironment(
                                        javax.naming.Context.SECURITY_PRINCIPAL,
                                        dnBuffer.toString());
                                conn.addToEnvironment(
                                        javax.naming.Context.SECURITY_CREDENTIALS,
                                        this.myOpaqueCredentials.credentialstring);
                                searchCtls = new SearchControls();
                                searchCtls.setReturningAttributes(new String[0]);
                                searchCtls.setSearchScope(SearchControls.OBJECT_SCOPE);

                                String attrSearch = "(" + ldapConn.getUidAttribute() + "=*)";
                                log.debug(
                                        "SimpleLdapSecurityContext: Looking in {} for {}",
                                        dnBuffer.toString(),
                                        attrSearch);
                                conn.search(dnBuffer.toString(), attrSearch, searchCtls);

                                this.isauth = true;
                                this.myPrincipal.FullName = first_name + " " + last_name;
                                log.debug(
                                        "SimpleLdapSecurityContext: User {} ({}) is authenticated",
                                        this.myPrincipal.UID,
                                        this.myPrincipal.FullName);

                                // Since LDAP is case-insensitive with respect to uid, force
                                // user name to lower case for use by the portal
                                this.myPrincipal.UID = this.myPrincipal.UID.toLowerCase();
                            } // while (results != null && results.hasMore())
                        } else {
                            log.error(
                                    "SimpleLdapSecurityContext: No such user: {}",
                                    this.myPrincipal.UID);
                        }
                    } catch (AuthenticationException ae) {
                        log.info(
                                "SimpleLdapSecurityContext: Password invalid for user: "
                                        + this.myPrincipal.UID);
                    } catch (Exception e) {
                        log.error(
                                "SimpleLdapSecurityContext: LDAP Error with user: "
                                        + this.myPrincipal.UID
                                        + "; ",
                                e);
                        throw new PortalSecurityException(
                                "SimpleLdapSecurityContext: LDAP Error"
                                        + e
                                        + " with user: "
                                        + this.myPrincipal.UID);
                    } finally {
                        ldapConn.releaseConnection(conn);
                    }
                } else {
                    log.error("LDAP Server Connection unavailable");
                }
            } catch (final NamingException ne) {
                log.error("Error getting connection to LDAP server.", ne);
            }
        } else {
            // If the principal and/or credential are missing, the context authentication
            // simply fails. It should not be construed that this is an error. It happens for guest
            // access.
            log.info("Principal or OpaqueCredentials not initialized prior to authenticate");
        }
        // Ok...we are now ready to authenticate all of our subcontexts.
        super.authenticate();
        return;
    }

    /*--------------------- Helper methods ---------------------*/

    /**
     * Return a single value of an attribute from possibly multiple values, grossly ignoring
     * anything else. If there are no values, then return an empty string.
     *
     * @param attrs LDAP query results
     * @param attribute LDAP attribute we are interested in
     * @return a single value of the attribute
     */
    private String getAttributeValue(Attributes attrs, int attribute) throws NamingException {
        NamingEnumeration values = null;
        String aValue = "";
        if (!isAttribute(attribute)) return aValue;
        Attribute attrib = attrs.get(attributes[attribute]);
        if (attrib != null) {
            for (values = attrib.getAll(); values.hasMoreElements(); ) {
                aValue = (String) values.nextElement();
                break; // take only the first attribute value
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
}
