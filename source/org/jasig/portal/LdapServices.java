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

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.sql.*;

import javax.naming.*;
import javax.naming.directory.*;

/**
 * Provides LDAP access in a way similar to a relational DBMS.
 * @author Russell Tokuyama (University of Hawaii)
 * @version $Revision$
 */
public class LdapServices extends GenericPortalBean
{
  private static boolean bPropsLoaded = false;
  private static String sLdapContextFactory = null;
  private static String sLdapUrl            = null;
  private static String sLdapBaseDN         = null;
  private static String sLdapAuthMethod     = null;
  private static String sLdapPrincipal      = null;
  private static String sLdapCredentials    = null;

  /**
   * Constructor which loades LDAP parameters from property file
   * upon first invocation.
   */
  public LdapServices () {
    try {
      if (!bPropsLoaded) {
        File ldapPropsFile = new File (getPortalBaseDir () + "properties" + File.separator + "ldap.properties");
        Properties ldapProps = new Properties ();
        ldapProps.load (new FileInputStream (ldapPropsFile));
        
        sLdapContextFactory = ldapProps.getProperty ("ldap.contextFactory");
        sLdapUrl            = ldapProps.getProperty ("ldap.url");
        sLdapBaseDN         = ldapProps.getProperty ("ldap.baseDN");
        sLdapAuthMethod     = ldapProps.getProperty ("ldap.authMethod");
        sLdapPrincipal      = ldapProps.getProperty ("ldap.principal");
        sLdapCredentials    = ldapProps.getProperty ("ldap.credentials");

        Logger.log (Logger.DEBUG, "ldap.contextFactory = " + sLdapContextFactory);
        Logger.log (Logger.DEBUG, "ldap.url = " + sLdapUrl);
        Logger.log (Logger.DEBUG, "ldap.baseDN = " + sLdapBaseDN);
        Logger.log (Logger.DEBUG, "ldap.authMethod = " + sLdapAuthMethod);
        Logger.log (Logger.DEBUG, "ldap.principal = " + sLdapPrincipal);
        Logger.log (Logger.DEBUG, "ldap.credentials = " + sLdapCredentials);

        bPropsLoaded = true;
      }
    }
    catch (Exception e) {
      Logger.log (Logger.ERROR, e);
    }
  }
  
  /**
   * Gets an LDAP directory context.
   * @return an LDAP directory context object
   */
  public static DirContext getConnection() {
    DirContext conn = null;
    
    try {
      Hashtable env = new Hashtable(5, 0.75f);
      env.put(Context.INITIAL_CONTEXT_FACTORY, sLdapContextFactory);
      env.put(Context.PROVIDER_URL,            sLdapUrl);

      if (sLdapAuthMethod  != null ||
          sLdapPrincipal   != null ||
          sLdapCredentials != null ||
          sLdapAuthMethod.trim().equalsIgnoreCase("simple")) {
        env.put(Context.SECURITY_AUTHENTICATION, sLdapAuthMethod);
        env.put(Context.SECURITY_PRINCIPAL,      sLdapPrincipal);
        env.put(Context.SECURITY_CREDENTIALS,    sLdapCredentials);
      }
      // else default to anonymous bind
      conn = new InitialDirContext(env);
    }
    catch ( Exception e ) {
      Logger.log (Logger.ERROR, e);
    }
    
    return conn;
  }

  /**
   * Gets the base DN used to search the LDAP directory context.
   * @return a DN to use as reference point or context for queries
   */
  public static String getBaseDN() {
    return sLdapBaseDN;
  }
    
  /**
   * Releases an LDAP directory context.
   * @param an LDAP directory context object
   */
  public static void releaseConnection (DirContext conn) {
    // fake it to look like RdbmServices
  }
}

// eof: LdapServices.java
