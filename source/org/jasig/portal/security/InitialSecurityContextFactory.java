/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.jasig.portal.services.LogService;

/**
 * This class provides a static "factory" method that returns a security context
 * retrieved based on the information provided in security.properties,
 * including all relevant subcontexts.  A typical sequence would be:
 *
 * <pre>
 * SecurityContext sec = InitialSecurityContextFactory.getInitialContext("root");
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
 * @author Andrew Newman, newman@yale.edu
 * @author Susan Bramhall (susan.bramhall@yale.edu)
 * @author Shawn Bayern (shawn.bayern@yale.edu)
 * @version $Revision$
 */

public class InitialSecurityContextFactory {

  public static ISecurityContext getInitialContext(String ctx)
      throws PortalSecurityException {
    Properties pr;
    Enumeration ctxnames;
    String factoryname;
    ISecurityContextFactory factory;
    ISecurityContext ictx;

    // Initial contexts must have names that are not compound

    if (ctx.indexOf('.') != -1) {
      PortalSecurityException ep = new PortalSecurityException("Initial Context can't be compound");
      LogService.log(LogService.ERROR,ep);
      throw(ep);
    }

    // Find our properties file and open it
    java.io.InputStream secprops =
      InitialSecurityContextFactory.class.
       getResourceAsStream("/properties/security.properties");
    pr = new Properties();
    try {
      pr.load(secprops);
    }
    catch (IOException e) {
      PortalSecurityException ep = new PortalSecurityException(e.getMessage());
      LogService.log(LogService.ERROR,ep);
      throw(ep);
    } finally {
            try {
                if (secprops != null) 
                    secprops.close();
            } catch (IOException ioe) {
                LogService.log(LogService.ERROR,
                        "InitialSecurityContextFactory:getInitialContext::unable to close InputStream "
                                + ioe);
            }
        }

    // Look for our security context factory and instantiate an instance
    // of it or die trying.

    if ((factoryname = pr.getProperty(ctx)) == null) {
      PortalSecurityException ep = new PortalSecurityException("No such security context " + ctx);
      LogService.log(LogService.ERROR,ep);
      throw(ep);
    }
    try {
      factory = (ISecurityContextFactory)
          Class.forName(factoryname).newInstance();
    }
    catch (Exception e) {
      PortalSecurityException ep = new PortalSecurityException("Failed to instantiate " + factoryname);
      LogService.log(LogService.ERROR,ep);
      throw(ep);
    }

    // From our factory get an actual security context instance

    ictx = factory.getSecurityContext();

    // Iterate through all of the other property keys looking for ones
    // rooted in this initial context

    ctxnames = pr.propertyNames();
    while (ctxnames.hasMoreElements()) {
      String secname, sfactoryname;
      String candidate = (String)ctxnames.nextElement();
      ISecurityContextFactory sfactory;

      if (candidate.startsWith(ctx+".")) {
        secname = candidate.substring(ctx.length()+1);
        sfactoryname = pr.getProperty(candidate);

        try {
          sfactory = (ISecurityContextFactory)
              Class.forName(sfactoryname).newInstance();
          ictx.addSubContext(secname, sfactory.getSecurityContext());
        }
        catch (Exception e) {
          String errorMsg = "(Subcontext) Failed to instantiate " + sfactoryname;
          PortalSecurityException ep = new PortalSecurityException(errorMsg);
          ep.setRecordedException(e);
          LogService.log(LogService.ERROR, errorMsg);
          LogService.log(LogService.ERROR, e);
          throw ep;
        }
      }
    }

    return ictx;
  }

}
