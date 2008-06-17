/* Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.ldap;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * An ILdapServer impl that wraps a Spring-LDAP ContextSource for getting contections to provide
 * legacy ILdapServer support.
 * 
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision: 1.1 $
 * @deprecated see {@link ILdapServer} deprecation comment
 */
public class ContextSourceLdapServerImpl implements ILdapServer {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private LdapContextSource ldapContextSource;
    private String uidAttribute;
    private String baseDN;
    
    /**
     * @return the contextSource
     */
    public LdapContextSource getLdapContextSource() {
        return this.ldapContextSource;
    }
    /**
     * @param ldapContextSource the ldapContextSource to set
     */
    public void setLdapContextSource(LdapContextSource ldapContextSource) {
        this.ldapContextSource = ldapContextSource;
    }
    /**
     * @param uidAttribute the uidAttribute to set
     */
    public void setUidAttribute(String uidAttribute) {
        this.uidAttribute = uidAttribute;
    }

    
    /**
     * @see org.jasig.portal.ldap.ILdapServer#getBaseDN()
     */
    public String getBaseDN() {
        return this.baseDN;
    }
    
    /**
     * @see org.jasig.portal.ldap.ILdapServer#setBaseDN()
     */
    public void setBaseDN(String baseDN) {
    	this.baseDN = baseDN;
    }

    /**
     * @see org.jasig.portal.ldap.ILdapServer#getConnection()
     */
    public DirContext getConnection() throws NamingException {
        return this.ldapContextSource.getReadOnlyContext();
    }

    /**
     * @see org.jasig.portal.ldap.ILdapServer#getUidAttribute()
     */
    public String getUidAttribute() {
        return this.uidAttribute;
    }

    /**
     * @see org.jasig.portal.ldap.ILdapServer#releaseConnection(javax.naming.directory.DirContext)
     */
    public void releaseConnection(DirContext conn) {
        try {
            conn.close();
        }
        catch (NamingException ne) {
            this.logger.warn("An exception occured while closing DirContext='" + conn + "'", ne);
        }
    }
}
