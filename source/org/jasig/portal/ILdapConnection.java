/**
 * Created on Sep 14, 2004
 */
package org.jasig.portal;

import javax.naming.directory.DirContext;


/**
 * The <code>ILdapConnection</code> interface defines a set of methods
 * to be used to create a connection to an LDAP server, release the
 * connection and get information about the connection.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision $
 */
public interface ILdapConnection {

    /**
     * Gets an LDAP directory context.
     * 
     * @return an LDAP directory context object.
     */
    public DirContext getConnection();
    
    /**
     * Gets the base DN used to search the LDAP directory context.\
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
