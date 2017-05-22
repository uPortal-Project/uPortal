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
package org.apereo.portal.ldap;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * The <code>ILdapServer</code> interface defines a set of methods to be used to create a connection
 * to an LDAP server, release the connection and get information about the connection.
 *
 * @deprecated Framework code should access {@link LdapContextSource} objects in the spring context
 *     via injection instead of using these APIs.
 */
public interface ILdapServer {

    /**
     * Gets an LDAP directory context.
     *
     * @return an LDAP directory context object.
     * @throws NamingException If there is a problem connecting to the ldap server.
     */
    public DirContext getConnection() throws NamingException;

    /**
     * Gets the base DN used to search the LDAP directory context.
     *
     * @return a DN to use as reference point or context for queries
     */
    public String getBaseDN();

    /**
     * Gets the uid attribute used to search the LDAP directory context.
     *
     * @return a DN to use as reference point or context for queries
     */
    public String getUidAttribute();

    /**
     * Releases an LDAP directory context.
     *
     * @param conn an LDAP directory context object
     */
    public void releaseConnection(DirContext conn);
}
