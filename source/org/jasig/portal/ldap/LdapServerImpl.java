/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation of ILdapServer.
 * @author edalquist@unicon.net
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class LdapServerImpl implements ILdapServer {
    
    private Log log = LogFactory.getLog(getClass());
    
    /**
     * Name of the class that is the default context factory which will be used
     * if not overridden.
     */
    private static final String DEFAULT_CXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    
    /**
     * Name of this LDAP server for identification and debugging purposes.
     */
    private final String ldapName;
    
    
    private final String ldapBaseDn;
    
    /**
     * The attribute by which we query using uids.
     */
    private final String ldapUidAttribute;
   
    /**
     * Environment, populated at construction.
     */
    private Hashtable env = new Hashtable(5, 0.75f);
    
    
    /**
     * Instantiate an LdapServerImpl using somewhat abtstracted configuration
     * over that present in the more detailed constructor.
     * You communicate a desire to use SSL to this method by using an
     * ldaps//... URL rather than an ldap//... url.
     * @param name mnemonic name for this Ldap server instance
     * @param url URL of LDAP server
     * @param baseDn 
     * @param uidAttribute attribute against which to match for uid queries
     * @param managerDn principal for LDAP authentication, null implies no
     * authentication
     * @param managerPw password for LDAP authentication, null implies no
     * authentication
     * @param initialContextFactory the name of the class to use to instantiate
     * the context.
     */
    public LdapServerImpl(String name, String url, String baseDn, 
            String uidAttribute, String managerDn, String managerPw, 
            String initialContextFactory) {
       
        if (name == null)
            throw new IllegalArgumentException("name cannot be null.");
        
        this.ldapName = name;
        
        this.ldapBaseDn = checkNull(baseDn, "");
        this.ldapUidAttribute = checkNull(uidAttribute, "");
        
        managerDn = checkNull(managerDn, "");
        managerPw = checkNull(managerPw, "");
        initialContextFactory = checkNull(initialContextFactory, DEFAULT_CXT_FACTORY);
        
        // parse the URL parameter to detect SSL -- in case of SSL
        // fix the URL and set the useSsl flag.
        
        if (url.startsWith("ldaps")) { // Handle SSL connections
            // remove the 's' from "ldaps"
            url = "ldap" + url.substring(5);
            this.env.put(Context.SECURITY_PROTOCOL,"ssl");
          }
        
        this.env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);

        this.env.put(Context.PROVIDER_URL, url);
        this.env.put(Context.SECURITY_AUTHENTICATION, "simple");
        this.env.put(Context.SECURITY_PRINCIPAL, managerDn);
        this.env.put(Context.SECURITY_CREDENTIALS, managerPw);
        
        if (log.isDebugEnabled()) {
            log.debug("Instantiated: [" + this + "]");   
        }
    }
    
    /**
     * Instantiate an LdapServerImpl with the given low-level configuration.
     * Using this constructor has the advantage of doing some argument checking
     * on the port number.
     * @param name mnemonic name for this Ldap server instance
     * @param host host of the LDAP server
     * @param port port number. null implies default of 389
     * @param baseDn
     * @param uidAttribute attribute against which to match for uid queries
     * @param managerDn principal for LDAP authentication, null implies no
     * authentication
     * @param managerPw password for LDAP authentication
     * @param useSsl true if we should use SSL, false otherwise.
     * @param initialContextFactory name of the class to use for building init context,
     * null defaults to com.sun.jndi.ldap.LdapCtxFactory
     * @throws IllegalArgumentException when arguments do not specify valid server
     */
    public LdapServerImpl(
        String name, String host, String port, String baseDn,
        String uidAttribute, String managerDn, String managerPw,
        boolean useSsl, String initialContextFactory) {
        
        this(name, constructLdapUrl(host, port, useSsl), baseDn, 
                uidAttribute, managerDn, managerPw, initialContextFactory);
    }
    
    /**
     * Construct a URL for an LDAP server.
     * @param host
     * @param port
     * @param useSsl
     * @return URL constructed from the arguments
     */
    private static String constructLdapUrl(String host, String port, boolean useSsl) {
       
        if (host == null)
            throw new IllegalArgumentException("host cannot be null.");
        
        port = checkNull(port, "389");
        
        int portNum;
        try {
            portNum = Integer.parseInt(port);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("port, if specified, must be a " +
                    "positive integer.  It had invalid value [" + port + "]");
        }
        
        if (portNum < 1)
            throw new IllegalArgumentException("port, if specified, must be " +
                    "positive.  It had invalid value " + portNum);
        
        StringBuffer urlBuffer = new StringBuffer();
        if (useSsl) {
            urlBuffer.append("ldaps://");
        } else {
            urlBuffer.append("ldap://");
        }
        
        urlBuffer.append(host).append(":").append(port);
        
        return urlBuffer.toString();
    }

    /**
     * Returns the chckStr argument unless the chkStr argument is null,
     * in which case returns the defStr (default) argument.
     * @param chkStr - String to check
     * @param defStr - fallback default for when the string to check was null
     * @return chkStr if not null, defStr if chkStr was null
     */
    private static String checkNull(String chkStr, String defStr) {
        if (chkStr == null)
            return defStr;
        return chkStr;
    }
    
    
    public DirContext getConnection() {
        DirContext conn = null;

        try {
            conn = new InitialDirContext(this.env);
        }
        catch ( Exception e ) {
            log.error( "Error creating LDAP Connection to " + this.ldapName, e);
        }
 
        return conn;
    }

    public String getBaseDN() {
        return this.ldapBaseDn;
    }

    public String getUidAttribute() {
        return this.ldapUidAttribute;
    }

    public void releaseConnection (DirContext conn) {
        if (conn == null)
            return;
        
        try {
            conn.close();
        }
        catch (Exception e) {
            log.debug("Error closing the LDAP Connection to server [" + this  + "]", e);
        }
    }       
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append(" name=").append(this.ldapName);
        sb.append(" url=").append(this.env.get(Context.PROVIDER_URL));
        sb.append(" uidAttr=").append(this.ldapUidAttribute);
        sb.append(" baseDn=").append(this.ldapBaseDn);
        if ("ssl".equals(this.env.get(Context.SECURITY_PROTOCOL)))
            sb.append(" using SSL");
        if (this.env.get(Context.SECURITY_PRINCIPAL) != null)
            sb.append(" authentication principal=").append(this.env.get(Context.SECURITY_PRINCIPAL));
        if (this.env.get(Context.SECURITY_CREDENTIALS) != null)
            sb.append(" password=").append(this.env.get(Context.SECURITY_CREDENTIALS));
        if (!DEFAULT_CXT_FACTORY.equals(this.env.get(Context.INITIAL_CONTEXT_FACTORY)))
                sb.append(" initialContextFactory=").append(this.env.get(Context.INITIAL_CONTEXT_FACTORY));
        return sb.toString();
    }
}