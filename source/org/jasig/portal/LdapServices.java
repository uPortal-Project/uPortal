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

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.jasig.portal.services.LogService;

/**
 * Provides LDAP access in a way similar to a relational DBMS.
 * @author Russell Tokuyama (University of Hawaii)
 * @version $Revision$
 */
public class LdapServices
{
  private static boolean bPropsLoaded = false;
  private static String sLdapHost           = null;
  private static String sLdapPort           = null;
  private static String sLdapBaseDN         = null;
  private static String sLdapUidAttribute   = null;
  private static String sLdapManagerDN      = null;
  private static String sLdapManagerPW      = null;
  private static String sLdapManagerProto   = null;
  /**
   * Constructor that loads LDAP parameters from property file
   * upon first invocation.
   */
  public LdapServices () {
    try {
      if (!bPropsLoaded) {
        InputStream ins = this.getClass().getResourceAsStream("/properties/ldap.properties");
        Properties ldapProps = new Properties ();
        ldapProps.load (ins);


        sLdapHost         = ldapProps.getProperty ("ldap.host",         "");
        sLdapPort         = ldapProps.getProperty ("ldap.port",         "389");
        sLdapBaseDN       = ldapProps.getProperty ("ldap.baseDN",       "");
        sLdapUidAttribute = ldapProps.getProperty ("ldap.uidAttribute", "");
        sLdapManagerDN    = ldapProps.getProperty ("ldap.managerDN",    "");
        sLdapManagerPW    = ldapProps.getProperty ("ldap.managerPW",    "");
        sLdapManagerProto = ldapProps.getProperty ("ldap.protocol",     "");

        LogService.log(LogService.DEBUG, "ldap.host = "         + sLdapHost);
        LogService.log(LogService.DEBUG, "ldap.port = "         + sLdapPort);
        LogService.log(LogService.DEBUG, "ldap.baseDN = "       + sLdapBaseDN);
        LogService.log(LogService.DEBUG, "ldap.uidAttribute = " + sLdapUidAttribute);
        LogService.log(LogService.DEBUG, "ldap.managerDN = "    + sLdapManagerDN);
        LogService.log(LogService.DEBUG, "ldap.managerPW = "    + sLdapManagerPW);
	LogService.log(LogService.DEBUG, "ldap.protocol = "     + sLdapManagerProto);
        bPropsLoaded = true;
      }
    }
    catch (Exception e) {
      LogService.log(LogService.ERROR, e);
    }
  }

  /**
   * Gets an LDAP directory context.
   * @return an LDAP directory context object
   */
  public DirContext getConnection() {
    DirContext conn = null;

    try {
      Hashtable env = new Hashtable(5, 0.75f);
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      StringBuffer urlBuffer = new StringBuffer("ldap://");
      urlBuffer.append(sLdapHost).append(":").append(sLdapPort);

      env.put(Context.PROVIDER_URL, urlBuffer.toString());
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
      env.put(Context.SECURITY_PRINCIPAL,      sLdapManagerDN);
      env.put(Context.SECURITY_CREDENTIALS,    sLdapManagerPW);
      if(sLdapManagerProto.equals("ssl")) env.put(Context.SECURITY_PROTOCOL,"ssl");
      conn = new InitialDirContext(env);
    }
    catch ( Exception e ) {
      LogService.log(LogService.ERROR, e);
    }

    return conn;
  }

  /**
   * Gets the base DN used to search the LDAP directory context.
   * @return a DN to use as reference point or context for queries
   */
  public String getBaseDN() {
    return sLdapBaseDN;
  }

  /**
   * Gets the uid attribute used to search the LDAP directory context.
   * @return a DN to use as reference point or context for queries
   */
  public String getUidAttribute() {
    return sLdapUidAttribute;
  }

  /**
   * Releases an LDAP directory context.
   * @param an LDAP directory context object
   */
  public void releaseConnection (DirContext conn) {
    if (conn == null)
      return;
    try {
      conn.close();
    }
    catch (Exception e) {
      LogService.log(LogService.DEBUG, e);
    }
  }
}

// eof: LdapServices.java

