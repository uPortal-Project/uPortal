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
 * <p>Copyright (c) Yale University 2000</p>
 *
 * <p>(Note that Yale University intends to relinquish copyright claims to
 * this code once an appropriate JASIG copyright arrangement can be
 * established -ADN)</p>
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

}