/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.ldap;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;


/**
 * The <code>ILdapServer</code> interface defines a set of methods
 * to be used to create a connection to an LDAP server, release the
 * connection and get information about the connection.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
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
    public void releaseConnection (DirContext conn);
    
}
