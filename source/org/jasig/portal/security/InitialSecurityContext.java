/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.security;

import org.jasig.portal.Logger;
import org.jasig.portal.GenericPortalBean;
import java.util.*;
import java.io.*;

/**
 * This concrete class is responsible for returning a security context that
 * contains all of the subcontexts and associated interfaces. A typical
 * sequence would be:
 *
 * <pre>
 * SecurityContext sec = new InitialSecurityContext("root");
 * Principal princ = sec.getPrincipalInstance();
 * OpaqueCredentials pwd = sec.getOpaqueCredentialsInstance();
 * princ.setUID("user");
 * pwd.setCredentials("password");
 * sec.authenticate();
 * if (sec.isAuthenticated())
 *  System.out.println("Yup");
 * else
 *  System.out.println("Nope");
 * </pre>
 *
 * @author Andrew Newman
 * @version $Revision$
 */

public class InitialSecurityContext implements SecurityContext {

  private SecurityContext ictx;
  private Hashtable subcontext;

  public InitialSecurityContext(String ctx) {
    Properties pr;
    Enumeration ctxnames;
    String factoryname;
    SecurityContextFactory factory;
    File secprops;

    // Initial contexts must have names that are not compound

    if (ctx.indexOf('.') != -1) {
      Logger.log(Logger.ERROR,
          new PortalSecurityException("Initial Context can't be compound"));
      return;
    }

    // Find our properties file and open it

    secprops = new File (GenericPortalBean.getPortalBaseDir() + "properties" +
        File.separator + "security.properties");
    pr = new Properties();
    try {
      pr.load(new FileInputStream(secprops));
    }
    catch (IOException e) {
      Logger.log(Logger.ERROR,
          new PortalSecurityException(e.getMessage()));
      return;
    }

    // Look for our security context factory and instantiate an instance
    // of it or die trying.

    if ((factoryname = pr.getProperty(ctx)) == null) {
      Logger.log(Logger.ERROR,
          new PortalSecurityException("No such security context " + ctx));
      return;
    }
    try {
      factory = (SecurityContextFactory)
          Class.forName(factoryname).newInstance();
    }
    catch (Exception e) {
      Logger.log(Logger.ERROR,
          new PortalSecurityException("Failed to instantiate " + factoryname));
      return;
    }

    // From our factory get an actual security context instance

    ictx = factory.getSecurityContext();

    // Iterate through all of the other property keys looking for ones
    // rooted in this initial context

    ctxnames = pr.propertyNames();
    while (ctxnames.hasMoreElements()) {
      String secname, sfactoryname;
      String candidate = (String)ctxnames.nextElement();
      SecurityContextFactory sfactory;

      if (candidate.startsWith(ctx+".")) {
        secname = candidate.substring(ctx.length()+1);
        sfactoryname = pr.getProperty(candidate);

        try {
          sfactory = (SecurityContextFactory)
              Class.forName(sfactoryname).newInstance();
          ictx.addSubContext(secname, sfactory.getSecurityContext());
        }
        catch (Exception e) {
          Logger.log(Logger.ERROR, new
              PortalSecurityException("(Subcontext)Failed to instantiate " +
              sfactoryname));
        }
      }
    }
  }

  // Wrapper methods so we can legitimately claim to implement the
  // SecurityContext interface.

  public int getAuthType() {
    return (ictx == null ? 0 : ictx.getAuthType());
  }

  public Principal getPrincipalInstance() {
    return (ictx == null ? null: ictx.getPrincipalInstance());
  }

  public OpaqueCredentials getOpaqueCredentialsInstance() {
    return (ictx == null ? null : ictx.getOpaqueCredentialsInstance());
  }

  public void authenticate() {
    if (ictx != null)
      ictx.authenticate();
    return;
  }

  public Principal getPrincipal() {
    return (ictx == null ? null : ictx.getPrincipal());
  }

  public OpaqueCredentials getOpaqueCredentials() {
    return (ictx == null ? null : ictx.getOpaqueCredentials());
  }

  public AdditionalDescriptor getAdditionalDescriptor() {
    return (ictx == null ? null : ictx.getAdditionalDescriptor());
  }

  public boolean isAuthenticated() {
    return (ictx == null ? false : ictx.isAuthenticated());
  }

  public SecurityContext getSubContext(String ctx) {
    return (ictx == null ? null : ictx.getSubContext(ctx));
  }

  public Enumeration getSubContexts() {
    return (ictx == null ? null : ictx.getSubContexts());
  }

  public void addSubContext(String name, SecurityContext ctx) {
    if (ictx != null)
      ictx.addSubContext(name, ctx);
    return;
  }

    public String getAuthenticationFailure() {
	return (ictx == null ? "" : ictx.getAuthenticationFailure());
    }
}



