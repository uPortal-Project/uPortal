/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import javax.naming.directory.DirContext;

/**
 * Legacy placeholder class. As of uPortal 2.4 all work should now be done with
 * {@link org.jasig.portal.ldap.LdapServices}.
 * 
 * @see org.jasig.portal.ldap.LdapServices
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 * @deprecated As of uPortal 2.4, use {@link org.jasig.portal.ldap.LdapServices}.
 */
public class LdapServices {
    /**
     * Creates a new <code>LdapServices</code> object. Simply wraps
     * an the {@link org.jasig.portal.ldap.ILdapServer} returned by {@link org.jasig.portal.ldap.LdapServices#getDefaultLdapServer()}.
     * <code>LdapServices</code> objects should not be created. No reference
     * to the {@link org.jasig.portal.ldap.ILdapServer} is held in
     * an instance of <code>LdapServices</code>
     * 
     * @deprecated As of uPortal 2.4, use {@link org.jasig.portal.ldap.LdapServices} instead.
     */
    public LdapServices() {
    }

    /**
     * Simply wraps an the {@link org.jasig.portal.ldap.ILdapServer} returned by
     * {@link org.jasig.portal.ldap.LdapServices#getDefaultLdapServer()}.
     * 
     * @see org.jasig.portal.ldap.ILdapServer#getConnection()
     * @deprecated As of uPortal 2.4, use {@link org.jasig.portal.ldap.LdapServices#getDefaultLdapServer()} and {@link org.jasig.portal.ldap.ILdapServer#getConnection()}.
     */
    public DirContext getConnection() {
        return org.jasig.portal.ldap.LdapServices.getDefaultLdapServer().getConnection();
    }

    /**
     * Simply wraps an the {@link org.jasig.portal.ldap.ILdapServer} returned by
     * {@link org.jasig.portal.ldap.LdapServices#getDefaultLdapServer()}.
     * 
     * @see org.jasig.portal.ldap.ILdapServer#getBaseDN()
     * @deprecated As of uPortal 2.4, use {@link org.jasig.portal.ldap.LdapServices#getDefaultLdapServer()} and {@link org.jasig.portal.ldap.ILdapServer#getBaseDN()}. 
     */
    public String getBaseDN() {
      return org.jasig.portal.ldap.LdapServices.getDefaultLdapServer().getBaseDN();
    }

    /**
     * Simply wraps an the {@link org.jasig.portal.ldap.ILdapServer} returned by
     * {@link org.jasig.portal.ldap.LdapServices#getDefaultLdapServer()}.
     * 
     * @see org.jasig.portal.ldap.ILdapServer#getUidAttribute()
     * @deprecated As of uPortal 2.4, use {@link org.jasig.portal.ldap.LdapServices#getDefaultLdapServer()} and {@link org.jasig.portal.ldap.ILdapServer#getUidAttribute()}.
     */
    public String getUidAttribute() {
      return org.jasig.portal.ldap.LdapServices.getDefaultLdapServer().getUidAttribute();
    }

    /**
     * Simply wraps an the {@link org.jasig.portal.ldap.ILdapServer} returned by
     * {@link org.jasig.portal.ldap.LdapServices#getDefaultLdapServer()}.
     * 
     * @see org.jasig.portal.ldap.ILdapServer#releaseConnection(DirContext)
     * @deprecated As of uPortal 2.4, use {@link org.jasig.portal.ldap.LdapServices#getDefaultLdapServer()} and {@link org.jasig.portal.ldap.ILdapServer#releaseConnection(DirContext)}.
     */
    public void releaseConnection (DirContext conn) {
        org.jasig.portal.ldap.LdapServices.getDefaultLdapServer().releaseConnection(conn);
    }
}