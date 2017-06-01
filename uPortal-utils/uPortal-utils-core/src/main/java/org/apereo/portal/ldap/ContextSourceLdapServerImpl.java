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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.ContextSource;

/**
 * An ILdapServer impl that wraps a Spring-LDAP ContextSource for getting contections to provide
 * legacy ILdapServer support.
 *
 * @deprecated see {@link ILdapServer} deprecation comment
 */
public class ContextSourceLdapServerImpl implements ILdapServer {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private ContextSource contextSource;
    private String uidAttribute;
    private String baseDN;

    /** @return the contextSource */
    public ContextSource getContextSource() {
        return this.contextSource;
    }
    /** @param ldapContextSource the ldapContextSource to set */
    public void setContextSource(ContextSource ldapContextSource) {
        this.contextSource = ldapContextSource;
    }
    /** @param uidAttribute the uidAttribute to set */
    public void setUidAttribute(String uidAttribute) {
        this.uidAttribute = uidAttribute;
    }

    /** @see org.apereo.portal.ldap.ILdapServer#getBaseDN() */
    public String getBaseDN() {
        return this.baseDN;
    }

    /** @see org.apereo.portal.ldap.ILdapServer#setBaseDN() */
    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    /** @see org.apereo.portal.ldap.ILdapServer#getConnection() */
    public DirContext getConnection() throws NamingException {
        return this.contextSource.getReadOnlyContext();
    }

    /** @see org.apereo.portal.ldap.ILdapServer#getUidAttribute() */
    public String getUidAttribute() {
        return this.uidAttribute;
    }

    /**
     * @see org.apereo.portal.ldap.ILdapServer#releaseConnection(javax.naming.directory.DirContext)
     */
    public void releaseConnection(DirContext conn) {
        try {
            conn.close();
        } catch (NamingException ne) {
            this.logger.warn("An exception occured while closing DirContext='" + conn + "'", ne);
        }
    }
}
