/**
 * Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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